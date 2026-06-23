package com.essentia.llm

import com.essentia.data.model.LlmDigestResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * LLM output contract. Real providers (OpenCode, OpenAI, Anthropic) must produce
 * a JSON body matching [LlmDigestResponse]. The [DigestEngine] runs a two-call
 * flow:
 *   1. [LlmProvider.generateDigest] — scrape prompt with the `fetch_url` tool.
 *      The LLM calls `fetch_url` to pull raw data from Reddit / HN / X.
 *   2. Same call internally invokes the LLM again with a cluster prompt and
 *      the raw hits, asking for JSON output matching [LlmDigestResponse].
 *
 * The MockLlmProvider skips both calls and returns canned data after a short delay.
 */
interface LlmProvider {
    val id: String
    suspend fun generateDigest(
        topic: String,
        sinceMs: Long,
        enabledSources: Set<String>,
        xBearerToken: String?
    ): LlmDigestResponse
}

/**
 * A tool the LLM can invoke. `fetch_url` is the only one we ship in v1.
 */
@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val parametersSchema: JsonElement
)

@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: JsonElement
)

@Serializable
data class LlmRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val tools: List<ToolDefinition> = emptyList()
)

sealed interface LlmResult {
    data class Ok(
        val text: String?,
        val toolCalls: List<ToolCall> = emptyList()
    ) : LlmResult

    data class Err(val code: String, val message: String) : LlmResult
}
