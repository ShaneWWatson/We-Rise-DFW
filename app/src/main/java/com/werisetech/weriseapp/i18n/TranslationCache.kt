package com.werisetech.weriseapp.i18n

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * Persisted translation row. The composite [key] is `<langCode>:<hash(source)>`,
 * which keeps the primary key short while still uniquely identifying a
 * `(language, source-text)` pair.
 */
@Entity(tableName = "translations")
data class TranslationCacheEntry(
    @PrimaryKey val key: String,
    val langCode: String,
    val sourceHash: Int,
    val translated: String
)

/** Room DAO for the translation cache. */
@Dao
interface TranslationDao {

    @Query("SELECT translated FROM translations WHERE key = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: TranslationCacheEntry)

    @Query("DELETE FROM translations WHERE langCode = :langCode")
    suspend fun clearLanguage(langCode: String)

    @Query("DELETE FROM translations")
    suspend fun clearAll()
}

/** Builds the composite cache key used by [TranslationCacheEntry]. */
internal fun cacheKey(langCode: String, source: String): String =
    langCode + ":" + source.hashCode()
