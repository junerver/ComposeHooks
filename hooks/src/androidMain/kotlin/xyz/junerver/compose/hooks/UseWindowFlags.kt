package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.activity.compose.LocalActivity

/*
  Description:
  Author: Junerver
  Date: 2024/7/3-11:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useWindowFlags(key: String, flags: Int): Triple<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> {
    val window = LocalActivity.current?.window
    val isFlagSet = window?.let { (it.attributes.flags and flags) != 0 } ?: false
    val (isAdded, setIsAdded) = usePersistent(
        key = key,
        defaultValue = isFlagSet,
        forceUseMemory = true,
    )

    fun addFlags() {
        window?.addFlags(flags)
        setIsAdded(true)
    }

    fun clearFlags() {
        window?.clearFlags(flags)
        setIsAdded(false)
    }

    return remember {
        Triple(
            ::addFlags,
            ::clearFlags,
            isAdded,
        )
    }
}
