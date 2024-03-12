package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

/**
 * Description: 使用相应的数字可以提升些许性能
 * @author Junerver
 * date: 2024/3/7-15:31
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun useDouble(default: Double = 0.0) = remember {
    mutableDoubleStateOf(default)
}

@Composable
fun useFloat(default: Float = 0f) = remember {
    mutableFloatStateOf(default)
}

@Composable
fun useInt(default: Int = 0) = remember {
    mutableIntStateOf(default)
}

@Composable
fun useLong(default: Long = 0L) = remember {
    mutableLongStateOf(default)
}
