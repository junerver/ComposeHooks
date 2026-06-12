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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Switch
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
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usetts.useTts
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard

/*
  Description: useTts hook example page
  Author: Junerver
  Date: 2026/06/12
  Email: junerver@gmail.com
  Version: v1.0
*/

/** Preset voices for MiMo TTS */
private data class VoiceOption(val id: String, val displayName: String, val gender: String)

private val voiceOptions = listOf(
    VoiceOption("mimo_default", "MiMo Default", "F"),
    VoiceOption("冰糖", "冰糖 (Bingtang)", "F"),
    VoiceOption("茉莉", "茉莉 (Jasmine)", "F"),
    VoiceOption("苏打", "苏打 (Soda)", "M"),
    VoiceOption("白桦", "白桦 (Birch)", "M"),
    VoiceOption("Mia", "Mia", "F"),
    VoiceOption("Chloe", "Chloe", "F"),
    VoiceOption("Milo", "Milo", "M"),
    VoiceOption("Dean", "Dean", "M"),
)

@Composable
fun UseTtsExample() {
    var apiKey by useState("")
    var selectedVoice by useState("mimo_default")
    var streamEnabled by useState(true)
    var inputText by useState("你好，我是MiMo语音合成助手，很高兴认识你！")
    var styleInstruction by useState("用甜美可爱的语调说")

    val (audioDataBase64, isLoading, error, synthesize, stop, reset) = useTts {
        provider = Providers.MiMo(apiKey = apiKey)
        voice = selectedVoice
        stream = streamEnabled
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
                text = "useTts Example",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Text-to-Speech via MiMo-V2.5-TTS (OpenAI Compatible)",
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

                VoiceSelector(
                    selected = selectedVoice,
                    onSelect = { selectedVoice = it },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Output mode:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Blocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = streamEnabled,
                        enabled = !isLoading.value,
                        onCheckedChange = { streamEnabled = it },
                    )
                    Text(
                        text = "Streaming",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Text input section
            ExampleCard(title = "Text to Synthesize") {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Text Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text("Enter text to convert to speech...") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = styleInstruction,
                    onValueChange = { styleInstruction = it },
                    label = { Text("Style Instruction (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., 用甜美可爱的语调说") },
                    supportingText = { Text("Natural language instruction for voice style/emotion") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            synthesize(
                                inputText,
                                styleInstruction.ifBlank { null },
                            )
                        },
                        enabled = apiKey.isNotBlank() && inputText.isNotBlank() && !isLoading.value,
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Synthesize")
                        }
                    }

                    if (isLoading.value) {
                        OutlinedButton(onClick = stop) {
                            Text("Stop")
                        }
                    }

                    OutlinedButton(onClick = reset) {
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

            // Audio result section
            if (audioDataBase64.value.isNotEmpty()) {
                ExampleCard(title = "Audio Result") {
                    val audioData = audioDataBase64.value
                    Text(
                        text = if (streamEnabled) {
                            "PCM16 audio chunks received (${audioData.length} base64 chars)"
                        } else {
                            "WAV audio received (${audioData.length} base64 chars)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Format: ${if (streamEnabled) "pcm16 (streaming)" else "wav (complete)"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Show truncated base64 preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    ) {
                        Text(
                            text = audioData.take(200) + if (audioData.length > 200) "..." else "",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "💡 Audio playback requires platform-specific implementation. " +
                            "The base64 data above can be decoded to ${if (streamEnabled) "PCM16 raw audio" else "a WAV file"}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceSelector(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = voiceOptions.firstOrNull { it.id == selected }
        ?.let { "${it.displayName} (${it.gender})" }
        ?: selected

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Voice:",
            style = MaterialTheme.typography.bodyMedium,
        )
        androidx.compose.material3.TextButton(onClick = { expanded = true }) {
            Text(text = displayText, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            voiceOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${option.displayName} (${option.gender})",
                            fontWeight = if (option.id == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
