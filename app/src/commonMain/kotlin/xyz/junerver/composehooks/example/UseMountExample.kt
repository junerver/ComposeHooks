package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.setValue
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.hooks.useUnmount
import xyz.junerver.compose.hooks.useUnmountedRef
import xyz.junerver.compose.hooks.useUpdate
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.composehooks.utils.now

/*
  Description: Example component for useMount, useUnmount and useUnmountedRef hooks
  Author: Junerver
  Date: 2024/3/8-11:41
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Example component demonstrating the useMount, useUnmount and useUnmountedRef hooks
 */
@Composable
fun UseMountExample() {
    Surface {
        ScrollColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 页面标题
            Text(
                text = "useMount, useUnmount & useUnmountedRef Examples",
                style = MaterialTheme.typography.headlineMedium,
            )

            // 介绍文本
            Text(
                text = "These hooks provide lifecycle management for composable functions, allowing you to execute code when a component mounts or unmounts.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // 交互式演示
            InteractiveMountDemo()

            // 基本用法示例
            ExampleCard(title = "Basic Usage") {
                BasicMountExample()
            }

            // 使用 useUnmountedRef 示例
            ExampleCard(title = "Using useUnmountedRef") {
                UnmountedRefExample()
            }

            // 实际应用示例
            ExampleCard(title = "Practical Application: Resource Cleanup") {
                ResourceCleanupExample()
            }
        }
    }
}

/**
 * Interactive demonstration of mount and unmount hooks
 */
@Composable
private fun InteractiveMountDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            var mountCount by useState(0)
            var unmountCount by useState(0)
            val (isVisible, toggleVisibility) = useBoolean(true)

            // Display mount/unmount counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Mount count: $mountCount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Unmount count: $unmountCount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TButton(
                    text = if (isVisible.value) "Hide Component" else "Show Component",
                ) {
                    toggleVisibility()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional rendering of the component
            if (isVisible.value) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Mounted Component",
                            style = MaterialTheme.typography.titleSmall,
                        )

                        // Component with mount/unmount hooks
                        MountTrackerComponent(
                            onMount = { mountCount++ },
                            onUnmount = { unmountCount++ },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Component that tracks mount and unmount events
 */
@Composable
private fun MountTrackerComponent(onMount: () -> Unit, onUnmount: () -> Unit) {
    useMount {
        println("Component mounted")
        onMount()
    }

    useUnmount {
        println("Component unmounted")
        onUnmount()
    }

    Text(
        text = "This component uses useMount and useUnmount hooks to track lifecycle events.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

/**
 * Basic example of useMount and useUnmount hooks
 */
@Composable
private fun BasicMountExample() {
    val (isVisible, toggleVisibility) = useBoolean(false)
    val update = useUpdate()

    Column {
        Text(
            text = "This example demonstrates the basic usage of useMount and useUnmount hooks.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TButton(text = "Toggle Component") {
                toggleVisibility()
            }

            Spacer(modifier = Modifier.width(8.dp))

            TButton(text = "Force Update") {
                update()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isVisible.value) {
            SimpleLifecycleComponent()
        }
    }
}

/**
 * Simple component demonstrating lifecycle hooks
 */
@Composable
private fun SimpleLifecycleComponent() {
    var mountTime by useState("")

    useMount {
        mountTime = "Mounted at: ${now()}"
        println("SimpleLifecycleComponent: $mountTime")
    }

    useUnmount {
        println("SimpleLifecycleComponent unmounted after being mounted at: $mountTime")
    }

    Text(
        text = mountTime,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * Example demonstrating the useUnmountedRef hook
 */
@Composable
private fun UnmountedRefExample() {
    val (isVisible, toggleVisibility) = useBoolean(true)
    var asyncOperationResult by useState("No result yet")

    Column {
        Text(
            text = "This example demonstrates how to use useUnmountedRef to prevent state updates after unmounting.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            TButton(text = if (isVisible.value) "Unmount Component" else "Mount Component") {
                toggleVisibility()
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Result: $asyncOperationResult",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        if (isVisible.value) {
            UnmountableAsyncComponent(onResult = { result ->
                asyncOperationResult = result
            })
        }
    }
}

/**
 * Component that performs async operations and checks unmounted state
 */
@Composable
private fun UnmountableAsyncComponent(onResult: (String) -> Unit) {
    val unmountedRef = useUnmountedRef()

    useMount {
        println("UnmountableAsyncComponent mounted")
        // Simulate async operation
        launch(Dispatchers.Main + SupervisorJob()) {
            delay(2.seconds)
            // Check if component is still mounted before updating state
            if (!unmountedRef.current) {
                println("Async operation completed while component is still mounted")
                onResult("Operation completed successfully at ${now()}")
            } else {
                println("Component was unmounted, skipping state update")
                onResult("Operation completed but component was unmounted")
            }
        }
    }

    useUnmount {
        println("UnmountableAsyncComponent unmounted")
    }

    Text(
        text = "This component starts an async operation on mount that completes after 2 seconds.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    Text(
        text = "Try unmounting before the operation completes!",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

/**
 * Example demonstrating resource cleanup with useMount and useUnmount
 */
@Composable
private fun ResourceCleanupExample() {
    val (isActive, toggleActive) = useBoolean(false)
    var status by useState("Resource inactive")

    Column {
        Text(
            text = "This example demonstrates how to properly manage resources using lifecycle hooks.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            TButton(
                text = if (isActive.value) "Deactivate Resource" else "Activate Resource",
            ) {
                toggleActive()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        if (isActive.value) {
            ResourceComponent(onStatusChange = { newStatus ->
                status = newStatus
            })
        }
    }
}

/**
 * Component that simulates resource management
 */
@Composable
private fun ResourceComponent(onStatusChange: (String) -> Unit) {
    // Simulate a resource ID
    val resourceId by useCreation { Random.nextInt(1000, 9999) }
    var ready by useRef(true)

    useMount {
        // Simulate resource initialization
        println("Initializing resource #$resourceId")
        onStatusChange("Resource #$resourceId initialized")

        // Simulate periodic resource usage
        launch(Dispatchers.Main + SupervisorJob()) {
            var counter = 0
            while (ready) {
                counter++
                onStatusChange("Resource #$resourceId active (${counter}s)")
                delay(1.seconds)
            }
        }
    }

    useUnmount {
        // Simulate resource cleanup
        ready = false
        println("Cleaning up resource #$resourceId")
        onStatusChange("Resource #$resourceId released")
    }

    Text(
        text = "Resource #$resourceId is active and updating every second.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )

    Text(
        text = "When this component unmounts, the resource will be properly cleaned up.",
        style = MaterialTheme.typography.bodySmall,
    )
}
