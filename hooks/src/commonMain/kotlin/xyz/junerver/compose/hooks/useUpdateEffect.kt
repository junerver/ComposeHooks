package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable

/*
  Description: [useUpdateEffect] 用法等同于 [useEffect]，但是会忽略首次执行，只在依赖更新时执行。
  Author: Junerver
  Date: 2024/3/11-11:19
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useUpdateEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    var isMounted by useRef(false)
    useEffect(*deps) {
        if (!isMounted) {
            isMounted = true
        } else {
            block()
        }
    }
}
