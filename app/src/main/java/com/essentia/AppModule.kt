package com.essentia

import android.content.Context
import androidx.navigation.NavController
import com.essentia.data.prefs.SecureStore
import com.essentia.data.prefs.UserPreferences
import com.essentia.data.repository.DigestRepository
import com.essentia.data.repository.SettingsRepository
import com.essentia.llm.DigestEngine
import com.essentia.llm.LlmProvider
import com.essentia.llm.LlmProviderFactory
import com.essentia.llm.mock.MockLlmProvider
import com.essentia.llm.openai.OpenAiCompatibleProvider

/**
 * Service locator. The [LlmProvider] is selected at app start based on what
 * the user has configured in Settings: if an OpenAI-compatible API key + base
 * URL are present we use the real [OpenAiCompatibleProvider]; otherwise we fall
 * back to [MockLlmProvider] so the app remains usable for local development.
 */
object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val userPreferences: UserPreferences by lazy { UserPreferences(appContext) }
    val secureStore: SecureStore by lazy { SecureStore(appContext) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(userPreferences, secureStore) }

    private val mockLlmProvider: LlmProvider by lazy { MockLlmProvider() }

    /**
     * Provider chosen at app start. Reads the configured key + base URL from
     * secure storage synchronously (DataStore is async, but at first composition
     * we can block — this is a one-time read for the singleton).
     */
    val llmProvider: LlmProvider by lazy {
        val key = secureStore.getString(com.essentia.data.prefs.SecureKeys.OPENAI_COMPATIBLE_API_KEY)
        val baseUrl = secureStore.getString(com.essentia.data.prefs.SecureKeys.OPENAI_COMPATIBLE_BASE_URL)
            ?: OpenAiCompatibleProvider.DEFAULT_BASE_URL
        val model = secureStore.getString(com.essentia.data.prefs.SecureKeys.OPENAI_COMPATIBLE_MODEL)
            ?: OpenAiCompatibleProvider.DEFAULT_MODEL
        if (!key.isNullOrBlank()) {
            OpenAiCompatibleProvider(apiKey = key, model = model, baseUrl = baseUrl)
        } else {
            mockLlmProvider
        }
    }

    val llmProviderFactory: LlmProviderFactory by lazy {
        LlmProviderFactory(secureStore = secureStore, mockProvider = mockLlmProvider)
    }

    val digestEngine: DigestEngine by lazy {
        DigestEngine(
            userPrefs = userPreferences,
            settingsRepo = settingsRepository,
            secureStore = secureStore,
            providerFactory = llmProviderFactory
        )
    }

    val repository: DigestRepository by lazy { DigestRepository(llmProvider) }

    @Volatile
    var navController: NavController? = null

    fun nav(block: (NavController) -> Unit) {
        navController?.let(block)
    }
}
