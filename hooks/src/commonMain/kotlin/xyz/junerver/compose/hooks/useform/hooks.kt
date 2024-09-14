@file:Suppress("UnusedReceiverParameter")

package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.genFormFieldKey
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useEventSubscribe

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-10:06
  Email: junerver@gmail.com
  Version: v1.0
*/
object Form

@Composable
fun Form.useForm(): FormInstance = remember { FormInstance() }

@Deprecated(
    "Please use namespace `Form`, just like`Form.useForm()`",
    ReplaceWith("Form.useForm()", "xyz.junerver.compose.hooks.useform.Form")
)
@Composable
fun useForm(): FormInstance = Form.useForm()

/**
 * 使用这个 Hook 你可以在 [FormScope] 外直接获取一个字段的内容**状态**
 *
 * Using this Hook you can directly obtain the content [State] of a field
 * outside [FormScope]
 *
 * @param fieldName the form field to be tracked
 * @param formInstance form controller instance
 * @param T
 * @return
 */
@Composable
fun <T> Form.useWatch(fieldName: String, formInstance: FormInstance): State<T?> {
    val state = _useState<T?>(null)
    useEventSubscribe<T?>(fieldName.genFormFieldKey(formInstance)) { value ->
        state.value = value
    }
    return state
}

/**
 * 方便子组件获取到 [FormInstance]
 *
 * Convenient for subcomponents to obtain [FormInstance]
 *
 * @return
 */
@Composable
fun Form.useFormInstance(): FormInstance = useContext(context = FormContext)
