package xyz.junerver.composehooks.ui.component

import androidx.compose.runtime.Composable

@Composable
fun SimpleContainer(content: @Composable () -> Unit) {
    content.invoke()
}
