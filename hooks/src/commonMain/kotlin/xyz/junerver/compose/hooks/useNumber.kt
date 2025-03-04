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
  Author: Junerver
  Date: 2024/3/7-15:31
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * A hook for creating a mutable double state.
 *
 * This hook provides a way to create a [MutableDoubleState] with better performance
 * than using a generic state. It's optimized specifically for double values.
 *
 * @param default The initial value of the state
 * @return A [MutableDoubleState] containing the double value
 *
 * @example
 * ```kotlin
 * val doubleValue = useDouble(3.14)
 * 
 * // Update the value
 * doubleValue.doubleValue = 2.718
 * 
 * // Read the value
 * val currentValue = doubleValue.doubleValue
 * ```
 */
@Composable
fun useDouble(default: Double = 0.0): MutableDoubleState = remember {
    mutableDoubleStateOf(default)
}

/**
 * A hook for creating a mutable float state.
 *
 * This hook provides a way to create a [MutableFloatState] with better performance
 * than using a generic state. It's optimized specifically for float values.
 *
 * @param default The initial value of the state
 * @return A [MutableFloatState] containing the float value
 *
 * @example
 * ```kotlin
 * val floatValue = useFloat(3.14f)
 * 
 * // Update the value
 * floatValue.floatValue = 2.718f
 * 
 * // Read the value
 * val currentValue = floatValue.floatValue
 * ```
 */
@Composable
fun useFloat(default: Float = 0f): MutableFloatState = remember {
    mutableFloatStateOf(default)
}

/**
 * A hook for creating a mutable integer state.
 *
 * This hook provides a way to create a [MutableIntState] with better performance
 * than using a generic state. It's optimized specifically for integer values.
 *
 * @param default The initial value of the state
 * @return A [MutableIntState] containing the integer value
 *
 * @example
 * ```kotlin
 * val intValue = useInt(42)
 * 
 * // Update the value
 * intValue.intValue = 100
 * 
 * // Read the value
 * val currentValue = intValue.intValue
 * ```
 */
@Composable
fun useInt(default: Int = 0): MutableIntState = remember {
    mutableIntStateOf(default)
}

/**
 * A hook for creating a mutable long state.
 *
 * This hook provides a way to create a [MutableLongState] with better performance
 * than using a generic state. It's optimized specifically for long values.
 *
 * @param default The initial value of the state
 * @return A [MutableLongState] containing the long value
 *
 * @example
 * ```kotlin
 * val longValue = useLong(42L)
 * 
 * // Update the value
 * longValue.longValue = 100L
 * 
 * // Read the value
 * val currentValue = longValue.longValue
 * ```
 */
@Composable
fun useLong(default: Long = 0L): MutableLongState = remember {
    mutableLongStateOf(default)
}
