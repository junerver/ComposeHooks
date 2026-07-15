package xyz.junerver.compose.hooks.useprevious

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import xyz.junerver.compose.hooks.usecreation.useCreationImpl
import xyz.junerver.compose.hooks.usestate._useStateImpl
import xyz.junerver.compose.hooks.useeffect.useEffectImpl
import xyz.junerver.compose.hooks.useref.useRefImpl
import xyz.junerver.compose.hooks.utils.unwrap

/*
  Description: Hook that saves the previous state.
  Author: Junerver
  Date: 2024/2/1-14:55
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun <T> usePreviousImpl(present: T): State<T?> {
    val dependencyKey = useCreationImpl(*unwrap(arrayOf<Any?>(present))) { Any() }.current
    val initialDependencyKey = useCreationImpl { dependencyKey }.current
    val latestComposedValue = useRefImpl(present)
    val previousForDependency = useCreationImpl(dependencyKey) { latestComposedValue.current }.current
    latestComposedValue.current = present
    val previousState = _useStateImpl<T?>(default = null)
    useEffectImpl(dependencyKey) {
        previousState.value = if (dependencyKey === initialDependencyKey) null else previousForDependency
    }
    return previousState
}
