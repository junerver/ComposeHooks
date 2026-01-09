package xyz.junerver.composehooks.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.PersistentList
import xyz.junerver.compose.hooks.createMachine
import xyz.junerver.compose.hooks.useStateMachine
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: A Example to demonstrate how to use the useStateMachine Hook
  Author: kk
  Date: 2025/6/22-9:30
  Email: kkneverlie@gmail.com
  Version: v1.0
*/

// Define state enum
enum class LoadingState {
    IDLE, // Idle state
    LOADING, // Loading
    SUCCESS, // Success
    ERROR, // Error
}

// Define event enum
enum class LoadingEvent {
    START, // Start loading
    SUCCESS, // Loading success
    ERROR, // Loading failed
    RETRY, // Retry
    LOG, // Action-only: update context
}

@Composable
fun UseStateMachineExample() {
    // Define state transitions using DSL approach with infix style - cleaner and more readable
    val machineGraph = createMachine<LoadingState, LoadingEvent, Int> {
        // setup initial context
        context(1)
        // setup initial state
        initial(LoadingState.IDLE)

        state(LoadingState.IDLE) {
            on(LoadingEvent.START) {
                target(LoadingState.LOADING)
                action { ctx, e ->
                    println("current state is: $ctx")
                    println("current event is: $e")
                    2
                }
            }
            on(LoadingEvent.LOG) {
                // Action-only event: keep state as-is, only update context
                action { ctx, e ->
                    println("current context is: $ctx")
                    println("current event is: $e")
                    (ctx ?: 0) + 1
                }
            }
        }

        states {
            LoadingState.LOADING {
                // Loading can succeed or fail
                on(LoadingEvent.SUCCESS) {
                    target(LoadingState.SUCCESS)
                    action { ctx, e ->
                        println("current state is: $ctx")
                        println("current event is: $e")
                        3
                    }
                }
                on(LoadingEvent.ERROR) { target(LoadingState.ERROR) }
            }
            LoadingState.SUCCESS {
                // Success state can restart or fail
                on(LoadingEvent.START) { target(LoadingState.LOADING) }
                on(LoadingEvent.ERROR) { target(LoadingState.ERROR) }
            }
            LoadingState.ERROR {
                // Error state can retry
                on(LoadingEvent.RETRY) { target(LoadingState.LOADING) }
            }
        }
    }

    // Use state machine Hook with destructured assignment
    val (
        currentState,
        canTransition,
        transition,
        history,
        reset,
        canGoBack,
        goBack,
        getAvailableEvents,
        context,
    ) = useStateMachine(
        machineGraph = machineGraph,
    )

    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title
            Text(
                text = "State Machine Example - DSL Approach",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            // Current state display card
            StateDisplayCard(currentState.value)

            // Current context display
            ContextDisplayCard(context.value)

            // State history records
            HistoryCard(history.value)

            // Control buttons section
            ControlButtonsSection(
                canTransition = canTransition,
                transition = transition,
                reset = reset,
                canGoBack = canGoBack.value,
                goBack = goBack,
            )

            // Available events display
            AvailableEventsCard(getAvailableEvents())
        }
    }
}

@Composable
private fun StateDisplayCard(currentState: LoadingState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getStateColor(currentState).copy(alpha = 0.1f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(getStateColor(currentState)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Current state",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = getStateDisplayName(currentState),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ContextDisplayCard(context: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Current context",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = context?.toString() ?: "null",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HistoryCard(history: PersistentList<LoadingState>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "State history (${history.size} records)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                history.takeLast(10).forEach { state ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getStateColor(state)),
                    )
                }
                if (history.size > 10) {
                    Text(
                        text = "...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButtonsSection(
    canTransition: (LoadingEvent) -> Boolean,
    transition: (LoadingEvent) -> Unit,
    reset: () -> Unit,
    canGoBack: Boolean,
    goBack: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Control operations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            // State transition buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Start",
                    enabled = canTransition(LoadingEvent.START),
                    modifier = Modifier.weight(1f),
                ) {
                    transition(LoadingEvent.START)
                }

                TButton(
                    text = "Log (ctx+1)",
                    enabled = canTransition(LoadingEvent.LOG),
                    modifier = Modifier.weight(1f),
                ) {
                    transition(LoadingEvent.LOG)
                }

                TButton(
                    text = "Success",
                    enabled = canTransition(LoadingEvent.SUCCESS),
                    modifier = Modifier.weight(1f),
                ) {
                    transition(LoadingEvent.SUCCESS)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Error",
                    enabled = canTransition(LoadingEvent.ERROR),
                    modifier = Modifier.weight(1f),
                ) {
                    transition(LoadingEvent.ERROR)
                }

                TButton(
                    text = "Retry",
                    enabled = canTransition(LoadingEvent.RETRY),
                    modifier = Modifier.weight(1f),
                ) {
                    transition(LoadingEvent.RETRY)
                }
            }

            // Control operation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TButton(
                    text = "Reset",
                    modifier = Modifier.weight(1f),
                ) {
                    reset()
                }

                TButton(
                    text = "Go back",
                    enabled = canGoBack,
                    modifier = Modifier.weight(1f),
                ) {
                    goBack()
                }
            }
        }
    }
}

@Composable
private fun AvailableEventsCard(availableEvents: List<LoadingEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Current available events (${availableEvents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (availableEvents.isEmpty()) {
                Text(
                    text = "No available events",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Use Column to ensure all events are visible
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableEvents.chunked(3).forEach { eventRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            eventRow.forEach { event ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp),
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = getEventDisplayName(event),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function: Get state corresponding color
private fun getStateColor(state: LoadingState): Color = when (state) {
    LoadingState.IDLE -> Color.Gray
    LoadingState.LOADING -> Color.Blue
    LoadingState.SUCCESS -> Color.Green
    LoadingState.ERROR -> Color.Red
}

// Helper function: Get state display name
private fun getStateDisplayName(state: LoadingState): String = state.name

// Helper function: Get event display name
private fun getEventDisplayName(event: LoadingEvent): String = event.name
