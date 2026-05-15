package xyz.junerver.compose.ai.useagent

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

/*
  Description: Tool definition for AI function calling
  Author: Junerver
  Date: 2026/01/07
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Flattens a JSON Schema that uses `$defs`/`$ref` into a plain schema.
 *
 * Most LLM API providers (OpenAI, DeepSeek, etc.) do not support `$ref` or `$defs`
 * in tool parameter schemas. This function resolves the reference and returns a flat
 * schema compatible with these APIs.
 *
 * If the schema has no `$ref`, it returns the input with `$id` and `$defs` removed.
 */
fun JsonObject.flattenSchema(): JsonObject {
    val defs = this["\$defs"]?.jsonObject
    val ref = this["\$ref"]?.let { (it as? JsonPrimitive)?.content }

    val resolved = if (defs != null && ref != null) {
        val defName = ref.removePrefix("#/\$defs/")
        defs[defName]?.jsonObject ?: this
    } else {
        this
    }

    // Strip JSON Schema meta fields that LLM APIs don't understand
    return JsonObject(resolved.filterKeys { it !in setOf("\$id", "\$schema", "\$defs", "\$ref") })
}

/**
 * Represents a tool that can be called by the AI model.
 *
 * @param T The type of the tool's input parameters
 * @property name Unique name of the tool
 * @property description Description of what the tool does (shown to the model)
 * @property parameters JSON Schema describing the tool's parameters
 * @property execute Function to execute the tool with parsed parameters
 */
data class Tool<T>(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    internal val serializer: KSerializer<T>,
    internal val execute: suspend (T) -> JsonElement,
)

/**
 * Creates a tool with automatic parameter serialization.
 *
 * Example usage:
 * ```kotlin
 * @Serializable
 * data class WeatherParams(val city: String, val unit: String = "celsius")
 *
 * val weatherTool = tool<WeatherParams>(
 *     name = "get_weather",
 *     description = "Get the current weather for a city",
 *     parameters = """
 *         {
 *           "type": "object",
 *           "properties": {
 *             "city": { "type": "string", "description": "City name" },
 *             "unit": { "type": "string", "enum": ["celsius", "fahrenheit"] }
 *           },
 *           "required": ["city"]
 *         }
 *     """.trimIndent()
 * ) { params ->
 *     "Weather in ${params.city}: 25°${if (params.unit == "celsius") "C" else "F"}"
 * }
 * ```
 *
 * @param T The type of the tool's input parameters (must be @Serializable)
 * @param name Unique name of the tool
 * @param description Description of what the tool does
 * @param parameters JSON Schema string describing the parameters
 * @param execute Function to execute the tool
 */
inline fun <reified T> tool(
    name: String,
    description: String,
    parameters: String,
    noinline execute: suspend (T) -> JsonElement,
): Tool<T> {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    val schema = json.parseToJsonElement(parameters) as JsonObject
    return Tool(
        name = name,
        description = description,
        parameters = schema,
        serializer = serializer<T>(),
        execute = execute,
    )
}

/**
 * Creates a tool whose execution returns plain text.
 */
inline fun <reified T> toolText(
    name: String,
    description: String,
    parameters: String,
    noinline execute: suspend (T) -> String,
): Tool<T> = tool(
    name = name,
    description = description,
    parameters = parameters,
    execute = { params -> JsonPrimitive(execute(params)) },
)

/**
 * Creates a tool with a pre-parsed JsonObject schema.
 */
inline fun <reified T> tool(
    name: String,
    description: String,
    parameters: JsonObject,
    noinline execute: suspend (T) -> JsonElement,
): Tool<T> = Tool(
    name = name,
    description = description,
    parameters = parameters,
    serializer = serializer<T>(),
    execute = execute,
)

/**
 * Creates a tool with a pre-parsed JsonObject schema whose execution returns plain text.
 */
inline fun <reified T> toolText(
    name: String,
    description: String,
    parameters: JsonObject,
    noinline execute: suspend (T) -> String,
): Tool<T> = Tool(
    name = name,
    description = description,
    parameters = parameters,
    serializer = serializer<T>(),
    execute = { params -> JsonPrimitive(execute(params)) },
)

/**
 * Tool choice options for controlling how the model uses tools.
 */
sealed interface ToolChoice {
    /** Model decides whether to use tools */
    data object Auto : ToolChoice

    /** Model must not use any tools */
    data object None : ToolChoice

    /** Model must use at least one tool */
    data object Required : ToolChoice

    /** Model must use the specified tool */
    data class Specific(val name: String) : ToolChoice
}

/**
 * Internal helper to execute a tool with raw JSON arguments.
 */
internal suspend fun <T> Tool<T>.executeWithJson(args: JsonObject): JsonElement {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    val params = json.decodeFromJsonElement(serializer, args)
    return execute(params)
}
