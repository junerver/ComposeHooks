package xyz.junerver.compose.hooks.usepersistent
import xyz.junerver.compose.hooks.HookClear
import xyz.junerver.compose.hooks.PersistentContextValue
import xyz.junerver.compose.hooks.PersistentGet
import xyz.junerver.compose.hooks.SaveToPersistent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty
import xyz.junerver.compose.hooks.internal.persistentKey
import xyz.junerver.compose.hooks.usecontext.createContextImpl
import xyz.junerver.compose.hooks.usecontext.useContextImpl
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.uselatest.useLatestRefImpl
import xyz.junerver.compose.hooks.useref.useRefImpl
import xyz.junerver.compose.hooks.usestate._useStateImpl
import xyz.junerver.compose.hooks.utils.CacheManager
import xyz.junerver.compose.hooks.utils.HooksEventManager

/*
  Description: A persistence hook that is easy to extend. It uses memory for
  persistence by default. You can pass in the corresponding function yourself
  to customize the persistence solution.

  Author: Junerver
  Date: 2024/4/9-16:59
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A holder class for persistent state management.
 *
 * This class provides a way to manage persistent state with a tuple-like interface
 * [state, setState]. It implements property delegation through [getValue] and [setValue]
 * operators, allowing for clean property access syntax.
 *
 * @param state The current state value
 * @param save Function to save the state to persistent storage
 * @param clear Function to clear the state from persistent storage
 *
 * @example
 * ```kotlin
 * val persistentState = usePersistent("myKey", defaultValue)
 *
 * // Using property delegation
 * var value by persistentState
 *
 * // Direct access
 * persistentState.state.value
 * persistentState.save(newValue)
 * persistentState.clear()
 * ```
 */
@Stable
data class PersistentHolder<T>(
    val state: State<T>,
    val save: SaveToPersistent<T>,
    val clear: HookClear,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        save(newValue)
    }
}

/**
 * Default persistent context using memory storage.
 *
 * This context provides default persistence implementation using [CacheManager.cache].
 * It's a lightweight encapsulation that requires you to provide your own persistence
 * solution globally through `PersistentContext.Provider`.
 */
val PersistentContext by lazy {
    createContextImpl<PersistentContextValue>(
        Triple(
            ::memoryGetPersistent,
            ::memorySavePersistent,
            ::memoryClearPersistent,
        ),
    )
}

/**
 * Internal memory persistent context for forced memory storage.
 *
 * This context is used when memory storage is explicitly requested through
 * the `forceUseMemory` parameter in [usePersistent].
 */
internal val InternalMemoryPersistentContext by lazy {
    createContextImpl<PersistentContextValue>(
        Triple(
            ::memoryGetPersistent,
            ::memorySavePersistent,
            ::memoryClearPersistent,
        ),
    )
}

/**
 * A hook for managing persistent state.
 *
 * This hook provides a way to create and manage state that persists across
 * component recompositions and app restarts. It supports both memory-based
 * and custom persistence solutions.
 *
 * @param key The unique key for the persistent state
 * @param defaultValue The default value if no persisted value exists
 * @param forceUseMemory Whether to force using memory storage instead of the custom persistence solution
 * @return A [PersistentHolder] containing the state and persistence operations
 *
 * @example
 * ```kotlin
 * // Basic usage with default persistence
 * val persistentState = usePersistent("userSettings", defaultSettings)
 *
 * // Force using memory storage
 * val memoryState = usePersistent("tempData", initialData, forceUseMemory = true)
 *
 * // Using property delegation
 * var settings by persistentState
 * settings = newSettings // Automatically persists
 * ```
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T> usePersistentImpl(key: String, defaultValue: T, forceUseMemory: Boolean = false): PersistentHolder<T> {
    val (get, set, clear) = useContextImpl(context = if (forceUseMemory) InternalMemoryPersistentContext else PersistentContext)
    val latestKey = useLatestRefImpl(key)
    val latestDefaultValue = useLatestRefImpl(defaultValue)
    val latestGet = useLatestRefImpl(get)
    val latestSet = useLatestRefImpl(set)
    val latestClear = useLatestRefImpl(clear)
    val getValue = {
        getPersistentValueOrDefault(
            get = latestGet.current,
            key = latestKey.current,
            defaultValue = latestDefaultValue.current,
        )
    }
    val initialValue = useCreationImpl(factory = getValue).current
    val state = _useStateImpl(initialValue)
    val loadedConfiguration = useRefImpl(Triple(key, defaultValue, get))

    useEffectImpl(key, defaultValue, get) {
        val currentConfiguration = Triple(key, defaultValue, get)
        if (loadedConfiguration.current != currentConfiguration) {
            loadedConfiguration.current = currentConfiguration
            state.value = getValue()
        }
    }

    /**
     * Register an observer callback for each component that uses this state,
     * and notify the update component when this storage changes;
     */
    DisposableEffect(key) {
        val unObserver = memoryAddObserver(key) {
            state.value = getValue()
        }
        onDispose(unObserver)
    }
    return remember {
        PersistentHolder(
            state = state,
            save = { value -> latestSet.current(latestKey.current, value) },
            clear = { latestClear.current(latestKey.current) },
        )
    }
}

private object NullPersistentDefault

@Suppress("UNCHECKED_CAST")
private fun <T> getPersistentValueOrDefault(get: PersistentGet, key: String, defaultValue: T): T =
    try {
        val backendDefault = defaultValue ?: NullPersistentDefault
        val storedValue = get(key, backendDefault)
        when {
            storedValue === NullPersistentDefault -> defaultValue
            defaultValue == null -> storedValue as T
            (defaultValue as Any)::class.isInstance(storedValue) -> storedValue as T
            else -> defaultValue
        }
    } catch (_: Exception) {
        defaultValue
    }

/** Callback function when performing persistence operation */
private typealias SavePersistentCallback = (Unit) -> Unit

/**
 * Notifies all observers of a persistent state change.
 *
 * This function should be called in your [PersistentSave] implementation to notify
 * all components using the persistent state that the value has changed.
 *
 * @param key The key of the persistent state that changed
 */
fun notifyDefaultPersistentObserver(key: String) {
    HooksEventManager.post(key.persistentKey, Unit)
}

/**
 * Saves a value to memory storage and notifies observers.
 *
 * @param key The key to save the value under
 * @param value The value to save
 */
private fun memorySavePersistent(key: String, value: Any?) {
    CacheManager.saveCache(key.persistentKey, value)
    notifyDefaultPersistentObserver(key)
}

/**
 * Retrieves a value from memory storage.
 *
 * @param key The key to retrieve the value for
 * @param defaultValue The default value if no value exists
 * @return The stored value or the default value
 */
private fun memoryGetPersistent(key: String, defaultValue: Any): Any = CacheManager.getCache(key.persistentKey, defaultValue)

/**
 * Clears a value from memory storage and notifies observers.
 *
 * @param key The key to clear
 */
private fun memoryClearPersistent(key: String) {
    CacheManager.clearCache(key.persistentKey)
    notifyDefaultPersistentObserver(key)
}

/**
 * Registers an observer for persistent state changes.
 *
 * @param key The key to observe
 * @param observer The callback to invoke when the value changes
 * @return A function to unregister the observer
 */
private fun memoryAddObserver(key: String, observer: SavePersistentCallback): () -> Unit =
    HooksEventManager.register(key.persistentKey, observer)
