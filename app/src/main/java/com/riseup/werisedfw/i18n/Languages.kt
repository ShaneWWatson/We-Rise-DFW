package com.riseup.werisedfw.i18n

/**
 * Catalogue of languages the app can translate into via Google ML Kit on-device
 * translation. Codes are BCP-47 / ISO 639-1 and match ML Kit's supported set.
 *
 * English (`en`) is index 0 and represents "no translation".
 *
 * @property code BCP-47 / ISO 639-1 language code (e.g. `"es"`).
 * @property displayName Human-readable language name shown in the Settings picker.
 */
data class Language(val code: String, val displayName: String)

/**
 * The full list of languages exposed in the Settings picker.
 *
 * ML Kit ships first-party translation models for these ~59 languages. A model
 * is downloaded the first time the user picks a language, then runs entirely
 * on-device.
 */
object Languages {

    /** Every supported language, with English (`en`) first. */
    val all: List<Language> = listOf(
        Language("en", "English"),
        Language("af", "Afrikaans"),
        Language("ar", "Arabic"),
        Language("be", "Belarusian"),
        Language("bg", "Bulgarian"),
        Language("bn", "Bengali"),
        Language("ca", "Catalan"),
        Language("cs", "Czech"),
        Language("cy", "Welsh"),
        Language("da", "Danish"),
        Language("de", "German"),
        Language("el", "Greek"),
        Language("eo", "Esperanto"),
        Language("es", "Spanish"),
        Language("et", "Estonian"),
        Language("fa", "Persian"),
        Language("fi", "Finnish"),
        Language("fr", "French"),
        Language("ga", "Irish"),
        Language("gl", "Galician"),
        Language("gu", "Gujarati"),
        Language("he", "Hebrew"),
        Language("hi", "Hindi"),
        Language("hr", "Croatian"),
        Language("ht", "Haitian Creole"),
        Language("hu", "Hungarian"),
        Language("id", "Indonesian"),
        Language("is", "Icelandic"),
        Language("it", "Italian"),
        Language("ja", "Japanese"),
        Language("ka", "Georgian"),
        Language("kn", "Kannada"),
        Language("ko", "Korean"),
        Language("lt", "Lithuanian"),
        Language("lv", "Latvian"),
        Language("mk", "Macedonian"),
        Language("mr", "Marathi"),
        Language("ms", "Malay"),
        Language("mt", "Maltese"),
        Language("nl", "Dutch"),
        Language("no", "Norwegian"),
        Language("pl", "Polish"),
        Language("pt", "Portuguese"),
        Language("ro", "Romanian"),
        Language("ru", "Russian"),
        Language("sk", "Slovak"),
        Language("sl", "Slovenian"),
        Language("sq", "Albanian"),
        Language("sv", "Swedish"),
        Language("sw", "Swahili"),
        Language("ta", "Tamil"),
        Language("te", "Telugu"),
        Language("th", "Thai"),
        Language("tl", "Tagalog"),
        Language("tr", "Turkish"),
        Language("uk", "Ukrainian"),
        Language("ur", "Urdu"),
        Language("vi", "Vietnamese"),
        Language("zh", "Chinese"),
    )

    /** Display names for [androidx.preference.ListPreference.entries]. */
    val entries: Array<CharSequence> = all.map { it.displayName as CharSequence }.toTypedArray()

    /** BCP-47 codes for [androidx.preference.ListPreference.entryValues]. */
    val entryValues: Array<CharSequence> = all.map { it.code as CharSequence }.toTypedArray()
}

