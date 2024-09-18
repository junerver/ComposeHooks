package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.utils.currentTime
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
@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useTimestamp(
    options: TimestampOptions = remember { TimestampOptions() },
    autoResume: Boolean = true,
): Tuple4<Long, PauseFn, ResumeFn, State<IsActive>> {
    val (interval, offset, callback) = with(options) { tuple(interval, offset, callback) }
    var timestamp by useState(default = currentTime)
    val (resume, pause, isActive) = useInterval(
        IntervalOptions.optionOf {
            period = interval
        }
    ) {
        timestamp = currentTime + offset
        callback?.invoke(timestamp.toEpochMilliseconds())
    }
    useMount {
        if (autoResume) resume()
    }
    return remember {
        tuple(
            first = timestamp.toEpochMilliseconds(),
            second = pause,
            third = resume,
            fourth = isActive
        )
    }
}

@Composable
fun useTimestamp(optionsOf: TimestampOptions.() -> Unit, autoResume: Boolean = true): Tuple4<Long, PauseFn, ResumeFn, State<IsActive>> =
    useTimestamp(remember(optionsOf) { TimestampOptions.optionOf(optionsOf) }, autoResume)

/**
 * Use timestamp ref
 *
 * @param options
 * @param autoResume If automatically execute resume when entering the component
 * @return
 */
@Deprecated(
    "Please use the performance-optimized version. Do not pass the Options instance directly. You can simply switch by adding `=` after the `optionsOf` function. If you need to use an older version, you need to explicitly declare the parameters as `options`"
)
@Composable
fun useTimestampRef(
    options: TimestampOptions = remember { TimestampOptions() },
    autoResume: Boolean = true,
): Tuple4<Ref<Long>, PauseFn, ResumeFn, State<IsActive>> {
    val (interval, offset, callback) = with(options) { Triple(interval, offset, callback) }
    val timestampRef = useRef(default = currentTime.toEpochMilliseconds())
    val (resume, pause, isActive) = useInterval(
        IntervalOptions.optionOf {
            period = interval
        }
    ) {
        timestampRef.current = (currentTime + offset).toEpochMilliseconds()
        callback?.invoke(timestampRef.current)
    }
    useMount {
        if (autoResume) resume()
    }
    return remember {
        tuple(
            first = timestampRef,
            second = pause,
            third = resume,
            fourth = isActive
        )
    }
}

@Composable
fun useTimestampRef(
    optionsOf: TimestampOptions.() -> Unit,
    autoResume: Boolean = true,
): Tuple4<Ref<Long>, PauseFn, ResumeFn, State<IsActive>> = useTimestampRef(
    remember(optionsOf) {
        TimestampOptions.optionOf(optionsOf)
    },
    autoResume
)
