package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks.utils.then

/*
  Description:
  Author: Junerver
  Date: 2024/7/31-15:13
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Form controller class that manages form state and operations.
 * This class provides methods to control and interact with form fields from outside the form component.
 *
 * The controller must be passed to a [Form] component to function properly. It allows you to:
 * - Get and set field values
 * - Reset form fields
 * - Check validation status
 * - Access validation errors
 *
 * @example
 * ```kotlin
 * val form = Form.useForm()
 * 
 * // Set initial values
 * LaunchedEffect(Unit) {
 *     form.setFieldsValue(
 *         "username" to "default",
 *         "email" to "user@example.com"
 *     )
 * }
 * 
 * Form(form) {
 *     // Form content
 * }
 * ```
 */
@Stable
class FormInstance {
    /** Internal reference to form state, initialized when mounted to a Form component */
    internal lateinit var formRef: Ref<FormRef>

    /**
     * Internal property to access the current form field map.
     * Contains all form field states mapped by their field names.
     */
    private val currentFormFieldMap: MutableMap<String, MutableState<Any?>>
        get() = formRef.current.formFieldMap

    /**
     * Retrieves all form field values as a map.
     * Each entry contains the field name as the key and its current value.
     *
     * @return Map of field names to their current values
     *
     * @example
     * ```kotlin
     * val allValues = form.getAllFields()
     * println("Username: ${allValues["username"]}")
     * println("Email: ${allValues["email"]}")
     * ```
     */
    fun getAllFields(): Map<String, Any?> {
        checkRef()
        return currentFormFieldMap.entries.associate {
            it.key to it.value.value
        }
    }

    /**
     * Checks if all form fields pass their validation rules.
     *
     * @return true if all fields are valid, false otherwise
     *
     * @example
     * ```kotlin
     * Button(
     *     enabled = form.isValidated(),
     *     onClick = { /* handle submission */ }
     * ) {
     *     Text("Submit")
     * }
     * ```
     */
    fun isValidated() = formRef.current.isValidated

    /**
     * Sets multiple field values using a map.
     * Only updates fields that exist in the form.
     *
     * @param value Map of field names to their new values
     *
     * @example
     * ```kotlin
     * form.setFieldsValue(mapOf(
     *     "username" to "newUser",
     *     "email" to "new@example.com"
     * ))
     * ```
     */
    fun setFieldsValue(value: Map<String, Any>) {
        checkRef()
        value.filterKeys { currentFormFieldMap.keys.contains(it) }.forEach {
            currentFormFieldMap[it.key]!!.value = it.value
        }
    }

    /**
     * Sets multiple field values using vararg pairs.
     * Provides a more convenient syntax for setting multiple fields.
     *
     * @param pairs Vararg of field name to value pairs
     *
     * @example
     * ```kotlin
     * form.setFieldsValue(
     *     "username" to "newUser",
     *     "email" to "new@example.com",
     *     "age" to 25
     * )
     * ```
     */
    fun setFieldsValue(vararg pairs: Pair<String, Any>) {
        checkRef()
        setFieldsValue(mapOf(pairs = pairs))
    }

    /**
     * Sets a single field value.
     *
     * @param name Field name to update
     * @param value New value for the field
     *
     * @example
     * ```kotlin
     * form.setFieldValue("username", "newUser")
     * ```
     */
    fun setFieldValue(name: String, value: Any?) {
        checkRef()
        currentFormFieldMap.keys.find { it == name }?.let { key ->
            currentFormFieldMap[key]!!.value = value
        }
    }

    /**
     * Sets a single field value using a pair.
     *
     * @param pair Field name to value pair
     *
     * @example
     * ```kotlin
     * form.setFieldValue("username" to "newUser")
     * ```
     */
    fun setFieldValue(pair: Pair<String, Any?>) {
        setFieldValue(pair.first, pair.second)
    }

    /**
     * Retrieves validation error messages for a specific field.
     *
     * @param name Field name to get errors for
     * @return List of error messages, empty if field is valid
     *
     * @example
     * ```kotlin
     * val errors = form.getFieldError("email")
     * if (errors.isNotEmpty()) {
     *     Text(errors.joinToString(), color = Color.Red)
     * }
     * ```
     */
    fun getFieldError(name: String): List<String> {
        checkRef()
        return formRef.current.formFieldErrorMessagesMap[name] ?: emptyList()
    }

    /**
     * Resets all form fields to null, then optionally sets new values.
     *
     * @param value Optional map of field values to set after reset
     *
     * @example
     * ```kotlin
     * // Reset all fields to null
     * form.resetFields()
     * 
     * // Reset and set new values
     * form.resetFields(mapOf(
     *     "username" to "default",
     *     "email" to "default@example.com"
     * ))
     * ```
     */
    fun resetFields(value: Map<String, Any> = emptyMap()) {
        currentFormFieldMap.forEach { (_, state) ->
            state.value = null
        }.then {
            setFieldsValue(value)
        }
    }

    /**
     * Resets all form fields and sets new values using vararg pairs.
     *
     * @param pairs Vararg of field name to value pairs
     *
     * @example
     * ```kotlin
     * form.resetFields(
     *     "username" to "default",
     *     "email" to "default@example.com"
     * )
     * ```
     */
    fun resetFields(vararg pairs: Pair<String, Any>) {
        resetFields(mapOf(pairs = pairs))
    }

    /**
     * Internal function to verify that the form instance is properly initialized.
     * Throws an exception if the form instance hasn't been passed to a Form component.
     */
    private fun checkRef() {
        require(this::formRef.isInitialized) {
            "FormInstance must be passed to Form before it can be used"
        }
    }
}
