# UniversalBinDay

Flexible Android app for bin collection reminders that works with **any** council.

## Features

- **Select your council** (West Midlands councils pre-loaded + "Other / Manual")
- **Fully customisable bin types** – Residual, Recycling, Garden, Food, Glass, Paper, Card, etc.
- For each bin you can set:
  - Collection days (Mon–Sun)
  - Frequency (Weekly / Fortnightly)
  - Colour (pick any colour that matches your real bin)
  - Container type: **Bin**, **Box**, **Caddy**, or **Bag** (so blue box for paper vs blue bin for bottles looks different)
  - Or mark as N/A if you don’t have that service
- **Night-before push notifications** (default 19:00 the evening before collection)
- **Report Missed Collection** button – only becomes active after 5pm on a collection day (sends pre-filled email to the selected council)

## How to build

1. Open the project in Android Studio (Hedgehog or newer recommended)
2. Sync Gradle
3. Run on device or emulator (minSdk 26)

## Tech

- Kotlin + Jetpack Compose
- Material 3
- DataStore for preferences
- AlarmManager + NotificationCompat for exact night-before reminders
- ViewModel + StateFlow

Created for flexible local council bin tracking.
