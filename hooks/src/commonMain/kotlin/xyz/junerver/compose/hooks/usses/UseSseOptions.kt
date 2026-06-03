package xyz.junerver.compose.hooks.usses

/*
  Description: Options for useSse hook
  Author: Junerver
  Date: 2026/6/3
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Configuration options for the [useSse] hook.
 *
 * Follows the same pattern as [xyz.junerver.compose.hooks.userequest.UseRequestOptions]
 * with a DSL builder and companion object for options creation.
 *
 * @param TParams The type of parameters passed to the stream function
 * @param TEvent The type of events emitted by the stream
 */
data class UseSseOptions<TParams, TEvent> internal constructor(
    /**
     * Whether the stream should not start automatically.
     * When true, the stream only starts when [SseHolder.send] is called.
     * Default: false (auto-start).
     */
    var manual: Boolean = false,

    /**
     * Default parameters to use when the stream starts automatically
     * or when [SseHolder.send] is called without parameters.
     */
    var defaultParams: TParams? = null,

    /**
     * Whether the stream is ready to start.
     * When false, the auto-start is blocked until it becomes true.
     * Useful for conditional streaming (e.g., wait for auth token).
     */
    var ready: Boolean = true,

    /**
     * Dependencies that trigger a stream restart when changed.
     * Similar to useEffect dependencies — when any value in this array changes,
     * the current stream is cancelled and a new one starts.
     */
    var refreshDeps: Array<Any?> = emptyArray(),

    /** Callback invoked before the stream starts. */
    var onBefore: (TParams?) -> Unit = {},

    /** Callback invoked for each event received from the stream. */
    var onEvent: OnEventFn<TEvent> = {},

    /** Callback invoked when an error occurs during streaming. */
    var onError: (Throwable, TParams?) -> Unit = { e, _ -> e.printStackTrace() },

    /** Callback invoked when the stream completes or is cancelled. */
    var onFinally: (TParams?, Throwable?) -> Unit = { _, _ -> },
) {
    @Suppress("unused")
    companion object {
        fun <TParams, TEvent> optionOf(opt: UseSseOptions<TParams, TEvent>.() -> Unit): UseSseOptions<TParams, TEvent> =
            UseSseOptions<TParams, TEvent>().apply {
                opt()
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UseSseOptions<*, *>

        if (manual != other.manual) return false
        if (defaultParams != other.defaultParams) return false
        if (ready != other.ready) return false
        if (!refreshDeps.contentEquals(other.refreshDeps)) return false
        if (onBefore != other.onBefore) return false
        if (onEvent != other.onEvent) return false
        if (onError != other.onError) return false
        if (onFinally != other.onFinally) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manual.hashCode()
        result = 31 * result + (defaultParams?.hashCode() ?: 0)
        result = 31 * result + ready.hashCode()
        result = 31 * result + refreshDeps.contentHashCode()
        result = 31 * result + onBefore.hashCode()
        result = 31 * result + onEvent.hashCode()
        result = 31 * result + onError.hashCode()
        result = 31 * result + onFinally.hashCode()
        return result
    }
}
