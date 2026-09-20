# Changelog

All notable changes to Kimon are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
`VERSION_CODE` (in `gradle/version.properties`) is `major*10000 + minor*100 + patch`.

## [Unreleased]

## [1.1.0] - 2026-09-20

### Added
- Smooth drag-and-drop task reordering powered by Material 3 Expressive motion physics (`MotionScheme`).
- Dedicated collapsible "Completed" tasks section to organize active and done items neatly.
- Separated Steps and Sleep tracking into modular feature codebases (`ui.step` and `ui.sleep`).
- Flexible tracking navigation: when both Sleep and Steps are enabled, they seamlessly combine into a unified dashboard view; when individually enabled, each functions independently as its own tab.

### Changed
- Refined task reordering gestures with responsive drag handle controls, eliminating swipe conflicts.


## [1.0.0] - 2026-09-10

### Added
- In-app update check: on launch (throttled) and from Settings → About, the app
  checks GitHub Releases for a newer stable version and posts a notification.
- Settings → About now shows the real installed version / build number and links
  to the license.
- Confirmation dialog before deleting a tag.
- Bottom-navigation icons bounce on tap, with a haptic tick.
- Per-day step history, so past days in the sleep screen show real step totals.
- Room migration scaffolding: schemas are exported and `KimonMigrations` is wired
  into the builder for real migrations from version 5 onward.

### Changed
- The "Long break" toggle in Timer settings is now a real, persisted setting.

### Fixed
- `POST_NOTIFICATIONS` is now requested on first launch (Android 13+); previously
  a fresh install never prompted and all notifications silently failed.
- Sleep sessions that cross midnight are counted on the wake-up day only, not both.
- Backup restore and "clear all data" now run in a single transaction — a failure
  mid-restore can no longer leave the database half-wiped.
- Pomodoro foreground service no longer crashes when the timer completes while the
  app is in the background.
- Do-Not-Disturb no longer gets stuck on after a quick start-then-pause.
- Step counter no longer mis-attributes overnight steps to the new day.
- Analyze screen can no longer be paged into the future.
- Selected AppTheme is included in backups.
- Alarm playback moved off the main thread (`prepareAsync`).
