package xyz.junerver.compose.hooks

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/*
  Description:
  Author: Junerver
  Date: 2024/7/3-11:36
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useWindowFlags(key: String, flags: Int): Triple<AddFlagsFn, ClearFlagsFn, IsFlagsAdded> {
    val window = (LocalContext.current as Activity).window
    val isFlagSet = (window.attributes.flags and flags) != 0
    val (isAdded, setIsAdded) = usePersistent(
        key = key,
        defaultValue = isFlagSet,
        forceUseMemory = true
    )

    fun addFlags() {
        window.addFlags(flags)
        setIsAdded(true)
    }

    fun clearFlags() {
        window.clearFlags(flags)
        setIsAdded(false)
    }

    return remember {
        Triple(
            ::addFlags,
            ::clearFlags,
            isAdded
        )
    }
}
