package com.riseup.werisedfw.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.riseup.werisedfw.i18n.TranslationCacheEntry
import com.riseup.werisedfw.i18n.TranslationDao

/** Room [TypeConverter]s for the [Category] enum. */
class CategoryConverters {
    /** @return the enum's name as a string for storage. */
    @TypeConverter
    @Suppress("unused")
    fun fromCategory(value: Category): String = value.name

    /**
     * Converts a stored string back to [Category].
     *
     * Falls back to [Category.FOOD] instead of throwing [IllegalArgumentException]
     * if an unrecognised value is encountered (e.g. after a schema downgrade).
     */
    @TypeConverter
    @Suppress("unused")
    fun toCategory(value: String): Category =
        Category.entries.find { it.name == value } ?: Category.FOOD
}

/**
 * The single Room database for the app.
 *
 * Holds two tables:
 *  - `services` — cached provider list (seed + online imports).
 *  - `translations` — cached ML Kit translations keyed by `(language, source)`.
 *
 * The schema is intentionally simple; we use [RoomDatabase.Builder.fallbackToDestructiveMigration]
 * so that small structural changes during development don't require hand-written
 * migrations. Production releases should switch to versioned migrations.
 */
@Database(
    entities = [Service::class, TranslationCacheEntry::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(CategoryConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /** Returns the DAO for the `services` table. */
    abstract fun services(): ServiceDao

    /** Returns the DAO for the `translations` cache table. */
    abstract fun translations(): TranslationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Returns the process-wide singleton [AppDatabase], creating it on first call.
         *
         * @param context Any [Context]; the application context is used internally.
         */
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "we_rise.db",
            )
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}

