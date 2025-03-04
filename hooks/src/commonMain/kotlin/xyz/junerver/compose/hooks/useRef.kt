package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/*
  Description: [useRef]可以方便的创建一个不受重组影响的对象引用。另外，它不同于[rememberUpdatedState]
  ，修改[Ref.current]不会引发组件重组，它可以很好的用于保存现场，或者不需要引发重组的幕后变量。
  Author: Junerver
  Date: 2024/2/7-12:03
  Email: junerver@gmail.com
  Version: v1.0
*/

private typealias Observer<T> = (T) -> Unit

/**
 * A read-only reference interface that provides access to a value without triggering recomposition.
 *
 * Unlike [rememberUpdatedState], modifying the value through this interface does not cause
 * component recomposition. It's useful for storing values that shouldn't trigger UI updates.
 *
 * @param T The type of the referenced value
 */
@Stable
sealed interface Ref<T> {
    /** The current value of the reference */
    val current: T

    /**
     * Registers an observer to be notified of value changes.
     * By default, no observers are registered.
     *
     * @param observer The callback to be invoked when the value changes
     */
    fun observe(observer: Observer<T>) {}

    /**
     * Removes a previously registered observer.
     * By default, no observers are removed.
     *
     * @param observer The observer to remove
     */
    fun removeObserver(observer: Observer<T>) {}
}

/**
 * Property delegate operator for [Ref] to access the current value.
 *
 * @param thisObj The object on which the property is being accessed
 * @param property The property being accessed
 * @return The current value of the reference
 */
operator fun <T> Ref<T>.getValue(thisObj: Any?, property: KProperty<*>): T = current

/**
 * A mutable reference that supports value changes and observer notifications.
 *
 * This class provides a way to store and update values without triggering recomposition,
 * while still allowing components to observe changes when needed.
 *
 * @param T The type of the referenced value
 * @param initialValue The initial value of the reference
 *
 * @example
 * ```kotlin
 * val ref = useRef(0)
 * 
 * // Update value without triggering recomposition
 * ref.current = 42
 * 
 * // Use property delegation
 * var value by ref
 * value = 100
 * 
 * // Observe changes
 * ref.observe { newValue ->
 *     println("Value changed to: $newValue")
 * }
 * ```
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
 * Property delegate operator for [MutableRef] to set the current value.
 *
 * @param thisObj The object on which the property is being set
 * @param property The property being set
 * @param value The new value to set
 */
operator fun <T> MutableRef<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T) {
    this.current = value
}

/**
 * A hook for creating a mutable reference that persists across recompositions.
 *
 * This hook creates a [MutableRef] that maintains its value even when the component
 * recomposes. It's useful for storing values that shouldn't trigger UI updates but
 * need to persist between recompositions.
 *
 * @param default The initial value of the reference
 * @return A [MutableRef] containing the value
 *
 * @example
 * ```kotlin
 * // Create a reference
 * val counterRef = useRef(0)
 * 
 * // Update without triggering recomposition
 * counterRef.current++
 * 
 * // Use in callbacks
 * Button(onClick = { counterRef.current++ }) {
 *     Text("Increment")
 * }
 * ```
 */
@Composable
fun <T> useRef(default: T): MutableRef<T> = remember {
    MutableRef(default)
}

/**
 * Converts a [Ref] into a [State] that can be observed in the UI.
 *
 * This function creates a [State] that updates whenever the reference value changes,
 * allowing the UI to react to changes in the reference value.
 *
 * @return A [State] containing the current value of the reference
 *
 * @example
 * ```kotlin
 * val ref = useRef(0)
 * val state = ref.observeAsState()
 * 
 * // UI will update when ref.current changes
 * Text("Value: ${state.value}")
 * ```
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
