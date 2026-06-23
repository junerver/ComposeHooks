package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf

/*
  Description:
  Author: Junerver
  Date: 2024/7/31-15:14
  Email: junerver@gmail.com
  Version: v1.0
*/

@Stable
internal data class FormRef(
    /**
     * Corresponds to the data map in a [Form] component
     */
    private val formFieldStateMap: MutableMap<String, MutableState<Any?>> = observableMutableMap(),
    /**
     * A map corresponding to whether each field in a [Form] component passes the verification
     */
    private val formFieldValidationStateMap: MutableMap<String, Boolean> = observableMutableMap(),
) {
    // Counter that records data changes in the form
    internal val formOperationCount: MutableLongState = mutableLongStateOf(0L)

    // Record the error message of each field verification failure in the form
    private val formFieldErrorMessagesStateMap: MutableMap<String, List<String>> = observableMutableMap()

    // Track whether each field has been touched (user interacted with it)
    private val formFieldTouchedStateMap: MutableMap<String, Boolean> = observableMutableMap()

    // Track whether each field value differs from its initial value
    private val formFieldDirtyStateMap: MutableMap<String, Boolean> = observableMutableMap()

    // Store initial values for dirty state comparison
    private val formFieldInitialValueStateMap: MutableMap<String, Any?> = observableMutableMap()

    // Store validation trigger for each field
    private val formFieldValidationTriggerStateMap: MutableMap<String, ValidationTrigger> = observableMutableMap()

    // Track fields with pending validation (for OnBlur/OnSubmit triggers)
    private val formFieldPendingValidationStateMap: MutableMap<String, Boolean> = observableMutableMap()

    // Callback for form submission
    private var onSubmitCallback: ((Map<String, Any?>) -> Unit)? = null

    /** Is all fields in the form are verified successfully */
    val isValidated: Boolean
        get() = formFieldValidationStateMap.isEmpty() || formFieldValidationStateMap.values.all { it }

    internal val formFieldMap: Map<String, State<Any?>>
        get() = formFieldStateMap

    internal val formFieldValidationMap: Map<String, Boolean>
        get() = formFieldValidationStateMap

    internal val formFieldErrorMessagesMap: Map<String, List<String>>
        get() = formFieldErrorMessagesStateMap

    internal val formFieldTouchedMap: Map<String, Boolean>
        get() = formFieldTouchedStateMap

    internal val formFieldDirtyMap: Map<String, Boolean>
        get() = formFieldDirtyStateMap

    internal val formFieldInitialValueMap: Map<String, Any?>
        get() = formFieldInitialValueStateMap

    internal val formFieldValidationTriggerMap: Map<String, ValidationTrigger>
        get() = formFieldValidationTriggerStateMap

    internal val formFieldPendingValidationMap: Map<String, Boolean>
        get() = formFieldPendingValidationStateMap

    internal val hasSubmitCallback: Boolean
        get() = onSubmitCallback != null

    internal fun registerField(name: String, state: MutableState<Any?>) {
        formFieldStateMap[name] = state
    }

    internal fun containsField(name: String): Boolean = formFieldStateMap.containsKey(name)

    internal fun fieldNames(): Set<String> = formFieldStateMap.keys

    internal fun getAllFields(): Map<String, Any?> = formFieldStateMap.entries.associate { (name, state) ->
        name to state.value
    }

    internal fun setFieldValue(name: String, value: Any?) {
        formFieldStateMap[name]?.value = value
    }

    internal fun resetFieldValues() {
        formFieldStateMap.values.forEach { state ->
            state.value = null
        }
    }

    internal fun incrementOperationCount() {
        formOperationCount.longValue += 1
    }

    internal fun setValidation(name: String, isValid: Boolean) {
        formFieldValidationStateMap[name] = isValid
    }

    internal fun removeValidation(name: String) {
        formFieldValidationStateMap.remove(name)
    }

    internal fun validationState(name: String): Boolean = formFieldValidationStateMap[name] ?: true

    internal fun setErrorMessages(name: String, errors: List<String>) {
        formFieldErrorMessagesStateMap[name] = errors
    }

    internal fun errorMessages(name: String): List<String> = formFieldErrorMessagesStateMap[name] ?: emptyList()

    internal fun allErrorMessages(): Map<String, List<String>> = formFieldErrorMessagesStateMap.filterValues { it.isNotEmpty() }

    internal fun markTouched(name: String) {
        if (containsField(name)) {
            setTouched(name, true)
        }
    }

    internal fun markAllTouched() {
        fieldNames().forEach(::markTouched)
    }

    internal fun setTouched(name: String, isTouched: Boolean) {
        formFieldTouchedStateMap[name] = isTouched
    }

    internal fun isTouched(name: String): Boolean = formFieldTouchedStateMap[name] == true

    internal fun touchedFields(): Set<String> = formFieldTouchedStateMap.filterValues { it }.keys

    internal fun setDirty(name: String, isDirty: Boolean) {
        formFieldDirtyStateMap[name] = isDirty
    }

    internal fun isDirty(name: String): Boolean = formFieldDirtyStateMap[name] == true

    internal fun dirtyFields(): Set<String> = formFieldDirtyStateMap.filterValues { it }.keys

    internal fun clearTouchedAndDirty() {
        formFieldTouchedStateMap.clear()
        formFieldDirtyStateMap.clear()
    }

    internal fun setInitialValue(name: String, value: Any?) {
        formFieldInitialValueStateMap[name] = value
    }

    internal fun initialValue(name: String): Any? = formFieldInitialValueStateMap[name]

    internal fun setValidationTrigger(name: String, trigger: ValidationTrigger) {
        formFieldValidationTriggerStateMap[name] = trigger
    }

    internal fun validationTrigger(name: String): ValidationTrigger = formFieldValidationTriggerStateMap[name] ?: ValidationTrigger.OnChange

    internal fun setPendingValidation(name: String, isPending: Boolean) {
        formFieldPendingValidationStateMap[name] = isPending
    }

    internal fun clearPendingValidation(name: String) {
        formFieldPendingValidationStateMap.remove(name)
    }

    internal fun clearAllPendingValidation() {
        formFieldPendingValidationStateMap.clear()
    }

    internal fun setOnSubmitCallback(callback: ((Map<String, Any?>) -> Unit)?) {
        onSubmitCallback = callback
    }

    internal fun invokeSubmitCallback(values: Map<String, Any?>) {
        onSubmitCallback?.invoke(values)
    }
}

private fun <K, V> observableMutableMap(): MutableMap<K, V> = ObservableMutableMap()

private class ObservableMutableMap<K, V> : MutableMap<K, V> {
    private val backing = mutableStateMapOf<K, V>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = backing.entries
    override val keys: MutableSet<K>
        get() = backing.keys
    override val size: Int
        get() = backing.size
    override val values: MutableCollection<V>
        get() = backing.values

    override fun clear() = backing.clear()
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun remove(key: K): V? = backing.remove(key)
    override fun putAll(from: Map<out K, V>) = backing.putAll(from)
    override fun put(key: K, value: V): V? = backing.put(key, value)
    override fun get(key: K): V? = backing[key]
    override fun containsValue(value: V): Boolean = backing.containsValue(value)
    override fun containsKey(key: K): Boolean = backing.containsKey(key)
    override fun equals(other: Any?): Boolean = backing.toMap() == other
    override fun hashCode(): Int = backing.toMap().hashCode()
    override fun toString(): String = backing.toString()
}
