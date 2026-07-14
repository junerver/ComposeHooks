/*
  Description: Platform controller hook behavior tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

@file:Suppress("DEPRECATION")

package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import xyz.junerver.compose.hooks.CopyPasteHolder
import xyz.junerver.compose.hooks.KeyboardHolder
import xyz.junerver.compose.hooks.rememberClipboard
import xyz.junerver.compose.hooks.rememberKeyboard
import xyz.junerver.compose.hooks.useClipboard
import xyz.junerver.compose.hooks.useKeyboard

class UsePlatformControllersTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clipboard_alias_copies_pastes_and_maps_null_to_empty_text() = runComposeUiTest {
        val manager = RecordingClipboardManager()
        lateinit var holder: CopyPasteHolder
        setContent {
            CompositionLocalProvider(LocalClipboardManager provides manager) {
                holder = rememberClipboard()
                Text("ready=true")
            }
        }

        waitForIdle()
        onNodeWithText("ready=true").assertExists()
        assertEquals("", holder.paste())

        holder.copy("copied")

        assertEquals("copied", manager.text)
        assertEquals("copied", holder.paste())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clipboard_uses_latest_manager_without_replacing_holder() = runComposeUiTest {
        val firstManager = RecordingClipboardManager("first")
        val secondManager = RecordingClipboardManager()
        val currentManager = mutableStateOf<ClipboardManager>(firstManager)
        lateinit var holder: CopyPasteHolder
        setContent {
            CompositionLocalProvider(LocalClipboardManager provides currentManager.value) {
                holder = useClipboard()
                Text("manager=${currentManager.value === secondManager}")
            }
        }

        waitForIdle()
        onNodeWithText("manager=false").assertExists()
        val stableHolder = holder
        val stableCopy = holder.copy
        val stablePaste = holder.paste
        assertEquals("first", stableHolder.paste())

        runOnIdle { currentManager.value = secondManager }
        waitForIdle()

        onNodeWithText("manager=true").assertExists()
        assertSame(stableHolder, holder)
        assertSame(stableCopy, holder.copy)
        assertSame(stablePaste, holder.paste)
        assertEquals("", stableHolder.paste())

        stableHolder.copy("second")

        assertEquals("first", firstManager.text)
        assertEquals("second", secondManager.text)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun keyboard_alias_delegates_show_and_hide() = runComposeUiTest {
        val controller = RecordingKeyboardController()
        lateinit var holder: KeyboardHolder
        setContent {
            CompositionLocalProvider(LocalSoftwareKeyboardController provides controller) {
                holder = rememberKeyboard()
                Text("ready=true")
            }
        }

        waitForIdle()
        holder.showKeyboard()
        holder.hideKeyboard()

        assertEquals(1, controller.showCalls)
        assertEquals(1, controller.hideCalls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun keyboard_uses_latest_nullable_controller_without_replacing_holder() = runComposeUiTest {
        val firstController = RecordingKeyboardController()
        val secondController = RecordingKeyboardController()
        val currentController = mutableStateOf<SoftwareKeyboardController?>(firstController)
        lateinit var holder: KeyboardHolder
        setContent {
            CompositionLocalProvider(LocalSoftwareKeyboardController provides currentController.value) {
                holder = useKeyboard()
                Text("controller=${currentController.value === secondController}")
            }
        }

        waitForIdle()
        val stableHolder = holder
        val stableShow = holder.showKeyboard
        val stableHide = holder.hideKeyboard
        stableHolder.showKeyboard()
        stableHolder.hideKeyboard()
        assertEquals(1, firstController.showCalls)
        assertEquals(1, firstController.hideCalls)

        runOnIdle { currentController.value = secondController }
        waitForIdle()

        onNodeWithText("controller=true").assertExists()
        assertSame(stableHolder, holder)
        assertSame(stableShow, holder.showKeyboard)
        assertSame(stableHide, holder.hideKeyboard)
        stableHolder.showKeyboard()
        stableHolder.hideKeyboard()
        assertEquals(1, firstController.showCalls)
        assertEquals(1, firstController.hideCalls)
        assertEquals(1, secondController.showCalls)
        assertEquals(1, secondController.hideCalls)

        runOnIdle { currentController.value = null }
        waitForIdle()

        assertSame(stableHolder, holder)
        stableHolder.showKeyboard()
        stableHolder.hideKeyboard()
        assertEquals(1, firstController.showCalls)
        assertEquals(1, firstController.hideCalls)
        assertEquals(1, secondController.showCalls)
        assertEquals(1, secondController.hideCalls)
    }

    private class RecordingClipboardManager(initialText: String? = null) : ClipboardManager {
        var text: String? = initialText
            private set

        override fun setText(annotatedString: AnnotatedString) {
            text = annotatedString.text
        }

        override fun getText(): AnnotatedString? = text?.let(::AnnotatedString)
    }

    private class RecordingKeyboardController : SoftwareKeyboardController {
        var showCalls = 0
            private set
        var hideCalls = 0
            private set

        override fun show() {
            showCalls++
        }

        override fun hide() {
            hideCalls++
        }
    }
}
