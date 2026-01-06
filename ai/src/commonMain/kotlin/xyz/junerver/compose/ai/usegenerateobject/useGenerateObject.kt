package xyz.junerver.compose.ai.usegenerateobject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import xyz.junerver.compose.ai.StopFn
import xyz.junerver.compose.ai.SubmitFn
import xyz.junerver.compose.ai.usechat.FilePart
import xyz.junerver.compose.ai.usechat.ImagePart
import xyz.junerver.compose.ai.usechat.TextPart
import xyz.junerver.compose.ai.usechat.UserContentPart
import xyz.junerver.compose.ai.usechat.useChat
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useLatestRef

/*
  Description: useGenerateObject hook for structured output generation
  Author: Junerver
  Date: 2026/01/05
  Email: junerver@gmail.com
  Version: v1.0

  Inspired by Vercel AI SDK generateObject.
  See: https://ai-sdk.dev/docs/ai-sdk-core/generating-structured-data
*/

/**
 * Holder class for useGenerateObject hook return values.
 *
 * @property object_ The generated object (null while loading or on error)
 * @property rawJson The raw JSON response from the model
 * @property isLoading Whether a request is currently in progress
 * @property error The most recent error, if any
 * @property submit Function to submit content and generate an object (supports multimodal)
 * @property stop Function to cancel the current generation
 */
@Stable
data class GenerateObjectHolder<T>(
    val object_: State<T?>,
    val rawJson: State<String>,
    val isLoading: State<Boolean>,
    val error: State<Throwable?>,
    val submit: SubmitFn,
    val stop: StopFn,
)

/**
 * Extension function to submit a text-only prompt.
 */
fun <T> GenerateObjectHolder<T>.submitText(text: String) {
    submit(listOf(TextPart(text)))
}

/**
 * Extension function to submit with text and image.
 */
fun <T> GenerateObjectHolder<T>.submitWithImage(text: String, imageBase64: String, mimeType: String = "image/jpeg") {
    submit(
        listOf(
            TextPart(text),
            ImagePart.fromBase64(imageBase64, mimeType),
        ),
    )
}

/**
 * Extension function to submit with text and image URL.
 */
fun <T> GenerateObjectHolder<T>.submitWithImageUrl(text: String, imageUrl: String) {
    submit(
        listOf(
            TextPart(text),
            ImagePart.fromUrl(imageUrl),
        ),
    )
}

/**
 * Extension function to submit with text and file.
 */
fun <T> GenerateObjectHolder<T>.submitWithFile(
    text: String,
    fileBase64: String,
    mimeType: String,
    fileName: String? = null,
) {
    submit(
        listOf(
            TextPart(text),
            FilePart(fileBase64, mimeType, fileName),
        ),
    )
}

/**
 * Default JSON configuration for parsing responses.
 */
private val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Builds the system prompt with JSON schema injection.
 */
private fun buildSchemaSystemPrompt(userSystemPrompt: String?, schema: String): String {
    val basePrompt = userSystemPrompt?.let { "$it\n\n" } ?: ""
    return """
        |${basePrompt}You must respond in JSON format, strictly following this JSON Schema:
        |$schema
        |
        |Output only valid JSON, do not include any other text, markdown code blocks, or explanations.
        """.trimMargin()
}

/**
 * Cleans up potential markdown code blocks from JSON response.
 */
private fun cleanJsonResponse(raw: String): String = raw
    .trim()
    .removePrefix("```json")
    .removePrefix("```")
    .removeSuffix("```")
    .trim()

/**
 * A Composable hook for generating structured objects from AI responses.
 *
 * This hook builds on top of [useChat] to provide structured output generation with:
 * - JSON Schema injection for structured output
 * - Automatic JSON parsing and validation
 * - Loading and error states
 * - Control functions (submit, stop)
 *
 * ## Schema Generation
 *
 * You need to provide a JSON Schema string for the target type. Options:
 *
 * 1. **Manual schema**: Write JSON Schema by hand
 * 2. **kotlinx-schema (future)**: When released to Maven Central, use `MyClass::class.jsonSchemaString`
 * 3. **Other tools**: Use any JSON Schema generation tool
 *
 * Example usage:
 * ```kotlin
 * @Serializable
 * data class Recipe(
 *     val name: String,
 *     val ingredients: List<String>,
 *     val steps: List<String>,
 * )
 *
 * val recipeSchema = """
 *     {
 *       "type": "object",
 *       "properties": {
 *         "name": { "type": "string" },
 *         "ingredients": { "type": "array", "items": { "type": "string" } },
 *         "steps": { "type": "array", "items": { "type": "string" } }
 *       },
 *       "required": ["name", "ingredients", "steps"]
 *     }
 * """.trimIndent()
 *
 * @Composable
 * fun RecipeGenerator() {
 *     val (recipe, rawJson, isLoading, error, submit, stop) = useGenerateObject<Recipe>(
 *         schema = recipeSchema
 *     ) {
 *         provider = Providers.DeepSeek(apiKey = "your-api-key")
 *         systemPrompt = "You are a professional chef."
 *     }
 *
 *     Button(onClick = { submit("Generate a Chinese dish recipe") }) {
 *         Text("Generate Recipe")
 *     }
 *
 *     recipe.value?.let { r ->
 *         Text("Name: ${r.name}")
 *         r.ingredients.forEach { Text("- $it") }
 *     }
 * }
 * ```
 *
 * @param T The target data class type, must be @Serializable
 * @param schema The JSON Schema string describing the expected output structure
 * @param optionsOf Configuration factory function for options
 * @return GenerateObjectHolder containing object state and control functions
 */
