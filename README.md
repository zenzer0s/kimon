# Kimon

A focus, planning and sleep companion for Android, built with Jetpack Compose and
Material 3 Expressive.

## Features

- **Focus** – Pomodoro timer with a concentric-dial or flip-clock face, tags,
  auto-start, Do-Not-Disturb, custom alarm sound, and a foreground-service
  countdown notification.
- **Analyze** – day / week / year / overview stats: focus time, sessions, streaks,
  tag breakdowns, hourly and calendar heatmaps.
- **Plan** – a simple task list with estimated sessions and swipe-to-delete.
- **Sleep** – automatic sleep detection (Google Play Services Sleep API),
  Health Connect two-way sync, manual entry, weekly breakdown and a sleep score.
- **Steps** – hardware pedometer step counting with a daily goal and history.
- **Home-screen widgets** – last night's sleep, and a focus heatmap.
- **Backup** – export / import everything as a single JSON file.
- Multiple built-in themes, dynamic color, AMOLED black.

## Build

Requires JDK 21 and the Android SDK (compile/target API 37).

```
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit tests
./gradlew lint                   # lint
./gradlew assembleRelease        # signed release APK
```

`minSdk` is 29 (Android 10).

## Versioning

The version lives in [`gradle/version.properties`](gradle/version.properties) and
is the single source of truth:

```
VERSION_NAME=1.2.0
VERSION_CODE=10200          # major*10000 + minor*100 + patch
```

To cut a release:

```
scripts/bump-version.sh 1.2.0
git push origin master --follow-tags
```

This bumps the version, rolls `CHANGELOG.md`, commits and tags `v1.2.0`. Pushing
the tag triggers the **Release** workflow, which builds a signed APK, generates
`SHA256SUMS.txt`, and opens a **draft** GitHub release with the notes from the
changelog.

## CI

- **CI** (`.github/workflows/ci.yml`) – runs on every push and PR: unit tests,
  debug build, lint (non-blocking), uploads reports and the debug APK.
- **Release** (`.github/workflows/release.yml`) – runs on `v*` tags.

## Project layout

```
app/                     application module
  src/main/java/com/zenzeros/kimon/
    data/                Room database, DataStore settings, backup, repositories
    domain/              stats use-cases and models
    service/             pomodoro / sleep / step foreground services
    ui/                  Compose screens (focus, analyze, plan, sleep, settings)
    widget/              app-widget providers
baselineprofile/         Macrobenchmark baseline-profile generator
gradle/version.properties
scripts/bump-version.sh
```

## License

**TODO** – choose a license before publishing. Until a `LICENSE` file is added,
all rights are reserved.
