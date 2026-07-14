package xyz.junerver.compose.hooks.useevent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.uselatest.useLatestStateImpl
import xyz.junerver.compose.hooks.utils.HooksEventManager

/*
  Description: More convenient communication between components, just like using EventBus
  Author: Junerver
  Date: 2024/3/13-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Register a subscriber. Note that this subscription function will be
 * removed from the subscription list after the component is uninstalled.
 *
 * @param subscriber
 * @param T
 * @receiver
 */
@Composable
inline fun <reified T : Any> useEventSubscribeImpl(noinline subscriber: (T) -> Unit) {
    val latestSubscriber = useLatestStateImpl(subscriber)
    DisposableEffect(T::class) {
        val unsubscribe = HooksEventManager.register<T> { event ->
            latestSubscriber.value(event)
        }
        onDispose(unsubscribe)
    }
}

/**
 * This hook returns a publish function, use that fun to post a event.
 *
 * @param T
 * @return
 */
@Composable
inline fun <reified T : Any> useEventPublishImpl(): (T) -> Unit = remember {
    { event: T -> HooksEventManager.post(event, T::class) }
}

/**
 * 仅在库内部使用的事件订阅
 *
 * @param alias
 * @param subscriber
 * @param T
 * @receiver
 */
@Composable
internal fun <T> useEventSubscribeImpl(alias: String, subscriber: (T?) -> Unit) {
    val latestSubscriber = useLatestStateImpl(subscriber)
    DisposableEffect(alias) {
        val unsubscribe = HooksEventManager.register<T?>(alias) { event ->
            latestSubscriber.value(event)
        }
        onDispose(unsubscribe)
    }
}

/**
 * 仅在库内部使用的事件发布
 *
 * @param T
 * @return
 */
@Composable
internal fun <T> useEventPublishImpl(alias: String): (T) -> Unit {
    val latestAlias = useLatestRefImpl(alias)
    return remember {
        { event: T -> HooksEventManager.post(latestAlias.current, event) }
    }
}
