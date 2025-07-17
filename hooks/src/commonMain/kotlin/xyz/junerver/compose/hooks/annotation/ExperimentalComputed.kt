package xyz.junerver.compose.hooks.annotation

/*
  Description:
  Author: Junerver
  Date: 2025/7/17-17:04
  Email: junerver@gmail.com
  Version: v1.0
*/
@RequiresOptIn("The function is in the experimental stage and its performance may not match expectations", RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalComputed
