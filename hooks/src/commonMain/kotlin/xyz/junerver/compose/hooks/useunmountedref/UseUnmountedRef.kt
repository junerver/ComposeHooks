package xyz.junerver.compose.hooks.useunmountedref

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.useref.Ref
import xyz.junerver.compose.hooks.usemount.useMountImpl
import xyz.junerver.compose.hooks.useunmount.useUnmountImpl
import xyz.junerver.compose.hooks.useref.useRefImpl

/*
  Description: 获取当前组件是否已经卸载的 Hook
  A Hook can be used to get whether the component is unmounted.
  Author: Junerver
  Date: 2024/1/26-13:29
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useUnmountedRefImpl(): Ref<Boolean> {
    val unmountedRef = useRefImpl(default = false)
    useMountImpl {
        unmountedRef.current = false
    }
    useUnmountImpl {
        unmountedRef.current = true
    }
    return unmountedRef
}
