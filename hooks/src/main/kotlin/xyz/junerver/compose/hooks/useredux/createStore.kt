package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.kotlin.Tuple3

/**
 * key: state type
 * value: reducer\initState\ action type
 */
typealias Store = Map<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>

class StoreScope private constructor(val map: MutableMap<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>) {
    inline fun <reified T : Any, reified A : Any> add(pair: Pair<Reducer<T, A>, T>) {
        map[T::class] = Tuple3(pair.first as Reducer<Any, Any>, pair.second, A::class)
    }

    inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T) {
        add(Pair(this@with, state))
    }

    companion object {
        internal fun getInstance(map: MutableMap<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>) =
            StoreScope(map)
    }
}

/**
 * Create store
 *
 * @param fn
 * @return
 * @receiver
 */
fun createStore(fn: StoreScope.() -> Unit): Store {
    val map = mutableMapOf<KClass<*>, Tuple3<Reducer<Any, Any>, Any, KClass<*>>>()
    StoreScope.getInstance(map).fn()
    return map
}
