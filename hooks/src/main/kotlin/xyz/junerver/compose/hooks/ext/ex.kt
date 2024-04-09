package xyz.junerver.compose.hooks.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Description: DP <=> Pixels
 * @author Junerver
 * date: 2024/3/28-9:14
 * Email: junerver@gmail.com
 * Version: v1.0
 */
inline val Dp.px: Float
    @Composable get() = with(LocalDensity.current) { this@px.toPx() }

inline val Int.px2dp: Dp
    @Composable get() = with(LocalDensity.current) { this@px2dp.toDp() }
