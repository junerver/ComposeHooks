package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages
import xyz.junerver.compose.hooks.usetimeago.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.usetimeago.FormatTimeAgoOptions
import xyz.junerver.compose.hooks.usetimeago.UseTimeAgoOptions

class UseTimeAgoMessagesTest {
    @Test
    fun default_english_messages_expose_expected_phrases() {
        val msgs = DefaultEnglishTimeAgoMessages

        assertEquals("just now", msgs.justNow)
        assertEquals("invalid date", msgs.invalid)
        assertEquals("1 second", msgs.second(1))
        assertEquals("2 minutes", msgs.minute(2))
        assertEquals("5 minutes ago", msgs.past("5 minutes"))
        assertEquals("in 5 minutes", msgs.future("5 minutes"))
    }

    @Test
    fun default_chinese_messages_expose_expected_phrases() {
        val msgs = DefaultChineseTimeAgoMessages

        assertEquals("刚刚", msgs.justNow)
        assertEquals("无效日期", msgs.invalid)
        assertEquals("1秒", msgs.second(1))
        assertEquals("2分钟", msgs.minute(2))
        assertEquals("5分钟前", msgs.past("5分钟"))
        assertEquals("5分钟后", msgs.future("5分钟"))
    }

    @Test
    fun use_time_ago_options_have_documented_defaults() {
        val options = UseTimeAgoOptions.optionOf {}

        assertEquals(30.seconds, options.updateInterval)
        assertNull(options.max)
        assertNull(options.fullDateFormatter)
        assertEquals(false, options.showSecond)
        assertEquals("round", options.rounding)
        assertNull(options.units)
    }

    @Test
    fun format_time_ago_options_have_documented_defaults() {
        val options = FormatTimeAgoOptions.optionOf {}

        assertNull(options.max)
        assertNull(options.fullDateFormatter)
        assertEquals(false, options.showSecond)
        assertEquals("round", options.rounding)
        assertNull(options.units)
    }
}
