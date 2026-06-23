package com.essentia.llm

import android.util.Log
import com.essentia.data.model.LlmDigestResponse
import com.essentia.data.prefs.SecureKeys
import com.essentia.data.prefs.SecureStore
import com.essentia.data.prefs.UserPreferences
import com.essentia.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Top-level orchestrator for "build a digest". Reads topics, sources, and the
 * configured LLM key from preferences, picks the right LlmProvider, and runs
 * the scrape+cluster flow once per topic.
 *
 * Publishes progress through [state] so the UI can show loading + error states
 * without each screen re-implementing that plumbing.
 */
class DigestEngine(
    private val userPrefs: UserPreferences,
    private val settingsRepo: SettingsRepository,
    private val secureStore: SecureStore,
    private val providerFactory: LlmProviderFactory
) {

    private val _state = MutableStateFlow<DigestState>(DigestState.Idle)
    val state: StateFlow<DigestState> = _state.asStateFlow()

    /**
     * Run a digest cycle for the given topic (or all topics if null).
     * Idempotent: if already running, returns the existing job.
     */
    suspend fun run(onlyTopic: String? = null): Result<LlmDigestResponse> {
        if (_state.value is DigestState.Loading) {
            return Result.failure(IllegalStateException("Already running"))
        }
        _state.value = DigestState.Loading(topic = onlyTopic ?: "all")
        return try {
            val settings = settingsRepo.settings.first()
            val apiKey = secureStore.getString(SecureKeys.OPENAI_COMPATIBLE_API_KEY)
            val xToken = secureStore.getString(SecureKeys.X_BEARER_TOKEN)

            if (apiKey.isNullOrBlank()) {
                val err = "No API key configured. Add one in Settings → LLM Provider."
                _state.value = DigestState.Error(err)
                return Result.failure(IllegalStateException(err))
            }

            if (settings.sourcesEnabled.isEmpty()) {
                val err = "No data sources enabled. Enable Reddit or Hacker News in Settings → Data Sources."
                _state.value = DigestState.Error(err)
                return Result.failure(IllegalStateException(err))
            }

            val provider = providerFactory.create(
                apiKey = apiKey,
                model = settings.model,
                baseUrl = secureStore.getString(com.essentia.data.prefs.SecureKeys.OPENAI_COMPATIBLE_BASE_URL)
                    ?: com.essentia.llm.openai.OpenAiCompatibleProvider.DEFAULT_BASE_URL
            )

            val topics = if (onlyTopic != null) {
                listOf(onlyTopic)
            } else {
                settings.topics
            }
            if (topics.isEmpty()) {
                val err = "No topics configured. Add a topic in Settings → Topics."
                _state.value = DigestState.Error(err)
                return Result.failure(IllegalStateException(err))
            }

            val since = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000  // last 3 days
            var firstResponse: LlmDigestResponse? = null

            for ((idx, topic) in topics.withIndex()) {
                _state.value = DigestState.Loading(
                    topic = topic,
                    progress = idx.toFloat() / topics.size
                )
                Log.i(TAG, "Generating digest for topic='$topic' (${idx + 1}/${topics.size})")
                val response = provider.generateDigest(
                    topic = topic,
                    sinceMs = since,
                    enabledSources = settings.sourcesEnabled,
                    xBearerToken = xToken
                )
                if (firstResponse == null) firstResponse = response
            }

            // Push the result into the repository so the UI updates immediately.
            firstResponse?.let { com.essentia.AppModule.repository.setToday(it) }

            _state.value = DigestState.Success(
                timestamp = System.currentTimeMillis(),
                topic = onlyTopic ?: "all topics"
            )
            Result.success(firstResponse!!)
        } catch (t: Throwable) {
            Log.e(TAG, "Digest failed", t)
            val msg = t.message ?: t::class.java.simpleName
            _state.value = DigestState.Error(msg)
            Result.failure(t)
        }
    }

    fun reset() {
        _state.value = DigestState.Idle
    }

    companion object {
        private const val TAG = "DigestEngine"
    }
}

sealed interface DigestState {
    data object Idle : DigestState
    data class Loading(val topic: String, val progress: Float = 0f) : DigestState
    data class Success(val timestamp: Long, val topic: String) : DigestState
    data class Error(val message: String) : DigestState
}

/**
 * Factory for [LlmProvider]s. Picks the real implementation when a key is
 * configured, falls back to the mock otherwise (so the app still runs in
 * local development without a key).
 */
class LlmProviderFactory(
    private val secureStore: SecureStore,
    private val mockProvider: LlmProvider
) {
    fun create(apiKey: String, model: String, baseUrl: String): LlmProvider {
        return com.essentia.llm.openai.OpenAiCompatibleProvider(
            apiKey = apiKey,
            model = model,
            baseUrl = baseUrl
        )
    }
}
