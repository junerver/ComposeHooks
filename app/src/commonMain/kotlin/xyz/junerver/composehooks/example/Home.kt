package xyz.junerver.composehooks.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.junerver.composehooks.route.useNavigate

/*
  Description: Home screen with hooks categorized by type
  Author: Junerver
  Date: 2024/9/9-15:36
  Email: junerver@gmail.com
  Version: v2.0
*/

/** Hook category definition */
private data class HookCategory(
    val name: String,
    val icon: ImageVector,
    val hooks: List<HookItem>,
)

/** Individual hook item */
private data class HookItem(
    val name: String,
    val route: String,
)

/** All hook categories based on README classification */
private val hookCategories = listOf(
    HookCategory(
        name = "State",
        icon = Icons.Default.ToggleOn,
        hooks = listOf(
            HookItem("useState", "useState"),
            HookItem("useGetState", "useGetState"),
            HookItem("useBoolean", "useBoolean"),
            HookItem("useToggle", "useToggle"),
            HookItem("useReducer", "useReducer"),
            HookItem("useRef", "useRef"),
            HookItem("useCreation", "useCreation"),
            HookItem("usePrevious", "usePrevious"),
            HookItem("useLatest", "useLatest"),
            HookItem("useAutoReset", "useAutoReset"),
            HookItem("useResetState", "useResetState"),
            HookItem("useDebounce", "useDebounce"),
            HookItem("useThrottle", "useThrottle"),
            HookItem("useContext", "useContext"),
            HookItem("useRedux", "useRedux"),
            HookItem("useSelectable", "useSelectable"),
            HookItem("useStateMachine", "useStateMachine"),
            HookItem("usePersistent", "usePersistent"),
            HookItem("useImmutableList", "useImmutableList"),
            HookItem("useLastChanged", "useLastChanged"),
            HookItem("useForm", "useForm"),
            HookItem("useList", "useList"),
            HookItem("useMap", "useMap"),
            HookItem("useNumber", "useNumber"),
            HookItem("useControllable", "useControllable"),
            HookItem("useMemoizedFn", "useMemoizedFn"),
        ),
    ),
    HookCategory(
        name = "Effect",
        icon = Icons.Outlined.Bolt,
        hooks = listOf(
            HookItem("useEffect", "useEffect"),
            HookItem("useUpdateEffect", "useUpdateEffect"),
            HookItem("usePausableEffect", "usePausableEffect"),
            HookItem("useDebounceEffect", "useDebounce"),
            HookItem("useThrottleEffect", "useThrottle"),
            HookItem("useBackToFrontEffect", "useBackFront"),
            HookItem("useFrontToBackEffect", "useBackFront"),
        ),
    ),
    HookCategory(
        name = "LifeCycle",
        icon = Icons.Default.PlayCircle,
        hooks = listOf(
            HookItem("useMount", "useMount"),
            HookItem("useUnmount", "useUnmount"),
            HookItem("useUnmountedRef", "useUnmount"),
        ),
    ),
    HookCategory(
        name = "Time",
        icon = Icons.Default.HourglassEmpty,
        hooks = listOf(
            HookItem("useNow", "useNow"),
            HookItem("useTimestamp", "useTimestamp"),
            HookItem("useDateFormat", "useDateFormat"),
            HookItem("useTimeAgo", "useTimeAgo"),
            HookItem("useInterval", "useInterval"),
            HookItem("useTimeout", "useTimeout"),
            HookItem("useTimeoutFn", "useTimeoutFn"),
            HookItem("useTimeoutPoll", "useTimeoutPoll"),
            HookItem("useCountdown", "useCountdown"),
        ),
    ),
    HookCategory(
        name = "Network",
        icon = Icons.Default.Public,
        hooks = listOf(
            HookItem("useRequest", "useRequest"),
            HookItem("useAsync", "useAsync"),
            HookItem("useSse", "useSse"),
        ),
    ),
    HookCategory(
        name = "Utilities",
        icon = Icons.Default.Build,
        hooks = listOf(
            HookItem("useCounter", "useCounter"),
            HookItem("useClipboard", "useClipboard"),
            HookItem("useCycleList", "useCycleList"),
            HookItem("useEvent", "useEvent"),
            HookItem("useSorted", "useSorted"),
            HookItem("useUndo", "useUndo"),
            HookItem("useUpdate", "useUpdate"),
            HookItem("useKeyboard", "useKeyboard"),
        ),
    ),
    HookCategory(
        name = "Table",
        icon = Icons.Default.TableChart,
        hooks = listOf(
            HookItem("useTable", "useTable"),
            HookItem("useTableRequest", "useTableRequest"),
        ),
    ),
    HookCategory(
        name = "Math",
        icon = Icons.Default.Calculate,
        hooks = listOf(
            HookItem("useAbs", "useMath"),
            HookItem("useCeil", "useMath"),
            HookItem("useFloor", "useMath"),
            HookItem("useRound", "useMath"),
            HookItem("useTrunc", "useMath"),
            HookItem("useMin", "useMath"),
            HookItem("useMax", "useMath"),
            HookItem("usePow", "useMath"),
            HookItem("useSqrt", "useMath"),
        ),
    ),
    HookCategory(
        name = "AI",
        icon = Icons.Default.SmartToy,
        hooks = listOf(
            HookItem("useChat", "useChat"),
            HookItem("useAgent", "useAgent"),
            HookItem("useGenerateObject", "useGenerateObject"),
            HookItem("useAsr", "useAsr"),
            HookItem("useTts", "useTts"),
        ),
    ),
    HookCategory(
        name = "Android",
        icon = Icons.Outlined.Smartphone,
        hooks = listOf(
            HookItem("useBiometric", "useBiometric"),
            HookItem("useDeviceInfo", "useDeviceInfo"),
            HookItem("useIdle", "useIdle"),
            HookItem("useIlluminance", "useIlluminance"),
            HookItem("useNetwork", "useNetwork"),
            HookItem("useSensor", "useSensor"),
            HookItem("useVibrate", "useVibrate"),
        ),
    ),
    HookCategory(
        name = "Desktop",
        icon = Icons.Default.DesktopWindows,
        hooks = listOf(
            HookItem("useKeyPress", "useKeyPress"),
        ),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen() {
    val nav = useNavigate()
    var expandedCategory by remember { mutableStateOf<String?>("State") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Text(
            text = "ComposeHooks",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "React Hooks for Jetpack Compose",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category cards
        hookCategories.forEach { category ->
            val isExpanded = expandedCategory == category.name

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column {
                    // Category header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCategory = if (isExpanded) null else category.name
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${category.hooks.size} hooks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Hooks list (expandable)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            category.hooks.forEach { hook ->
                                SuggestionChip(
                                    onClick = { nav.navigate(hook.route) },
                                    label = {
                                        Text(
                                            text = hook.name,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Footer spacer
        Spacer(modifier = Modifier.height(16.dp))
    }
}
