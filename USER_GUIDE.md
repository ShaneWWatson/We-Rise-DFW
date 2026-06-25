# We Rise DFW — User Guide

> **Translations:** [English](USER_GUIDE.md) · [Español](USER_GUIDE.es.md) · [العربية](USER_GUIDE.ar.md) · [中文](USER_GUIDE.zh.md)

A plain-English walkthrough of how the app works. No technical background needed.

---

## What this app is for

We Rise DFW helps you find nearby places that offer **food**, **clothing**, or **a place to stay** in the Dallas / Fort Worth area. You tap a button, and it shows you what's close to you right now — along with their address, phone number, hours, and website.

It works whether your situation is short-term, ongoing, or just a hard day. There's no sign-in. No account. The app does not track you.

---

## What's on the main screen

When you open We Rise DFW, the screen is split into a few zones, top to bottom:

**Top bar** — the app's name and a settings icon (gear ⚙) on the right.

**Map** (top half) — a map of the DFW area. After you search, this map will show colored dots for the places near you:
- 🟢 **Green dot** = open right now
- 🔴 **Red dot** = closed right now

**Tabs** — three tabs in the middle: **Food**, **Clothing**, **Shelter**. Tap one to see that kind of provider. The map also updates to show only that category's pins.

**List** — under the tabs, a scrollable list of providers in the selected category. Each card shows the place's name, address, distance from you, current open/closed status, and a short description.

**Action bar** (just above the footer) — two buttons side by side:
- **Find more online** — searches the internet for additional providers (more on this below).
- **Search** — does a fresh search using your current location.

**Footer** — a small "© We Rise DFW" line at the very bottom.

---

## How to do a search

1. Tap **Search** in the action bar.
2. The first time you tap it, Android will ask if you want to allow location access. Tap **Allow** (or **While using the app**). If you say no, the app can't search; you can change your mind later in your phone's app settings.
3. The app reads your location once, finds nearby providers within your chosen radius, and fills in the list and map.

That's it. Tap **Search** again any time you want to refresh.

> **Important:** Your location is read **only at the moment you tap Search**. It is never saved on the phone, never sent to a server, never logged. The next time you tap Search, the app reads your location again from scratch.

If you're outside the DFW metro area, you'll see an **OUT OF RANGE** banner on the map. This app only covers DFW.

---

## How to use a provider card

Tap any card in the list to open the **detail screen**. From there you can:

- **Tap the address** — opens your favorite map app with directions.
- **Tap the phone number** — opens your phone's dialer with the number ready to call.
- **Tap the website** — opens your browser at the provider's site.

