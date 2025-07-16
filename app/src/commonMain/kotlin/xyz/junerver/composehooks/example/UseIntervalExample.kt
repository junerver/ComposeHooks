package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.seconds
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.LogCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: Example component for useInterval hook
  Author: Junerver
  Date: 2024/3/8-13:05
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useInterval hook
 */
@Composable
fun UseIntervalExample() {
    ScrollColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useInterval Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "The useInterval hook provides a way to run periodic callbacks with configurable delay and period.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Manual control example
        ExampleCard(title = "Manual Control") {
            ManualControlExample()
        }

        // Ready state control example
        ExampleCard(title = "Ready State Control") {
            ReadyStateControlExample()
        }

        // Practical application example
        ExampleCard(title = "Practical Application: Verification Code") {
            VerificationCodeExample()
        }
    }
}

/**
 * Demonstrates manual control of useInterval with resume and pause functions
 */
@Composable
private fun ManualControlExample() {
    // Use useGetState to access the current value in callbacks
    val (countDown, setCountDown, getCountDown) = useGetState(60)
    var ref by useRef(10)
    val logs = useList<String>()

    // Initialize interval with manual control
    val (resume, pause, isActive) = useInterval(
        optionsOf = {
            initialDelay = 2.seconds
            period = 1.seconds
        },
    ) {
        setCountDown { it - 1 }
        ref -= 1
        logs.add("Countdown: ${getCountDown() - 1}, Ref: ${ref - 1}")
        if (logs.size > 5) logs.removeAt(0)
    }

    // Stop interval when countdown reaches zero
    useEffect(countDown) {
        if (getCountDown() == 0) {
            pause()
            logs.add("Countdown reached zero, interval paused")
            if (logs.size > 5) logs.removeAt(0)
        }
    }

    // Start interval on mount
    useMount {
        resume()
        logs.add("Interval started automatically on mount")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Configuration: initialDelay=2s, period=1s",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "This example demonstrates manual control of the interval using resume() and pause() functions.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Status display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                Text(
                    text = "Countdown: ${countDown.value}",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Interval Active: ${isActive.value}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "Reference Value: $ref",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Resume",
                onClick = {
                    resume()
                    logs.add("Interval resumed manually")
                    if (logs.size > 5) logs.removeAt(0)
                },
                enabled = !isActive.value,
            )

            TButton(
                text = "Pause",
                onClick = {
                    pause()
                    logs.add("Interval paused manually")
                    if (logs.size > 5) logs.removeAt(0)
                },
                enabled = isActive.value,
            )

            TButton(
                text = "Reset",
                onClick = {
                    pause()
                    setCountDown(60)
                    ref = 10
                    logs.add("Countdown and reference reset")
                    if (logs.size > 5) logs.removeAt(0)
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}

/**
 * Demonstrates controlling useInterval with a ready state
 */
@Composable
private fun ReadyStateControlExample() {
    val (countDown, setCountDown) = useGetState(60)
    val (isReady, toggle, setReady) = useBoolean(true)
    var ref by useRef(10)
    val logs = useList<String>()

    // Control interval with ready state
    useInterval(
        optionsOf = {
            initialDelay = 2.seconds
            period = 1.seconds
        },
        ready = isReady.value,
    ) {
        setCountDown { it - 1 }
        ref -= 1
        logs.add("Countdown: ${countDown.value - 1}, Ref: ${ref - 1}")
        if (logs.size > 5) logs.removeAt(0)
    }

    // Auto-stop when countdown reaches zero
    useEffect(countDown) {
        if (countDown.value == 0) {
            setReady(false)
            logs.add("Countdown reached zero, ready set to false")
            if (logs.size > 5) logs.removeAt(0)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Configuration: initialDelay=2s, period=1s, ready=${isReady.value}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Text(
            text = "This example demonstrates controlling the interval using the ready parameter.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Status display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                Text(
                    text = "Countdown: ${countDown.value}",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ready State: ${isReady.value}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "Reference Value: $ref",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Toggle Ready",
                onClick = {
                    toggle()
                    logs.add("Ready state toggled to ${isReady.value}")
                    if (logs.size > 5) logs.removeAt(0)
                },
            )

            TButton(
                text = "Reset",
                onClick = {
                    setReady(false)
                    setCountDown(60)
                    ref = 10
                    logs.add("Countdown and reference reset")
                    if (logs.size > 5) logs.removeAt(0)
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}

/**
 * Demonstrates a practical application of useInterval with a verification code input
 */
@Composable
private fun VerificationCodeExample() {
    var phoneNumber by useState("")
    val logs = useList<String>()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "This example demonstrates a practical application of useInterval for a verification code countdown.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Phone input with verification code button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Phone Verification",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                VerificationInput(
                    text = phoneNumber,
                    onTextChanged = { phoneNumber = it },
                    onSendClicked = {
                        logs.add("Verification code sent to: $phoneNumber")
                        if (logs.size > 5) logs.removeAt(0)
                    },
                )
            }
        }

        // Log display
        LogCard(title = "Activity Log:", logs = logs)
    }
}

/**
 * Custom input field with verification code button
 */
@Composable
private fun VerificationInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    placeholderText: String = "Enter phone number",
    buttonText: String = "Get Code",
) {
    BasicTextField(
        value = text,
        onValueChange = { onTextChanged(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Send,
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSendClicked() },
        ),
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        decorationBox = { innerTextField ->
            VerificationInputDecoration(
                innerTextField = innerTextField,
                text = text,
                placeholderText = placeholderText,
                buttonText = buttonText,
                onSendClicked = onSendClicked,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Decoration for verification input with countdown timer
 */
@Composable
private fun VerificationInputDecoration(
    innerTextField: @Composable () -> Unit,
    countDownTimer: Int = 60,
    text: String,
    placeholderText: String,
    buttonText: String,
    onSendClicked: () -> Unit,
) {
    // Control the countdown state
    val (isActive, _, _, setActiveTrue, setActiveFalse) = useBoolean(false)
    val (countdown, setCountdown) = useGetState(countDownTimer)

    // Interval for countdown
    useInterval(
        optionsOf = {
            initialDelay = 1.seconds
            period = 1.seconds
        },
        ready = isActive.value,
    ) {
        setCountdown { it - 1 }
    }

    // Reset when countdown reaches zero
    useEffect(countdown) {
        if (countdown.value == 0) {
            setCountdown(countDownTimer)
            setActiveFalse()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = Color(0xFFF7F7F7), shape = RoundedCornerShape(size = 4.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            innerTextField()
            if (text.isEmpty()) {
                Text(
                    text = placeholderText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                )
            }
        }

        if (isActive.value) {
            // Show countdown
            Text(
                text = "${countdown.value}s",
                modifier = Modifier
                    .width(60.dp)
                    .padding(end = 16.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.End,
                ),
            )
        } else {
            // Show button
            Text(
                text = buttonText,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            if (!isActive.value && text.isNotEmpty()) {
                                setActiveTrue()
                                onSendClicked()
                            }
                        },
                    )
                    .clip(RoundedCornerShape(size = 4.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
