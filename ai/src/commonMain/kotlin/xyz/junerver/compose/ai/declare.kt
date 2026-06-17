@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.ai

import androidx.compose.runtime.Composable
import xyz.junerver.compose.ai.useagent.AgentHolder
import xyz.junerver.compose.ai.useagent.AgentOptions
import xyz.junerver.compose.ai.useagent.useAgent
import xyz.junerver.compose.ai.useasr.AsrHolder
import xyz.junerver.compose.ai.useasr.AsrOptionsConfig
import xyz.junerver.compose.ai.useasr.useAsr
import xyz.junerver.compose.ai.usechat.ChatHolder
import xyz.junerver.compose.ai.usechat.ChatOptions
import xyz.junerver.compose.ai.usechat.useChat
import xyz.junerver.compose.ai.usegenerateobject.GenerateObjectHolder
import xyz.junerver.compose.ai.usegenerateobject.GenerateObjectOptions
import xyz.junerver.compose.ai.usegenerateobject.useGenerateObject
import xyz.junerver.compose.ai.usetts.TtsHolder
import xyz.junerver.compose.ai.usetts.TtsOptionsConfig
import xyz.junerver.compose.ai.usetts.useTts

@Composable
fun rememberAgent(optionsOf: AgentOptions.() -> Unit = {}): AgentHolder = useAgent(optionsOf)

@Composable
fun rememberAsr(optionsOf: AsrOptionsConfig.() -> Unit = {}): AsrHolder = useAsr(optionsOf)

@Composable
fun rememberChat(optionsOf: ChatOptions.() -> Unit = {}): ChatHolder = useChat(optionsOf)

@Composable
inline fun <reified T : Any> rememberGenerateObject(
    schema: String,
    noinline optionsOf: GenerateObjectOptions<T>.() -> Unit = {},
): GenerateObjectHolder<T> = useGenerateObject(schema, optionsOf)

@Composable
fun rememberTts(optionsOf: TtsOptionsConfig.() -> Unit = {}): TtsHolder = useTts(optionsOf)
