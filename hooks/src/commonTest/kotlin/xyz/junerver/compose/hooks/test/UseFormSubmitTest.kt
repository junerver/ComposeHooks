package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.FormRef

/*
  Description: TDD tests for form submission handling in useForm
  Author: Junerver
  Date: 2026/1/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormSubmitTest {
    private fun createInitializedFormInstance(): Pair<FormInstance, FormRef> {
        val formInstance = FormInstance()
        val formRef = FormRef()
        val ref = MutableRef(formRef)
        val field = FormInstance::class.java.getDeclaredField("formRef")
        field.isAccessible = true
        field.set(formInstance, ref)
        return formInstance to formRef
    }

    // ==================== FormInstance submit with callbacks Tests ====================

    @Test
    fun formInstance_submit_calls_onSuccess_when_valid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("john@test.com")
        formRef.formFieldValidationMap["name"] = true
        formRef.formFieldValidationMap["email"] = true

        var successCalled = false
        var receivedValues: Map<String, Any?>? = null

        formInstance.submit(
            onSuccess = { values ->
                successCalled = true
                receivedValues = values
            },
        )

        assertTrue(successCalled)
        assertEquals("John", receivedValues?.get("name"))
        assertEquals("john@test.com", receivedValues?.get("email"))
    }

    @Test
    fun formInstance_submit_does_not_call_onSuccess_when_invalid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("invalid")
        formRef.formFieldValidationMap["email"] = false

        var successCalled = false

        formInstance.submit(
            onSuccess = { successCalled = true },
        )

        assertFalse(successCalled)
    }

    @Test
    fun formInstance_submit_calls_onError_when_invalid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("invalid")
        formRef.formFieldValidationMap["email"] = false
        formRef.formFieldErrorMessagesMap["email"] = listOf("Invalid email")

        var errorCalled = false
        var receivedErrors: Map<String, List<String>>? = null

        formInstance.submit(
            onSuccess = { },
            onError = { errors ->
                errorCalled = true
                receivedErrors = errors
            },
        )

        assertTrue(errorCalled)
        assertEquals(listOf("Invalid email"), receivedErrors?.get("email"))
    }

    @Test
    fun formInstance_submit_does_not_call_onError_when_valid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("test@example.com")
        formRef.formFieldValidationMap["email"] = true

        var errorCalled = false

        formInstance.submit(
            onSuccess = { },
            onError = { errorCalled = true },
        )

        assertFalse(errorCalled)
    }

    @Test
    fun formInstance_submit_marks_all_fields_as_touched() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("john@test.com")
        formRef.formFieldValidationMap["name"] = true
        formRef.formFieldValidationMap["email"] = true

        formInstance.submit(onSuccess = { })

        assertTrue(formRef.formFieldTouchedMap["name"] == true)
        assertTrue(formRef.formFieldTouchedMap["email"] == true)
    }

    @Test
    fun formInstance_submit_with_null_onError_does_not_throw() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("invalid")
        formRef.formFieldValidationMap["email"] = false

        // Should not throw
        formInstance.submit(
            onSuccess = { },
            onError = null,
        )
    }

    // ==================== FormInstance getAllFieldsErrors Tests ====================

    @Test
    fun formInstance_getAllFieldsErrors_returns_all_errors() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldErrorMessagesMap["name"] = listOf("Name required")
        formRef.formFieldErrorMessagesMap["email"] = listOf("Invalid email", "Email required")

        val errors = formInstance.getAllFieldsErrors()

        assertEquals(listOf("Name required"), errors["name"])
        assertEquals(listOf("Invalid email", "Email required"), errors["email"])
    }

    @Test
    fun formInstance_getAllFieldsErrors_returns_empty_map_when_no_errors() {
        val (formInstance, _) = createInitializedFormInstance()

        val errors = formInstance.getAllFieldsErrors()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun formInstance_getAllFieldsErrors_excludes_empty_error_lists() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldErrorMessagesMap["name"] = emptyList()
        formRef.formFieldErrorMessagesMap["email"] = listOf("Invalid email")

        val errors = formInstance.getAllFieldsErrors()

        assertFalse(errors.containsKey("name"))
        assertEquals(listOf("Invalid email"), errors["email"])
    }

    // ==================== FormRef onSubmitCallback Tests ====================

    @Test
    fun formRef_onSubmitCallback_initializes_null() {
        val formRef = FormRef()
        assertEquals(null, formRef.onSubmitCallback)
    }

    @Test
    fun formRef_onSubmitCallback_can_be_set() {
        val formRef = FormRef()
        val callback: (Map<String, Any?>) -> Unit = { }
        formRef.onSubmitCallback = callback
        assertEquals(callback, formRef.onSubmitCallback)
    }

    // ==================== FormInstance submit() no-arg Tests ====================

    @Test
    fun formInstance_submit_no_arg_triggers_onSubmitCallback_when_valid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldValidationMap["name"] = true

        var callbackTriggered = false
        formRef.onSubmitCallback = { callbackTriggered = true }

        formInstance.submit()

        assertTrue(callbackTriggered)
    }

    @Test
    fun formInstance_submit_no_arg_does_not_trigger_callback_when_invalid() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>(null)
        formRef.formFieldValidationMap["name"] = false

        var callbackTriggered = false
        formRef.onSubmitCallback = { callbackTriggered = true }

        formInstance.submit()

        assertFalse(callbackTriggered)
    }

    @Test
    fun formInstance_submit_no_arg_does_nothing_when_no_callback() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldValidationMap["name"] = true

        // Should not throw
        formInstance.submit()
    }
}
