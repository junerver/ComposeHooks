@file:Suppress("unused")

package xyz.junerver.compose.hooks.usses

import kotlinx.coroutines.flow.Flow

/*
  Description: SSE hook type definitions
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Stream function type: receives params, returns a Flow of events.
 *
 * This is the SSE/streaming counterpart to [xyz.junerver.compose.hooks.SuspendNormalFunction].
 * Instead of returning a single TData, it returns a Flow<TEvent> that emits
 * multiple events over time.
 */
typealias SseStreamFn<TParams, TEvent> = suspend (TParams) -> Flow<TEvent>

/** Function to start/restart the SSE stream with optional params. */
typealias SendFn<TParams> = (TParams?) -> Unit

/** Callback invoked for each event received from the stream. */
typealias OnEventFn<TEvent> = (TEvent) -> Unit
