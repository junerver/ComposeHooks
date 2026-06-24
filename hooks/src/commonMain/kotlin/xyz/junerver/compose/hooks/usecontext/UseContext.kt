package xyz.junerver.compose.hooks.usecontext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import xyz.junerver.compose.hooks.ReactContext

/*
  Description: 这段代码完全借鉴自[pavi2410/useCompose](https://github.com/pavi2410/useCompose/blob/main/app/src/main/java/me/pavi2410/useCompose/demo/screens/ContextExample.kt)
  Author: pavi2410
  Date: 2024/3/5-9:44
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A React-ish hook that returns the current value for that context.
 *
 * @see [useContext](https://reactjs.org/docs/hooks-reference.html#usecontext)
 */
@ReadOnlyComposable
@Composable
fun <T> useContextImpl(context: ReactContext<T>): T = context.LocalCtx.current
