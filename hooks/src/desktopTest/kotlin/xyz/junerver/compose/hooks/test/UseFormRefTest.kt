package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useform.FormRef

/*
  Description: FormRef comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormRefTest {

    @Test
    fun formRef_initializes_with_empty_maps() {
        val formRef = FormRef()

        assertTrue(formRef.formFieldMap.isEmpty())
        assertTrue(formRef.formFieldValidationMap.isEmpty())
        assertTrue(formRef.formFieldErrorMessagesMap.isEmpty())
    }

    @Test
    fun formRef_isValidated_returns_true_when_validation_map_is_empty() {
        val formRef = FormRef()

        assertTrue(formRef.isValidated, "Empty validation map should return true")
    }

    @Test
    fun formRef_isValidated_returns_true_when_all_fields_are_valid() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field1"] = true
        formRef.formFieldValidationMap["field2"] = true
        formRef.formFieldValidationMap["field3"] = true

        assertTrue(formRef.isValidated, "All valid fields should return true")
    }

    @Test
    fun formRef_isValidated_returns_false_when_any_field_is_invalid() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field1"] = true
        formRef.formFieldValidationMap["field2"] = false
        formRef.formFieldValidationMap["field3"] = true

        assertFalse(formRef.isValidated, "Any invalid field should return false")
    }

    @Test
    fun formRef_isValidated_returns_false_when_all_fields_are_invalid() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field1"] = false
        formRef.formFieldValidationMap["field2"] = false

        assertFalse(formRef.isValidated, "All invalid fields should return false")
    }

    @Test
    fun formRef_isValidated_returns_false_when_single_field_is_invalid() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field1"] = false

        assertFalse(formRef.isValidated, "Single invalid field should return false")
    }

    @Test
    fun formRef_formFieldMap_stores_mutable_states() {
        val formRef = FormRef()
        val state1 = mutableStateOf<Any?>("value1")
        val state2 = mutableStateOf<Any?>(123)

        formRef.formFieldMap["field1"] = state1
        formRef.formFieldMap["field2"] = state2

        assertEquals(2, formRef.formFieldMap.size)
        assertEquals("value1", formRef.formFieldMap["field1"]?.value)
        assertEquals(123, formRef.formFieldMap["field2"]?.value)
    }

    @Test
    fun formRef_formFieldMap_allows_value_updates() {
        val formRef = FormRef()
        val state = mutableStateOf<Any?>("initial")
        formRef.formFieldMap["field"] = state

        state.value = "updated"

        assertEquals("updated", formRef.formFieldMap["field"]?.value)
    }

    @Test
    fun formRef_formFieldErrorMessagesMap_stores_error_lists() {
        val formRef = FormRef()
        formRef.formFieldErrorMessagesMap["field1"] = listOf("Error 1", "Error 2")
        formRef.formFieldErrorMessagesMap["field2"] = listOf("Error 3")

        assertEquals(2, formRef.formFieldErrorMessagesMap.size)
        assertEquals(listOf("Error 1", "Error 2"), formRef.formFieldErrorMessagesMap["field1"])
        assertEquals(listOf("Error 3"), formRef.formFieldErrorMessagesMap["field2"])
    }

    @Test
    fun formRef_formFieldErrorMessagesMap_allows_empty_error_list() {
        val formRef = FormRef()
        formRef.formFieldErrorMessagesMap["field"] = emptyList()

        assertEquals(emptyList<String>(), formRef.formFieldErrorMessagesMap["field"])
    }

    @Test
    fun formRef_formOperationCount_initializes_to_zero() {
        val formRef = FormRef()

        assertEquals(0L, formRef.formOperationCount.longValue)
    }

    @Test
    fun formRef_formOperationCount_can_be_incremented() {
        val formRef = FormRef()

        formRef.formOperationCount.longValue += 1
        assertEquals(1L, formRef.formOperationCount.longValue)

        formRef.formOperationCount.longValue += 1
        assertEquals(2L, formRef.formOperationCount.longValue)
    }

    @Test
    fun formRef_validation_state_can_be_updated() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field"] = false

        assertFalse(formRef.isValidated)

        formRef.formFieldValidationMap["field"] = true

        assertTrue(formRef.isValidated)
    }

    @Test
    fun formRef_handles_field_removal() {
        val formRef = FormRef()
        formRef.formFieldValidationMap["field1"] = true
        formRef.formFieldValidationMap["field2"] = false

        assertFalse(formRef.isValidated)

        formRef.formFieldValidationMap.remove("field2")

        assertTrue(formRef.isValidated)
    }

    @Test
    fun formRef_data_class_equality() {
        val formRef1 = FormRef()
        val formRef2 = FormRef()

        // Both have empty maps, should be equal
        assertEquals(formRef1.formFieldMap, formRef2.formFieldMap)
        assertEquals(formRef1.formFieldValidationMap, formRef2.formFieldValidationMap)
    }

    @Test
    fun formRef_handles_large_number_of_fields() {
        val formRef = FormRef()

        repeat(100) { i ->
            formRef.formFieldValidationMap["field$i"] = true
        }

        assertTrue(formRef.isValidated)

        formRef.formFieldValidationMap["field50"] = false

        assertFalse(formRef.isValidated)
    }
}
