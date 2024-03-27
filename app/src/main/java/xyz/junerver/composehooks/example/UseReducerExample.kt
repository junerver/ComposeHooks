package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.Reducer
import xyz.junerver.compose.hooks.useReducer
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/3/11-9:56
 * Email: junerver@gmail.com
 * Version: v1.0
 */
data class SimpleData(
    val name: String,
    val age: Int,
)

sealed interface SimpleAction {
    data class ChangeName(val newName: String) : SimpleAction
    data object AgeIncrease : SimpleAction
}

val simpleReducer: Reducer<SimpleData, SimpleAction> = { prevState: SimpleData, action: SimpleAction ->
    when (action) {
        is SimpleAction.ChangeName -> prevState.copy(name = action.newName)
        is SimpleAction.AgeIncrease -> prevState.copy(age = prevState.age + 1)
    }
}

@Composable
fun UseReducerExample() {
    val (state, dispatch) = useReducer(simpleReducer, initialState = SimpleData("default", 18))
    val (input, setInput) = useState("")
    Surface {
        Column {
            Text(text = "User: $state")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = setInput)
            TButton(text = "changeName") {
                dispatch(SimpleAction.ChangeName(input))
            }
            TButton(text = "+1") {
                dispatch(SimpleAction.AgeIncrease)
            }
        }
    }
}
