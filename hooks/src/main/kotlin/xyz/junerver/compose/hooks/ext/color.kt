package xyz.junerver.compose.hooks.ext

import androidx.compose.ui.graphics.Color
import xyz.junerver.kotlin.toColor

/**
 * Description:
 *
 * @author Junerver date: 2024/3/22-11:40 Email: junerver@gmail.com
 *     Version: v1.0
 */
fun String.toColor(): Color = Color(this.toColor())
