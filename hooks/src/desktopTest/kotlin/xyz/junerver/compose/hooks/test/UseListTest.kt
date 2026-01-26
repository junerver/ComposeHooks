package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.junerver.compose.hooks.useList
import xyz.junerver.compose.hooks.useState

/*
  Description: useList comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseListTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_vararg_creates_list() = runComposeUiTest {
        setContent {
            val list = useList(1, 2, 3)
            Text("size=${list.size} first=${list.firstOrNull()}")
        }
        waitForIdle()
        onNodeWithText("size=3 first=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_collection_creates_list() = runComposeUiTest {
        setContent {
            val list = useList(listOf("a", "b", "c"))
            Text("size=${list.size} first=${list.firstOrNull()}")
        }
        waitForIdle()
        onNodeWithText("size=3 first=a").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_empty_vararg() = runComposeUiTest {
        setContent {
            val list = useList<Int>()
            Text("size=${list.size}")
        }
        waitForIdle()
        onNodeWithText("size=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_add_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList(1, 2, 3)

            SideEffect {
                if (!fired) {
                    fired = true
                    list.add(4)
                }
            }

            Text("size=${list.size} last=${list.lastOrNull()}")
        }
        waitForIdle()
        onNodeWithText("size=4 last=4").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_remove_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList(1, 2, 3)

            SideEffect {
                if (!fired) {
                    fired = true
                    list.remove(2)
                }
            }

            Text("size=${list.size} contains2=${list.contains(2)}")
        }
        waitForIdle()
        onNodeWithText("size=2 contains2=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_clear_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList(1, 2, 3)

            SideEffect {
                if (!fired) {
                    fired = true
                    list.clear()
                }
            }

            Text("size=${list.size}")
        }
        waitForIdle()
        onNodeWithText("size=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_set_by_index() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList(1, 2, 3)

            SideEffect {
                if (!fired) {
                    fired = true
                    list[1] = 99
                }
            }

            Text("second=${list[1]}")
        }
        waitForIdle()
        onNodeWithText("second=99").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_addAll() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList(1, 2)

            SideEffect {
                if (!fired) {
                    fired = true
                    list.addAll(listOf(3, 4, 5))
                }
            }

            Text("size=${list.size}")
        }
        waitForIdle()
        onNodeWithText("size=5").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_removeAt() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList("a", "b", "c")

            SideEffect {
                if (!fired) {
                    fired = true
                    list.removeAt(0)
                }
            }

            Text("first=${list.firstOrNull()}")
        }
        waitForIdle()
        onNodeWithText("first=b").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_multiple_operations() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val list = useList(1, 2, 3)

            SideEffect {
                when (phase) {
                    0 -> {
                        list.add(4)
                        phase = 1
                    }
                    1 -> {
                        list.remove(2)
                        phase = 2
                    }
                    2 -> {
                        list[0] = 10
                        phase = 3
                    }
                }
            }

            Text("list=${list.toList()} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("list=[10, 3, 4] phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_with_strings() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val list = useList("hello", "world")

            SideEffect {
                if (!fired) {
                    fired = true
                    list.add("!")
                }
            }

            Text("joined=${list.joinToString(" ")}")
        }
        waitForIdle()
        onNodeWithText("joined=hello world !").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useList_retains_state_across_recomposition() = runComposeUiTest {
        setContent {
            var counter by useState(0)
            val list = useList(1, 2, 3)

            SideEffect {
                if (counter == 0) {
                    list.add(4)
                    counter = 1
                } else if (counter == 1) {
                    counter = 2 // Trigger another recomposition
                }
            }

            Text("size=${list.size} counter=$counter")
        }
        waitForIdle()
        // List should retain the added element
        onNodeWithText("size=4 counter=2").assertExists()
    }
}
