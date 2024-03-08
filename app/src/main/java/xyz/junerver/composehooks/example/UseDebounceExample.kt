package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.useDebounce
import xyz.junerver.compose.hooks.useDebounceFn
import xyz.junerver.compose.hooks.useLatestRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/8-14:13
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseDebounceExample() {
    val (state, setState) = useState(0)
    val latest = useLatestRef(value = state)
    fun simpleFn() {
        /**
         * Because [useDebounceFn] needs to pass in a function reference,
         * it will cause closure problems and needs to be avoided by using [useLatestRef];
         */
        setState(latest.current + 1)
    }

    val debouncedState = useDebounce(value = state)
    val debouncedFn = useDebounceFn(fn = { simpleFn() })
    Surface {
        Column {
            Text(text = "current: $state")
            Text(text = "debounced: $debouncedState")

            TButton(text = "+1") {
                simpleFn()
            }
            TButton(text = "debounced +1") {
                /**
                 *  Manual import：`import xyz.junerver.compose.hooks.invoke`
                 */
                debouncedFn()
            }
        }
    }
}
