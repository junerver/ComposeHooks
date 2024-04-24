package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Description: 获取当前组件是否已经卸载的 Hook
 *
 * A Hook can be used to get whether the component is unmounted.
 * @author Junerver
 * date: 2024/1/26-13:29
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@SuppressLint("ComposableNaming")
@Composable
fun useUnmountedRef(): Ref<Boolean> {
    val unmountedRef = useRef(default = false)
    DisposableEffect(Unit) {
        unmountedRef.current = false
        onDispose {
            unmountedRef.current = false
        }
    }
    return unmountedRef
}
