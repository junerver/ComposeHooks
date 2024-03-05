package xyz.junerver.compose.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import java.io.Serializable

/**
 * Description: [useRef]可以方便的创建一个不受重组影响的对象引用。另外，它不同于[rememberUpdatedState]
 * ，修改[Ref.current]不会引发组件重组，它可以很好的用于保存现场，或者不需要引发重组的幕后变量。
 * @author Junerver
 * date: 2024/2/7-12:03
 * Email: junerver@gmail.com
 * Version: v1.0
 */

data class Ref<T>(var current: T) : Serializable

@Composable
fun <T> useRef(default: T): Ref<T> = rememberSaveable {
    Ref(default)
}
