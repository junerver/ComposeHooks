package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.MutableRef
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.FormRef

/*
  Description: FormInstance comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormInstanceTest {
    private fun createInitializedFormInstance(): Pair<FormInstance, FormRef> {
        val formInstance = FormInstance()
        val formRef = FormRef()
        val ref = MutableRef(formRef)
        // Access internal formRef field
        val field = FormInstance::class.java.getDeclaredField("formRef")
        field.isAccessible = true
        field.set(formInstance, ref)
        return formInstance to formRef
    }

    @Test
    fun formInstance_throws_when_not_initialized() {
        val formInstance = FormInstance()

        assertFailsWith<IllegalArgumentException> {
            formInstance.getAllFields()
        }
    }

    @Test
    fun formInstance_getAllFields_returns_empty_map_when_no_fields() {
        val (formInstance, _) = createInitializedFormInstance()

        val fields = formInstance.getAllFields()

        assertTrue(fields.isEmpty())
    }

    @Test
    fun formInstance_getAllFields_returns_all_field_values() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("John")
        formRef.formFieldMap["age"] = mutableStateOf<Any?>(25)

        val fields = formInstance.getAllFields()

        assertEquals(2, fields.size)
        assertEquals("John", fields["name"])
        assertEquals(25, fields["age"])
    }

    @Test
    fun formInstance_isValidated_returns_formRef_validation_state() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldValidationMap["field1"] = true

        assertTrue(formInstance.isValidated())
    }

    @Test
    fun formInstance_setFieldsValue_with_map_updates_existing_fields() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>(null)
        val ageState = mutableStateOf<Any?>(null)
        formRef.formFieldMap["name"] = nameState
        formRef.formFieldMap["age"] = ageState

        formInstance.setFieldsValue(mapOf("name" to "Alice", "age" to 30))

        assertEquals("Alice", nameState.value)
        assertEquals(30, ageState.value)
    }

    @Test
    fun formInstance_setFieldsValue_ignores_non_existing_fields() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>(null)
        formRef.formFieldMap["name"] = nameState

        formInstance.setFieldsValue(mapOf("name" to "Bob", "nonExistent" to "value"))

        assertEquals("Bob", nameState.value)
        assertEquals(1, formRef.formFieldMap.size)
    }

    @Test
    fun formInstance_setFieldsValue_with_vararg_pairs() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>(null)
        val emailState = mutableStateOf<Any?>(null)
        formRef.formFieldMap["name"] = nameState
        formRef.formFieldMap["email"] = emailState

        formInstance.setFieldsValue("name" to "Charlie", "email" to "charlie@test.com")

        assertEquals("Charlie", nameState.value)
        assertEquals("charlie@test.com", emailState.value)
    }

    @Test
    fun formInstance_setFieldValue_updates_single_field() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>(null)
        formRef.formFieldMap["name"] = nameState

        formInstance.setFieldValue("name", "David")

        assertEquals("David", nameState.value)
    }

    @Test
    fun formInstance_setFieldValue_with_pair() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>(null)
        formRef.formFieldMap["name"] = nameState

        formInstance.setFieldValue("name" to "Eve")

        assertEquals("Eve", nameState.value)
    }

    @Test
    fun formInstance_setFieldValue_ignores_non_existing_field() {
        val (formInstance, formRef) = createInitializedFormInstance()

        // Should not throw
        formInstance.setFieldValue("nonExistent", "value")

        assertTrue(formRef.formFieldMap.isEmpty())
    }

    @Test
    fun formInstance_setFieldValue_allows_null_value() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>("initial")
        formRef.formFieldMap["name"] = nameState

        formInstance.setFieldValue("name", null)

        assertEquals(null, nameState.value)
    }

    @Test
    fun formInstance_getFieldError_returns_errors_for_field() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldErrorMessagesMap["email"] = listOf("Invalid email", "Email required")

        val errors = formInstance.getFieldError("email")

        assertEquals(listOf("Invalid email", "Email required"), errors)
    }

    @Test
    fun formInstance_getFieldError_returns_empty_list_for_valid_field() {
        val (formInstance, _) = createInitializedFormInstance()

        val errors = formInstance.getFieldError("nonExistent")

        assertTrue(errors.isEmpty())
    }

    @Test
    fun formInstance_resetFields_clears_all_field_values() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>("John")
        val ageState = mutableStateOf<Any?>(25)
        formRef.formFieldMap["name"] = nameState
        formRef.formFieldMap["age"] = ageState

        formInstance.resetFields()

        assertEquals(null, nameState.value)
        assertEquals(null, ageState.value)
    }

    @Test
    fun formInstance_resetFields_with_new_values() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>("John")
        val ageState = mutableStateOf<Any?>(25)
        formRef.formFieldMap["name"] = nameState
        formRef.formFieldMap["age"] = ageState

        formInstance.resetFields(mapOf("name" to "Default"))

        assertEquals("Default", nameState.value)
        assertEquals(null, ageState.value)
    }

    @Test
    fun formInstance_resetFields_with_vararg_pairs() {
        val (formInstance, formRef) = createInitializedFormInstance()
        val nameState = mutableStateOf<Any?>("John")
        val emailState = mutableStateOf<Any?>("john@test.com")
        formRef.formFieldMap["name"] = nameState
        formRef.formFieldMap["email"] = emailState

        formInstance.resetFields("name" to "Reset", "email" to "reset@test.com")

        assertEquals("Reset", nameState.value)
        assertEquals("reset@test.com", emailState.value)
    }

    @Test
    fun formInstance_resetFields_throws_when_not_initialized() {
        val formInstance = FormInstance()

        assertFailsWith<IllegalArgumentException> {
            formInstance.resetFields()
        }
    }
}
