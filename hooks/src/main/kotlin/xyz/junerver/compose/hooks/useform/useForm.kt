package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useEventSubscribe
import xyz.junerver.kotlin.then

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
 * Using this Hook you can directly obtain the content [State] of a field outside [FormScope]
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

class FormInstance {
    /** after Form Mount ref will assignment */
    internal lateinit var formRef: Ref<FormRef>
    private val formMap: MutableMap<String, MutableState<Any?>>
        get() = formRef.current.form

    fun getAllFields(): Map<String, Any?> {
        checkRef()
        return formMap.entries.associate {
            it.key to it.value.value
        }
    }

    fun isValidated() = formRef.current.isValidated

    /**
     * Set fields value，it only can be called after component mounted;
     *
     * @param value
     */
    fun setFieldsValue(value: Map<String, Any>) {
        checkRef()
        value.filterKeys { formMap.keys.contains(it) }.forEach {
            val (_, setState) = formMap[it.key]!!
            setState(it.value)
        }
    }

    fun setFieldsValue(vararg pairs: Pair<String, Any>) {
        setFieldsValue(mapOf(pairs = pairs))
    }

    /**
     * Set field value
     *
     * @param name
     * @param value
     */
    fun setFieldValue(name: String, value: Any?) {
        checkRef()
        formMap.keys.find { it == name }?.let { key ->
            val (_, setState) = formMap[key]!!
            setState(value)
        }
    }

    fun setFieldValue(pair: Pair<String, Any?>) {
        setFieldValue(pair.first, pair.second)
    }

    /**
     * Get field error
     *
     * @param name
     * @return
     */
    fun getFieldError(name: String): List<String> {
        checkRef()
        return formRef.current.fieldErrorMsgsMap[name] ?: emptyList()
    }

    /**
     * Reset fields
     *
     * @param value
     */
    fun resetFields(value: Map<String, Any> = emptyMap()) {
        formMap.forEach { (_, state) ->
            state.value = null
        }.then {
            setFieldsValue(value)
        }
    }

    fun resetFields(vararg pairs: Pair<String, Any>) {
        resetFields(mapOf(pairs = pairs))
    }

    private fun checkRef() {
        require(this::formRef.isInitialized) {
            "FormInstance must be passed to Form before it can be used"
        }
    }
}
