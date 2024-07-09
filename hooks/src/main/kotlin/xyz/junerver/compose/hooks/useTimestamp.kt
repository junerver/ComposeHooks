package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.datetime.Clock
import xyz.junerver.kotlin.Tuple4
import xyz.junerver.kotlin.tuple

/*
  Description:
  Author: Junerver
  Date: 2024/3/14-10:18
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
    var offset: Duration = 0.milliseconds,
    var callback: ((Long) -> Unit)? = null,
) {
    companion object : Options<TimestampOptions>(::TimestampOptions)
}

/**
 * Use timestamp
 *
 * @param options
 * @param autoResume If automatically execute resume when entering the component
 * @return
 */
@Composable
fun useTimestamp(
    options: TimestampOptions = defaultOption(),
    autoResume: Boolean = true,
): Tuple4<Long, PauseFn, ResumeFn, IsActive> {
    val (interval, offset, callback) = with(options) { tuple(interval, offset, callback) }
    var timestamp by useState(default = Clock.System.now())
    val (resume, pause, isActive) = useInterval(
        optionsOf {
            period = interval
        }
    ) {
        timestamp = Clock.System.now() + offset
        callback?.invoke(timestamp.toEpochMilliseconds())
    }
    useMount {
        if (autoResume) resume()
    }
    return tuple(
        first = timestamp.toEpochMilliseconds(),
        second = pause,
        third = resume,
        fourth = isActive
    )
}

/**
 * Use timestamp ref
 *
 * @param options
 * @param autoResume If automatically execute resume when entering the component
 * @return
 */
@Composable
fun useTimestampRef(
    options: TimestampOptions = defaultOption(),
    autoResume: Boolean = true,
): Tuple4<Ref<Long>, PauseFn, ResumeFn, IsActive> {
    val (interval, offset, callback) = with(options) { tuple(interval, offset, callback) }
    val timestampRef = useRef(default = Clock.System.now().toEpochMilliseconds())
    val (resume, pause, isActive) = useInterval(
        optionsOf {
            period = interval
        }
    ) {
        timestampRef.current = (Clock.System.now() + offset).toEpochMilliseconds()
        callback?.invoke(timestampRef.current)
    }
    useMount {
        if (autoResume) resume()
    }
    return tuple(
        first = timestampRef,
        second = pause,
        third = resume,
        fourth = isActive
    )
}
