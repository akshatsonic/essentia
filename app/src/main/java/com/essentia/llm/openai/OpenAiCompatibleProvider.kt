package com.essentia.llm.openai

import android.util.Log
import com.essentia.data.model.Edition
import com.essentia.data.model.EditionType
import com.essentia.data.model.LlmDigestResponse
import com.essentia.llm.LlmProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * OpenAI-compatible chat completions provider. Works with:
 *  - OpenRouter (default, has a free tier)
 *  - OpenAI
 *  - Groq
 *  - Together AI
 *  - Any other OpenAI-compatible endpoint
 *
 * The user provides a base URL and a model name. The provider speaks the
 * standard /v1/chat/completions protocol with function-calling.
 */
class OpenAiCompatibleProvider(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
    private val baseUrl: String = DEFAULT_BASE_URL
) : LlmProvider {

    override val id: String = "openai_compatible"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 120_000
        }
        defaultRequest {
            url(baseUrl.trimEnd('/') + "/")
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun generateDigest(
        topic: String,
        sinceMs: Long,
        enabledSources: Set<String>,
        xBearerToken: String?
    ): LlmDigestResponse {
        Log.i(TAG, "generateDigest: topic='$topic' sources=$enabledSources since=$sinceMs model=$model baseUrl=$baseUrl")
        val rawHits = scrape(topic, sinceMs, enabledSources, xBearerToken)
        Log.i(TAG, "scrape returned ${rawHits.rawText.length} chars across ${rawHits.sourcesHit}")
        if (rawHits.rawText.isBlank()) return emptyDigest()
        val finalJson = cluster(topic, rawHits.rawText)
        return parseDigest(finalJson)
    }

    // ----- Call 1: scrape with tool use -----

    private suspend fun scrape(
        topic: String,
        sinceMs: Long,
        sources: Set<String>,
        xToken: String?
    ): RawHits {
        val tools = ToolRegistry.all.map(ToolRegistry::toOpenAiJson)
        val messages = mutableListOf(
            buildJsonObject { put("role", "system"); put("content", scrapeSystemPrompt(sources, xToken != null)) },
            buildJsonObject { put("role", "user"); put("content", scrapeUserPrompt(topic, sinceMs, sources)) }
        )

        val agg = StringBuilder()
        val sourcesHit = mutableSetOf<String>()

        repeat(MAX_SCRAPE_ROUNDS) { round ->
            val request = buildRequest(messages, tools, jsonMode = false)
            val response = postChat(request) ?: return RawHits(agg.toString(), sourcesHit)
            val choice = response["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?: return RawHits(agg.toString(), sourcesHit)
            val message = choice["message"]?.jsonObject
                ?: return RawHits(agg.toString(), sourcesHit)

            val content = message["content"]?.let { (it as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull }
                ?: (message["content"]?.jsonPrimitive?.contentOrNull)
            if (!content.isNullOrBlank()) {
                agg.append(content).append("\n")
            }

            val toolCalls = (message["tool_calls"] as? JsonArray).orEmpty()
            if (toolCalls.isEmpty()) {
                Log.d(TAG, "scrape done (no tool calls in round $round)")
                return RawHits(agg.toString(), sourcesHit)
            }

            messages.add(
                buildJsonObject {
                    put("role", "assistant")
                    putJsonArray("tool_calls") {
                        toolCalls.forEach { tc -> add(tc) }
                    }
                }
            )

            toolCalls.forEach { tcEl ->
                val tc = tcEl.jsonObject
                val callId = tc["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val fn = tc["function"]?.jsonObject
                val name = fn?.get("name")?.jsonPrimitive?.contentOrNull.orEmpty()
                val args = fn?.get("arguments")?.jsonPrimitive?.contentOrNull.orEmpty()

                val resultText = if (name == "fetch_url") {
                    val url = runCatching {
                        json.parseToJsonElement(args).jsonObject["url"]?.jsonPrimitive?.contentOrNull
                    }.getOrNull().orEmpty()
                    if (url.isBlank()) {
                        "{\"error\": \"missing url argument\"}"
                    } else {
                        runCatching { HttpFetcher.fetch(url, xToken) }
                            .onSuccess { sourcesHit += sourceFromUrl(url) }
                            .fold(
                                onSuccess = { it },
                                onFailure = {
                                    Log.w(TAG, "fetch_url failed: $url", it)
                                    "{\"error\": \"${(it.message ?: "fetch failed").take(200)}\"}"
                                }
                            )
                    }
                } else {
                    "{\"error\": \"unknown tool: $name\"}"
                }

                messages.add(
                    buildJsonObject {
                        put("role", "tool")
                        put("tool_call_id", callId)
                        put("content", resultText)
                    }
                )
            }

            delay(POLITE_DELAY_MS)
        }

        return RawHits(agg.toString(), sourcesHit)
    }

    // ----- Call 2: cluster to JSON -----

    private suspend fun cluster(topic: String, rawHits: String): String {
        val messages = listOf<JsonObject>(
            buildJsonObject { put("role", "system"); put("content", CLUSTER_SYSTEM_PROMPT) },
            buildJsonObject {
                put("role", "user")
                put("content", "Topic: $topic\n\nRaw hits:\n---\n$rawHits")
            }
        )
        val request = buildRequest(messages, tools = emptyList(), jsonMode = true)
        val response = postChat(request) ?: return ""
        return response["choices"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull
            .orEmpty()
    }

    // ----- HTTP plumbing -----

    private suspend fun postChat(body: JsonObject): JsonObject? = runCatching {
        val response = client.post("chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
        val text = response.bodyAsText()
        if (response.status.value !in 200..299) {
            Log.e(TAG, "OpenAI-compatible HTTP ${response.status.value}: ${text.take(500)}")
            return@runCatching null
        }
        runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull()
    }.onFailure { Log.e(TAG, "postChat threw", it) }.getOrNull()

    private fun buildRequest(
        messages: List<JsonObject>,
        tools: List<JsonObject>,
        jsonMode: Boolean
    ): JsonObject = buildJsonObject {
        put("model", model)
        putJsonArray("messages") { messages.forEach { add(it) } }
        if (tools.isNotEmpty()) {
            putJsonArray("tools") { tools.forEach { add(it) } }
            put("tool_choice", "auto")
        }
        put("temperature", 0.2)
        if (jsonMode) {
            put("response_format", buildJsonObject { put("type", "json_object") })
        }
    }

    // ----- Parsing / fallbacks -----

    private fun parseDigest(rawJson: String): LlmDigestResponse {
        val cleaned = rawJson.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        if (cleaned.isEmpty()) return emptyDigest()
        return runCatching { json.decodeFromString(LlmDigestResponse.serializer(), cleaned) }
            .getOrElse { err ->
                Log.e(TAG, "parseDigest failed: ${err.message}", err)
                emptyDigest()
            }
    }

    private fun emptyDigest(): LlmDigestResponse {
        val now = System.currentTimeMillis()
        val edition = Edition(
            id = "edition-${now / 86400000}",
            date = isoDate(now),
            type = if (isWeekend(now)) EditionType.WEEKEND else EditionType.DAILY,
            digestIds = emptyList(),
            createdAt = now
        )
        return LlmDigestResponse(edition = edition, items = emptyList())
    }

    fun close() {
        client.close()
    }

    private fun sourceFromUrl(url: String): String = when {
        url.contains("reddit.com") -> "reddit"
        url.contains("hn.algolia.com") || url.contains("news.ycombinator.com") -> "hn"
        url.contains("twitter.com") || url.contains("x.com") || url.contains("api.twitter.com") -> "x"
        else -> "unknown"
    }

    private data class RawHits(val rawText: String, val sourcesHit: Set<String>)

    companion object {
        private const val TAG = "OpenAIProvider"
        const val DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free"
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        private const val MAX_SCRAPE_ROUNDS = 6
        private const val POLITE_DELAY_MS = 200L

        private val CLUSTER_SYSTEM_PROMPT = """
            You are an editorial assistant. You receive raw search hits from multiple
            sources (Reddit, Hacker News, X.com) and produce a deduplicated, clustered
            daily digest in strict JSON.

            Output JSON shape (no markdown fences, no other text):
            {
              "edition": {
                "id": "string",
                "date": "YYYY-MM-DD",
                "type": "DAILY" | "WEEKEND",
                "digestIds": ["..."],
                "createdAt": 0
              },
              "items": [
                {
                  "id": "stable hash of (title + topicTag)",
                  "topicId": "topic slug",
                  "editionId": "edition id above",
                  "title": "headline (<=120 chars)",
                  "summary": "2-3 sentence card summary (1-400 chars)",
                  "detailSummary": "multi-paragraph article body (200-3000 chars)",
                  "pullQuote": "best pull-worthy sentence from the article (<=200 chars) or null",
                  "category": "BREAKING" | "ANALYSIS" | "OPINION",
                  "readTimeMinutes": 1..30,
                  "isFeatured": true | false,
                  "isArchived": false,
                  "byline": "By <name> · <Mon D, YYYY>" | null,
                  "sourceIds": ["reddit","hn","x"],
                  "sourceUrls": ["https://..."],
                  "topicTag": "human readable topic",
                  "timestamp": <unix ms>,
                  "createdAt": <unix ms>
                }
              ]
            }

            Rules:
            - Cluster stories that refer to the same underlying event. Merge
              sourceIds and sourceUrls across all hits in a cluster.
            - Cap at 10 items, newest first.
            - Mark exactly ONE item as isFeatured=true (the strongest cluster).
            - Pick BREAKING for time-sensitive events, ANALYSIS for explainers,
              OPINION for takes/arguments.
            - readTimeMinutes: roughly word_count(detailSummary) / 220, rounded,
              clamped to 1..30.
            - byline: a synthetic editorial byline; if you make one up, keep
              it plausible and not impersonating real journalists.
            - All sourceUrls must be https. If a source has no URL, omit it.
            - Do NOT include items with empty title or summary.
        """.trimIndent()

        private fun scrapeSystemPrompt(sources: Set<String>, hasXToken: Boolean): String = buildString {
            appendLine("You are a research assistant for a daily news app.")
            appendLine("Your only job is to gather raw search hits from a small set of public APIs.")
            appendLine("You have one tool: `fetch_url`. It takes a single URL and returns the raw response body.")
            appendLine()
            appendLine("Rules:")
            appendLine("- Call `fetch_url` at most once per source, plus a couple of follow-ups if a source is empty.")
            appendLine("- If a URL returns an error or empty data, do not retry more than once.")
            appendLine("- Do not invent URLs. Use only the endpoints listed by the user.")
            appendLine("- Do not write any summary. Just fetch.")
            appendLine("- When you are done fetching, respond with a short plain-text note like 'done'.")
            if (!hasXToken && "x" in sources) {
                appendLine()
                appendLine("NOTE: X.com scraping is unavailable (no bearer token configured). Skip X.")
            }
        }.trimIndent()

        private fun scrapeUserPrompt(topic: String, sinceMs: Long, sources: Set<String>): String = buildString {
            appendLine("Topic: $topic")
            appendLine("Window: hits newer than $sinceMs (${isoFormat(sinceMs)})")
            appendLine()
            appendLine("Fetch raw data from these endpoints (call fetch_url for each):")
            if ("reddit" in sources) {
                appendLine("- Reddit:")
                appendLine("  https://www.reddit.com/r/redditdev/new.json?limit=25&after=${sinceMs / 1000L}")
            }
            if ("hn" in sources) {
                val q = URLEncoder.encode(topic, "UTF-8")
                appendLine("- Hacker News Algolia:")
                appendLine("  https://hn.algolia.com/api/v1/search?query=$q&tags=story&numericFilters=created_at_i>${sinceMs / 1000L}&hitsPerPage=25")
            }
            if ("x" in sources) {
                val q = URLEncoder.encode(topic, "UTF-8")
                appendLine("- X.com (requires bearer token; authenticated server-side):")
                appendLine("  https://api.twitter.com/2/tweets/search/recent?query=$q&start_time=${isoZ(sinceMs)}&max_results=25")
            }
        }.trimIndent()

        private fun isoFormat(ms: Long): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            return fmt.format(Date(ms))
        }

        private fun isoZ(ms: Long): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.format(Date(ms))
        }

        private fun isoDate(ms: Long): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(Date(ms))
        }

        private fun isWeekend(ms: Long): Boolean {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = ms }
            val day = cal.get(java.util.Calendar.DAY_OF_WEEK)
            return day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY
        }

        /**
         * Verify a key against a specific base URL by hitting a lightweight endpoint.
         * Tries /models first; if that 404s, falls back to a tiny chat completion.
         */
        suspend fun testKey(key: String, baseUrl: String, model: String): Result<Unit> = runCatching {
            val provider = OpenAiCompatibleProvider(apiKey = key, model = model, baseUrl = baseUrl)
            try {
                // Tiny probe: a 1-token completion request
                val body = buildJsonObject {
                    put("model", model)
                    putJsonArray("messages") {
                        addJsonObject {
                            put("role", "user")
                            put("content", "ping")
                        }
                    }
                    put("max_tokens", 1)
                    put("temperature", 0f)
                }
                val response = provider.client.post("chat/completions") {
                    contentType(ContentType.Application.Json)
                    setBody(body.toString())
                }
                val status = response.status.value
                if (status !in 200..299) {
                    val text = response.bodyAsText().take(300)
                    error("HTTP $status: $text")
                }
                // success
            } finally {
                provider.close()
            }
        }
    }
}
