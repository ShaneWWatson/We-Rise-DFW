package com.riseup.werisedfw.data

/**
 * Geographic bounding box for the Dallas / Fort Worth metropolitan area.
 *
 * Generously sized to include the suburbs that residents would still call
 * "DFW" — north past McKinney/Frisco, south past Waxahachie, west past
 * Weatherford, east past Forney.
 */
object DfwBounds {

    /** Northern edge of the bounding box in decimal degrees. */
    const val NORTH = 33.30

    /** Southern edge of the bounding box in decimal degrees. */
    const val SOUTH = 32.45

    /** Western edge of the bounding box in decimal degrees. */
    const val WEST = -97.55

    /** Eastern edge of the bounding box in decimal degrees. */
    const val EAST = -96.50

    /** Geographic centre latitude of the bounding box. */
    val centerLat: Double = (NORTH + SOUTH) / 2.0

    /** Geographic centre longitude of the bounding box. */
    val centerLon: Double = (EAST + WEST) / 2.0

    /**
     * Returns `true` if `(lat, lon)` lies inside the DFW bounding box.
     *
     * @param lat WGS-84 latitude in decimal degrees.
     * @param lon WGS-84 longitude in decimal degrees.
     */
    fun contains(lat: Double, lon: Double): Boolean =
        lat in SOUTH..NORTH && lon in WEST..EAST
}

