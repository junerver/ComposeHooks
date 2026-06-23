package xyz.junerver.composehooks.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.schema.Description
import kotlinx.schema.Schema
import kotlinx.serialization.Serializable
import xyz.junerver.compose.ai.TokenUsageProvider
import xyz.junerver.compose.ai.invoke
import xyz.junerver.compose.ai.usechat.Providers
import xyz.junerver.compose.ai.usegenerateobject.useGenerateObject
import xyz.junerver.compose.ai.useTokenStats
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.utils.PickedFile
import xyz.junerver.composehooks.utils.rememberFilePickerLauncher

/*
  Description: useGenerateObject Example - Recipe Generator
  Author: Junerver
  Date: 2026/01/05
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Recipe data class for structured output
 */
@Serializable
@Schema
data class Recipe(
    @Description("菜谱名称")
    val name: String = "",
    @Description("菜品简介")
    val description: String = "",
    @Description("食材列表")
    val ingredients: List<Ingredient> = emptyList(),
    @Description("烹饪步骤")
    val steps: List<String> = emptyList(),
    @Description("烹饪时间")
    val cookingTime: String = "",
    @Description("难度等级")
    val difficulty: String = "",
    @Description("份量（人数）")
    val servings: Int = 0,
)

@Serializable
@Schema
data class Ingredient(
    @Description("食材名称")
    val name: String = "",
    @Description("用量")
    val amount: String = "",
)

/**
 * JSON Schema for Recipe
 */
private val recipeSchema = Recipe::class.jsonSchemaString

/** Available provider types for selection */
private enum class ObjectProviderType(val displayName: String) {
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
fun UseGenerateObjectExample() {
    TokenUsageProvider {
        // Provider configuration
        var selectedType by useState(ObjectProviderType.DeepSeek)
        var apiKey by useState("")
        var model by useState("")
        var streamEnabled by useState(true)
        var incrementalEnabled by useState(true)

        // Create provider instance
        val provider by useCreation(selectedType, apiKey) {
            when (selectedType) {
                ObjectProviderType.OpenAI -> Providers.OpenAI(apiKey = apiKey)
                ObjectProviderType.DeepSeek -> Providers.DeepSeek(apiKey = apiKey)
                ObjectProviderType.Moonshot -> Providers.Moonshot(apiKey = apiKey)
                ObjectProviderType.Zhipu -> Providers.Zhipu(apiKey = apiKey)
                ObjectProviderType.Qwen -> Providers.Qwen(apiKey = apiKey)
                ObjectProviderType.Groq -> Providers.Groq(apiKey = apiKey)
                ObjectProviderType.Together -> Providers.Together(apiKey = apiKey)
                ObjectProviderType.MiMo -> Providers.MiMo(apiKey = apiKey)
                ObjectProviderType.Anthropic -> Providers.Anthropic(apiKey = apiKey)
            }
        }

        // Reset model when provider changes
        useEffect(selectedType) {
            model = ""
        }

        // Use the hook
        val (recipe, rawJson, isLoading, error, submit, stop) = useGenerateObject<Recipe>(
            schema = recipeSchema,
        ) {
            this.provider = provider
            this.model = model.ifBlank { null }
            stream = streamEnabled
            enableIncrementalParsing = incrementalEnabled
            systemPrompt = "你是一位专业的中餐厨师，擅长创作各种美味的菜谱。请根据用户的描述生成详细的菜谱。"
            onFinish = { r, usage ->
                println("Generated recipe: ${r.name}")
                usage?.let { println("Tokens used: ${it.totalTokens}") }
            }
            onError = { e ->
                println("Error: ${e.message}")
            }
        }

        // Token usage statistics
        val tokenStats = useTokenStats()

        var inputText by remember { mutableStateOf("") }
        var pickedFile: PickedFile? by remember { mutableStateOf(null) }
        val filePickerLauncher = rememberFilePickerLauncher { file ->
            pickedFile = file
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "useGenerateObject 示例",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "AI 菜谱生成器 - 结构化输出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Provider selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ObjectProviderSelector(
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

                    // API Key input
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("输入你的 ${selectedType.displayName} API Key") },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "生成模式:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "阻塞",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = streamEnabled,
                        enabled = !isLoading.value,
                        onCheckedChange = { streamEnabled = it },
                    )
                    Text(
                        text = "流式",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "增量解析:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = incrementalEnabled,
                        enabled = streamEnabled && !isLoading.value,
                        onCheckedChange = { incrementalEnabled = it },
                    )
                }

