package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlin.reflect.KClass

/**
 * Description:
 * @author Junerver
 * date: 2024/3/13-8:11
 * Email: junerver@gmail.com
 * Version: v1.0
 */
object EventManager {
    val subscriberMap = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    inline fun <reified T : Any> register(noinline subscriber: (T) -> Unit): () -> Unit {
        if (!subscriberMap.containsKey(T::class)) {
            subscriberMap[T::class] = mutableListOf()
        }
        subscriberMap[T::class]?.add(subscriber as (Any) -> Unit)
        return {
            subscriberMap[T::class]?.remove(subscriber)
        }
    }

    inline fun <reified T : Any> post(event: T) {
        if (subscriberMap.containsKey(T::class)) {
            subscriberMap[T::class]?.forEach { it.invoke(event) }
        }

    }
}

@Composable
inline fun <reified T : Any> useSubscribe(noinline subscriber: (T) -> Unit) {
    val latest by useLatestState(subscriber)
    val unSubscribeRef = useRef<(() -> Unit)?>(null)

    useMount {
        unSubscribeRef.current = EventManager.register(latest)
    }

    useUnmount {
        unSubscribeRef.current?.invoke()
    }
}

inline fun <reified T : Any> useEvent(): (T) -> Unit {
    return { event: T -> EventManager.post(event) }
}
