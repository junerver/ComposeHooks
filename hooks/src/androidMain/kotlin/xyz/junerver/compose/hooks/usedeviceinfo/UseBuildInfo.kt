package xyz.junerver.compose.hooks.usedeviceinfo

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import xyz.junerver.compose.hooks.useCreation

/*
  Description:
  Author: Junerver
  Date: 2024/7/1-9:46
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun useBuildInfo(): BuildInfo = useCreation {
    createBuildInfo(Build.BRAND, Build.MODEL, Build.VERSION.RELEASE)
}.current

internal fun createBuildInfo(brand: String?, model: String?, release: String?): BuildInfo = BuildInfo(
    brand = brand.orEmpty(),
    model = model.orEmpty(),
    release = release.orEmpty(),
)

@Stable
data class BuildInfo(val brand: String, val model: String, val release: String)
