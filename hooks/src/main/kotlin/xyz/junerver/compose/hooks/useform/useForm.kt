package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.useCreation

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-10:06
  Email: junerver@gmail.com
  Version: v1.0
*/

@Composable
fun useForm(): FormInstance {
    return useCreation { FormInstance() }.current
}

class FormInstance {
    /**
     * after Form Mount ref will assignment
     */
    internal lateinit var formRef: Ref<FormRef>

    fun getAllFields(): Map<String, Any?> {
        if (!this::formRef.isInitialized) error("FormInstance must be passed to Form before it can be used")
        return formRef.current.form.entries.associate {
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
        val form = formRef.current.form
        value.filterKeys { form.keys.contains(it) }.forEach {
            val (_, setState) = form[it.key]!!
            setState(it.value)
        }
    }

    fun setFieldsValue(vararg pairs: Pair<String, Any>) {
        setFieldsValue(mapOf(pairs = pairs))
    }
}
