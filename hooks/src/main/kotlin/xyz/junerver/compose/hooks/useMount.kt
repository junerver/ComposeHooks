package xyz.junerver.compose.hooks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable

/**
 * Description:组件挂载时执行
 * @author Junerver
 * date: 2024/1/25-8:26
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@SuppressLint("ComposableNaming")
@Composable
fun useMount(fn: () -> Unit) = useEffect(Unit) { fn() }
