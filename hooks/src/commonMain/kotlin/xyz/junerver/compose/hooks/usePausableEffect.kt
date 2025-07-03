package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/*
  Description: A pausable effect hook that provides the ability to pause, resume, and stop effect execution
  Author: Junerver
  Date: 2025/7/3-8:49
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Creates a pausable effect hook that allows controlling the execution state of the effect.
 *
 * This hook is built on top of [useEffect] but adds the ability to pause, resume, and completely stop
 * the execution of the effect. When dependencies change, the effect will re-execute unless it is paused or stopped.
 *
 * @param deps Dependencies that trigger the re-execution of the effect
 * @param block The suspend function to execute when dependencies change and the effect is neither paused nor stopped
 * @return A [PausableEffectHolder] containing functions to control the effect execution
 *
 * @example
 * ```kotlin
 * // Basic usage
 * val (source, setSource) = useState("")
 * val (log, setLog) = useState("")
 * val (stop, pause, resume) = usePausableEffect(source) {
 *     setLog(log + "Changed to \"${source}\"\n")
 * }
 *
 * // Pause the effect
 * Button(onClick = { pause() }) {
 *     Text("Pause")
 * }
 *
 * // Resume the effect
 * Button(onClick = { resume() }) {
 *     Text("Resume")
 * }
 *
 * // Completely stop the effect
 * Button(onClick = { stop() }) {
 *     Text("Stop")
 * }
 * ```
 */
@Composable
fun usePausableEffect(vararg deps: Any?, block: SuspendAsyncFn): PausableEffectHolder {
    // Use useRef to store state, ensuring state persistence during recomposition
    var isStop by useRef(false) // Controls whether to completely stop the effect
    var isPause by useRef(false) // Controls whether to pause the effect

    // Only create the effect if it's not stopped
    if (!isStop) {
        useEffect(*deps) {
            // Only execute the effect function if it's not paused
            if (!isPause) {
                block()
            }
        }
    }

    // Define control functions
    val stop = {
        isStop = true // Completely stop the effect, no longer respond to dependency changes
    }

    val pause = {
        isPause = true // Pause the effect, but still respond to dependency changes
    }

    val resume = {
        isPause = false // Resume effect execution
    }

    // Use remember to ensure returning the same instance, avoiding unnecessary recomposition
    return remember { PausableEffectHolder(stop, pause, resume) }
}

/**
 * A data class that holds functions to control the execution of a pausable effect.
 *
 * @property stop Function to completely stop the effect, after which the effect will no longer respond to dependency changes
 * @property pause Function to pause effect execution, the effect will still respond to dependency changes but won't execute
 * @property resume Function to resume effect execution, allowing the effect to execute normally
 */
@Stable
data class PausableEffectHolder(
    val stop: StopFn, // Stop function type alias, defined as () -> Unit
    val pause: PauseFn, // Pause function type alias, defined as () -> Unit
    val resume: ResumeFn, // Resume function type alias, defined as () -> Unit
) {
    operator fun invoke() {
        stop()
    }
}
