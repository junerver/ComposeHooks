package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.FormRef
import xyz.junerver.compose.hooks.useform.ValidationTrigger

/*
  Description: TDD tests for validation trigger control in useForm
  Author: Junerver
  Date: 2026/1/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormValidationTriggerTest {

    private fun createInitializedFormInstance(): Pair<FormInstance, FormRef> {
        val formInstance = FormInstance()
        val formRef = FormRef()
        val ref = MutableRef(formRef)
        val field = FormInstance::class.java.getDeclaredField("formRef")
        field.isAccessible = true
        field.set(formInstance, ref)
        return formInstance to formRef
    }

    // ==================== ValidationTrigger Enum Tests ====================

    @Test
    fun validationTrigger_has_onChange_value() {
        assertEquals("OnChange", ValidationTrigger.OnChange.name)
    }

    @Test
    fun validationTrigger_has_onBlur_value() {
        assertEquals("OnBlur", ValidationTrigger.OnBlur.name)
    }

    @Test
    fun validationTrigger_has_onSubmit_value() {
        assertEquals("OnSubmit", ValidationTrigger.OnSubmit.name)
    }

    @Test
    fun validationTrigger_onChange_is_default() {
        // Default should be OnChange for backward compatibility
        val defaultTrigger = ValidationTrigger.OnChange
        assertEquals(ValidationTrigger.OnChange, defaultTrigger)
    }

    // ==================== FormRef Validation Trigger Storage Tests ====================

    @Test
    fun formRef_validationTriggerMap_initializes_empty() {
        val formRef = FormRef()
        assertTrue(formRef.formFieldValidationTriggerMap.isEmpty())
    }

    @Test
    fun formRef_validationTriggerMap_can_store_trigger() {
        val formRef = FormRef()
        formRef.formFieldValidationTriggerMap["email"] = ValidationTrigger.OnBlur
        assertEquals(ValidationTrigger.OnBlur, formRef.formFieldValidationTriggerMap["email"])
    }

    @Test
    fun formRef_pendingValidationMap_initializes_empty() {
        val formRef = FormRef()
        assertTrue(formRef.formFieldPendingValidationMap.isEmpty())
    }

    // ==================== FormInstance validateField Tests ====================

    @Test
    fun formInstance_validateField_returns_true_for_valid_field() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("test@example.com")
        formRef.formFieldValidationMap["email"] = true

        val result = formInstance.validateField("email")

        assertTrue(result)
    }

    @Test
    fun formInstance_validateField_returns_false_for_invalid_field() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("invalid")
        formRef.formFieldValidationMap["email"] = false

        val result = formInstance.validateField("email")

        assertFalse(result)
    }

    @Test
    fun formInstance_validateField_returns_true_for_non_existing_field() {
        val (formInstance, _) = createInitializedFormInstance()

        val result = formInstance.validateField("nonExistent")

        assertTrue(result) // Non-existing fields are considered valid
    }

    @Test
    fun formInstance_validateField_triggers_pending_validation() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("test")
        formRef.formFieldPendingValidationMap["email"] = true

        formInstance.validateField("email")

        // After validation, pending should be cleared
        assertFalse(formRef.formFieldPendingValidationMap["email"] == true)
    }

    // ==================== FormInstance validateFields Tests ====================

    @Test
    fun formInstance_validateFields_returns_true_when_all_valid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("john@test.com")
        formRef.formFieldValidationMap["name"] = true
        formRef.formFieldValidationMap["email"] = true

        val result = formInstance.validateFields()

        assertTrue(result)
    }

    @Test
    fun formInstance_validateFields_returns_false_when_any_invalid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("invalid")
        formRef.formFieldValidationMap["name"] = true
        formRef.formFieldValidationMap["email"] = false

        val result = formInstance.validateFields()

        assertFalse(result)
    }

    @Test
    fun formInstance_validateFields_returns_true_when_no_fields() {
        val (formInstance, _) = createInitializedFormInstance()

        val result = formInstance.validateFields()

        assertTrue(result)
    }

    @Test
    fun formInstance_validateFields_marks_all_as_touched() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("john@test.com")
        formRef.formFieldValidationMap["name"] = true
        formRef.formFieldValidationMap["email"] = true

        formInstance.validateFields()

        assertTrue(formRef.formFieldTouchedMap["name"] == true)
        assertTrue(formRef.formFieldTouchedMap["email"] == true)
    }

    // ==================== FormInstance getFieldValidationTrigger Tests ====================

    @Test
    fun formInstance_getFieldValidationTrigger_returns_onChange_by_default() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        val trigger = formInstance.getFieldValidationTrigger("email")

        assertEquals(ValidationTrigger.OnChange, trigger)
    }

    @Test
    fun formInstance_getFieldValidationTrigger_returns_stored_trigger() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)
        formRef.formFieldValidationTriggerMap["email"] = ValidationTrigger.OnBlur

        val trigger = formInstance.getFieldValidationTrigger("email")

        assertEquals(ValidationTrigger.OnBlur, trigger)
    }
}
