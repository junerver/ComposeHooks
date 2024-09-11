package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import xyz.junerver.compose.hooks.IntervalOptions
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useInterval
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/8-13:05
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseIntervalExample() {
    Surface {
        Column {
            Manual()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            ByReady()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            val (text, setText) = remember { mutableStateOf("") }
            CustomTextField(
                text = text,
                onTextChanged = { setText(it) },
                onSendClicked = { /* Handle send button click */ }
            )
        }
    }
}

@Composable
private fun Manual() {
    // if you prefer to this usage, use `useGetState`
    val (countDown, setCountDown) = useGetState(60)
    val (resume, pause, isActive) = useInterval(
        IntervalOptions.optionOf {
            initialDelay = 2.seconds
            period = 1.seconds
        }
    ) {
        setCountDown(countDown - 1)
    }
    useEffect(countDown) {
        if (countDown == 0) pause()
    }
    Column {
        Text(text = "You can get the function by destructuring the return value")
        Text(text = "current: $countDown")
        Text(text = "isActive: $isActive")

        TButton(text = "resume", onClick = { resume() })
        TButton(text = "pause", onClick = { pause() })
    }
}

@Composable
private fun ByReady() {
    val (countDown, setCountDown) = useGetState(60)
    val (isReady, toggle, setReady) = useBoolean(false)
    useInterval(
        options = IntervalOptions.optionOf {
            initialDelay = 2.seconds
            period = 1.seconds
        },
        ready = isReady
    ) {
        setCountDown(countDown - 1)
    }
    useEffect(countDown) {
        if (countDown == 0) setReady(false)
    }
    Column {
        Text(text = "You can also control it by switching the `ready` state:")
        Text(text = "current: $countDown")
        Text(text = "isReady: $isReady")
        TButton(text = "toggle Ready", onClick = { toggle() })
    }
}

@Composable
fun CustomTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    placeholderText: String = "请输入手机号",
    buttonText: String = "获取验证码",
) {
    BasicTextField(
        value = text,
        onValueChange = { onTextChanged(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
            onSend = { onSendClicked() }
        ),
        textStyle = TextStyle(
            fontSize = 12.sp,
            color = Color(0xFF222222)
        ),
        decorationBox = { innerTextField ->
            MyDecorationBox(
                innerTextField = innerTextField,
                text = text,
                placeholderText = placeholderText,
                buttonText = buttonText,
                onSendClicked = onSendClicked
            )
        }
    )
}

@Composable
fun MyDecorationBox(
    innerTextField: @Composable () -> Unit,
    countDownTimer: Int = 10,
    text: String,
    placeholderText: String,
    buttonText: String,
    onSendClicked: () -> Unit,
) {
    val (isReady, _, _, setReadyTrue, setReadyFalse) = useBoolean(false)
    val (countdown, setCountdown) = useGetState(countDownTimer)
    useInterval(
        IntervalOptions.optionOf {
            initialDelay = 1.seconds
            period = 1.seconds
        },
        ready = isReady
    ) {
        setCountdown(countdown - 1)
    }
    useEffect(countdown) {
        if (countdown == 0) {
            setCountdown(countDownTimer)
            setReadyFalse()
        }
    }
    Row(
        modifier = Modifier
            .width(235.dp)
            .height(40.dp)
            .background(color = Color(0xFFF7F7F7), shape = RoundedCornerShape(size = 4.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp)
        ) {
            innerTextField()
            if (text.isEmpty()) {
                Text(
                    text = placeholderText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFFBBBBBB)
                    )
                )
            }
        }
        if (isReady) {
            Text(
                text = "${countdown}s",
                modifier = Modifier
                    .width(40.dp)
                    .padding(end = 12.dp),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFBBBBBB),
                    textAlign = TextAlign.End
                )
            )
        } else {
            Text(
                text = buttonText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF045FFE),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .clickable(onClick = {
                        if (!isReady) {
                            setReadyTrue()
                        }
                        onSendClicked()
                    })
                    .clip(RoundedCornerShape(size = 4.dp))
                    .padding(12.dp, 10.dp, 12.dp, 10.dp)
            )
        }
    }
}
