package xyz.junerver.composehooks.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import xyz.junerver.compose.ai.invoke
import xyz.junerver.compose.ai.useagent.ToolChoice
import xyz.junerver.compose.ai.useagent.tool
import xyz.junerver.compose.ai.useagent.useAgent
import xyz.junerver.compose.ai.usechat.AssistantMessage
import xyz.junerver.compose.ai.usechat.ChatMessage
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usechat.ToolMessage
import xyz.junerver.compose.ai.usechat.UserMessage
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useState

/*
  Description: useAgent Example - Tool Calling Agent
  Author: Junerver
  Date: 2026/01/07
  Email: junerver@gmail.com
  Version: v1.0
*/

private enum class AgentProviderType(val displayName: String) {
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

@Serializable
private data class GetTimeParams(val timezone: String? = null)

@Serializable
private data class AddParams(val a: Int, val b: Int)

@Serializable
private data class EchoParams(val text: String)

@Composable
fun UseAgentExample() {
    var selectedType by useState(AgentProviderType.DeepSeek)
    var apiKey by useState("")
    var model by useState("")

    val provider by useCreation(selectedType, apiKey) {
        when (selectedType) {
            AgentProviderType.OpenAI -> Providers.OpenAI(apiKey = apiKey)
            AgentProviderType.DeepSeek -> Providers.DeepSeek(apiKey = apiKey)
            AgentProviderType.Moonshot -> Providers.Moonshot(apiKey = apiKey)
            AgentProviderType.Zhipu -> Providers.Zhipu(apiKey = apiKey)
            AgentProviderType.Qwen -> Providers.Qwen(apiKey = apiKey)
            AgentProviderType.Groq -> Providers.Groq(apiKey = apiKey)
            AgentProviderType.Together -> Providers.Together(apiKey = apiKey)
            AgentProviderType.MiMo -> Providers.MiMo(apiKey = apiKey)
            AgentProviderType.Anthropic -> Providers.Anthropic(apiKey = apiKey)
        }
    }

    useEffect(selectedType) {
        model = ""
    }

    val getTimeTool = remember {
        tool<GetTimeParams>(
            name = "get_time",
            description = "Get current time. Optionally provide a timezone string.",
            parameters =
                """
                {
                  "type": "object",
                  "properties": {
                    "timezone": { "type": "string", "description": "Timezone name, optional" }
                  }
                }
                """.trimIndent(),
        ) { params ->
            buildJsonObject {
                put("timezone", JsonPrimitive(params.timezone ?: "local"))
                put("epochMillis", JsonPrimitive(Clock.System.now().toEpochMilliseconds()))
                put("time", JsonPrimitive(Clock.System.now().toString()))
            }
        }
    }

    val addTool = remember {
        tool<AddParams>(
            name = "add",
            description = "Add two integers and return the sum.",
            parameters =
                """
                {
                  "type": "object",
                  "properties": {
                    "a": { "type": "integer" },
                    "b": { "type": "integer" }
                  },
                  "required": ["a", "b"]
                }
                """.trimIndent(),
        ) { params ->
            JsonPrimitive(params.a + params.b)
        }
    }

    val echoTool = remember {
        tool<EchoParams>(
            name = "echo",
            description = "Echo back text.",
            parameters =
                """
                {
                  "type": "object",
                  "properties": {
                    "text": { "type": "string" }
                  },
                  "required": ["text"]
                }
                """.trimIndent(),
        ) { params ->
            JsonPrimitive(params.text)
        }
    }

    val (messages, isLoading, error, send, _, _, stop) = useAgent {
        this.provider = provider
        this.model = model.ifBlank { null }
        tools = listOf(getTimeTool, addTool, echoTool)
        toolChoice = ToolChoice.Auto
        systemPrompt =
            """
            你是一个会使用本地工具的助手。
            - 需要获取时间时调用 get_time
            - 需要计算加法时调用 add
            - 需要原样回显时调用 echo
            工具返回的结果是可信的，可以直接用于回答。
            """.trimIndent()
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    useEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(text = "useAgent 示例", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "多轮会话 + 工具调用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AgentProviderSelector(
                        selectedType = selectedType,
                        onTypeChange = { selectedType = it },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        placeholder = { Text(provider.defaultModel) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("输入你的 ${selectedType.displayName} API Key") },
                )
            }

            AnimatedVisibility(
                visible = error.value != null,
                enter = slideInVertically() + fadeIn(),
                exit = fadeOut(),
            ) {
                error.value?.let { err ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Text(
                            text = "错误: ${err.message}",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = messages.value, key = { it.id }) { msg ->
                    AgentMessageBubble(message = msg)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading.value,
                    label = { Text("输入") },
                    placeholder = { Text("例如：现在几点？ / 3+5 等于几？") },
                    maxLines = 3,
                )
                IconButton(
                    onClick = {
                        if (isLoading.value) {
                            stop()
                        } else if (apiKey.isNotBlank() && inputText.isNotBlank()) {
                            send(inputText)
                            inputText = ""
                        }
                    },
                    enabled = isLoading.value || (apiKey.isNotBlank() && inputText.isNotBlank()),
                ) {
                    if (isLoading.value) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentProviderSelector(
    selectedType: AgentProviderType,
    onTypeChange: (AgentProviderType) -> Unit,
    modifier: Modifier = Modifier,
) {
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

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AgentProviderType.entries.forEach { type ->
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
private fun AgentMessageBubble(message: ChatMessage) {
    val (title, body) = when (message) {
        is UserMessage -> "User" to message.textContent
        is AssistantMessage -> "Assistant" to message.textContent
        is ToolMessage -> {
            val first = message.content.firstOrNull()
            val toolName = first?.toolName ?: "tool"
            val result = first?.result?.toString() ?: message.textContent
            "Tool:$toolName" to result
        }
        else -> "Message" to message.textContent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
