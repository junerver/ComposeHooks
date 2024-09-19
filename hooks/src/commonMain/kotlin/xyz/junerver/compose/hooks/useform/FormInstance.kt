package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.kotlin.then

/*
  Description:
  Author: Junerver
  Date: 2024/7/31-15:13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Form controller, The [Form] component needs to pass this controller instance so that
 * the controller can control the component outside.
 */
@Stable
class FormInstance {
    /** after Form Mount ref will assignment */
    internal lateinit var formRef: Ref<FormRef>
    private val currentFormFieldMap: MutableMap<String, MutableState<Any?>>
        get() = formRef.current.formFieldMap

    /**
     * get the full field content of the form
     */
    fun getAllFields(): Map<String, Any?> {
        checkRef()
        return currentFormFieldMap.entries.associate {
            it.key to it.value.value
        }
    }

    /**
     * all fields of the form pass through the validator
     */
    fun isValidated() = formRef.current.isValidated

    /**
     * Set fields value，it only can be called after component mounted;
     *
     * @param value A map consisting of form fields and content
     */
    fun setFieldsValue(value: Map<String, Any>) {
        checkRef()
        value.filterKeys { currentFormFieldMap.keys.contains(it) }.forEach {
            currentFormFieldMap[it.key]!!.value = it.value
        }
    }

    /**
     * Set fields value，it only can be called after component mounted;
     *
     * @param pairs
     */
    fun setFieldsValue(vararg pairs: Pair<String, Any>) {
        checkRef()
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
        currentFormFieldMap.keys.find { it == name }?.let { key ->
            currentFormFieldMap[key]!!.value = value
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
        return formRef.current.formFieldErrorMessagesMap[name] ?: emptyList()
    }

    /**
     * Reset fields
     *
     * @param value
     */
    fun resetFields(value: Map<String, Any> = emptyMap()) {
        currentFormFieldMap.forEach { (_, state) ->
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
