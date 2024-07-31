package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
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
fun Form.useForm(): FormInstance {
    return remember { FormInstance() }
}

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
 * @param fieldName
 * @param formInstance
 * @param T
 * @return
 */
@Composable
fun <T> Form.useWatch(fieldName: String, formInstance: FormInstance): State<T?> {
    val state = _useState<T?>(null)
    useEventSubscribe<T?>("HOOK_INTERNAL_FORM_FIELD_${formInstance}_$fieldName") { value ->
        state.value = value
    }
    return state
}

/**
 * 方便子组件获取到 [FormInstance]
 *
 * Convenient for sub-components to obtain [FormInstance]
 *
 * @return
 */
@Composable
fun Form.useFormInstance(): FormInstance = useContext(context = FormContext)
