package xyz.junerver.compose.hooks.usses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useDynamicOptions
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.usecontrollable._useControllableImpl

/*
  Description: useSse - A hook for managing SSE/streaming connections
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.2
*/

private data class SseConfiguration<TParams, TEvent>(
    val streamFn: SseStreamFn<TParams, TEvent>,
    val defaultParams: TParams?,
    val onBefore: (TParams?) -> Unit,
    val onEvent: OnEventFn<TEvent>,
    val onError: (Throwable, TParams?) -> Unit,
    val onFinally: (TParams?, Throwable?) -> Unit,
)

private class SseSession<TParams, TEvent>(
    val params: TParams?,
    val configuration: SseConfiguration<TParams, TEvent>,
) {
    lateinit var job: Job
    var terminalError: Throwable? = null
    var finalized: Boolean = false
}

private data class SseStartTransition<TParams, TEvent>(
    val session: SseSession<TParams, TEvent>,
    val previousJob: Job?,
)

private class SseController<TParams, TEvent>(
    private val scope: CoroutineScope,
    private val setData: (TEvent?) -> Unit,
    private val setStreaming: (Boolean) -> Unit,
    private val setError: (Throwable?) -> Unit,
    private val setParams: (TParams?) -> Unit,
) {
    private val lock = SynchronizedObject()
    private var configuration: SseConfiguration<TParams, TEvent>? = null
    private var activeSession: SseSession<TParams, TEvent>? = null
    private var latestParams: TParams? = null
    private var disposed: Boolean = false

    val send: SendFn<TParams> = { params -> start(params) }
    val cancel: () -> Unit = ::cancelActive
    val refresh: () -> Unit = ::refreshActive

    fun update(streamFn: SseStreamFn<TParams, TEvent>, options: UseSseOptions<TParams, TEvent>) {
        synchronized(lock) {
            if (!disposed) {
                configuration = SseConfiguration(
                    streamFn = streamFn,
                    defaultParams = options.defaultParams,
                    onBefore = options.onBefore,
                    onEvent = options.onEvent,
                    onError = options.onError,
                    onFinally = options.onFinally,
                )
            }
        }
    }

    private fun start(params: TParams?) {
        val transition = synchronized(lock) {
            val currentConfiguration = configuration
            if (disposed || currentConfiguration == null) {
                null
            } else {
                val resolvedParams = params ?: currentConfiguration.defaultParams
                val session = SseSession(
                    params = resolvedParams,
                    configuration = currentConfiguration,
                )
                session.job = scope.launch(start = CoroutineStart.LAZY) {
                    collectSession(session)
                }

                SseStartTransition(
                    session = session,
                    previousJob = activeSession?.job,
                ).also {
                    activeSession = session
                    latestParams = resolvedParams
                    setParams(resolvedParams)
                    setStreaming(true)
                    setError(null)
                }
            }
        } ?: return

        transition.session.job.invokeOnCompletion {
            finalizeSession(transition.session)
        }
        transition.previousJob?.cancel()
        transition.session.job.start()
    }

    private suspend fun collectSession(session: SseSession<TParams, TEvent>) {
        try {
            session.configuration.onBefore(session.params)
            currentCoroutineContext().ensureActive()

            @Suppress("UNCHECKED_CAST")
            session.configuration.streamFn(session.params as TParams).collect { event ->
                val callback = synchronized(lock) {
                    if (activeSession === session && !session.finalized) {
                        setData(event)
                        (configuration ?: session.configuration).onEvent
                    } else {
                        null
                    }
                }
                callback?.let { invokeCallback { it(event) } }
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            currentCoroutineContext().ensureActive()

            val callback = synchronized(lock) {
                session.terminalError = error
                if (activeSession === session && !session.finalized) {
                    setError(error)
                    (configuration ?: session.configuration).onError
                } else {
                    null
                }
            }
            callback?.let { invokeCallback { it(error, session.params) } }
        }
    }

    private fun finalizeSession(session: SseSession<TParams, TEvent>) {
        var callback: ((TParams?, Throwable?) -> Unit)? = null
        synchronized(lock) {
            if (!session.finalized) {
                session.finalized = true
                if (activeSession === session) {
                    activeSession = null
                    setStreaming(false)
                }
                callback = (configuration ?: session.configuration).onFinally
            }
        }

        callback?.let {
            try {
                it(session.params, session.terminalError)
            } catch (callbackError: Throwable) {
                callbackError.printStackTrace()
            }
        }
    }

    private fun cancelActive() {
        val jobToCancel = synchronized(lock) {
            activeSession?.job.also {
                activeSession = null
                setStreaming(false)
                setError(null)
            }
        }
        jobToCancel?.cancel()
    }

    private fun refreshActive() {
        val params = synchronized(lock) { latestParams }
        start(params)
    }

    fun dispose() {
        val jobToCancel = synchronized(lock) {
            if (disposed) {
                null
            } else {
                disposed = true
                activeSession?.job.also { activeSession = null }
            }
        }
        jobToCancel?.cancel()
    }

    private fun invokeCallback(callback: () -> Unit) {
        try {
            callback()
        } catch (callbackError: Throwable) {
            if (callbackError is CancellationException) throw callbackError
            callbackError.printStackTrace()
        }
    }
}

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

    val (dataState, setData) = _useControllableImpl<TEvent?>(null)
    val (streamingState, setStreaming) = _useControllableImpl(false)
    val (errorState, setError) = _useControllableImpl<Throwable?>(null)
    val (paramsState, setParams) = _useControllableImpl<TParams?>(null)
    val scope = rememberCoroutineScope()

    val controller = useCreation {
        SseController<TParams, TEvent>(
            scope = scope,
            setData = setData,
            setStreaming = setStreaming,
            setError = setError,
            setParams = setParams,
        )
    }.current
    controller.update(streamFn = streamFn, options = options)

    useEffect(options.ready, options.manual, *options.refreshDeps) {
        if (!options.manual && options.ready) {
            controller.send(options.defaultParams)
        }
    }

    useUnmount {
        controller.dispose()
    }

    return SseHolder(
        data = dataState,
        isStreaming = streamingState,
        error = errorState,
        params = paramsState.value,
        send = controller.send,
        cancel = controller.cancel,
        refresh = controller.refresh,
    )
}
