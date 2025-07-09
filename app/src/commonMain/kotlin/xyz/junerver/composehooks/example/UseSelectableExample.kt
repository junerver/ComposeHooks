package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.SelectionMode
import xyz.junerver.compose.hooks.useSelectable
import xyz.junerver.compose.hooks.useToggle

/*
  Description:
  Author: kk
  Date: 2024/3/23-9:35
  Email: kkneverlie@gmail.com
  Version: v1.0
*/

private data class Demo(
    val userName: String,
    val userId: String,
)

private val DEMO_LIST = listOf(
    Demo("jack", "0x00123"),
    Demo("mike", "0x00125"),
    Demo("groovy", "0x00127"),
    Demo("jackson", "0x00129"),
    Demo("michale", "0x00131"),
    Demo("charles", "0x00133"),
    Demo("sara", "0x00135"),
    Demo("duke", "0x00137"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UseSelectableExample() {
    val (selectionMode, toggleFn) = useToggle(SelectionMode.MultiSelect<String>(null), SelectionMode.SingleSelect<String>(null))

    val (selectedItems, isSelected, toggleSelected, selectAll, invertSelection, revertAll) = useSelectable(
        selectionMode!!,
        DEMO_LIST,
        Demo::userId,
    )

    val coroutineScope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            items(DEMO_LIST) { demo ->
                Column(Modifier.height(50.dp)) {
                    Row(Modifier.padding(10.dp)) {
                        Text(modifier = Modifier.align(Alignment.CenterVertically), text = demo.userName)
                        Checkbox(
                            modifier = Modifier.align(
                                Alignment.CenterVertically,
                            ),
                            checked = isSelected(demo.userId),
                            onCheckedChange = {
                                toggleSelected(demo.userId)
                            },
                        )
                    }
                }
            }
        }

        FlowRow(modifier = Modifier.align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Text(modifier = Modifier.align(Alignment.CenterVertically), text = "enable multi select")
                Spacer(modifier = Modifier.width(10.dp))
                Switch(checked = selectionMode is SelectionMode.MultiSelect, onCheckedChange = {
                    toggleFn()
                    revertAll()
                })
            }
            Button(onClick = {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar("selected ${selectedItems.value.joinToString(separator = ";") { it.userName }}")
                }
            }) {
                Text("get selected items")
            }
            Button(enabled = selectionMode is SelectionMode.MultiSelect, onClick = {
                selectAll()
            }) {
                Text("select all")
            }
            Button(enabled = selectionMode is SelectionMode.MultiSelect, onClick = {
                invertSelection()
            }) {
                Text("invert selection")
            }
            Button(enabled = selectionMode is SelectionMode.MultiSelect, onClick = {
                revertAll()
            }) {
                Text("revert all")
            }
        }

        SnackbarHost(modifier = Modifier.align(Alignment.BottomStart), hostState = snackBarHostState) {
            Snackbar {
                Text(it.visuals.message)
            }
        }
    }
}
