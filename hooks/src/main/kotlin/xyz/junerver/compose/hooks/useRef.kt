package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.properties.Delegates

/**
 * Description: [useRef]可以方便的创建一个不受重组影响的对象引用。另外，它不同于[rememberUpdatedState]
 * ，修改[Ref.current]不会引发组件重组，它可以很好的用于保存现场，或者不需要引发重组的幕后变量。
 * @author Junerver
 * date: 2024/2/7-12:03
 * Email: junerver@gmail.com
 * Version: v1.0
 */

private typealias Observer<T> = (T) -> Unit

/**
 * Mutable ref
 *
 * @param T
 * @constructor
 *
 * @param initialValue
 */
@Stable
class MutableRef<T>(initialValue: T) : Ref<T> {
    override var current: T by Delegates.observable(initialValue) { _, _, newValue ->
        mObservers.takeIf { it.isEmpty() } ?: notify(newValue)
    }

    private val mObservers = mutableListOf<Observer<T>>()

    override fun observe(observer: Observer<T>) {
        observer.invoke(current)
        mObservers.add(observer)
    }

    override fun removeObserver(observer: Observer<T>) {
        mObservers.remove(observer)
    }

    private fun notify(newValue: T) {
        mObservers.forEach {
            it.invoke(newValue)
        }
    }
}

/**
 * Read-only Ref interface
 *
 * @param T
 */
@Stable
sealed interface Ref<T> {
    val current: T
    fun observe(observer: Observer<T>) {}
    fun removeObserver(observer: Observer<T>) {}
}

@Composable
fun <T> useRef(default: T): MutableRef<T> = remember {
    MutableRef(default)
}

/**
 * Observe Ref as State
 *
 * @param T
 * @return
 */
@Composable
fun <T> Ref<T>.observeAsState(): State<T> {
    val state = _useState(default = this.current)
    DisposableEffect(Unit) {
        val observer = { it: T -> state.value = it }
        observe(observer)
        onDispose {
            removeObserver(observer)
        }
    }
    return state
}
