@file:Suppress("unused", "ComposableNaming")

package xyz.junerver.compose.ai

import androidx.compose.runtime.Composable
import xyz.junerver.compose.ai.useagent.AgentHolder as AgentHolderImpl
import xyz.junerver.compose.ai.useagent.AgentOptions as AgentOptionsImpl
import xyz.junerver.compose.ai.useagent.OnAgentFinishCallback as OnAgentFinishCallbackImpl
import xyz.junerver.compose.ai.useagent.OnToolCallCallback as OnToolCallCallbackImpl
import xyz.junerver.compose.ai.useagent.OnToolResultCallback as OnToolResultCallbackImpl
import xyz.junerver.compose.ai.useagent.Tool as ToolImpl
import xyz.junerver.compose.ai.useagent.useAgent
import xyz.junerver.compose.ai.useasr.AsrHolder as AsrHolderImpl
import xyz.junerver.compose.ai.useasr.AsrOptionsConfig as AsrOptionsConfigImpl
import xyz.junerver.compose.ai.useasr.useAsr
import xyz.junerver.compose.ai.usechat.ChatHolder as ChatHolderImpl
import xyz.junerver.compose.ai.usechat.ChatOptions as ChatOptionsImpl
import xyz.junerver.compose.ai.usechat.ChatProvider as ChatProviderImpl
import xyz.junerver.compose.ai.usechat.ChatResponseResult as ChatResponseResultImpl
import xyz.junerver.compose.ai.usechat.OnFinishCallback as OnFinishCallbackImpl
import xyz.junerver.compose.ai.usechat.OnStreamCallback as OnStreamCallbackImpl
import xyz.junerver.compose.ai.usechat.useChat
import xyz.junerver.compose.ai.usegenerateobject.GenerateObjectHolder as GenerateObjectHolderImpl
import xyz.junerver.compose.ai.usegenerateobject.GenerateObjectOptions as GenerateObjectOptionsImpl
import xyz.junerver.compose.ai.usegenerateobject.OnObjectFinishCallback as OnObjectFinishCallbackImpl
import xyz.junerver.compose.ai.usegenerateobject.useGenerateObject
import xyz.junerver.compose.ai.usetts.TtsHolder as TtsHolderImpl
import xyz.junerver.compose.ai.usetts.TtsOptionsConfig as TtsOptionsConfigImpl
import xyz.junerver.compose.ai.usetts.useTts

/**
 * Public API facade for ComposeHooks AI module.
 *
 * This file re-exports the module's public surface so consumers can import
 * everything from the root package `xyz.junerver.compose.ai` instead of
 * digging into subpackages. It mirrors the "barrel/facade" pattern used by
 * the Palette design system and the hooks module's [Hooks.kt].
 *
 * Re-exports come in two forms:
 *  - `typealias` for public **types** declared in subpackages (Holders,
 *    Options, interfaces, function types). Each is imported under an
 *    `...Impl` alias and re-aliased to its public name, so the declaration
 *    is not self-referential.
 *  - `@Composable fun rememberXxx(...)` wrappers for the **Compose-style
 *    aliases** of every `useXxx` hook, kept as full function declarations
 *    (Kotlin function references drop default arguments). The
 *    `rememberGenerateObject` wrapper is `inline`/`reified` because it
 *    captures a type parameter.
 */

//region 类型集中导出 — useAgent
typealias AgentHolder = AgentHolderImpl
typealias AgentOptions = AgentOptionsImpl
typealias Tool<T> = ToolImpl<T>
typealias OnToolCallCallback = OnToolCallCallbackImpl
typealias OnToolResultCallback = OnToolResultCallbackImpl
typealias OnAgentFinishCallback = OnAgentFinishCallbackImpl
//endregion

//region 类型集中导出 — useAsr
typealias AsrHolder = AsrHolderImpl
typealias AsrOptionsConfig = AsrOptionsConfigImpl
//endregion

//region 类型集中导出 — useChat
typealias ChatHolder = ChatHolderImpl
typealias ChatOptions = ChatOptionsImpl
typealias ChatProvider = ChatProviderImpl
typealias ChatResponseResult = ChatResponseResultImpl
typealias OnFinishCallback = OnFinishCallbackImpl
typealias OnStreamCallback = OnStreamCallbackImpl
//endregion

//region 类型集中导出 — useGenerateObject
typealias GenerateObjectHolder<T> = GenerateObjectHolderImpl<T>
typealias GenerateObjectOptions<T> = GenerateObjectOptionsImpl<T>
typealias OnObjectFinishCallback<T> = OnObjectFinishCallbackImpl<T>
//endregion

//region 类型集中导出 — useTts
typealias TtsHolder = TtsHolderImpl
typealias TtsOptionsConfig = TtsOptionsConfigImpl
//endregion

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
