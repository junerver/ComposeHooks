package xyz.junerver.composehooks.utils

import androidx.compose.runtime.Composable

/**
 * Result of file picking operation
 */
data class PickedFile(
    val name: String,
    val mimeType: String,
    val base64Content: String,
)

/**
 * File picker launcher interface
 */
interface FilePickerLauncher {
    fun launch()
}

/**
 * Creates a file picker launcher that can be triggered to open a file selection dialog.
 *
 * @param onFilePicked Callback when a file is picked, provides the file as base64
 */
@Composable
expect fun rememberFilePickerLauncher(
    onFilePicked: (PickedFile) -> Unit,
): FilePickerLauncher
