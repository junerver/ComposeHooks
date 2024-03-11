package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUpdateEffect
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-11:09
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun UseUpdateEffectExample() {
    val ctx = LocalContext.current

    val (state, setState) = useState(0)
    useUpdateEffect(state) {
        // A hook alike useEffect but skips running the effect for the first time.
        ctx.toast("useUpdateEffect deps change")
    }
    Surface {
        Column {
            TButton(text = "+1") {
                setState(state + 1)
            }
        }
    }
}
