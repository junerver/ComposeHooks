package xyz.junerver.compose.hooks.usses

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/*
  Description: Holder for useSse hook return value
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.1
*/

/**
 * Holder class returned by the [useSse] hook.
 *
 * Follows the project convention: State fields first, functions after.
 * Provides access to stream state and control functions.
 *
 * Note: [data] holds only the **latest** event. To process every event
 * (e.g., for accumulation), use [UseSseOptions.onEvent].
 *
 * @param TParams The type of parameters passed to the stream function
 * @param TEvent The type of events emitted by the stream
 * @property data State containing the latest event received, null if no event yet
 * @property isStreaming State indicating whether the stream is currently active
 * @property error State containing any error that occurred during streaming
 * @property params The last used parameters (null if no stream has been started)
 * @property send Function to start or restart the stream with optional parameters
 * @property cancel Function to cancel the active stream and clear error state
 * @property refresh Function to restart the stream with the last used parameters
 */
@Stable
data class SseHolder<TParams, TEvent>(
    val data: State<TEvent?>,
    val isStreaming: State<Boolean>,
    val error: State<Throwable?>,
    val params: TParams?,
    val send: SendFn<TParams>,
    val cancel: () -> Unit,
    val refresh: () -> Unit,
)
