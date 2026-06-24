package xyz.junerver.compose.hooks.usses
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.usecontrollable._useControllableImpl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useControllable
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useUnmount

/*
  Description: useSse - A hook for managing SSE/streaming connections
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.2
*/

/**
 * A hook for managing Server-Sent Events (SSE) and streaming connections.
 *
 * This is the streaming counterpart to [xyz.junerver.compose.hooks.userequest.useRequest].
 * While useRequest handles one-shot HTTP requests (suspend -> T), useSse handles
 * streaming connections (suspend -> Flow<T>).
 *
 * The [SseHolder.data] state holds the **latest** event received. To process every
 * event (e.g., for accumulation), use the [UseSseOptions.onEvent] callback.
 *
 * Usage:
 * ```kotlin
 * // Auto-start stream
 * val (lastEvent, isStreaming, error, params, send, cancel, refresh) = useSse(
 *     streamFn = { params: String -> myService.subscribe(params) },
 *     optionsOf = {
 *         defaultParams = "initial-value"
 *         onEvent = { event -> println("Got: $event") }
 *     }
 * )
 *
 * // Manual stream
 * val (lastEvent, isStreaming, error, params, send, cancel, refresh) = useSse(
 *     streamFn = { url: String -> sseClient.connect(url) },
 *     optionsOf = { manual = true }
 * )
 * // Later: send("https://api.example.com/events")
 * ```
 *
 * @param TParams The type of parameters passed to the stream function
 * @param TEvent The type of events emitted by the stream
 * @param streamFn The streaming function that returns a Flow of events
 * @param optionsOf Configuration DSL for the hook
 * @return [SseHolder] containing stream state and control functions
 */
@Composable
fun <TParams, TEvent> useSse(
    streamFn: SseStreamFn<TParams, TEvent>,
    optionsOf: UseSseOptions<TParams, TEvent>.() -> Unit = {},
): SseHolder<TParams, TEvent> {
    @Suppress("UNCHECKED_CAST")
    val options = useDynamicOptions(optionsOf as UseSseOptions<Any, Any>.() -> Unit)
        as UseSseOptions<TParams, TEvent>

    val (dataState, setData) = _useControllable<TEvent?>(null)
    val (streamingState, setStreaming) = _useControllableImpl(false)
    val (errorState, setError) = _useControllable<Throwable?>(null)
    val scope = rememberCoroutineScope()

    // Track the current active stream job and a monotonic id to prevent stale finally blocks
    var currentJob by useRef<Job?>(null)
    var streamId by useRef(0L)

    // Hold latest streamFn reference to avoid stale closures
    var latestStreamFn by useRef(streamFn)
    latestStreamFn = streamFn

    // Track the last used params for refresh capability
    var latestParams by useRef<TParams?>(null)

    // Core send function — launches a coroutine to call the suspend streamFn
    val sendFn: SendFn<TParams> = { params ->
        // Cancel any existing stream
        currentJob?.cancel()
        currentJob = null
        streamId += 1
        val currentStreamId = streamId

        val resolvedParams = params ?: options.defaultParams
        latestParams = resolvedParams
        options.onBefore(resolvedParams)
        setStreaming(true)
        setError(null)

        currentJob = scope.launch {
            var caughtError: Throwable? = null
            try {
                @Suppress("UNCHECKED_CAST")
                latestStreamFn(resolvedParams as TParams).collect { event ->
                    setData(event)
                    try {
                        options.onEvent(event)
                    } catch (callbackError: Throwable) {
                        if (callbackError is CancellationException) throw callbackError
                        // Swallow callback errors to avoid treating them as stream errors
                        callbackError.printStackTrace()
                    }
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                caughtError = e
                setError(e)
                options.onError(e, resolvedParams)
            } finally {
                // Only update state if this is still the current stream (not superseded)
                if (streamId == currentStreamId) {
                    currentJob = null
                    setStreaming(false)
                    options.onFinally(resolvedParams, caughtError)
                }
            }
        }
    }

    // Cancel function
    val cancelFn: () -> Unit = {
        currentJob?.cancel()
        currentJob = null
        streamId += 1
        setStreaming(false)
        setError(null)
    }

    // Refresh function — re-sends with the last used params
    val refreshFn: () -> Unit = {
        sendFn(latestParams)
    }

    // Single useEffect for auto-run and refreshDeps to avoid double-trigger
    useEffect(options.ready, options.manual, *options.refreshDeps) {
        if (!options.manual && options.ready) {
            sendFn(options.defaultParams)
        }
    }

    // Cleanup on unmount
    useUnmount {
        currentJob?.cancel()
        currentJob = null
        streamId += 1
    }

    return SseHolder(
        data = dataState,
        isStreaming = streamingState,
        error = errorState,
        params = latestParams,
        send = sendFn,
        cancel = cancelFn,
        refresh = refreshFn,
    )
}
