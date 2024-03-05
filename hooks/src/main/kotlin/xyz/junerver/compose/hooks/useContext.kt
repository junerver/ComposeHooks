package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Description: 这段代码完全借鉴自[pavi2410/useCompose](https://github.com/pavi2410/useCompose/blob/main/app/src/main/java/me/pavi2410/useCompose/demo/screens/ContextExample.kt)
 * @author pavi2410
 * date: 2024/3/5-9:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */

@Suppress("PropertyName")
interface ReactContext<T> {
    val LocalCtx: ProvidableCompositionLocal<T>

    @Composable
    fun Provider(value: T, content: @Composable () -> Unit)
}

/**
 * function to create an context object.
 */
fun <T> createContext(initialValue: T): ReactContext<T> {
    return object : ReactContext<T> {
        override val LocalCtx = compositionLocalOf { initialValue }

        @Composable
        override fun Provider(value: T, content: @Composable () -> Unit) {
            CompositionLocalProvider(
                LocalCtx provides value,
                content = content
            )
        }
    }
}

/**
 * A React-ish hook that returns the current value for that context.
 *
 * @see [useContext](https://reactjs.org/docs/hooks-reference.html#usecontext)
 */
@SuppressLint("ComposableNaming")
@Composable
fun <T> useContext(context: ReactContext<T>): T {
    return context.LocalCtx.current
}
