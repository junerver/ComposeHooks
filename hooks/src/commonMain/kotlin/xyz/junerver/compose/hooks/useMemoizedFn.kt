package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import arrow.core.MemoizedDeepRecursiveFunction

/*
  Description:
  Author: Junerver
  Date: 2025/7/18-11:31
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Defines a recursive **pure** function that:
 * - keeps its stack on the heap, which allows very deep recursive computations that do not use the actual call stack;
 * - memoizes every call, which means that the function is execute only once per argument.
 *
 * [MemoizedDeepRecursiveFunction] takes one parameter of type [T] and returns a result of type [R].
 * The [block] of code defines the body of a recursive function. In this block
 * [callRecursive][DeepRecursiveScope.callRecursive] function can be used to make a recursive call
 * to the declared function.
 */
@Composable
fun <T, R> useMemoizedFn(fn: suspend DeepRecursiveScope<T, R>.(T) -> R): DeepRecursiveFunction<T, R> = useCreation {
    MemoizedDeepRecursiveFunction(block = fn)
}.current
