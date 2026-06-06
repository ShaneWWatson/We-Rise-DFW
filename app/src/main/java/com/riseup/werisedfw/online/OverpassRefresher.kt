package com.riseup.werisedfw.online

import com.riseup.werisedfw.data.Category
import com.riseup.werisedfw.data.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.cos

/**
 * Online search for nearby food, clothing, and shelter providers using the
 * OpenStreetMap Overpass API.
 *
 * - Free, no API key, no account.
 * - The only data sent off-device is a bounding box derived from the user's
 *   one-shot location. The location itself is never persisted.
 * - Results are mapped to [Service] objects and returned to the caller; the
 *   caller is responsible for caching them in the local database.
 *
 * The Overpass server is a public community resource; we set a polite
 * `User-Agent` and keep the query tight to avoid abuse.
 */
object OverpassRefresher {

    private const val OVERPASS_ENDPOINT = "https://overpass-api.de/api/interpreter"
    private const val USER_AGENT = "WeRiseDFW/1.0 (We Rise DFW)"
    private const val REQUEST_TIMEOUT_MS = 20_000

    /**
     * Query Overpass for service POIs near ([lat], [lon]) within [radiusMiles].
     *
     * @param lat WGS-84 latitude of the search origin.
     * @param lon WGS-84 longitude of the search origin.
     * @param radiusMiles Search radius in miles.
     * @return Matched [Service] entries, or an empty list on any error
     *   (network failure, timeout, parse error, etc.).
     */
    suspend fun search(
        lat: Double,
        lon: Double,
        radiusMiles: Int,
    ): List<Service> = withContext(Dispatchers.IO) {
        val (south, west, north, east) = boundingBox(lat, lon, radiusMiles)
        val query = buildOverpassQuery(south, west, north, east)
        val responseBody = postOverpass(query) ?: return@withContext emptyList()
        parseOverpassResponse(responseBody)
    }

    // -----------------------------------------------------------------------
    // Query construction
    // -----------------------------------------------------------------------

    /**
     * Builds an Overpass QL query that pulls every node tagged with one of the
     * service categories we care about, inside the supplied bounding box.
     */
    private fun buildOverpassQuery(
        south: Double, west: Double, north: Double, east: Double
    ): String {
        val bbox = "$south,$west,$north,$east"
        return """
            [out:json][timeout:15];
            (
              node["social_facility"="food_bank"]($bbox);
              node["social_facility"="soup_kitchen"]($bbox);
              node["social_facility"="clothing_bank"]($bbox);
              node["social_facility"="shelter"]($bbox);
              node["amenity"="social_facility"]["social_facility"~"food|cloth|shelter"]($bbox);
              node["amenity"="shelter"]["shelter_type"~"homeless|emergency"]($bbox);
            );
            out body 200;
        """.trimIndent()
    }

    // -----------------------------------------------------------------------
    // Networking
    // -----------------------------------------------------------------------

    /**
     * POSTs [query] to the Overpass endpoint and returns the raw response body,
     * or `null` on any network or HTTP error.
     */
    private fun postOverpass(query: String): String? {
        val url = URI(OVERPASS_ENDPOINT).toURL()
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = REQUEST_TIMEOUT_MS
            readTimeout = REQUEST_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        return try {
            conn.outputStream.use { os ->
                os.write(
                    ("data=" + URLEncoder.encode(
                        query,
                        StandardCharsets.UTF_8.name()
                    )).toByteArray()
                )
            }
            if (conn.responseCode !in (200..299)) return null
            BufferedReader(
                InputStreamReader(
                    conn.inputStream,
                    StandardCharsets.UTF_8
                )
            ).use { it.readText() }
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { conn.disconnect() }
        }
    }

    // -----------------------------------------------------------------------
    // Response parsing
    // -----------------------------------------------------------------------

    /** Convert an Overpass JSON response into our domain [Service] list. */
    private fun parseOverpassResponse(body: String): List<Service> {
        val root = runCatching { JSONObject(body) }.getOrNull() ?: return emptyList()
        val elements = root.optJSONArray("elements") ?: return emptyList()
        val out = ArrayList<Service>(elements.length())

        for (i in 0 until elements.length()) {
            val el = elements.optJSONObject(i) ?: continue
            val service = elementToService(el) ?: continue
            out += service
        }
        return out
    }

    /**
     * Converts a single Overpass element object into a [Service], or returns
     * `null` if the element lacks the required fields (id, coordinates, category).
     */
    private fun elementToService(el: JSONObject): Service? {
        val id = el.optLong("id", -1L).takeIf { it >= 0 } ?: return null
        val lat = el.optDouble("lat", Double.NaN).takeIf { !it.isNaN() } ?: return null
        val lon = el.optDouble("lon", Double.NaN).takeIf { !it.isNaN() } ?: return null
        val tags = el.optJSONObject("tags") ?: return null

        val category = inferCategory(tags) ?: return null
        val name = tags.optString("name").ifBlank { "Unnamed ${category.displayLabel()}" }
        val address = composeAddress(tags)
        val phone =
            tags.optString("phone").ifBlank { tags.optString("contact:phone") }.ifBlank { null }
        val website =
            tags.optString("website").ifBlank { tags.optString("contact:website") }.ifBlank { null }
        val hours = normalizeOpeningHours(tags.optString("opening_hours"))
        val faithBased = looksFaithBased(tags)

        return Service(
            id = "osm_$id",
            name = name,
            category = category,
            address = address,
            phone = phone,
            hours = hours,
            latitude = lat,
            longitude = lon,
            faithBased = faithBased,
            blurb = buildBlurb(tags, category),
            website = website,
            source = "overpass"
        )
    }

