# We Rise DFW

> **Translations:** [English](README.md) · [Español](README.es.md) · [العربية](README.ar.md) · [中文](README.zh.md)

An open-source Android app for the Dallas / Fort Worth area that surfaces nearby **food**, **clothing**, and **shelter** providers based on the user's current location. Built by **We Rise DFW** (Shane W. Watson) and offered freely under the MIT License — see [LICENSE](LICENSE).

## Why
A lot of people in DFW — including those struggling with housing, food security, recovery, or sudden displacement — don't have time, bandwidth, or a stable phone to dig through provider directories. We Rise is meant to be a quick, low-friction way to see what's around them right now.

> **Looking for how to use the app?** See [USER_GUIDE.md](USER_GUIDE.md) — written for end users, no technical background required. This README is the developer-facing doc.

## Privacy first
- Location is read **only** when the user taps **Search** or **Find more online**.
- It is **never written to disk**, never logged, never shared with any analytics provider.
- The local Room database stores only the cached list of providers (so the app keeps working offline). User location is not part of that cache.
- Translations run on-device via Google ML Kit — text never leaves the phone for translation.
- The only outbound network calls happen for: OpenStreetMap map tiles, an explicit "Find more online" search against the public Overpass API, and ML Kit's one-time language-model downloads.

## Features
- Three tabs: **Food**, **Clothing**, **Shelter**
- User-selectable search radius (1–25 miles)
- Faith-based providers marked with a cross icon, with a setting to include or exclude them
- Open / closed status: green dot when open now, red when closed
- Tap an address → opens in your default map app
- Tap a phone number → opens in your default dialer
- Tap a website → opens in your default browser
- Map (top half) with red/green pins matching the current tab
- Black + red color scheme
- Hard-bounded to the DFW metro; outside that bounding box the map shows an "out of range" overlay
- **Find more online** button that pulls additional providers from OpenStreetMap and merges them into the local cache
- **Language picker** with ~59 languages, on-device translation via Google ML Kit (defaults to English)

## Tech stack
- Kotlin · AndroidX · classic XML views (intentionally — keeps the binary small and the code accessible)
- Min SDK 24 (Android 7.0) · Target SDK 34
- [OSMDroid](https://github.com/osmdroid/osmdroid) — map rendering, no API key
- [Room](https://developer.android.com/training/data-storage/room) — local cache database
- Platform `LocationManager` — one-shot location, no Google Play Services dependency for this code path
- [ML Kit Translation](https://developers.google.com/ml-kit/language/translation) — on-device translation
- [Overpass API](https://overpass-api.de/) — free OpenStreetMap query endpoint, used by the online-search button

## Project layout
```
app/src/main/java/com/werisetech/weriseapp/
├── MainActivity.kt              // Map + tabs + search buttons
├── ServiceDetailActivity.kt     // Per-provider detail screen
├── WeRiseApplication.kt         // Application bootstrap
├── data/
│   ├── AppDatabase.kt           // Room database + type converters
│   ├── Category.kt              // (in Service.kt) the FOOD/CLOTHING/SHELTER enum
│   ├── DfwBounds.kt             // DFW bounding box
│   ├── SeedData.kt              // Bundled curated provider list
│   ├── Service.kt               // Service entity
│   └── ServiceDao.kt            // Service DAO
├── i18n/
│   ├── Languages.kt             // ML Kit-supported language catalogue
│   ├── TranslationCache.kt      // Room entity + DAO for cached translations
│   └── Translator.kt            // ML Kit translator + caching wrapper
├── location/
│   └── LocationProvider.kt      // One-shot location fetch (never persisted)
├── online/
│   └── OverpassRefresher.kt     // OpenStreetMap online search
├── ui/
│   ├── PreferencesActivity.kt   // Settings host
│   ├── ServiceListAdapter.kt    // RecyclerView adapter
│   ├── ServiceListFragment.kt   // One tab's list
│   └── ServicePagerAdapter.kt   // ViewPager2 adapter
└── util/
    ├── DistanceUtil.kt          // Haversine helper
    └── HoursParser.kt           // Open/closed parsing + pretty printing
```

## Build & run
1. Open the `WeRiseApp` folder in Android Studio (`File → Open`).
2. When Android Studio prompts about the Gradle wrapper, let it use the version declared in `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.4). If it complains that `gradle-wrapper.jar` is missing, run **File → Sync Project with Gradle Files**, or in a terminal at the project root run `gradle wrapper` once.
3. Let Gradle sync. The first sync downloads dependencies; this takes a few minutes.
4. Run on a device or emulator (API 24+).

## Adding more providers to the bundled list
Edit `app/src/main/java/com/werisetech/weriseapp/data/SeedData.kt`. Each entry takes a stable `id`, `name`, `category`, address, phone, hours, lat/lon, faith-based flag, blurb, and website. Hours format is documented in `util/HoursParser.kt`.

## Translation
The Settings screen has a language picker with the ~59 languages Google ML Kit supports on-device. Defaults to English (no translation).

When a user picks a non-English language for the first time, ML Kit downloads the corresponding language model (one-time, ~10–30 MB). After that, translation runs entirely on-device. Translated strings are cached in the local Room database keyed by `(language, source-text)` so each phrase only goes through ML Kit once.

If you'd rather use a different translator (e.g. for languages outside ML Kit's set), implement the `Translator` interface in `i18n/Translator.kt` and swap the constructor argument in `TranslatorFactory.get()`.

## Online search
The **Find more online** button fires a query against the OpenStreetMap [Overpass API](https://overpass-api.de/) for nodes tagged `social_facility=food_bank | soup_kitchen | clothing_bank | shelter` (and a few related amenity tags) inside a bounding box derived from the user's location and selected radius. Results are mapped to the same `Service` schema and merged into the local cache, so they show up in the regular search and on the map alongside the bundled list.

The Overpass API is a free community resource. The query sends a bounding box derived from the user's one-shot location to `overpass-api.de`. The location itself is not persisted on the device.

## Documentation
- [USER_GUIDE.md](USER_GUIDE.md) — end-user walkthrough (main screen, search flow, settings, privacy, troubleshooting).
- [README.md](README.md) — this file. Developer-facing.
- [LICENSE](LICENSE) — MIT license text.

## License
Released under the [MIT License](LICENSE). Copyright © 2026 We Rise Technologies.

You are free to use, copy, modify, merge, publish, distribute, sublicense, and sell copies of the software, provided you keep the copyright and license notice intact.

## Status
This is a personal open-source project offered **as-is**, with no warranty and no promise of support, updates, or bug fixes. Pull requests are welcome but not guaranteed to be merged.

## Acknowledgements
- The DFW providers in `SeedData.kt` are real organizations doing critical work. Please consider supporting them directly.
- Map data © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors.
- Translation models © Google, distributed under their ML Kit terms.
