package xyz.junerver.compose.hooks.useredux

import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.kotlin.Tuple5

/**
 * tuple: reducer \ initialState \ state type \action type\ alias
 */
internal typealias Store = List<
    Tuple5<
        Reducer<Any, Any>, // reducer
        Any, // initialState
        KClass<*>, // state type
        KClass<*>, // action type
        String // alias
        >
    >

class StoreScope private constructor(val list: MutableList<Tuple5<Reducer<Any, Any>, Any, KClass<*>, KClass<*>, String>>) {
    inline fun <reified T : Any, reified A : Any> add(
        pair: Pair<Reducer<T, A>, T>,
        alias: String? = null,
    ) {
        list.add(
            Tuple5(
                pair.first as Reducer<Any, Any>,
                pair.second,
                T::class,
                A::class,
                alias ?: T::class.qualifiedName ?: "unknown" // default alias
            )
        )
    }

    inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T) {
        add(Pair(this@with, state))
    }

    object NamedScope {
        inline infix fun <reified T : Any, reified A : Any> Reducer<T, A>.with(state: T): Pair<Reducer<T, A>, T> {
            return Pair(this@with, state)
        }
    }

    inline fun <reified T : Any, reified A : Any> named(
        alias: String,
        fn: NamedScope.() -> Pair<Reducer<T, A>, T>,
    ) {
        add(NamedScope.fn(), alias)
    }

    companion object {
        internal fun getInstance(map: MutableList<Tuple5<Reducer<Any, Any>, Any, KClass<*>, KClass<*>, String>>) =
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
    val list = mutableListOf<Tuple5<Reducer<Any, Any>, Any, KClass<*>, KClass<*>, String>>()
    StoreScope.getInstance(list).fn()
    return list
}

fun registerErr(): Nothing {
    error("Please confirm that you have correctly registered in `createStore`!")
}
