package com.zenzeros.kimon.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zenzeros.kimon.data.local.dao.FocusSessionDao
import com.zenzeros.kimon.data.local.dao.SleepSessionDao
import com.zenzeros.kimon.data.local.dao.TagDao
import com.zenzeros.kimon.data.local.dao.TaskDao
import com.zenzeros.kimon.data.local.entity.FocusSessionEntity
import com.zenzeros.kimon.data.local.entity.SleepSessionEntity
import com.zenzeros.kimon.data.local.entity.TagEntity
import com.zenzeros.kimon.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FocusSessionEntity::class, TagEntity::class, TaskEntity::class, SleepSessionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class KimonDatabase : RoomDatabase() {

    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun tagDao(): TagDao
    abstract fun taskDao(): TaskDao
    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: KimonDatabase? = null

        fun getInstance(context: Context): KimonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KimonDatabase::class.java,
                    "kimon_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with default tags
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).tagDao().insertAll(
                                    listOf(
                                        TagEntity(name = "Study", colorHex = "#7C4DFF", iconName = "ic_sparkles"),
                                        TagEntity(name = "Work", colorHex = "#2979FF", iconName = "ic_briefcase"),
                                        TagEntity(name = "Coding", colorHex = "#00B0FF", iconName = "ic_terminal"),
                                        TagEntity(name = "Reading", colorHex = "#00E676", iconName = "ic_book"),
                                        TagEntity(name = "Design", colorHex = "#FF9100", iconName = "ic_palette")
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
