package com.essentia.data.repository

import com.essentia.data.prefs.SecureKeys
import com.essentia.data.prefs.SecureStore
import com.essentia.data.prefs.UserPreferences
import com.essentia.llm.openai.OpenAiCompatibleProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class AppSettings(
    val providerId: String,
    val model: String,
    val openaiApiKey: String?,
    val baseUrl: String,
    val xBearerToken: String?,
    val topics: List<String>,
    val sourcesEnabled: Set<String>
) {
    val hasOpenaiKey: Boolean get() = !openaiApiKey.isNullOrBlank()
    val redditEnabled: Boolean get() = "reddit" in sourcesEnabled
    val hnEnabled: Boolean get() = "hn" in sourcesEnabled
    val xEnabled: Boolean get() = "x" in sourcesEnabled
}

class SettingsRepository(
    private val userPrefs: UserPreferences,
    private val secureStore: SecureStore
) {

    val settings: Flow<AppSettings> = combine(
        userPrefs.providerId,
        userPrefs.model,
        userPrefs.topics,
        userPrefs.sourcesEnabled
    ) { providerId, model, topics, sources ->
        AppSettings(
            providerId = providerId,
            model = model,
            openaiApiKey = secureStore.getString(SecureKeys.OPENAI_COMPATIBLE_API_KEY),
            baseUrl = secureStore.getString(SecureKeys.OPENAI_COMPATIBLE_BASE_URL)
                ?: OpenAiCompatibleProvider.DEFAULT_BASE_URL,
            xBearerToken = secureStore.getString(SecureKeys.X_BEARER_TOKEN),
            topics = topics,
            sourcesEnabled = sources
        )
    }

    suspend fun setProviderId(value: String) = userPrefs.setProviderId(value)
    suspend fun setModel(value: String) = userPrefs.setModel(value)
    suspend fun setOpenaiCompatibleApiKey(value: String?) {
        secureStore.putString(SecureKeys.OPENAI_COMPATIBLE_API_KEY, value?.takeIf { it.isNotBlank() })
    }
    suspend fun setBaseUrl(value: String?) {
        secureStore.putString(SecureKeys.OPENAI_COMPATIBLE_BASE_URL, value?.takeIf { it.isNotBlank() })
    }
    suspend fun setModelString(value: String?) {
        secureStore.putString(SecureKeys.OPENAI_COMPATIBLE_MODEL, value?.takeIf { it.isNotBlank() })
    }
    suspend fun setXBearerToken(value: String?) {
        secureStore.putString(SecureKeys.X_BEARER_TOKEN, value?.takeIf { it.isNotBlank() })
    }
    suspend fun setSourcesEnabled(value: Set<String>) = userPrefs.setSourcesEnabled(value)
    suspend fun addTopic(query: String) = userPrefs.addTopic(query)
    suspend fun removeTopic(query: String) = userPrefs.removeTopic(query)
}
