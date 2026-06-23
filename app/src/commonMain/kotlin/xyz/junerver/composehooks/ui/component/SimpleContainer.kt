package xyz.junerver.composehooks.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SimpleContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        content.invoke()
    }
}

@Preview
@Composable
private fun SimpleContainerPreview() {
    SimpleContainer {
        TButton(text = "Refresh") {}
    }
}
