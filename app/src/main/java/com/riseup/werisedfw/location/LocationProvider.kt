package com.riseup.werisedfw.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * One-shot location fetch used at the moment the user taps "Search".
 *
 * Implementation notes:
 *  - Uses the platform [LocationManager] directly. We deliberately avoid
 *    `FusedLocationProviderClient` so the app has no Google Play Services
 *    dependency for this code path.
 *  - The returned [Location] is held in memory only by the caller and is
 *    never written to disk by this class.
 *  - The caller must already hold either `ACCESS_COARSE_LOCATION` or
 *    `ACCESS_FINE_LOCATION`.
 */
object LocationProvider {

    /** Cached fixes within this age are accepted without requesting an update. */
    private const val ACCEPTABLE_AGE_MS = 60_000L

    /** Hard cap on how long we'll wait for a single update before giving up. */
    private const val SINGLE_UPDATE_TIMEOUT_MS = 15_000L

    /**
     * Returns `true` if either `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`
     * has been granted.
     *
     * @param context Any [Context] used to check permission state.
     */
    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Returns the user's current location, or `null` if permission is missing,
     * no provider is enabled, or no fix arrives within the timeout.
     *
     * The returned [Location] is held only by the caller; this class never
     * persists location data.
     *
     * @param context Any [Context] with location services available.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchOnce(context: Context): Location? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = enabledProviders(lm)
        if (providers.isEmpty()) return null

        return mostRecentCachedFix(lm, providers) ?: requestSingleFix(lm, providers)
    }

    // -------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------

    /** Provider names in descending precision order. */
    private val PROVIDER_PRIORITY = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )

    /** Returns the subset of [PROVIDER_PRIORITY] providers currently enabled on [lm]. */
    private fun enabledProviders(lm: LocationManager): List<String> =
        PROVIDER_PRIORITY.filter { lm.isProviderEnabled(it) }

    /**
     * Returns the most recently cached fix from any of [providers] that is still
     * fresh enough (within [ACCEPTABLE_AGE_MS]), preferring the fix with the
     * smallest accuracy radius (most precise). Returns `null` if no acceptable
     * cached fix exists.
     */
    @SuppressLint("MissingPermission")
    private fun mostRecentCachedFix(lm: LocationManager, providers: List<String>): Location? {
        val now = System.currentTimeMillis()
        return providers.asSequence()
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .filter { it.time > 0 && (now - it.time) < ACCEPTABLE_AGE_MS }
            .minByOrNull { fix -> if (fix.accuracy <= 0f) Float.MAX_VALUE else fix.accuracy }
    }

    /**
     * Requests a single location update from the first available provider,
     * waiting at most [SINGLE_UPDATE_TIMEOUT_MS] before returning `null`.
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(
        lm: LocationManager,
        providers: List<String>
    ): Location? = suspendCancellableCoroutine { cont ->
        val provider = providers.first()
        val mainHandler = Handler(Looper.getMainLooper())

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (cont.isActive) cont.resume(location)
                runCatching { lm.removeUpdates(this) }
            }

            @Deprecated("Required by the platform API")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }

        try {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (_: Throwable) {
            if (cont.isActive) cont.resume(null)
            return@suspendCancellableCoroutine
        }

        cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
        mainHandler.postDelayed({
            runCatching { lm.removeUpdates(listener) }
            if (cont.isActive) cont.resume(null)
        }, SINGLE_UPDATE_TIMEOUT_MS)
    }
}

