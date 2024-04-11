package xyz.junerver.composehooks.example.sub

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

/**
 * Description:
 * @author Junerver
 * date: 2024/4/11-11:48
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class PersistentVm : ViewModel() {
    val vmState = mutableStateOf("")

}
