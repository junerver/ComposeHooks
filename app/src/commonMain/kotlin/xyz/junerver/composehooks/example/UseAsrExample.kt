package xyz.junerver.composehooks.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.ai.useasr.useAsr
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.utils.PickedFile
import xyz.junerver.composehooks.utils.rememberFilePickerLauncher

/*
  Description: useAsr hook example page
  Author: Junerver
  Date: 2026/06/12
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UseAsrExample() {
    var apiKey by useState("")
    var selectedLanguage by useState("auto")
    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }

    val (text, isLoading, error, recognize, reset) = useAsr {
        provider = Providers.MiMo(apiKey = apiKey)
        language = selectedLanguage
    }

    val filePickerLauncher = rememberFilePickerLauncher { file ->
        pickedFile = file
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "useAsr Example",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Speech Recognition via MiMo-V2.5-ASR (OpenAI Compatible)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Config section
            ExampleCard(title = "Configuration") {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter MiMo API Key") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                LanguageSelector(
                    selected = selectedLanguage,
                    onSelect = { selectedLanguage = it },
                )
            }

            // Audio input section
            ExampleCard(title = "Audio Input") {
                Text(
                    text = "Pick an audio file (mp3/wav) for speech recognition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { filePickerLauncher.launch() }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Audio File")
                    }

                    if (pickedFile != null) {
                        Text(
                            text = pickedFile!!.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            pickedFile?.let { file ->
                                // Build data URL: data:{MIME};base64,{BASE64}
                                val dataUrl = "data:${file.mimeType};base64,${file.base64Content}"
                                recognize(dataUrl)
                            }
                        },
                        enabled = apiKey.isNotBlank() && pickedFile != null && !isLoading.value,
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recognizing...")
                        } else {
                            Text("Recognize Speech")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            reset()
                            pickedFile = null
                        },
                    ) {
                        Text("Reset")
                    }
                }
            }

            // Error display
            AnimatedVisibility(
                visible = error.value != null,
                enter = slideInVertically() + fadeIn(),
                exit = fadeOut(),
            ) {
                error.value?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = "Error: ${err.message}",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Result section
            if (text.value.isNotEmpty()) {
                ExampleCard(title = "Recognized Text") {
                    Text(
                        text = text.value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageSelector(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("auto" to "Auto Detect", "zh" to "Chinese", "en" to "English")
    val displayText = languages.firstOrNull { it.first == selected }?.second ?: selected

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Language:",
            style = MaterialTheme.typography.bodyMedium,
        )
        androidx.compose.material3.TextButton(onClick = { expanded = true }) {
            Text(text = displayText, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (code == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    },
                )
            }
        }
    }
}
