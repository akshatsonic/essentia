package com.essentia.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore(name = "essentia_user_prefs")

/**
 * Non-sensitive user preferences. Anything secret (API keys, bearer tokens) lives
 * in [SecureStore] backed by EncryptedSharedPreferences.
 */
class UserPreferences(private val context: Context) {

    private val store = context.userDataStore

    val onboarded: Flow<Boolean> = store.data.map { it[ONBOARDED] ?: false }

    val providerId: Flow<String> = store.data.map { it[PROVIDER_ID] ?: "opencode" }

    val model: Flow<String> = store.data.map { it[MODEL] ?: DEFAULT_MODEL }

    val topics: Flow<List<String>> = store.data.map { p ->
        (p[TOPICS] ?: defaultTopics).toList()
    }

    val sourcesEnabled: Flow<Set<String>> = store.data.map { p ->
        p[SOURCES_ENABLED] ?: defaultSources
    }

    suspend fun setOnboarded(value: Boolean) {
        store.edit { it[ONBOARDED] = value }
    }

    suspend fun setProviderId(value: String) {
        store.edit { it[PROVIDER_ID] = value }
    }

    suspend fun setModel(value: String) {
        store.edit { it[MODEL] = value }
    }

    suspend fun setSourcesEnabled(value: Set<String>) {
        store.edit { it[SOURCES_ENABLED] = value }
    }

    suspend fun addTopic(query: String) {
        val cleaned = query.trim()
        if (cleaned.isEmpty()) return
        store.edit { p ->
            val current = (p[TOPICS] ?: defaultTopics).toMutableSet()
            current.add(cleaned)
            p[TOPICS] = current
        }
    }

    suspend fun removeTopic(query: String) {
        store.edit { p ->
            val current = (p[TOPICS] ?: defaultTopics).toMutableSet()
            current.remove(query)
            p[TOPICS] = current
        }
    }

    companion object {
        // OpenRouter's free tier default. Override in Settings if you have a
        // paid key for a stronger model.
        const val DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free"

        private val ONBOARDED = booleanPreferencesKey("onboarded")
        private val PROVIDER_ID = stringPreferencesKey("llm_provider_id")
        private val MODEL = stringPreferencesKey("llm_model")
        private val TOPICS = stringSetPreferencesKey("topics")
        private val SOURCES_ENABLED = stringSetPreferencesKey("sources_enabled")

        // User must explicitly enable at least one source in onboarding.
        // We do not pre-pick defaults — otherwise the user might not realize
        // they have to revisit Sources in Settings when something breaks.
        private val defaultTopics = emptySet<String>()
        private val defaultSources = emptySet<String>()
    }
}
