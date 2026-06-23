package com.essentia.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Source {
    @SerialName("reddit") REDDIT,
    @SerialName("hn") HN,
    @SerialName("x") X;

    val displayName: String
        get() = when (this) {
            REDDIT -> "Reddit"
            HN -> "Hacker News"
            X -> "X.com"
        }
}

@Serializable
enum class Category {
    @SerialName("BREAKING") BREAKING,
    @SerialName("ANALYSIS") ANALYSIS,
    @SerialName("OPINION") OPINION;

    val label: String
        get() = name
}

@Serializable
enum class EditionType {
    @SerialName("DAILY") DAILY,
    @SerialName("WEEKEND") WEEKEND;
}

@Serializable
data class Topic(
    val id: String,
    val name: String,
    val query: String
)

@Serializable
data class SourceCheckpoint(
    val source: String,
    val lastFetchedAt: Long
)

@Serializable
data class DigestItem(
    val id: String,
    val topicId: String,
    val editionId: String,
    val title: String,
    val summary: String,
    val detailSummary: String,
    val pullQuote: String? = null,
    val category: Category,
    val readTimeMinutes: Int,
    val isFeatured: Boolean = false,
    val isArchived: Boolean = false,
    val byline: String? = null,
    val sourceIds: List<String>,
    val sourceUrls: List<String>,
    val topicTag: String,
    val timestamp: Long,
    val createdAt: Long
) {
    val sources: List<Source>
        get() = sourceIds.mapNotNull { id ->
            runCatching { Source.valueOf(id.uppercase()) }.getOrNull()
        }

    val timestampFormatted: String
        get() {
            val hours = ((System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)).coerceAtLeast(0)
            return when {
                hours < 1 -> "Just now"
                hours < 24 -> "${hours}h ago"
                else -> "${hours / 24}d ago"
            }
        }
}

@Serializable
data class Edition(
    val id: String,
    val date: String,
    val type: EditionType,
    val digestIds: List<String>,
    val createdAt: Long
)

@Serializable
data class LlmDigestResponse(
    val edition: Edition,
    val items: List<DigestItem>
)
