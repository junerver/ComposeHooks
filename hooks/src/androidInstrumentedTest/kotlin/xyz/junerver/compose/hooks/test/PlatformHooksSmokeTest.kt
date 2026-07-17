package xyz.junerver.compose.hooks.test

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.junerver.compose.hooks.IlluminanceHolder
import xyz.junerver.compose.hooks.IlluminanceInfo
import xyz.junerver.compose.hooks.rememberBiometric
import xyz.junerver.compose.hooks.rememberIdle
import xyz.junerver.compose.hooks.rememberIlluminance
import xyz.junerver.compose.hooks.rememberSensor
import xyz.junerver.compose.hooks.useBiometric
import xyz.junerver.compose.hooks.useIlluminance
import xyz.junerver.compose.hooks.useSensor
import xyz.junerver.compose.hooks.useidle.IdleInfo
import xyz.junerver.compose.hooks.useidle.useIdle

/*
  Description: Android public platform hook smoke tests.
  Author: Junerver
  Date: 2026/7/17
  Email: junerver@gmail.com
  Version: v1.0
*/

@RunWith(AndroidJUnit4::class)
class PlatformHooksSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun biometricHookAndRememberAliasComposeWithoutUnsupportedOptions() {
        lateinit var hook: Pair<() -> Unit, State<Boolean>>
        lateinit var alias: Pair<() -> Unit, State<Boolean>>

        composeRule.setContent {
            hook = useBiometric {
                title = "hook"
                negativeButtonText = "cancel"
            }
            alias = rememberBiometric {
                title = "alias"
                negativeButtonText = "stop"
            }
            Text("biometric-ready")
        }

        composeRule.onNodeWithText("biometric-ready").assertExists()
        composeRule.runOnIdle {
            assertFalse(hook.second.value)
            assertFalse(alias.second.value)
        }
    }

    @Test
    fun idleSubscribersShareWindowAndRemainingAliasSurvivesPeerDispose() {
        val showFirst = mutableStateOf(true)
        lateinit var first: State<IdleInfo>
        lateinit var second: State<IdleInfo>

        composeRule.setContent {
            if (showFirst.value) {
                first = useIdle(5.seconds)
            }
            second = rememberIdle(5.seconds)
            Text("idle-ready")
        }

        composeRule.onNodeWithText("idle-ready").assertExists()
        composeRule.waitUntil(5_000) {
            first.value.lastActive != Instant.DISTANT_PAST && second.value.lastActive != Instant.DISTANT_PAST
        }
        val firstBefore = first.value.lastActive
        val secondBefore = second.value.lastActive

        composeRule.activity.runOnUiThread {
            composeRule.activity.window.callback.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A))
        }
        composeRule.waitUntil(5_000) {
            first.value.lastActive > firstBefore && second.value.lastActive > secondBefore
        }

        composeRule.runOnIdle {
            showFirst.value = false
        }
        composeRule.waitForIdle()
        val secondAfterPeerDispose = second.value.lastActive

        composeRule.activity.runOnUiThread {
            composeRule.activity.window.callback.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B))
        }
        composeRule.waitUntil(5_000) {
            second.value.lastActive > secondAfterPeerDispose
        }
    }

    @Test
    fun sensorAndIlluminanceHooksAndAliasesComposeSafely() {
        val calibration = mutableFloatStateOf(Float.NaN)
        lateinit var hook: IlluminanceHolder
        lateinit var alias: IlluminanceHolder

        composeRule.setContent {
            useSensor(sensorType = Int.MIN_VALUE)
            rememberSensor(sensorType = Int.MIN_VALUE)
            hook = useIlluminance(calibration.floatValue)
            alias = rememberIlluminance(Float.NaN)
            Text("sensor-ready")
        }

        composeRule.onNodeWithText("sensor-ready").assertExists()
        val initialHolder = hook
        composeRule.runOnIdle {
            assertTrue(hook.state.value == IlluminanceInfo())
            assertTrue(alias.state.value == IlluminanceInfo())
            calibration.floatValue = -1f
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertSame(initialHolder, hook)
            assertTrue(hook.state.value == IlluminanceInfo())
        }
    }
}
