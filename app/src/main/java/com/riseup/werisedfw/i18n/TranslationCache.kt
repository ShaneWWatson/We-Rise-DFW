package com.riseup.werisedfw.i18n

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * Persisted translation row. The [cacheKey] is `"<langCode>:<source>"`,
 * uniquely identifying a `(language, source-text)` pair without risk of
 * hash collisions. [sourceHash] is retained as a diagnostic field only.
 */
@Entity(tableName = "translations")
data class TranslationCacheEntry(
    @PrimaryKey val cacheKey: String,
    val langCode: String,
    val sourceHash: Int,
    val translated: String,
)

/** Room DAO for the translation cache. */
@Dao
interface TranslationDao {

    /**
     * Returns the cached translated string for [key], or `null` if not cached.
     *
     * @param key Composite cache key produced by [cacheKey].
     */
    @Query("SELECT translated FROM translations WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): String?

    /**
     * Inserts or replaces a [TranslationCacheEntry].
     *
     * @param entry The entry to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: TranslationCacheEntry)
}

/**
 * Builds the cache key used by [TranslationCacheEntry].
 *
 * Uses the full source text so that hash collisions cannot cause the wrong
 * translation to be returned for a different string.
 *
 * @param langCode BCP-47 target language code.
 * @param source Original English text.
 * @return Unique string key for the `(langCode, source)` pair.
 */
internal fun cacheKey(langCode: String, source: String): String = "$langCode:$source"

