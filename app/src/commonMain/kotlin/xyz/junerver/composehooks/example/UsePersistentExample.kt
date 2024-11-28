package xyz.junerver.composehooks.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import xyz.junerver.compose.hooks.invoke
import xyz.junerver.compose.hooks.left
import xyz.junerver.compose.hooks.useGetState
import xyz.junerver.compose.hooks.useKeyboard
import xyz.junerver.compose.hooks.usePersistent
import xyz.junerver.composehooks.example.sub.PersistentVm
import xyz.junerver.composehooks.mmkvClear
import xyz.junerver.composehooks.mmkvGet
import xyz.junerver.composehooks.mmkvSave
import xyz.junerver.composehooks.route.useNavigate
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/4/10-14:44
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun UsePersistentExample() {
    Surface {
        Column {
            DefaultPersistent()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
            MMKVPersistent()
            HorizontalDivider(
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
    var count by usePersistent(key = "count", -1)
    Column {
        Text(text = "DefaultPersistent : exit app will lose state")
        TButton(text = "+1") {
            count += 1
        }
        SubShowCount()
    }
}

@Composable
private fun SubShowCount() {
    val (countState, _, clear) = usePersistent(key = "count", -1)
    Text(text = "persistent: ${countState.value} ,click to clear", modifier = Modifier.clickable { clear() })
}

@Composable
private fun MMKVPersistent() {
    /** 使用持久化需要传递重写三个函数，分别是持久化的 get、set、clear */
    PersistentContext.Provider(
        value = Triple(
            first = ::mmkvGet,
            second = ::mmkvSave,
            third = ::mmkvClear
        )
    ) {
        val (hideKeyboard) = useKeyboard()
        var token by usePersistent(key = "token", "123")
        val (state, setState) = useGetState("")
        Column {
            Text(text = "MMKVPersistent : exit app will NOT lose state")
            Text(text = "token: $token")
            OutlinedTextField(value = state.value, onValueChange = setState.left())
            TButton(text = "saveToken") {
                hideKeyboard()
                token = state.value
                setState("")
                println("now you can exit app,and reopen")
            }
            MMKVPersistentSub()
        }
    }
}

@Composable
private fun MMKVPersistentSub() {
    val (token, _, clear) = usePersistent(key = "token", "321")
    var clearCount by usePersistent(key = "clearCount", 0, forceUseMemory = true)
    Text(
        text = "sub component token: ${token.value} ,click to clear",
        modifier = Modifier.clickable {
            clear()
            clearCount += 1
        }
    )
    Text("current clear count: $clearCount")
}

@Composable
private fun VsViewModel() {
    var vsvm by usePersistent(key = "vsVm", "")
    val (state, setState) = useGetState("")
    val vm = viewModel { PersistentVm() }
    var vmstate by vm.vmState
    val nav = useNavigate()
    val (hideKeyboard) = useKeyboard()

    Column {
        Text(text = "state from persistent: $vsvm")
        Text(text = "state from vm: $vmstate")
        OutlinedTextField(value = state.value, onValueChange = setState.left())
        Row {
            TButton(text = "set state ") {
                hideKeyboard()
                vsvm = state.value
                vmstate = state.value
                setState("")
            }
            TButton(text = "nav to sub") {
                nav.navigate("PersistentSub")
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
        VsViewModelSub()
    }
}

@Composable
private fun VsViewModelSub() {
    val vsvm by usePersistent(key = "vsVm", "")
    val vm = viewModel { PersistentVm() }
    val vmstate by vm.vmState
    Column {
        Text(text = "state from persistent: $vsvm")
        Text(text = "state from vm: $vmstate")
    }
}
