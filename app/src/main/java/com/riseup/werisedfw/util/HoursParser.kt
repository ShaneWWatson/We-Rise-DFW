package com.riseup.werisedfw.util

import java.util.Calendar

/**
 * Parser and formatter for the app's compact opening-hours format.
 *
 * Format:
 *  - One token per day, separated by `;`.
 *  - Day prefix is one of `MON TUE WED THU FRI SAT SUN`, or `ALL` for every day.
 *  - Times are 24-hour `HH:MM-HH:MM`. Multiple ranges per day are comma-separated.
 *  - Days with no entry are treated as closed.
 *
 * Examples:
 * ```
 * "MON 09:00-17:00; TUE 09:00-17:00; WED CLOSED"
 * "MON 06:00-10:00,17:00-19:00"
 * "ALL 00:00-23:59"      // 24/7
 * ```
 */
object HoursParser {

    private val DAYS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    /**
     * Returns `true` if the schedule says the provider is open at [calendar]'s wall-clock time.
     *
     * @param hoursString Encoded schedule in the app's hours format.
     * @param calendar The point in time to check against; defaults to now.
     */
    fun isOpenAt(hoursString: String, calendar: Calendar = Calendar.getInstance()): Boolean {
        if (hoursString.isBlank()) return false
        val dayKey = DAYS[calendar[Calendar.DAY_OF_WEEK] - 1]
        val nowMinutes = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE)

        for (token in tokenize(hoursString)) {
            val (key, ranges) = splitDayAndRanges(token) ?: continue
            if (!appliesToDay(key, dayKey)) continue
            if (ranges.equals("CLOSED", ignoreCase = true)) return false
            if (anyRangeContains(ranges, nowMinutes)) return true
        }
        return false
    }

    /**
     * Formats [hoursString] as a multi-line, human-readable schedule.
     *
     * @param hoursString Encoded schedule in the app's hours format.
     * @return A newline-separated string with one line per day of the week.
     */
    fun pretty(hoursString: String): String {
        val byDay = mutableMapOf<String, MutableList<String>>()
        for (token in tokenize(hoursString)) {
            val (key, ranges) = splitDayAndRanges(token) ?: continue
            if (key == "ALL") {
                DAYS.forEach { byDay.getOrPut(it) { mutableListOf() }.add(ranges) }
            } else {
                byDay.getOrPut(key) { mutableListOf() }.add(ranges)
            }
        }
        return DAYS_IN_DISPLAY_ORDER.joinToString("\n") { day ->
            val ranges = byDay[day]
            "${DISPLAY_LABELS[day]}: ${ranges?.joinToString(", ") ?: "Closed"}"
        }
    }

    // -------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------

    private val DAYS_IN_DISPLAY_ORDER = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private val DISPLAY_LABELS = mapOf(
        "MON" to "Mon", "TUE" to "Tue", "WED" to "Wed",
        "THU" to "Thu", "FRI" to "Fri", "SAT" to "Sat", "SUN" to "Sun",
    )

    /** Splits [hoursString] on `;` and trims whitespace, discarding empty tokens. */
    private fun tokenize(hoursString: String): List<String> =
        hoursString.split(";").asSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()

    /**
     * Splits a single token (e.g. `"MON 09:00-17:00"`) into its day key and
     * time-range string. Returns `null` if the token is malformed.
     */
    private fun splitDayAndRanges(token: String): Pair<String, String>? {
        val parts = token.split(" ", limit = 2)
        if (parts.size < 2) return null
        return parts[0].uppercase() to parts[1].trim()
    }

    /** Returns `true` if [key] is either `"ALL"` or exactly matches [today]. */
    private fun appliesToDay(key: String, today: String): Boolean =
        key == "ALL" || key == today

    /**
     * Returns `true` if [nowMinutes] (minutes since midnight) falls within any
     * of the comma-separated time ranges in [ranges].
     */
    private fun anyRangeContains(ranges: String, nowMinutes: Int): Boolean =
        ranges.split(",").any { range ->
            val r = range.trim()
            val dash = r.indexOf('-')
            if (dash <= 0) return@any false
            val start = parseHm(r.substring(0, dash)) ?: return@any false
            val end = parseHm(r.substring(dash + 1)) ?: return@any false
            nowMinutes in start..end
        }

    /** Parses `"HH:MM"` into minutes-since-midnight. */
    private fun parseHm(s: String): Int? {
        val t = s.trim()
        val colon = t.indexOf(':')
        if (colon <= 0) return null
        val h = t.substring(0, colon).toIntOrNull() ?: return null
        val m = t.substring(colon + 1).toIntOrNull() ?: return null
        return h * 60 + m
    }
}

