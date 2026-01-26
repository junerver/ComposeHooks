package xyz.junerver.compose.hooks.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.utils.HooksEventManager

/*
  Description: useEvent comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

// Test event classes
data class TestEvent(val message: String)

data class CounterEvent(val delta: Int)

data class RefreshEvent(val timestamp: Long = System.currentTimeMillis())

class UseEventTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun subscriber_receives_published_event() = runComposeUiTest {
        setContent {
            var received by useState("")
            val publish = useEventPublish<TestEvent>()

            useEventSubscribe<TestEvent> { event ->
                received = event.message
            }

            SideEffect {
                if (received.isEmpty()) {
                    publish(TestEvent("hello"))
                }
            }

            Text("received=$received")
        }
        waitForIdle()
        onNodeWithText("received=hello").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun multiple_subscribers_receive_same_event() = runComposeUiTest {
        setContent {
            var received1 by useState("")
            var received2 by useState("")
            val publish = useEventPublish<TestEvent>()

            useEventSubscribe<TestEvent> { event ->
                received1 = "sub1:${event.message}"
            }

            useEventSubscribe<TestEvent> { event ->
                received2 = "sub2:${event.message}"
            }

            SideEffect {
                if (received1.isEmpty()) {
                    publish(TestEvent("broadcast"))
                }
            }

            Text("r1=$received1 r2=$received2")
        }
        waitForIdle()
        onNodeWithText("r1=sub1:broadcast r2=sub2:broadcast").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun different_event_types_are_isolated() = runComposeUiTest {
        setContent {
            var testReceived by useState("")
            var counterReceived by useState(0)
            val publishTest = useEventPublish<TestEvent>()
            val publishCounter = useEventPublish<CounterEvent>()

            useEventSubscribe<TestEvent> { event ->
                testReceived = event.message
            }

            useEventSubscribe<CounterEvent> { event ->
                counterReceived = event.delta
            }

            SideEffect {
                if (testReceived.isEmpty()) {
                    publishTest(TestEvent("test"))
                    publishCounter(CounterEvent(42))
                }
            }

            Text("test=$testReceived counter=$counterReceived")
        }
        waitForIdle()
        onNodeWithText("test=test counter=42").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun publish_without_subscriber_does_not_crash() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val publish = useEventPublish<TestEvent>()

            SideEffect {
                if (!fired) {
                    fired = true
                    publish(TestEvent("no subscriber"))
                }
            }

            Text("fired=$fired")
        }
        waitForIdle()
        onNodeWithText("fired=true").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun subscriber_receives_multiple_events() = runComposeUiTest {
        setContent {
            var count by useState(0)
            var phase by useState(0)
            val publish = useEventPublish<CounterEvent>()

            useEventSubscribe<CounterEvent> { event ->
                count += event.delta
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        publish(CounterEvent(1))
                        phase = 1
                    }
                    1 -> {
                        publish(CounterEvent(2))
                        phase = 2
                    }
                    2 -> {
                        publish(CounterEvent(3))
                        phase = 3
                    }
                }
            }

            Text("count=$count phase=$phase")
        }
        waitForIdle()
        onNodeWithText("count=6 phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun parent_child_communication() = runComposeUiTest {
        @Composable
        fun ChildComponent(onReceived: (String) -> Unit) {
            useEventSubscribe<RefreshEvent> {
                onReceived("refreshed")
            }
            Text("Child")
        }

        setContent {
            var childStatus by useState("waiting")
            var fired by useState(false)
            val publishRefresh = useEventPublish<RefreshEvent>()

            Column {
                ChildComponent { status ->
                    childStatus = status
                }

                SideEffect {
                    if (!fired) {
                        fired = true
                        publishRefresh(RefreshEvent())
                    }
                }

                Text("status=$childStatus")
            }
        }
        waitForIdle()
        onNodeWithText("status=refreshed").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun button_click_publishes_event() = runComposeUiTest {
        setContent {
            var received by useState("")
            val publish = useEventPublish<TestEvent>()

            useEventSubscribe<TestEvent> { event ->
                received = event.message
            }

            Column {
                Button(onClick = { publish(TestEvent("clicked")) }) {
                    Text("Click Me")
                }
                Text("received=$received")
            }
        }

        waitForIdle()
        onNodeWithText("received=").assertExists()

        onNodeWithText("Click Me").performClick()
        waitForIdle()
        onNodeWithText("received=clicked").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun subscriber_updates_with_latest_callback() = runComposeUiTest {
        setContent {
            var multiplier by useState(1)
            var result by useState(0)
            var phase by useState(0)
            val publish = useEventPublish<CounterEvent>()

            useEventSubscribe<CounterEvent> { event ->
                result = event.delta * multiplier
            }

            SideEffect {
                when (phase) {
                    0 -> {
                        publish(CounterEvent(10))
                        phase = 1
                    } // 10 * 1 = 10
                    1 -> {
                        multiplier = 5
                        phase = 2
                    }
                    2 -> {
                        publish(CounterEvent(10))
                        phase = 3
                    } // 10 * 5 = 50
                }
            }

            Text("result=$result multiplier=$multiplier phase=$phase")
        }
        waitForIdle()
        onNodeWithText("result=50 multiplier=5 phase=3").assertExists()
    }

    // Direct HooksEventManager tests (unit tests without Compose)
    @Test
    fun hooksEventManager_register_and_post() {
        var received = ""
        val unsubscribe = HooksEventManager.register<TestEvent> { event ->
            received = event.message
        }

        HooksEventManager.post(TestEvent("direct"))
        assertEquals("direct", received)

        unsubscribe()
    }

    @Test
    fun hooksEventManager_unsubscribe_stops_receiving() {
        var count = 0
        val unsubscribe = HooksEventManager.register<CounterEvent> { event ->
            count += event.delta
        }

        HooksEventManager.post(CounterEvent(1))
        assertEquals(1, count)

        unsubscribe()

        HooksEventManager.post(CounterEvent(10))
        assertEquals(1, count) // Should not increase after unsubscribe
    }

    @Test
    fun hooksEventManager_multiple_subscribers() {
        var sum = 0
        val unsub1 = HooksEventManager.register<CounterEvent> { sum += it.delta }
        val unsub2 = HooksEventManager.register<CounterEvent> { sum += it.delta * 2 }

        HooksEventManager.post(CounterEvent(5))
        assertEquals(15, sum) // 5 + 5*2 = 15

        unsub1()
        unsub2()
    }
}
