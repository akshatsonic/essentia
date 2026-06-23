package com.essentia.llm.openai

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder

/**
 * Plain HTTP fetcher used by the LLM's `fetch_url` tool.
 * 30-second timeout per request; small max body (200 KB) to keep tool results bounded.
 */
object HttpFetcher {

    private val TAG = "HttpFetcher"

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        defaultRequest {
            headers {
                append(HttpHeaders.UserAgent, "Essentia/0.2 (Android)")
            }
        }
    }

    /**
     * Fetches the URL. If [xToken] is provided AND the URL is for X.com, the
     * Authorization header is added. The body is truncated to ~200 KB.
     */
    suspend fun fetch(url: String, xToken: String?): String {
        Log.d(TAG, "GET $url")
        val response = client.request(URLBuilder(url).build()) {
            method = HttpMethod.Get
            if (xToken != null && (url.contains("twitter.com") || url.contains("x.com") || url.contains("api.twitter.com"))) {
                headers { append(HttpHeaders.Authorization, "Bearer $xToken") }
            }
        }
        val body = response.bodyAsText()
        return if (body.length > MAX_BODY_CHARS) {
            body.take(MAX_BODY_CHARS) + "\n[...truncated]"
        } else body
    }

    private const val MAX_BODY_CHARS = 200_000
}
