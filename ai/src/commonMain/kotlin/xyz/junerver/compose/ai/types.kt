@file:Suppress("unused")

package xyz.junerver.compose.ai

import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.FilePart
import xyz.junerver.compose.ai.usechat.ImagePart
import xyz.junerver.compose.ai.usechat.TextPart
import xyz.junerver.compose.ai.usechat.UserContentPart

/*
  Description: AI Types
  Author: Junerver
  Date: 2026/01/06
  Email: junerver@gmail.com
  Version: v1.0
*/

// region Function Type Aliases

/**
 * Function to send/submit a message with multimodal content.
 * Used by both useChat (sendMessage) and useGenerateObject (submit).
 */
typealias SendMessageFn = (content: List<UserContentPart>) -> Unit

/**
 * Alias for SendMessageFn, used in useGenerateObject for semantic clarity.
 */
typealias SubmitFn = SendMessageFn

/**
 * Function to set/replace the entire messages list.
 */
typealias SetMessagesFn = (messages: List<ChatMessage>) -> Unit

/**
 * Function to append a single message without triggering AI response.
 */
typealias AppendMessageFn = (message: ChatMessage) -> Unit

/**
 * Function to reload/regenerate the last AI response.
 */
typealias ReloadFn = () -> Unit

/**
 * Function to stop/cancel the current streaming response or generation.
 */
typealias StopFn = () -> Unit

/**
 * Alias for StopFn, used in useGenerateObject for semantic clarity.
 */
typealias StopGenerateFn = StopFn

// endregion

// region Invoke Extensions

/**
 * Extension to send a text-only message directly.
 *
 * Example:
 * ```kotlin
 * import xyz.junerver.compose.ai.invoke
 *
 * val (_, _, _, sendMessage, ...) = useChat { ... }
 * sendMessage("Hello!") // Instead of sendMessage(listOf(TextPart("Hello!")))
 * ```
 */
operator fun SendMessageFn.invoke(text: String) = this(listOf(TextPart(text)))

/**
 * Extension to send a message with text and base64-encoded image.
 *
 * Example:
 * ```kotlin
 * import xyz.junerver.compose.ai.invoke
 *
 * sendMessage("What's in this image?", imageBase64, "image/png")
 * ```
 */
operator fun SendMessageFn.invoke(text: String, imageBase64: String, mimeType: String = "image/jpeg") = this(
    listOf(
        TextPart(text),
        ImagePart.fromBase64(imageBase64, mimeType),
    ),
)

/**
 * Extension to send a message with text and image URL.
 *
 * Example:
 * ```kotlin
 * import xyz.junerver.compose.ai.invokeWithImageUrl
 *
 * sendMessage.invokeWithImageUrl("Describe this image", "https://example.com/image.jpg")
 * ```
 */
fun SendMessageFn.invokeWithImageUrl(text: String, imageUrl: String) = this(
    listOf(
        TextPart(text),
        ImagePart.fromUrl(imageUrl),
    ),
)

/**
 * Extension to send a message with text and file.
 *
 * Example:
 * ```kotlin
 * import xyz.junerver.compose.ai.invokeWithFile
 *
 * sendMessage.invokeWithFile("Analyze this PDF", pdfBase64, "application/pdf", "document.pdf")
 * ```
 */
fun SendMessageFn.invokeWithFile(
    text: String,
    fileBase64: String,
    mimeType: String,
    fileName: String? = null,
) = this(
    listOf(
        TextPart(text),
        FilePart(fileBase64, mimeType, fileName),
    ),
)

// endregion
