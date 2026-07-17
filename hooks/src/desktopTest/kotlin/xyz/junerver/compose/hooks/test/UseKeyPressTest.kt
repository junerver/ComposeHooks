package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.KeyPressDelegate
import xyz.junerver.compose.hooks.rememberKeyPress
import xyz.junerver.compose.hooks.useKeyPress

/*
  Description: Desktop keyboard hook behavior tests.
  Author: Junerver
  Date: 2026/7/16
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseKeyPressTest {
    @AfterTest
    fun cleanup() {
        KeyPressDelegate.keyPress.clear()
    }

    @Test
    fun delegateMatchesOnlyExactDistinctKeySets() {
        KeyPressDelegate.keyPress[Key.CtrlLeft] = true

        assertTrue(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft)))
        assertFalse(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft, Key.CtrlLeft)))
        assertFalse(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft, Key.S)))

        KeyPressDelegate.keyPress[Key.S] = true

        assertTrue(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft, Key.S)))
        assertFalse(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft)))

        KeyPressDelegate.keyPress[Key.AltLeft] = true

        assertFalse(KeyPressDelegate.areExactKeysPressed(listOf(Key.CtrlLeft, Key.S)))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun hookFiresOnlyForExactCombinationAndUsesLatestCallback() = runComposeUiTest {
        val label = mutableStateOf("first")
        val calls = mutableListOf<String>()

        setContent {
            useKeyPress(Key.CtrlLeft, Key.S) {
                calls += "save:${label.value}"
            }
            Text("ready")
        }

        waitForIdle()
        onNodeWithText("ready").assertExists()

        runOnIdle {
            KeyPressDelegate.keyPress[Key.CtrlLeft] = true
        }
        waitForIdle()
        assertEquals(emptyList(), calls)

        runOnIdle {
            KeyPressDelegate.keyPress[Key.S] = true
        }
        waitForIdle()
        assertEquals(listOf("save:first"), calls)

        runOnIdle {
            KeyPressDelegate.keyPress[Key.AltLeft] = true
            label.value = "latest"
        }
        waitForIdle()
        assertEquals(listOf("save:first"), calls)

        runOnIdle {
            KeyPressDelegate.keyPress.clear()
            KeyPressDelegate.keyPress[Key.CtrlLeft] = true
            KeyPressDelegate.keyPress[Key.S] = true
        }
        waitForIdle()
        assertEquals(listOf("save:first", "save:latest"), calls)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rememberAliasMatchesSingleAndFourKeyOverloads() = runComposeUiTest {
        val calls = mutableListOf<String>()

        setContent {
            rememberKeyPress(Key.Spacebar) {
                calls += "space"
            }
            rememberKeyPress(Key.CtrlLeft, Key.AltLeft, Key.ShiftLeft, Key.D) {
                calls += "combo"
            }
            Text("alias-ready")
        }

        waitForIdle()

        runOnIdle {
            KeyPressDelegate.keyPress[Key.Spacebar] = true
        }
        waitForIdle()
        assertEquals(listOf("space"), calls)

        runOnIdle {
            KeyPressDelegate.keyPress.clear()
            KeyPressDelegate.keyPress[Key.CtrlLeft] = true
            KeyPressDelegate.keyPress[Key.AltLeft] = true
            KeyPressDelegate.keyPress[Key.ShiftLeft] = true
            KeyPressDelegate.keyPress[Key.D] = true
        }
        waitForIdle()
        assertEquals(listOf("space", "combo"), calls)
    }
}
