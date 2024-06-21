package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

/*
  Description: Some performance improvements can be achieved by using corresponding number State
  @author Junerver
  date: 2024/3/7-15:31
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useDouble(default: Double = 0.0): MutableDoubleState = remember {
    mutableDoubleStateOf(default)
}

@Composable
fun useFloat(default: Float = 0f): MutableFloatState = remember {
    mutableFloatStateOf(default)
}

@Composable
fun useInt(default: Int = 0): MutableIntState = remember {
    mutableIntStateOf(default)
}

@Composable
fun useLong(default: Long = 0L): MutableLongState = remember {
    mutableLongStateOf(default)
}
