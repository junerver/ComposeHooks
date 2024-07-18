package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.Ref

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-10:06
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useForm(): FormInstance {
    return remember { FormInstance() }
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

    fun getFieldError(name: String): List<String> {
        checkRef()
        return formRef.current.fieldErrorMsgsMap[name] ?: emptyList()
    }

    private fun checkRef() {
        require(this::formRef.isInitialized) {
            "FormInstance must be passed to Form before it can be used"
        }
    }
}