                // Token usage display
                if (tokenStats.totalTokens > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Tokens: ${tokenStats.totalTokens}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Requests: ${tokenStats.requestCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "In: ${tokenStats.totalPromptTokens} / Out: ${tokenStats.totalCompletionTokens}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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

            // Recipe display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                val recipeValue = recipe.value
                if (recipeValue != null) {
                    if (isLoading.value && streamEnabled && incrementalEnabled) {
                        IncrementalHint()
                    }
                    RecipeCard(recipe = recipeValue)
                } else if (isLoading.value) {
                    LoadingIndicator()
                } else {
                    EmptyState()
                }

                RawJsonCard(
                    rawJson = rawJson.value,
                    isLoading = isLoading.value,
                    streamEnabled = streamEnabled,
                )
            }

            // Input area
            InputArea(
                value = inputText,
                onValueChange = { inputText = it },
                pickedFile = pickedFile,
                onFileClear = { pickedFile = null },
                onAddFile = { filePickerLauncher.launch() },
                onSend = {
                    if (inputText.isNotBlank() && apiKey.isNotBlank()) {
                        pickedFile?.let { file ->
                            submit(inputText, file.base64Content, file.mimeType)
                        } ?: submit(inputText)
                        inputText = ""
                        pickedFile = null
                    }
                },
                onStop = stop,
                isLoading = isLoading.value,
                canSend = apiKey.isNotBlank() && inputText.isNotBlank(),
            )
        }
    }
    }
}

@Composable
private fun ObjectProviderSelector(
    selectedType: ObjectProviderType,
    onTypeChange: (ObjectProviderType) -> Unit,
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
            ObjectProviderType.entries.forEach { type ->
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
private fun RecipeCard(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header
            Text(
                text = recipe.name.ifBlank { "（生成中...）" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recipe.description.ifBlank { "（等待模型输出描述）" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Meta info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetaChip(label = "⏱️ ${recipe.cookingTime.ifBlank { "未知" }}")
                MetaChip(label = "📊 ${recipe.difficulty.ifBlank { "未知" }}")
                MetaChip(label = "👥 ${recipe.servings.takeIf { it > 0 } ?: 0}人份")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Ingredients
            Text(
                text = "🥬 食材",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (recipe.ingredients.isEmpty()) {
                Text(
                    text = "（等待模型输出食材列表）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                recipe.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "• ${ingredient.name}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = ingredient.amount,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Steps
            Text(
                text = "👨‍🍳 烹饪步骤",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (recipe.steps.isEmpty()) {
                Text(
                    text = "（等待模型输出步骤）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                recipe.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncrementalHint() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            text = "增量解析中：当 JSON 临时可解析时，会提前更新菜谱预览（可能包含默认值）。",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun RawJsonCard(rawJson: String, isLoading: Boolean, streamEnabled: Boolean) {
    if (rawJson.isBlank() && !isLoading) return

    val preview = if (rawJson.length > 4000) rawJson.takeLast(4000) else rawJson
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (streamEnabled) "Raw JSON（流式输出）" else "Raw JSON（阻塞输出）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preview.ifBlank { "（等待模型输出...）" },
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (rawJson.length > 4000) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "（仅展示最后 4000 字符）",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在生成菜谱...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🍳",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "描述你想要的菜品",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "例如：\"一道简单的家常川菜\" 或 \"适合夏天的清淡汤品\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun InputArea(
    value: String,
    onValueChange: (String) -> Unit,
    pickedFile: PickedFile?,
    onFileClear: () -> Unit,
    onAddFile: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isLoading: Boolean,
    canSend: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // File preview
            if (pickedFile != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pickedFile.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onFileClear,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "移除图片",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Add file button
                IconButton(
                    onClick = onAddFile,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加图片",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("描述你想要的菜品...") },
                    enabled = !isLoading,
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    shape = RoundedCornerShape(24.dp),
                )

                IconButton(
                    onClick = { if (isLoading) onStop() else onSend() },
                    enabled = isLoading || canSend,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading || canSend) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "生成",
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
