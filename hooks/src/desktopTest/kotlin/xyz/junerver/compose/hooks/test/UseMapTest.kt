package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useState

/*
  Description: useMap comprehensive TDD tests
  Author: AI Assistant
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseMapTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_vararg_creates_map() = runComposeUiTest {
        setContent {
            val map = useMap("a" to 1, "b" to 2)
            Text("size=${map.size} a=${map["a"]}")
        }
        waitForIdle()
        onNodeWithText("size=2 a=1").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_iterable_creates_map() = runComposeUiTest {
        setContent {
            val pairs = listOf("x" to 10, "y" to 20)
            val map = useMap(pairs)
            Text("size=${map.size} x=${map["x"]}")
        }
        waitForIdle()
        onNodeWithText("size=2 x=10").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_empty() = runComposeUiTest {
        setContent {
            val map = useMap<String, Int>()
            Text("size=${map.size}")
        }
        waitForIdle()
        onNodeWithText("size=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_put_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val map = useMap("a" to 1)

            SideEffect {
                if (!fired) {
                    fired = true
                    map["b"] = 2
                }
            }

            Text("size=${map.size} b=${map["b"]}")
        }
        waitForIdle()
        onNodeWithText("size=2 b=2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_remove_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val map = useMap("a" to 1, "b" to 2)

            SideEffect {
                if (!fired) {
                    fired = true
                    map.remove("a")
                }
            }

            Text("size=${map.size} hasA=${map.containsKey("a")}")
        }
        waitForIdle()
        onNodeWithText("size=1 hasA=false").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_clear_triggers_recomposition() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val map = useMap("a" to 1, "b" to 2, "c" to 3)

            SideEffect {
                if (!fired) {
                    fired = true
                    map.clear()
                }
            }

            Text("size=${map.size}")
        }
        waitForIdle()
        onNodeWithText("size=0").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_update_value() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val map = useMap("key" to 100)

            SideEffect {
                if (!fired) {
                    fired = true
                    map["key"] = 200
                }
            }

            Text("value=${map["key"]}")
        }
        waitForIdle()
        onNodeWithText("value=200").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_putAll() = runComposeUiTest {
        setContent {
            var fired by useState(false)
            val map = useMap("a" to 1)

            SideEffect {
                if (!fired) {
                    fired = true
                    map.putAll(mapOf("b" to 2, "c" to 3))
                }
            }

            Text("size=${map.size}")
        }
        waitForIdle()
        onNodeWithText("size=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_getOrDefault() = runComposeUiTest {
        setContent {
            val map = useMap("a" to 1)
            val existing = map.getOrDefault("a", 0)
            val missing = map.getOrDefault("z", 99)
            Text("existing=$existing missing=$missing")
        }
        waitForIdle()
        onNodeWithText("existing=1 missing=99").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_keys_and_values() = runComposeUiTest {
        setContent {
            val map = useMap("a" to 1, "b" to 2)
            val keys = map.keys.sorted().joinToString(",")
            val values = map.values.sorted().joinToString(",")
            Text("keys=$keys values=$values")
        }
        waitForIdle()
        onNodeWithText("keys=a,b values=1,2").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_multiple_operations() = runComposeUiTest {
        setContent {
            var phase by useState(0)
            val map = useMap("a" to 1, "b" to 2)

            SideEffect {
                when (phase) {
                    0 -> {
                        map["c"] = 3
                        phase = 1
                    }
                    1 -> {
                        map.remove("a")
                        phase = 2
                    }
                    2 -> {
                        map["b"] = 20
                        phase = 3
                    }
                }
            }

            Text("map=${map.toSortedMap()} phase=$phase")
        }
        waitForIdle()
        onNodeWithText("map={b=20, c=3} phase=3").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_with_different_types() = runComposeUiTest {
        setContent {
            val map = useMap(1 to "one", 2 to "two")
            Text("1=${map[1]} 2=${map[2]}")
        }
        waitForIdle()
        onNodeWithText("1=one 2=two").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun useMap_retains_state_across_recomposition() = runComposeUiTest {
        setContent {
            var counter by useState(0)
            val map = useMap("key" to 0)

            SideEffect {
                if (counter == 0) {
                    map["key"] = 100
                    counter = 1
                } else if (counter == 1) {
                    counter = 2
                }
            }

            Text("value=${map["key"]} counter=$counter")
        }
        waitForIdle()
        onNodeWithText("value=100 counter=2").assertExists()
    }
}
