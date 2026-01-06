package xyz.junerver.composehooks.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private class DesktopFilePickerLauncher(
    private val onFilePicked: (PickedFile) -> Unit,
) : FilePickerLauncher {

    @OptIn(ExperimentalEncodingApi::class)
    override fun launch() {
        val dialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD).apply {
            isMultipleMode = false
            setFilenameFilter { _, name ->
                name.lowercase().let {
                    it.endsWith(".jpg") || it.endsWith(".jpeg") ||
                        it.endsWith(".png") || it.endsWith(".gif") ||
                        it.endsWith(".webp") || it.endsWith(".bmp")
                }
            }
        }
        dialog.isVisible = true

        val fileName = dialog.file
        val directory = dialog.directory

        if (fileName != null && directory != null) {
            val file = File(directory, fileName)
            if (file.exists()) {
                val bytes = Files.readAllBytes(file.toPath())
                val base64 = Base64.encode(bytes)
                val mimeType = when {
                    fileName.lowercase().endsWith(".png") -> "image/png"
                    fileName.lowercase().endsWith(".gif") -> "image/gif"
                    fileName.lowercase().endsWith(".webp") -> "image/webp"
                    fileName.lowercase().endsWith(".bmp") -> "image/bmp"
                    else -> "image/jpeg"
                }
                onFilePicked(PickedFile(fileName, mimeType, base64))
            }
        }
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    onFilePicked: (PickedFile) -> Unit,
): FilePickerLauncher {
    return remember(onFilePicked) {
        DesktopFilePickerLauncher(onFilePicked)
    }
}
