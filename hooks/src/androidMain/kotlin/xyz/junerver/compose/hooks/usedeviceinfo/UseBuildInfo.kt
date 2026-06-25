package xyz.junerver.compose.hooks.usedeviceinfo

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:46
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useBuildInfo(): BuildInfo = remember { BuildInfo(Build.BRAND, Build.MODEL, Build.VERSION.RELEASE) }

@Stable
data class BuildInfo(val brand: String, val model: String, val release: String)
