package com.essentia.data.repository

import com.essentia.data.model.DigestItem
import com.essentia.data.model.Edition
import com.essentia.data.model.LlmDigestResponse
import com.essentia.llm.LlmProvider
import com.essentia.llm.mock.MockEditions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DigestRepository(private val llm: LlmProvider) {

    private val _today = MutableStateFlow<LlmDigestResponse?>(null)
    val today: StateFlow<LlmDigestResponse?> = _today.asStateFlow()

    private val _pastEditions = MutableStateFlow(MockEditions.allEditions)
    val pastEditions: StateFlow<List<Edition>> = _pastEditions.asStateFlow()

    private val _savedForLater = MutableStateFlow(MockEditions.savedForLater)
    val savedForLater: StateFlow<List<DigestItem>> = _savedForLater.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun generateTodayDigest(
        topic: String,
        enabledSources: Set<String> = setOf("reddit", "hn"),
        xBearerToken: String? = null
    ) {
        _isLoading.value = true
        try {
            val response = llm.generateDigest(
                topic = topic,
                sinceMs = 0L,
                enabledSources = enabledSources,
                xBearerToken = xBearerToken
            )
            _today.value = response
        } finally {
            _isLoading.value = false
        }
    }

    fun itemsForEdition(edition: Edition): List<DigestItem> =
        MockEditions.allItems[edition.id].orEmpty()

    fun getItem(id: String): DigestItem? {
        _today.value?.items?.firstOrNull { it.id == id }?.let { return it }
        MockEditions.allItems.values.flatten().firstOrNull { it.id == id }?.let { return it }
        _savedForLater.value.firstOrNull { it.id == id }?.let { return it }
        return null
    }

    fun toggleArchive(id: String) {
        _savedForLater.update { current ->
            val existing = current.firstOrNull { it.id == id }
            if (existing != null) {
                current.filter { it.id != id }
            } else {
                val found = getItem(id) ?: return@update current
                current + found.copy(isArchived = true)
            }
        }
    }

    fun isArchived(id: String): Boolean =
        _savedForLater.value.any { it.id == id }

    fun setToday(response: LlmDigestResponse) {
        _today.value = response
    }
}
