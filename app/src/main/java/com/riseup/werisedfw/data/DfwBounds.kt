package com.riseup.werisedfw.data

/**
 * Geographic bounding box for the Dallas / Fort Worth metropolitan area.
 *
 * Generously sized to include the suburbs that residents would still call
 * "DFW" — north past McKinney/Frisco, south past Waxahachie, west past
 * Weatherford, east past Forney.
 */
object DfwBounds {

    const val NORTH = 33.30
    const val SOUTH = 32.45
    const val WEST = -97.55
    const val EAST = -96.50

    val centerLat: Double = (NORTH + SOUTH) / 2.0
    val centerLon: Double = (EAST + WEST) / 2.0

    /** Returns true if `(lat, lon)` is inside the bounding box. */
    fun contains(lat: Double, lon: Double): Boolean =
        lat in SOUTH..NORTH && lon in WEST..EAST
}

