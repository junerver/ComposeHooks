package xyz.junerver.compose.hooks.test

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SmallHooksTest {
    @Test
    fun placeholder_small_hooks_file_remains_for_future_split() = runComposeUiTest {
        waitForIdle()
        assertTrue(true)
    }
}