    /** Map OSM tags to our [Category] enum. */
    private fun inferCategory(tags: JSONObject): Category? {
        val social = tags.optString("social_facility").lowercase()
        val amenity = tags.optString("amenity").lowercase()
        val shelterType = tags.optString("shelter_type").lowercase()
        return when {
            social.contains("food") || social == "soup_kitchen" -> Category.FOOD
            social.contains("cloth") -> Category.CLOTHING
            social == "shelter" || amenity == "shelter" || shelterType.isNotEmpty() -> Category.SHELTER
            else -> null
        }
    }

    /** Assembles a human-readable address string from OSM `addr:*` tags. */
    private fun composeAddress(tags: JSONObject): String {
        val parts = listOf(
            tags.optString("addr:housenumber"),
            tags.optString("addr:street"),
            tags.optString("addr:city"),
            tags.optString("addr:state"),
            tags.optString("addr:postcode"),
        ).filter { it.isNotBlank() }

        if (parts.isEmpty()) return "Address not listed — call first"
        // "123 Main St, Dallas, TX 75201"
        val streetPart = listOfNotNull(parts.getOrNull(0), parts.getOrNull(1)).joinToString(" ")
        val tail = parts.drop(2).joinToString(", ")
        return if (tail.isBlank()) streetPart else "$streetPart, $tail"
    }

    /** Best-effort conversion of the OSM `opening_hours` tag into our format. */
    private fun normalizeOpeningHours(raw: String): String {
        if (raw.isBlank()) return ""
        // OSM's opening_hours is its own grammar; we recognise a couple of common cases
        // and fall through to "ALL 00:00-23:59" otherwise so the open/closed indicator
        // doesn't mislead the user.
        return when {
            raw.equals("24/7", ignoreCase = true) -> "ALL 00:00-23:59"
            raw.contains("Mo-Fr", ignoreCase = true) -> {
                val match = Regex("""(\d{2}:\d{2})-(\d{2}:\d{2})""").find(raw)
                if (match != null) {
                    val (start, end) = match.destructured
                    "MON $start-$end; TUE $start-$end; WED $start-$end; THU $start-$end; FRI $start-$end"
                } else {
                    "ALL 00:00-23:59"
                }
            }

            else -> "ALL 00:00-23:59"
        }
    }

    /**
     * Heuristically determines whether a provider is faith-based by checking the
     * `religion` tag and looking for religious keywords in `operator` and `name`.
     */
    private fun looksFaithBased(tags: JSONObject): Boolean {
        val religion = tags.optString("religion")
        val operator = tags.optString("operator").lowercase()
        val name = tags.optString("name").lowercase()
        if (religion.isNotBlank()) return true
        val religiousTokens = listOf(
            "church", "ministries", "ministry", "catholic", "baptist",
            "methodist", "lutheran", "christian", "salvation army", "jewish",
            "islamic", "mosque", "synagogue", "diocese", "parish",
        )
        return religiousTokens.any { it in operator || it in name }
    }

    /** Returns the OSM `description` tag, or a generic fallback blurb for the given [category]. */
    private fun buildBlurb(tags: JSONObject, category: Category): String {
        val description = tags.optString("description")
        if (description.isNotBlank()) return description
        return "Listed on OpenStreetMap as a ${category.displayLabel().lowercase()} service. " +
                "Hours and details may be incomplete; call ahead to confirm."
    }

    // -----------------------------------------------------------------------
    // Geometry
    // -----------------------------------------------------------------------

    private data class BoundingBox(
        val south: Double,
        val west: Double,
        val north: Double,
        val east: Double
    )

    /**
     * Converts a centre point and radius into a [BoundingBox].
     *
     * Uses a flat-earth approximation: 1° latitude ≈ 69 miles, longitude scaled
     * by cos(lat). Accurate enough for searches up to ~25 miles.
     *
     * @param lat WGS-84 latitude of the centre point.
     * @param lon WGS-84 longitude of the centre point.
     * @param radiusMiles Desired radius in miles.
     * @return A [BoundingBox] that fully encloses the circle.
     */
    private fun boundingBox(lat: Double, lon: Double, radiusMiles: Int): BoundingBox {
        val deltaLat = radiusMiles / 69.0
        val deltaLon = radiusMiles / (69.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.01))
        return BoundingBox(
            south = lat - deltaLat,
            west = lon - deltaLon,
            north = lat + deltaLat,
            east = lon + deltaLon
        )
    }
}

/** User-friendly label for a [Category]. Kept private to this file. */
private fun Category.displayLabel(): String = when (this) {
    Category.FOOD -> "Food"
    Category.CLOTHING -> "Clothing"
    Category.SHELTER -> "Shelter"
}

