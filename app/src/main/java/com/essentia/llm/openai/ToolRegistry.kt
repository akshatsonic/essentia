package com.essentia.llm.openai

import com.essentia.llm.ToolDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object ToolRegistry {

    val fetchUrl: ToolDefinition = ToolDefinition(
        name = "fetch_url",
        description = "Fetches the raw HTTP response body of a single URL. " +
            "Use this to call public JSON APIs like Reddit, Hacker News, or X.com. " +
            "Returns the body as a string. Do not call more than once per URL.",
        parametersSchema = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("url") {
                    put("type", "string")
                    put("description", "Absolute URL to fetch.")
                }
            }
            put("required", buildJsonArray { add("url") })
        }
    )

    val all: List<ToolDefinition> = listOf(fetchUrl)

    fun toOpenAiJson(tool: ToolDefinition): JsonObject = buildJsonObject {
        put("type", "function")
        putJsonObject("function") {
            put("name", tool.name)
            put("description", tool.description)
            put("parameters", tool.parametersSchema)
        }
    }
}

