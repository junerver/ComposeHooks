package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.kotlin.Tuple4
import xyz.junerver.kotlin.tuple

/*
  Description:
  @author Junerver
  date: 2024/3/14-10:18
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Description: [useTimestamp]和[useTimestampRef]的配置项
 *
 * @param interval the interval between timestamp changes
 * @param offset timestamp offset
 * @param callback callback
 */
data class TimestampOptions internal constructor(
    var interval: Duration = 1.0.milliseconds,
    var offset: Long = 0,
    var callback: ((Long) -> Unit)? = null,
) {
    companion object : Options<TimestampOptions>(::TimestampOptions)
}

@Composable
fun useTimestamp(
    options: TimestampOptions = defaultOption(),
): Tuple4<Long, PauseFn, ResumeFn, IsActive> {
    val (interval, offset, callback) = with(options) { tuple(interval, offset, callback) }
    var timestamp by useState(default = System.currentTimeMillis())
    val (resume, pause, isActive) = useInterval(
        optionsOf {
            period = interval
        }
    ) {
        timestamp = System.currentTimeMillis() + offset
        callback?.invoke(timestamp)
    }
    return tuple(
        first = timestamp,
        second = pause,
        third = resume,
        fourth = isActive
    )
}

@Composable
fun useTimestampRef(
    options: TimestampOptions = defaultOption(),
): Tuple4<Ref<Long>, PauseFn, ResumeFn, IsActive> {
    val (interval, offset, callback) = with(options) { tuple(interval, offset, callback) }
    val timestampRef = useRef(default = System.currentTimeMillis())
    val (resume, pause, isActive) = useInterval(
        optionsOf {
            period = interval
        }
    ) {
        timestampRef.current = System.currentTimeMillis() + offset
        callback?.invoke(timestampRef.current)
    }
    return tuple(
        first = timestampRef,
        second = pause,
        third = resume,
        fourth = isActive
    )
}