@Composable
inline fun <reified T : Any> useGenerateObject(
    schema: String,
    noinline optionsOf: GenerateObjectOptions<T>.() -> Unit = {},
): GenerateObjectHolder<T> = useGenerateObject(
    schema = schema,
    serializer = serializer<T>(),
    optionsOf = optionsOf,
)

/**
 * A Composable hook for generating structured objects from AI responses.
 *
 * This overload accepts an explicit KSerializer for cases where reified generics
 * cannot be used.
 *
 * @param T The target data class type
 * @param schema The JSON Schema string describing the expected output structure
 * @param serializer The KSerializer for type T
 * @param optionsOf Configuration factory function for options
 * @return GenerateObjectHolder containing object state and control functions
 */
@Composable
fun <T : Any> useGenerateObject(
    schema: String,
    serializer: KSerializer<T>,
    optionsOf: GenerateObjectOptions<T>.() -> Unit = {},
): GenerateObjectHolder<T> {
    val options = remember { GenerateObjectOptions.optionOf(optionsOf) }.apply(optionsOf)
    val optionsRef = useLatestRef(options)
    val serializerRef = useLatestRef(serializer)

    // State for parsed object and parse error
    val parsedObject = _useState<T?>(null)
    val parseError = _useState<Throwable?>(null)

    // Build schema-injected system prompt
    val schemaSystemPrompt = remember(schema, options.systemPrompt) {
        buildSchemaSystemPrompt(options.systemPrompt, schema)
    }

    // Use useChat internally
    val chatHolder = useChat {
        provider = optionsRef.current.provider
        model = optionsRef.current.model
        systemPrompt = schemaSystemPrompt
        temperature = optionsRef.current.temperature
        maxTokens = optionsRef.current.maxTokens
        timeout = optionsRef.current.timeout
        headers = optionsRef.current.headers
        onResponse = optionsRef.current.onResponse

        onFinish = { message, usage, _ ->
            // Parse JSON to object
            val rawJson = message.textContent
            try {
                val cleanJson = cleanJsonResponse(rawJson)
                val obj = defaultJson.decodeFromString(serializerRef.current, cleanJson)
                parsedObject.value = obj
                parseError.value = null
                optionsRef.current.onFinish?.invoke(obj, usage)
            } catch (e: Exception) {
                parsedObject.value = null
                parseError.value = e
                optionsRef.current.onError?.invoke(e)
            }
        }

        onError = { error ->
            parseError.value = error
            optionsRef.current.onError?.invoke(error)
        }
    }

    // Derive rawJson from last assistant message
    val rawJsonState = remember {
        derivedStateOf {
            chatHolder.messages.value.lastOrNull()?.textContent ?: ""
        }
    }

    // Combine chat error with parse error
    val combinedError = remember {
        derivedStateOf {
            chatHolder.error.value ?: parseError.value
        }
    }

    // Submit function - sends content and clears previous state
    val submit: SubmitFn = remember(chatHolder) {
        { content: List<UserContentPart> ->
            parsedObject.value = null
            parseError.value = null
            chatHolder.setMessages(emptyList())
            chatHolder.sendMessage(content)
        }
    }

    return remember(chatHolder, parsedObject.value, parseError.value) {
        GenerateObjectHolder(
            object_ = parsedObject,
            rawJson = rawJsonState,
            isLoading = chatHolder.isLoading,
            error = combinedError,
            submit = submit,
            stop = chatHolder.stop,
        )
    }
}

/**
 * Alias for useGenerateObject following the project's naming convention.
 */
@Composable
inline fun <reified T : Any> rememberGenerateObject(
    schema: String,
    noinline optionsOf: GenerateObjectOptions<T>.() -> Unit = {},
): GenerateObjectHolder<T> = useGenerateObject(schema, optionsOf)
