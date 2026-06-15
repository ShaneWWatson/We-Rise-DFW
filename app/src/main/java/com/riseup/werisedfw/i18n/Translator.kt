package com.riseup.werisedfw.i18n

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.riseup.werisedfw.data.AppDatabase
import com.riseup.werisedfw.data.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.mlkit.nl.translate.Translator as MlKitTranslatorClient

// ---------------------------------------------------------------------------
// Translator interface
// ---------------------------------------------------------------------------

/**
 * Pluggable translation provider used by the rest of the app.
 *
 * The default implementation is [MlKitTranslator], which runs Google ML Kit
 * fully on-device. Models are downloaded once per language and then cached by
 * ML Kit on the device — no network round-trips for translation after that.
 */
interface Translator {

	/** Translate [text] from English into [targetLangCode]. Returns the original on failure. */
	suspend fun translate(text: String, targetLangCode: String): String

	/** Whether translation can actually be performed (e.g. ML Kit available). */
	fun isConfigured(): Boolean

	/** Release any held resources. Safe to call repeatedly. */
	fun close() {}
}

// ---------------------------------------------------------------------------
// ML Kit implementation
// ---------------------------------------------------------------------------

/**
 * On-device translator backed by Google ML Kit.
 *
 * The first translation into a given language triggers a one-time model
 * download (~10–30 MB). All subsequent calls run offline.
 *
 * Keeps a lightweight pool of [MlKitTranslatorClient] instances keyed by
 * target language so we don't recreate them on every call.
 */
class MlKitTranslator : Translator {

	private val clients = ConcurrentHashMap<String, MlKitTranslatorClient>()

	override fun isConfigured(): Boolean = true

	override suspend fun translate(text: String, targetLangCode: String): String {
		if (text.isBlank() || targetLangCode.equals("en", ignoreCase = true)) return text

		val targetTag = TranslateLanguage.fromLanguageTag(targetLangCode) ?: return text
		val client = clients.getOrPut(targetTag) {
			val options = TranslatorOptions.Builder()
				.setSourceLanguage(TranslateLanguage.ENGLISH)
				.setTargetLanguage(targetTag)
				.build()
			Translation.getClient(options)
		}

		return try {
			// Allow downloads on any network; small models, downloaded once.
			client.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
			client.translate(text).await()
		} catch (_: Throwable) {
			text
		}
	}

	override fun close() {
		clients.values.forEach { runCatching { it.close() } }
		clients.clear()
	}
}

// ---------------------------------------------------------------------------
// Caching wrapper
// ---------------------------------------------------------------------------

/**
 * Wraps a [Translator] with a Room-backed cache so a given
 * `(language, source-text)` pair only invokes the upstream translator once.
 */
class CachingTranslator(
	private val context: Context,
	private val upstream: Translator
                       ) {

	/** Translate every user-facing field of a [Service] in one go. */
	suspend fun translateService(service: Service, langCode: String): Service {
		if (langCode.equals("en", ignoreCase = true) || !upstream.isConfigured()) return service
		return service.copy(
			name = translateField(service.name, langCode),
			address = translateField(service.address, langCode),
			blurb = translateField(service.blurb, langCode)
		                   )
	}

	/** Translate a single string, hitting the cache before the upstream provider. */
	suspend fun translateField(text: String, langCode: String): String {
		if (text.isBlank() ||
		    langCode.equals("en", ignoreCase = true) ||
		    !upstream.isConfigured()
		) return text

		val key = cacheKey(langCode, text)
		val dao = AppDatabase.get(context).translations()

		val cached = withContext(Dispatchers.IO) { dao.get(key) }
		if (cached != null) return cached

		val translated = upstream.translate(text, langCode)
		if (translated != text) {
			withContext(Dispatchers.IO) {
				dao.put(TranslationCacheEntry(key, langCode, text.hashCode(), translated))
			}
		}
		return translated
	}
}

// ---------------------------------------------------------------------------
// Singleton accessor
// ---------------------------------------------------------------------------

/** Process-wide [CachingTranslator] backed by [MlKitTranslator]. */
object TranslatorFactory {

	@SuppressLint("StaticFieldLeak")
	@Volatile
	private var instance: CachingTranslator? = null

	/**
	 * Returns the singleton [CachingTranslator], creating it on first call.
	 *
	 * @param context Any [Context]; the application context is used internally.
	 */
	fun get(context: Context): CachingTranslator = instance ?: synchronized(this) {
		instance ?: CachingTranslator(context.applicationContext, MlKitTranslator())
			.also { instance = it }
	}
}

// ---------------------------------------------------------------------------
// Coroutine adapter for Google Tasks
// ---------------------------------------------------------------------------

/**
 * Bridges a Google Play Services [Task] into a coroutine. Avoids pulling in
 * `kotlinx-coroutines-play-services` for a single use.
 */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
	addOnCompleteListener { task ->
		if (task.isSuccessful) {
			@Suppress("UNCHECKED_CAST")
			cont.resume(task.result as T)
		} else if (task.isCanceled) {
			cont.cancel()
		} else {
			cont.resumeWithException(task.exception ?: RuntimeException("Task failed"))
		}
	}
}

