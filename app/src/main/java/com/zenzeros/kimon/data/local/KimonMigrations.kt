package com.zenzeros.kimon.data.local

import androidx.room.migration.Migration

/**
 * Room migrations for [KimonDatabase].
 *
 * The database currently ships at version 5. **From version 5 onward, every
 * schema change must add a [Migration] here and bump the `@Database` version**,
 * so a user's focus sessions, sleep sessions, tasks and tags survive an app
 * update. Do not rely on the destructive fallback for released versions.
 *
 * Example for the next schema bump:
 * ```
 * private val MIGRATION_5_6 = Migration(5, 6) { db ->
 *     db.execSQL("ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
 * }
 *
 * val ALL: Array<Migration> = arrayOf(MIGRATION_5_6)
 * ```
 *
 * Exported schema JSON lives in `app/schemas/` (configured in build.gradle.kts) —
 * commit those files; they are the reference for writing correct migrations.
 */
object KimonMigrations {
    val ALL: Array<Migration> = emptyArray()
}
