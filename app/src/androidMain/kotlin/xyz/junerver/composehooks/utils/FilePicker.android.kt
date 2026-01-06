package xyz.junerver.composehooks.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private class AndroidFilePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
) : FilePickerLauncher {
    override fun launch() {
        launcher.launch("image/*")
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun readFileAsBase64(context: Context, uri: Uri): Pair<String, String>? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()
        val base64 = Base64.encode(bytes)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        Pair(base64, mimeType)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "image"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

@Composable
actual fun rememberFilePickerLauncher(onFilePicked: (PickedFile) -> Unit): FilePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val (base64, mimeType) = readFileAsBase64(context, it) ?: return@let
            val fileName = getFileName(context, it)
            onFilePicked(PickedFile(fileName, mimeType, base64))
        }
    }

    return remember(launcher) {
        AndroidFilePickerLauncher(launcher)
    }
}
