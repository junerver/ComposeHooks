package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableLongStateOf

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
    val formFieldMap: MutableMap<String, MutableState<Any?>> = mutableMapOf(),
    /**
     * A map corresponding to whether each field in a [Form] component passes the verification
     */
    val formFieldValidationMap: MutableMap<String, Boolean> = mutableMapOf(),
) {
    // Counter that records data changes in the form
    internal val formOperationCount: MutableLongState = mutableLongStateOf(0L)

    // Record the error message of each field verification failure in the form
    internal val formFieldErrorMessagesMap: MutableMap<String, List<String>> = mutableMapOf()

    // Track whether each field has been touched (user interacted with it)
    internal val formFieldTouchedMap: MutableMap<String, Boolean> = mutableMapOf()

    // Track whether each field value differs from its initial value
    internal val formFieldDirtyMap: MutableMap<String, Boolean> = mutableMapOf()

    // Store initial values for dirty state comparison
    internal val formFieldInitialValueMap: MutableMap<String, Any?> = mutableMapOf()

    // Store validation trigger for each field
    internal val formFieldValidationTriggerMap: MutableMap<String, ValidationTrigger> = mutableMapOf()

    // Track fields with pending validation (for OnBlur/OnSubmit triggers)
    internal val formFieldPendingValidationMap: MutableMap<String, Boolean> = mutableMapOf()

    // Callback for form submission
    internal var onSubmitCallback: ((Map<String, Any?>) -> Unit)? = null

    /** Is all fields in the form are verified successfully */
    val isValidated: Boolean
        get() = formFieldValidationMap.isEmpty() || formFieldValidationMap.values.all { it }
}
