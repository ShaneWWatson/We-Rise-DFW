package com.werisetech.weriseapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The three categories of services the app surfaces. */
enum class Category { FOOD, CLOTHING, SHELTER }

/**
 * A service provider entry. Persisted in Room so the app keeps working when
 * the device is offline. Never contains any user information — only the
 * provider's public details.
 *
 * @property hours Encoded weekly schedule. See
 *   [com.werisetech.weriseapp.util.HoursParser] for the format and parsing.
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
