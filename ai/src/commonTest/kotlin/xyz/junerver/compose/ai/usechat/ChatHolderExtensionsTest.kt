package xyz.junerver.compose.ai.usechat

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf

class ChatHolderExtensionsTest {

    private fun createHolder(onSend: (List<UserContentPart>) -> Unit): ChatHolder {
        val messagesState = mutableStateOf(persistentListOf<ChatMessage>())
        val loadingState = mutableStateOf(false)
        val errorState = mutableStateOf<Throwable?>(null)
        return ChatHolder(
            messages = messagesState,
            isLoading = loadingState,
            error = errorState,
            sendMessage = onSend,
            setMessages = {},
            append = {},
            reload = {},
            stop = {},
        )
    }

    @Test
    fun sendTextDelegatesToSendMessage() {
        val captured = mutableListOf<List<UserContentPart>>()
        val holder = createHolder { captured += it }

        holder.sendText("Hello")

        assertEquals(1, captured.size)
        val parts = captured.single()
        assertEquals(1, parts.size)
        val textPart = parts.first() as TextPart
        assertEquals("Hello", textPart.text)
    }

    @Test
    fun sendWithImageCreatesImagePart() {
        val captured = mutableListOf<List<UserContentPart>>()
        val holder = createHolder { captured += it }

        holder.sendWithImage("Describe", "base64data", "image/png")

        val parts = captured.single()
        assertTrue(parts.any { it is ImagePart && !it.isUrl && it.data == "base64data" })
    }

    @Test
    fun sendWithImageUrlCreatesUrlImagePart() {
        val captured = mutableListOf<List<UserContentPart>>()
        val holder = createHolder { captured += it }

        holder.sendWithImageUrl("Look", "https://example.com/img.png")

        val parts = captured.single()
        val image = parts.filterIsInstance<ImagePart>().single()
        assertTrue(image.isUrl)
        assertEquals("https://example.com/img.png", image.data)
    }

    @Test
    fun sendWithFileCreatesFilePart() {
        val captured = mutableListOf<List<UserContentPart>>()
        val holder = createHolder { captured += it }

        holder.sendWithFile(
            text = "Inspect",
            fileBase64 = "filedata",
            mimeType = "application/pdf",
            fileName = "report.pdf",
        )

        val parts = captured.single()
        val file = parts.filterIsInstance<FilePart>().single()
        assertEquals("application/pdf", file.mimeType)
        assertEquals("report.pdf", file.fileName)
    }
}
