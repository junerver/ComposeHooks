package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.*
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.tuple

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
        {}
    )
)

@Composable
fun UseContextExample() {
    val (state, dispatch) = useReducer(simpleReducer, initialState = initialState.value)
    /**
     * 通过[ReactContext.Provider]向子组件提供上下文，子组件只需要通过[useContext]即可拿到正确的上下文；
     */
    SimpleContext.Provider(
        value = tuple(
            state,
            /**
             * 不建议直接将[dispatch]传递给[Provider]
             */
            { newName: String -> dispatch(SimpleAction.ChangeName(newName)) },
            { dispatch(SimpleAction.AgeIncrease) }
        )
    ) {
        Surface {
            Column {
                Text(text = "ChileOne:")
                ChildOne()
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "ChileTwo:")
                ChildTwo()
            }
        }
    }
}

@Composable
fun ChildOne() {
    val (state) = useContext(context = SimpleContext)
    Text(text = "state: ${state.value}")
}

@Composable
fun ChildTwo() {
    val (_, changName, ageIncrease) = useContext(context = SimpleContext)
    val (_, setState, getState) = useGetState("")

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