You'll also see:
- The provider's full name and current open/closed status.
- A "✝" cross icon if the provider is **faith-based**. (You can hide these in Settings if you'd prefer.)
- A short blurb about what the provider does.
- Their full weekly hours.

Tap the back arrow (top-left) to return to the main list.

---

## "Find more online" — searching the internet

The bundled list inside the app is hand-curated, but it doesn't have everything. The **Find more online** button asks **OpenStreetMap** — a free, community-maintained map database — for additional providers near your location.

**When to use it:**
- The bundled list is sparse for your specific area.
- You want to see options that aren't in the curated set.
- You're in a corner of DFW the seed list doesn't cover well.

**What happens when you tap it:**
1. The button says "Searching OpenStreetMap…" while it works.
2. New providers found online are merged into the list, alongside the bundled ones.
3. A small message appears showing how many were added (e.g. "Added 7 new providers from OpenStreetMap").

**Notes about online results:**
- They may be missing some details. OpenStreetMap is volunteer-maintained, so phone numbers, hours, and addresses can be incomplete. Always **call ahead** to confirm before traveling.
- Online providers stay cached on your device after the search, so they keep showing up in the regular **Search** results.
- This is the only network call the app makes besides downloading map tiles and (optionally) translation models.

---

## Settings

Tap the gear icon (⚙) in the top-right corner of the main screen.

**Search radius** — A slider from 1 to 25 miles. The app shows only providers within this distance from you.

**Include faith-based services** — Default is **on**. Faith-based providers show a small cross icon ✝ next to their name. Turn this off if you'd prefer to see only non-faith-based providers.

**Language** — Defaults to English. The picker lists about 59 languages. When you choose a different language:
- The first time, your phone downloads a small translation file (10–30 MB) for that language. This needs an internet connection.
- After that, all translation runs **fully on your phone** — your text never leaves the device.
- Provider names, addresses, and descriptions get translated automatically.

To go back to English, just open Settings → Language → English.

---

## Privacy — what the app does and doesn't do

We take this seriously, especially because of who this app is meant to help.

**What the app does NOT do:**
- ❌ Save your location to the phone, even temporarily.
- ❌ Send your location to any server (ever).
- ❌ Track you between app sessions.
- ❌ Log your activity.
- ❌ Have an account or sign-in.
- ❌ Use any analytics or crash-reporting service.

**The only things the app sends over the internet:**
- Map tiles (the actual pictures of the map) — from OpenStreetMap.
- Translation language files (the first time you pick a non-English language) — from Google ML Kit.
- A bounding box around your area, **only when you tap "Find more online"** — sent to OpenStreetMap's Overpass query service. Your exact location is not sent; only a rough rectangle so it knows where to look.

**What is saved on your phone:**
- The provider list (so the app works without internet after your first search).
- Your settings (radius, faith-based toggle, language).
- Translated text strings, cached so the same phrase doesn't have to be re-translated.

That's it. No location data is ever written to the phone's storage.

---

## Common situations & fixes

**Compatible Phones**

- Works on any phone running **Android 7.0 (Nougat)** or newer.
- Fully supports the latest features of **Android 17**, including predictive back gestures and
  edge-to-edge display.

**"Could not get location"**
- Make sure Location is turned on in your phone's quick-settings panel.
- Make sure the app has location permission. Open your phone's **Settings → Apps → We Rise → Permissions → Location**, and pick **Allow only while using the app**.
- Try moving near a window if you're indoors.

**"OUT OF RANGE" banner on the map**
- The app is built specifically for the Dallas / Fort Worth area. If you're outside DFW it won't return results. This is by design.

**"Online search failed"**
- Check your internet connection (Wi-Fi or mobile data).
- The OpenStreetMap server occasionally rate-limits busy users. Wait a minute and try again.

**The list is empty after a search**
- Try widening your radius in Settings (up to 25 miles).
- Try **Find more online** to pull in additional providers.

**A translated language is showing English text**
- The first translation in a language can take a moment while the language model downloads. Make sure you're online for that first download.
- If translation never starts, the language you picked might not be supported by the on-device translator. Pick a different one or stay in English.

**Hours look wrong**
- Hours can change. Always **call ahead** before traveling, especially for shelters and pantries that have limited intake windows.

---

## Crisis lines (DFW)

If you're in immediate danger or crisis, please reach out — a list of round-the-clock numbers serving DFW:

- **988** — Suicide & Crisis Lifeline (call or text, nationwide)
- **211** — Texas resource hotline (food, housing, utility help)
- **(214) 946-HELP (4357)** — Genesis Women's Shelter, 24/7 (domestic violence)
- **(877) 701-7233** — SafeHaven of Tarrant County, 24/7 (domestic violence)

These numbers are also embedded in the app's seed data; you'll see them on the relevant provider's detail screen.

---

## A note from the maker

This app is offered freely, with no sign-up, no ads, and no expectation. It was built by one person trying to make a small contribution. If it helps you on a hard day, that's the whole point.

If you're a developer who wants to fork it, improve it, or adapt it to another city — please do. The code is MIT-licensed; see the [LICENSE](LICENSE) file. The technical README is in [README.md](README.md).

Take care of yourself.

— Shane W. Watson
