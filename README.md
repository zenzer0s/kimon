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

## Roadmap

Upcoming features planned for future releases:

| Feature | Status |
|---|---|
| 🔁 **Habit Tracker** – build streaks for daily habits alongside your focus sessions | Planned |

## Contributing

Contributions of all kinds are welcome!

- 🌍 **Translations** – help bring Kimon to more languages. Add or improve a locale under `app/src/main/res/values-<lang>/strings.xml` and open a PR.
- 🐛 **Bug reports** – open an issue with steps to reproduce and your Android version.
- 💡 **Feature requests** – check the roadmap first, then open a discussion.
- ⭐ **Star the repo** – if Kimon is useful to you, a star helps others discover it and keeps motivation high!

## License

Source-available under the [PolyForm Noncommercial License 1.0.0](LICENSE.md).

You may view, modify, and redistribute the code for any **noncommercial**
purpose (personal use, hobby projects, study, and use by nonprofits, schools,
or government). You may **not** sell it, publish it on a paid or ad-supported
app store, or use it in a commercial product or service.

This is *source-available*, not open source — the [Open Source
Definition](https://opensource.org/osd) requires permitting commercial use.
The copyright holder is not bound by these terms.
