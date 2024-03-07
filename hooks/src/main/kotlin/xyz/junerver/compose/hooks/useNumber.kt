package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import xyz.junerver.kotlin.Tuple2

/**
 * Description: 使用相应的数字可以提升些许性能
 * @author Junerver
 * date: 2024/3/7-15:31
 * Email: junerver@gmail.com
 * Version: v1.0
 */
@Composable
fun useDouble(default: Double = 0.0): Tuple2<Double, (Double) -> Unit> {
    val (state, setState) = remember {
        mutableDoubleStateOf(default)
    }
    return state to setState
}

@Composable
fun useFloat(default: Float = 0f): Tuple2<Float, (Float) -> Unit> {
    val (state, setState) = remember {
        mutableFloatStateOf(default)
    }
    return state to setState
}

@Composable
fun useInt(default: Int = 0): Tuple2<Int, (Int) -> Unit> {
    val (state, setState) = remember {
        mutableIntStateOf(default)
    }
    return state to setState
}

@Composable
fun useLong(default: Long = 0L): Tuple2<Long, (Long) -> Unit> {
    val (state, setState) = remember {
        mutableLongStateOf(default)
    }
    return state to setState
}
