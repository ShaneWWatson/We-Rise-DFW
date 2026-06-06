package com.riseup.werisedfw.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The three categories of services the app surfaces. */
enum class Category {
    /** Food pantries, soup kitchens, and food distribution sites. */
    FOOD,

    /** Clothing banks, thrift stores, and wardrobe assistance programs. */
    CLOTHING,

    /** Emergency shelters, transitional housing, and domestic-violence refuges. */
    SHELTER,
}

/**
 * A service provider entry. Persisted in Room so the app keeps working when
 * the device is offline. Never contains any user information — only the
 * provider's public details.
 *
 * @property id Stable unique identifier. Seed entries use a human-readable slug;
 *   Overpass imports use `"osm_<node-id>"`.
 * @property name Display name of the provider.
 * @property category The type of service offered.
 * @property address Street address, or a note like "Confidential — call first".
 * @property phone Optional phone number; `null` when not publicly listed.
 * @property hours Encoded weekly schedule. See
 *   [com.riseup.werisedfw.util.HoursParser] for the format and parsing.
 * @property latitude WGS-84 latitude of the provider's location.
 * @property longitude WGS-84 longitude of the provider's location.
 * @property faithBased `true` if the provider is affiliated with a religious organisation.
 * @property blurb Short human-readable description shown on list and detail screens.
 * @property website Optional website URL; `null` when not available.
 * @property source Origin tag — `"seed"` for the bundled curated list,
 *   `"overpass"` for entries imported by the online search.
 */
@Entity(tableName = "services")
data class Service(
    @PrimaryKey val id: String,
    val name: String,
    val category: Category,
    val address: String,
    val phone: String?,
    val hours: String,
    val latitude: Double,
    val longitude: Double,
    val faithBased: Boolean,
    val blurb: String,
    val website: String? = null,
    val source: String = "seed"
)

