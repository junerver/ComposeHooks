@file:Suppress("DuplicatedCode")

package xyz.junerver.compose.hooks.userequest

import androidx.compose.runtime.Stable
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.DebounceOptions
import xyz.junerver.compose.hooks.TParams
import xyz.junerver.compose.hooks.ThrottleOptions
import xyz.junerver.compose.hooks.userequest.utils.CachedData

/*
  Description: 请求参数
  Author: Junerver
  Date: 2024/1/31-9:58
  Email: junerver@gmail.com
  Version: v1.0
*/
internal typealias OnBeforeCallback = (TParams) -> Unit
internal typealias OnSuccessCallback<TData> = (TData?, TParams) -> Unit
internal typealias OnErrorCallback = (Throwable, TParams) -> Unit
internal typealias OnFinallyCallback<TData> = (TParams, TData?, Throwable?) -> Unit

data class RequestOptions<TData> internal constructor(
    /**
     * 默认 false。 即在初始化时自动执行 requestFn。
     * 如果设置为 true，则需要手动调用 run
     */
    @Stable
    var manual: Boolean = false,
    /**
     * 首次默认执行时，传递给 requestFn 的参数
     */
    var defaultParams: TParams = emptyArray(),
    /**
     * requestFn 执行前触发
     */
    @Stable
    var onBefore: OnBeforeCallback = {},
    /**
     * requestFn 成功时触发；参数1：请求返回值，参数2：请求参数
     */
    @Stable
    var onSuccess: OnSuccessCallback<TData> = { _, _ -> },
    /**
     * requestFn 抛出异常时触发
     */
    @Stable
    var onError: OnErrorCallback = { error, _ -> error.printStackTrace() },
    /**
     * requestFn 执行完成时触发；参数1：请求参数，参数2：返回值，参数3：异常
     */
    @Stable
    var onFinally: OnFinallyCallback<TData> = { _, _, _ -> },
    /**
     * 错误重试次数。如果设置为 -1，则无限次重试。
     */
    @Stable
    var retryCount: Int = 0,
    /**
     * 重试时间间隔，单位为毫秒。
     * 如果不设置，默认采用简易的指数退避算法，取 1000 * 2 * retryCount
     */
    @Stable
    var retryInterval: Duration = 0.milliseconds,
    /**
     * 轮询间隔，单位为毫秒。如果值大于 0，则处于轮询模式。
     */
    @Stable
    var pollingInterval: Duration = 0.milliseconds,
    /**
     * 在页面隐藏时，是否继续轮询。如果设置为 false，在页面隐藏时会暂时停止轮询，页面重新显示时继续上次轮询。
     */
    @Stable
    var pollingWhenHidden: Boolean = false,
    /**
     * 轮询错误重试次数。如果设置为 -1，则无限次
     */
    @Stable
    var pollingErrorRetryCount: Int = -1,
    /**
     * 通过配置参数为 [DebounceOptions.wait] 开启防抖功能，默认值为0，不开启防抖
     */
    @Deprecated("Use debounceOptionsOf instead", ReplaceWith("debounceOptionsOf"))
    @Stable
    var debounceOptions: DebounceOptions = DebounceOptions.optionOf { wait = 0.seconds },
    /**
     * 通过配置参数为 [ThrottleOptions.wait] 开启节流功能，默认值为0，不开启节流
     */
    @Deprecated("Use throttleOptionsOf instead", ReplaceWith("throttleOptionsOf"))
    @Stable
    var throttleOptions: ThrottleOptions = ThrottleOptions.optionOf { wait = 0.seconds },
    /**
     * 通过设置 options.ready，可以控制请求是否发出。当其值为 false 时，请求永远都不会发出。
     *
     * 其具体行为如下：
     *
     * 当 [manual]=false 自动请求模式时，每次 [ready] 从 false 变为 true 时，都会自动发起请求，会带上参数 options.[defaultParams]。
     * 当 [manual]=true 手动请求模式时，只要 [ready]=false，则通过 run/runAsync 触发的请求都不会执行。
     */
    var ready: Boolean = true,
    /**
     * 通过设置 options.[refreshDeps]，在依赖变化时， [useRequest] 会自动调用 [Fetch.refresh] 方法，实现刷新（重复上一次请求）的效果。
     * 如果设置 options.[manual] = true，则 [refreshDeps] 不再生效
     */
    var refreshDeps: Array<Any?> = emptyArray(),
    /**
     * 如果存在依赖刷新Action函数，则不执行默认的[Fetch.refresh]函数，改为执行[refreshDepsAction]
     */
    var refreshDepsAction: (() -> Unit)? = null,
    /**
     * 请求的唯一标识。相同 cacheKey 的数据全局同步（cacheTime、staleTime 参数会使该机制失效
     */
    @Stable
    var cacheKey: String = "",
    /**
     * 设置缓存数据回收时间。默认缓存数据 5 分钟后回收
     * 如果设置为 `(-1).seconds`, 则表示缓存数据永不过期
     */
    @Stable
    var cacheTime: Duration = 5.minutes,
    /**
     * 缓存数据保持新鲜时间。在该时间间隔内，认为数据是新鲜的，不会重新发请求
     * 如果设置为 `(-1).seconds`，则表示数据永远新鲜
     */
    @Stable
    var staleTime: Duration = 0.seconds,
    /**
     * 自定义缓存策略，无则采取默认策略
     */
    @Stable
    var setCache: ((data: CachedData<TData>) -> Unit)? = null,
    @Stable
    var getCache: ((params: TParams) -> CachedData<TData>)? = null,
    /**
     * 通过设置 options.[loadingDelay] ，可以延迟 [FetchState.loading] 变成 true 的时间，有效防止闪烁。
     * 例如当一个接口正常会较快返回，我们如果常规使用会出现闪烁。从请求发起后，极快的从 false -> true ->false;
     * 我们可以设置一个大于这个返回时长的[loadingDelay]，例如[50.milliseconds]，这样在50ms内返回的接口，
     * 不会引起闪烁。这种闪烁其实还有一种变形场景，例如一个接口会极快返回，我们不希望用户继续快速点击，我们期望
     * 将loading延时，增加loading的对外表现时间，这种需求接近于节流，又稍有区别
     */
    @Stable
    var loadingDelay: Duration = 0.seconds,
) {
    @Suppress("unused")
    companion object {
        fun <T> optionOf(opt: RequestOptions<T>.() -> Unit): RequestOptions<T> = RequestOptions<T>().apply {
            opt()
        }
    }

    var debounceOptionsOf: DebounceOptions.() -> Unit by DebounceOptionsDelegate { wait = 0.seconds }
    var throttleOptionsOf: ThrottleOptions.() -> Unit by ThrottleOptionsDelegate { wait = 0.seconds }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RequestOptions<*>

        if (manual != other.manual) return false
        if (!defaultParams.contentEquals(other.defaultParams)) return false
        if (onBefore != other.onBefore) return false
        if (onSuccess != other.onSuccess) return false
        if (onError != other.onError) return false
        if (onFinally != other.onFinally) return false
        if (retryCount != other.retryCount) return false
        if (retryInterval != other.retryInterval) return false
        if (pollingInterval != other.pollingInterval) return false
        if (pollingWhenHidden != other.pollingWhenHidden) return false
        if (pollingErrorRetryCount != other.pollingErrorRetryCount) return false
        if (debounceOptions != other.debounceOptions) return false
        if (throttleOptions != other.throttleOptions) return false
        if (ready != other.ready) return false
        if (!refreshDeps.contentEquals(other.refreshDeps)) return false
        if (refreshDepsAction != other.refreshDepsAction) return false
        if (cacheKey != other.cacheKey) return false
        if (cacheTime != other.cacheTime) return false
        if (staleTime != other.staleTime) return false
        if (setCache != other.setCache) return false
        if (getCache != other.getCache) return false
        if (loadingDelay != other.loadingDelay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manual.hashCode()
        result = 31 * result + defaultParams.contentHashCode()
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
 * 使用代理来实现通过函数配置防抖选项
 */
private class DebounceOptionsDelegate(
    private var configure: DebounceOptions.() -> Unit,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): DebounceOptions.() -> Unit = configure

    operator fun <TData> setValue(requestOptions: RequestOptions<TData>, property: KProperty<*>, function: DebounceOptions.() -> Unit) {
        this.configure = function
        requestOptions.debounceOptions = DebounceOptions.optionOf(function)
    }
}

/**
 * 使用代理来实现通过函数配置节流选项
 */
private class ThrottleOptionsDelegate(
    private var configure: ThrottleOptions.() -> Unit,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): ThrottleOptions.() -> Unit = configure

    operator fun <TData> setValue(requestOptions: RequestOptions<TData>, property: KProperty<*>, function: ThrottleOptions.() -> Unit) {
        this.configure = function
        requestOptions.throttleOptions = ThrottleOptions.optionOf(function)
    }
}
