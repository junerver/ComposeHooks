package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import xyz.junerver.compose.hooks.rememberDateFormat
import xyz.junerver.compose.hooks.useDateFormat

/*
  Description: useDateFormat composable behavior tests
  Author: Junerver
  Date: 2026/7/15
  Email: junerver@gmail.com
  Version: v1.0
*/

@Suppress("DEPRECATION")
class UseDateFormatHookTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun all_public_overloads_format_without_recursion() = runComposeUiTest {
        val instant = Instant.parse("2025-01-01T00:00:00Z")
        val millis = instant.toEpochMilliseconds()

        setContent {
            val fromInstant = useDateFormat(instant, "YYYY-MM-DD HH") { timeZone = TimeZone.UTC }
            val fromLocal = useDateFormat(LocalDateTime(2025, 1, 2, 3, 4), "YYYY-MM-DD HH:mm")
            val fromString = rememberDateFormat("2025-01-03", "YYYY-MM-DD HH:mm:ss")
            val fromMillis = rememberDateFormat(millis, "YYYY-MM-DD HH") { timeZone = TimeZone.UTC }
            Text("${fromInstant.value}|${fromLocal.value}|${fromString.value}|${fromMillis.value}")
        }

        waitForIdle()
        onNodeWithText("2025-01-01 00|2025-01-02 03:04|2025-01-03 00:00:00|2025-01-01 00").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun options_update_reactively_without_replacing_state_identity() = runComposeUiTest {
        val useChinese = mutableStateOf(false)
        val useNewYork = mutableStateOf(false)
        val instant = Instant.parse("2025-01-01T00:00:00Z")
        lateinit var state: androidx.compose.runtime.State<String>

        setContent {
            state = useDateFormat(instant, "MMMM DD HH A z") {
                locale = if (useChinese.value) "zh-CN" else "en-US"
                timeZone = if (useNewYork.value) TimeZone.of("America/New_York") else TimeZone.UTC
                customMeridiem = if (useChinese.value) {
                    { hours, _, _, _ -> if (hours < 12) "上午" else "下午" }
                } else {
                    null
                }
            }
            Text(state.value)
        }

        waitForIdle()
        val initialState = state
        onNodeWithText("January 01 00 AM GMT+0").assertExists()

        runOnIdle {
            useChinese.value = true
            useNewYork.value = true
        }
        waitForIdle()

        assertSame(initialState, state)
        onNodeWithText("十二月 31 19 下午 GMT-5").assertExists()
    }
}
