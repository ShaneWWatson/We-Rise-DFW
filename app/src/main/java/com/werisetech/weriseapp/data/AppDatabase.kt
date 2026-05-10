package com.werisetech.weriseapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.werisetech.weriseapp.i18n.TranslationCacheEntry
import com.werisetech.weriseapp.i18n.TranslationDao

/** Room [TypeConverter]s for the [Category] enum. */
class CategoryConverters {
    @TypeConverter fun fromCategory(value: Category): String = value.name
    @TypeConverter fun toCategory(value: String): Category = Category.valueOf(value)
}

/**
 * The single Room database for the app.
 *
 * Holds two tables:
 *  - `services` — cached provider list (seed + online imports).
 *  - `translations` — cached ML Kit translations keyed by `(language, source)`.
 *
 * The schema is intentionally simple; we use [fallbackToDestructiveMigration]
 * so that small structural changes during development don't require hand-written
 * migrations. Production releases should switch to versioned migrations.
 */
@Database(
    entities = [Service::class, TranslationCacheEntry::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(CategoryConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun services(): ServiceDao
    abstract fun translations(): TranslationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "we_rise.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
