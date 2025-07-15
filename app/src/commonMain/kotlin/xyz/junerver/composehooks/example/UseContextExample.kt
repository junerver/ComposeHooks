package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.tuple
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useControllable
import xyz.junerver.compose.hooks.useReducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description: 使用[useContext]可以避免复杂的状态提升，状态由父组件通过[ReactContext.Provider]提供，子组件无论嵌套多少级，都可以使用[useContext]轻松获取上下文

  Using [useContext] can avoid complex state promotion. The state is provided by the parent component through [ReactContext.Provider]. No matter how many levels of nesting the child component has, you can use [useContext] to easily obtain the context.
  Author: Junerver
  Date: 2024/3/11-11:47
  Email: junerver@gmail.com
  Version: v1.0
*/
val initialState: State<SimpleData> = mutableStateOf(SimpleData("default", 18))

/**
 * 上下文的初始值并没有限定，但是我推荐使用[tuple]来传递一个元组
 */
val SimpleContext = createContext(
    tuple(
        initialState,
        { _: String -> },
        {},
    ),
)

@Composable
fun UseContextExample() {

    ScrollColumn(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useContext Examples",
            style = MaterialTheme.typography.headlineMedium,
        )

        // 交互式演示
        InteractiveContextDemo()

        // 基本用法示例
        ExampleCard(title = "Basic Usage") {
            BasicUsageExample()
        }

        // 嵌套组件示例
        ExampleCard(title = "Nested Components") {
            NestedComponentsExample()
        }

        // 实际应用场景：主题切换
        ExampleCard(title = "Real-world Usage: Theme Switching") {
            ThemeSwitchingExample()
        }
    }
}

@Composable
private fun InteractiveContextDemo() {
    val (state, dispatch) = useReducer(simpleReducer, initialState = initialState.value)
    var inputText by useState("")

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Interactive Demo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // 上下文提供者
            SimpleContext.Provider(
                value = tuple(
                    state,
                    { newName: String -> dispatch(SimpleAction.ChangeName(newName)) },
                    { dispatch(SimpleAction.AgeIncrease) },
                ),
            ) {
                // 当前状态显示
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Context State:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Name: ${state.value.name}",
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        Text(
                            text = "Age: ${state.value.age}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                // 交互控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter new name") },
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        TButton(text = "Change Name") {
                            dispatch(SimpleAction.ChangeName(inputText))
                            inputText = ""
                        }

                        TButton(text = "Increase Age") {
                            dispatch(SimpleAction.AgeIncrease)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 子组件示例
                Text(
                    text = "Child Components (using context):",
                    style = MaterialTheme.typography.titleSmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Card(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Child One (Read-only)",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            ChildOne()
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Child Two (Interactive)",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            ChildTwo()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicUsageExample() {
    Column {
        Text(
            text = "The useContext hook allows components to consume a context value:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "1. Create a context with createContext()",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "2. Wrap components with Context.Provider",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = "3. Access context with useContext() in any child component",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Code: val (state, ...) = useContext(MyContext)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NestedComponentsExample() {
    val (state, dispatch) = useReducer(simpleReducer, initialState = initialState.value)

    Column {
        Text(
            text = "Context allows data to be passed through the component tree without manually passing props:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 上下文提供者
        SimpleContext.Provider(
            value = tuple(
                state,
                { newName: String -> dispatch(SimpleAction.ChangeName(newName)) },
                { dispatch(SimpleAction.AgeIncrease) },
            ),
        ) {
            Surface {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Use `useContext` to easily pass states or events to child components, avoiding layer-by-layer transmission between components",
                        style = MaterialTheme.typography.bodySmall,
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color,
                    )

                    Text(text = "Child One:")
                    ChildOne()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Child Two:")
                    ChildTwo()
                }
            }
        }
    }
}

// 主题上下文
private val ThemeContext = createContext(tuple(false, {}))

@Composable
private fun ThemeSwitchingExample() {
    var isDarkTheme by useState(false)

    Column {
        Text(
            text = "A real-world example of using context for theme switching:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 主题上下文提供者
        ThemeContext.Provider(
            value = tuple(
                isDarkTheme,
                { isDarkTheme = !isDarkTheme },
            ),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 主题状态显示
                    Text(
                        text = "Current Theme: ${if (isDarkTheme) "Dark" else "Light"}",
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 主题切换按钮
                    ThemeToggleButton()

                    Spacer(modifier = Modifier.height(8.dp))

                    // 使用主题的组件
                    ThemedComponent()
                }
            }
        }
    }
}

@Composable
private fun ThemeToggleButton() {
    val (_, toggleTheme) = useContext(ThemeContext)

    TButton(text = "Toggle Theme") {
        toggleTheme()
    }
}

@Composable
private fun ThemedComponent() {
    val (isDarkTheme) = useContext(ThemeContext)

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "This component uses the theme from context",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "I am using ${if (isDarkTheme) "Dark" else "Light"} theme",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun ChildOne() {
    val (state) = useContext(context = SimpleContext)
    Text(text = "state: ${state.value}")
}

@Composable
private fun ChildTwo() {
    val (_, changName, ageIncrease) = useContext(context = SimpleContext)
    val (_, setState, getState) = useControllable("")

    Column {
        OutlinedTextField(value = getState(), onValueChange = setState)
        TButton(text = "changeName") {
            changName(getState())
            setState("")
        }
        TButton(text = "age +1") {
            ageIncrease()
        }
    }
}
