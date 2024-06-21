package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.junerver.compose.hooks.PersistentContext
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useKeyboard
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.compose.hooks.useState
import xyz.junerver.composehooks.example.sub.PersistentVm
import xyz.junerver.composehooks.mmkvClear
import xyz.junerver.composehooks.mmkvGet
import xyz.junerver.composehooks.mmkvSave
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 * @author Junerver
 * date: 2024/4/10-14:44
 * Email: junerver@gmail.com
 * Version: v1.0
 */

@Composable
fun UsePersistentExample() {
    Surface {
        Column {
            DefaultPersistent()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            MMKVPersistent()
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            VsViewModel()
        }
    }
}

@Composable
private fun DefaultPersistent() {
    val (count, saveCount) = usePersistent(key = "count", -1)
    Column {
        Text(text = "DefaultPersistent : exit app will lose state")
        TButton(text = "+1") {
            saveCount(count + 1)
        }
        Text(text = "persistent: $count")
    }
}

@Composable
private fun MMKVPersistent() {
    /**
     * 使用持久化需要传递重写三个函数，分别是持久化的 get、set、clear
     */
    PersistentContext.Provider(
        value = PersistentContext.LocalCtx.current.copy(
            first = ::mmkvGet,
            second = ::mmkvSave,
            third = ::mmkvClear
        )
    ) {
        val (hideKeyboard) = useKeyboard()
        val (token, saveToken) = usePersistent(key = "token", "")
        val (state, setState) = useGetState("")
        Column {
            Text(text = "MMKVPersistent : exit app will NOT lose state")
            Text(text = "token: $token")
            OutlinedTextField(value = state, onValueChange = setState)
            TButton(text = "saveToken") {
                hideKeyboard()
                saveToken(state)
                setState("")
                toast("now you can exit app,and reopen")
            }
        }
    }
}

@Composable
private fun VsViewModel() {
    val (vsvm, saveVsvm) = usePersistent(key = "vsVm", "")
    val (state, setState) = useGetState("")
    val vm: PersistentVm = viewModel()
    var vmstate by vm.vmState
    val nav = useNavigate()
    val (hideKeyboard) = useKeyboard()

    Column {
        Text(text = "state from persistent: $vsvm")
        Text(text = "state from vm: $vmstate")
        OutlinedTextField(value = state, onValueChange = setState)
        Row {
            TButton(text = "set state ") {
                hideKeyboard()
                saveVsvm(state)
                vmstate = state
                setState("")
            }
            TButton(text = "nav to sub") {
                nav.navigate("PersistentSub")
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
        VsViewModelSub()
    }
}

@Composable
private fun VsViewModelSub() {
    val (vsvm, _) = usePersistent(key = "vsVm", "")
    val vm: PersistentVm = viewModel()
    var vmstate by vm.vmState
    Column {
        Text(text = "state from persistent: $vsvm")
        Text(text = "state from vm: $vmstate")
    }
}
