@file:Suppress("DuplicatedCode")

package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Stable
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import xyz.junerver.compose.hooks.UseDebounceOptions
import xyz.junerver.compose.hooks.UseThrottleOptions
import xyz.junerver.compose.hooks.userequest.utils.CachedData

/*
  Description: Request options for useRequest hook
  Author: Junerver
  Date: 2024/1/31-9:58
  Email: junerver@gmail.com
  Version: v1.0
*/
internal typealias OnBeforeCallback<TParams> = (TParams?) -> Unit
internal typealias OnSuccessCallback<TParams, TData> = (TData?, TParams?) -> Unit
internal typealias OnErrorCallback<TParams> = (Throwable, TParams?) -> Unit
internal typealias OnFinallyCallback<TParams, TData> = (TParams?, TData?, Throwable?) -> Unit

@Stable
data class UseRequestOptions<TParams, TData> internal constructor(
    /**
     * Default false. Automatically execute requestFn on component mounted.
     * If set to true, you need to manually call run
     */
    @Stable
    var manual: Boolean = false,
    /**
     * Parameters passed to requestFn when executed for the first time by default
     */
    var defaultParams: TParams? = null,
    /**
     * Triggered before requestFn execution
     */
    @Stable
    var onBefore: OnBeforeCallback<TParams> = {},
    /**
     * Triggered when requestFn succeeds; param1: request return value, param2: request parameters
     */
    @Stable
    var onSuccess: OnSuccessCallback<TParams, TData> = { _, _ -> },
    /**
     * Triggered when requestFn throws an exception
     */
    @Stable
    var onError: OnErrorCallback<TParams> = { error, _ -> error.printStackTrace() },
    /**
     * Triggered when requestFn execution completes; param1: request parameters, param2: return value, param3: exception
     */
    @Stable
    var onFinally: OnFinallyCallback<TParams, TData> = { _, _, _ -> },
    /**
     * Number of error retries. If set to -1, retry infinitely.
     */
    @Stable
    var retryCount: Int = 0,
    /**
     * Retry time interval in milliseconds.
     * If not set, a simple exponential backoff algorithm is used by default, taking 1000 * 2 * retryCount
     */
    @Stable
    var retryInterval: Duration = Duration.ZERO,
    /**
     * Polling interval in milliseconds. If the value is greater than 0, it is in polling mode.
     */
    @Stable
    var pollingInterval: Duration = Duration.ZERO,
    /**
     * Whether to continue polling when the page is hidden. If set to false, polling will be temporarily stopped when the page is hidden, and will continue the last polling when the page is displayed again.
     */
    @Stable
    var pollingWhenHidden: Boolean = false,
    /**
     * Number of polling error retries. If set to -1, retry infinitely
     */
    @Stable
    var pollingErrorRetryCount: Int = -1,
    /**
     * By setting options.ready, you can control whether the request is sent. When its value is false, the request will never be sent.
     *
     * Its specific behavior is as follows:
     *
     * When [manual]=false (automatic request mode), every time [ready] changes from false to true, a request will be automatically initiated with the parameter options.[defaultParams].
     * When [manual]=true (manual request mode), as long as [ready]=false, requests triggered by run/runAsync will not be executed.
     */
    var ready: Boolean = true,
    /**
     * By setting options.[refreshDeps], when dependencies change, [useRequest] will automatically call the [Fetch.refresh] method to achieve the effect of refreshing (repeating the last request).
     * If options.[manual] = true is set, [refreshDeps] will no longer take effect
     */
    var refreshDeps: Array<Any?> = emptyArray(),
    /**
     * If there is a dependency refresh Action function, the default [Fetch.refresh] function will not be executed, and [refreshDepsAction] will be executed instead
     */
    var refreshDepsAction: (() -> Unit)? = null,
    /**
     * Unique identifier for the request. Data with the same cacheKey is globally synchronized (cacheTime and staleTime parameters will disable this mechanism)
     */
    @Stable
    var cacheKey: String = "",
    /**
     * Set cache data recycle time. By default, cache data is recycled after 5 minutes
     * If set to `(-1).seconds`, it means cache data never expires
     */
    @Stable
    var cacheTime: Duration = 5.minutes,
    /**
     * Cache data freshness time. Within this time interval, the data is considered fresh and no new request will be made
     * If set to `(-1).seconds`, it means the data is always fresh
     */
    @Stable
    var staleTime: Duration = Duration.ZERO,
    /**
     * Custom cache strategy, if none, use default strategy
     */
    @Stable
    var setCache: ((data: CachedData<TData>) -> Unit)? = null,
    @Stable
    var getCache: ((params: TParams) -> CachedData<TData>)? = null,
    /**
     * By setting options.[loadingDelay], you can delay the time when [FetchState.loading] becomes true, effectively preventing flickering.
     * For example, when an interface normally returns quickly, if we use it conventionally, flickering will occur. After the request is initiated, it changes very quickly from false -> true -> false;
     * We can set a [loadingDelay] that is greater than this return duration, such as [50.milliseconds], so that interfaces that return within 50ms will not cause flickering.
     * This flickering actually has another variant scenario, for example, an interface will return very quickly, we don't want users to continue clicking quickly, we expect to delay loading and increase the external performance time of loading, this requirement is close to throttling, but slightly different
     */
    @Stable
    var loadingDelay: Duration = Duration.ZERO,
) {
    @Suppress("unused")
    companion object {
        fun <TParams, TData> optionOf(opt: UseRequestOptions<TParams, TData>.() -> Unit): UseRequestOptions<TParams, TData> =
            UseRequestOptions<TParams, TData>().apply {
                opt()
            }
    }

    /**
     * Enable debounce functionality by configuring [UseDebounceOptions.wait], default value is 0, debounce is not enabled
     */
    @Stable
    internal var debounceOptions: UseDebounceOptions = UseDebounceOptions.optionOf { wait = Duration.ZERO }

    /**
     * Enable throttle functionality by configuring [UseThrottleOptions.wait], default value is 0, throttle is not enabled
     */
    @Stable
    internal var throttleOptions: UseThrottleOptions = UseThrottleOptions.optionOf { wait = Duration.ZERO }

    @Stable
    var debounceOptionsOf: UseDebounceOptions.() -> Unit by DebounceOptionsDelegate { wait = Duration.ZERO }

    @Stable
    var throttleOptionsOf: UseThrottleOptions.() -> Unit by ThrottleOptionsDelegate { wait = Duration.ZERO }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UseRequestOptions<*, *>

        if (manual != other.manual) return false
        if (retryCount != other.retryCount) return false
        if (pollingWhenHidden != other.pollingWhenHidden) return false
        if (pollingErrorRetryCount != other.pollingErrorRetryCount) return false
        if (ready != other.ready) return false
        if (defaultParams != other.defaultParams) return false
        if (onBefore != other.onBefore) return false
        if (onSuccess != other.onSuccess) return false
        if (onError != other.onError) return false
        if (onFinally != other.onFinally) return false
        if (retryInterval != other.retryInterval) return false
        if (pollingInterval != other.pollingInterval) return false
        if (!refreshDeps.contentEquals(other.refreshDeps)) return false
        if (refreshDepsAction != other.refreshDepsAction) return false
        if (cacheKey != other.cacheKey) return false
        if (cacheTime != other.cacheTime) return false
        if (staleTime != other.staleTime) return false
        if (setCache != other.setCache) return false
        if (getCache != other.getCache) return false
        if (loadingDelay != other.loadingDelay) return false
        if (debounceOptions != other.debounceOptions) return false
        if (throttleOptions != other.throttleOptions) return false
        if (debounceOptionsOf != other.debounceOptionsOf) return false
        if (throttleOptionsOf != other.throttleOptionsOf) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manual.hashCode()
        result = 31 * result + defaultParams.hashCode()
        result = 31 * result + onBefore.hashCode()
        result = 31 * result + onSuccess.hashCode()
        result = 31 * result + onError.hashCode()
        result = 31 * result + onFinally.hashCode()
        result = 31 * result + retryCount
        result = 31 * result + retryInterval.hashCode()
        result = 31 * result + pollingInterval.hashCode()
        result = 31 * result + pollingWhenHidden.hashCode()
        result = 31 * result + pollingErrorRetryCount
        result = 31 * result + debounceOptions.hashCode()
        result = 31 * result + throttleOptions.hashCode()
        result = 31 * result + ready.hashCode()
        result = 31 * result + refreshDeps.contentHashCode()
        result = 31 * result + (refreshDepsAction?.hashCode() ?: 0)
        result = 31 * result + cacheKey.hashCode()
        result = 31 * result + cacheTime.hashCode()
        result = 31 * result + staleTime.hashCode()
        result = 31 * result + (setCache?.hashCode() ?: 0)
        result = 31 * result + (getCache?.hashCode() ?: 0)
        result = 31 * result + loadingDelay.hashCode()
        return result
    }
}

/**
 * Use delegate to implement debounce options configuration through functions
 */
private class DebounceOptionsDelegate(
    private var configure: UseDebounceOptions.() -> Unit,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): UseDebounceOptions.() -> Unit = configure

    operator fun <TParams, TData> setValue(
        useRequestOptions: UseRequestOptions<TParams, TData>,
        property: KProperty<*>,
        function: UseDebounceOptions.() -> Unit,
    ) {
        this.configure = function
        useRequestOptions.debounceOptions = UseDebounceOptions.optionOf(function)
    }
}

/**
 * Use delegate to implement throttle options configuration through functions
 */
private class ThrottleOptionsDelegate(
    private var configure: UseThrottleOptions.() -> Unit,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): UseThrottleOptions.() -> Unit = configure

    operator fun <TParams, TData> setValue(
        useRequestOptions: UseRequestOptions<TParams, TData>,
        property: KProperty<*>,
        function: UseThrottleOptions.() -> Unit,
    ) {
        this.configure = function
        useRequestOptions.throttleOptions = UseThrottleOptions.optionOf(function)
    }
}
