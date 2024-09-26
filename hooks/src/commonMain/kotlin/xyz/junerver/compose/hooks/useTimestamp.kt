package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import xyz.junerver.compose.hooks.utils.currentTime

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
@Stable
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
fun useTimestamp(options: TimestampOptions = remember { TimestampOptions() }, autoResume: Boolean = true): TimestampHolder {
    val (interval, offset, callback) = with(options) { Triple(interval, offset, callback) }
    val timestamp = useState(default = currentTime)
    val (resume, pause, isActive) = useInterval(
        IntervalOptions.optionOf {
            period = interval
        }
    ) {
        timestamp.value = currentTime + offset
        callback?.invoke(timestamp.value.toEpochMilliseconds())
    }
    useMount {
        if (autoResume) resume()
    }
    val timestampState = useState { timestamp.value.toEpochMilliseconds() }
    return remember { TimestampHolder(timestampState, pause, resume, isActive) }
}

@Composable
fun useTimestamp(optionsOf: TimestampOptions.() -> Unit, autoResume: Boolean = true): TimestampHolder =
    useTimestamp(remember { TimestampOptions.optionOf(optionsOf) }, autoResume)

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
fun useTimestampRef(options: TimestampOptions = remember { TimestampOptions() }, autoResume: Boolean = true): TimestampRefHolder {
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
    return remember { TimestampRefHolder(timestampRef, pause, resume, isActive) }
}

@Composable
fun useTimestampRef(optionsOf: TimestampOptions.() -> Unit, autoResume: Boolean = true): TimestampRefHolder = useTimestampRef(
    remember { TimestampOptions.optionOf(optionsOf) },
    autoResume
)

@Stable
data class TimestampHolder(
    val state: State<Long>,
    val pause: PauseFn,
    val resume: ResumeFn,
    val isActive: IsActive,
)

@Stable
data class TimestampRefHolder(
    val ref: Ref<Long>,
    val pause: PauseFn,
    val resume: ResumeFn,
    val isActive: IsActive,
)
