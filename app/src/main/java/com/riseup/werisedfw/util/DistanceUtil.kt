package com.riseup.werisedfw.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Great-circle distance helpers. */
object DistanceUtil {

	private const val EARTH_RADIUS_MILES = 3958.8

	/** Returns the straight-line distance in miles between two `(lat, lon)` pairs. */
	fun milesBetween(
		lat1: Double, lon1: Double,
		lat2: Double, lon2: Double,
	                ): Double {
		val dLat = Math.toRadians(lat2 - lat1)
		val dLon = Math.toRadians(lon2 - lon1)
		val a = (sin(dLat / 2).let { it * it } +
		         (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
		          sin(dLon / 2).let { it * it })).coerceIn(0.0, 1.0)
		val c = 2 * atan2(sqrt(a), sqrt(1 - a))
		return EARTH_RADIUS_MILES * c
	}
}

