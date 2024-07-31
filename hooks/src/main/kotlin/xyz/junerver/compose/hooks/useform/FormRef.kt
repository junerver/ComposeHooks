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

    /** Is all fields in the form are verified successfully */
    val isValidated: Boolean
        get() {
            return formFieldValidationMap.isEmpty() || formFieldValidationMap.entries.map { it.value }
                .all { it }
        }
}
