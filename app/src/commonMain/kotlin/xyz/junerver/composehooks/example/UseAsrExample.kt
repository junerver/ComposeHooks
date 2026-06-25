package xyz.junerver.composehooks.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import xyz.junerver.compose.ai.usechat.ChatProvider
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.hooks.useref.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.utils.PickedFile
import xyz.junerver.composehooks.utils.rememberFilePickerLauncher

/*
  Description: useAsr hook example page
  Author: Junerver
  Date: 2026/06/12
  Email: junerver@gmail.com
  Version: v1.1 - Support multiple providers
*/

/** Available provider types for ASR selection */
private enum class AsrProviderType(val displayName: String) {
    OpenAI("OpenAI"),
    DeepSeek("DeepSeek"),
    Moonshot("Moonshot"),
    Zhipu("Zhipu"),
    Qwen("Qwen"),
    Groq("Groq"),
    Together("Together"),
    MiMo("MiMo"),
    Anthropic("Anthropic"),
}

@Composable
fun UseAsrExample() {
    // Provider configuration
    var selectedType by useState(AsrProviderType.MiMo)
    var apiKey by useState("")
    var customModel by useState("")
    var selectedLanguage by useState("auto")
    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }

    // Create provider instance based on selection
    val provider by useCreation(selectedType, apiKey) {
        when (selectedType) {
            AsrProviderType.OpenAI -> Providers.OpenAI(apiKey = apiKey)
            AsrProviderType.DeepSeek -> Providers.DeepSeek(apiKey = apiKey)
            AsrProviderType.Moonshot -> Providers.Moonshot(apiKey = apiKey)
            AsrProviderType.Zhipu -> Providers.Zhipu(apiKey = apiKey)
            AsrProviderType.Qwen -> Providers.Qwen(apiKey = apiKey)
            AsrProviderType.Groq -> Providers.Groq(apiKey = apiKey)
            AsrProviderType.Together -> Providers.Together(apiKey = apiKey)
            AsrProviderType.MiMo -> Providers.MiMo(apiKey = apiKey)
            AsrProviderType.Anthropic -> Providers.Anthropic(apiKey = apiKey)
        }
    }

    val (text, isLoading, error, recognize, reset) = useAsr {
        this.provider = provider
        if (customModel.isNotBlank()) {
            model = customModel
        }
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
                text = "Speech Recognition via OpenAI-compatible API (supports multiple providers)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Config section
            ExampleCard(title = "Configuration") {
                AsrProviderSelector(
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter API Key for ${selectedType.displayName}") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = { Text("Model (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Leave empty to use default model") },
                    supportingText = { Text("Default: ${provider.defaultModel}") },
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
private fun AsrProviderSelector(selectedType: AsrProviderType, onTypeChange: (AsrProviderType) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedType.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Provider") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                )
            },
            singleLine = true,
        )

        // Invisible clickable overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AsrProviderType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = type.displayName,
                            fontWeight = if (type == selectedType) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onTypeChange(type)
                        expanded = false
                    },
                )
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
