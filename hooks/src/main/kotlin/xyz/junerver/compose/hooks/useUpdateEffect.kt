package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable

/*
  Description: [useUpdateEffect] 用法等同于 [useEffect]，但是会忽略首次执行，只在依赖更新时执行。
  @author Junerver
  date: 2024/3/11-11:19
  Email: junerver@gmail.com
  Version: v1.0
*/
@SuppressLint("ComposableNaming")
@Composable
fun useUpdateEffect(vararg deps: Any?, block: SuspendAsyncFn) {
    val isMounted = useRef(false)
    useEffect(*deps) {
        if (!isMounted.current) {
            isMounted.current = true
        } else {
            block()
        }
    }
}
