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
  Description: TDD tests for touched/dirty state tracking in useForm
  Author: Junerver
  Date: 2026/1/26
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormTouchedDirtyTest {
    private fun createInitializedFormInstance(): Pair<FormInstance, FormRef> {
        val formInstance = FormInstance()
        val formRef = FormRef()
        val ref = MutableRef(formRef)
        formInstance.formRef = ref
        return formInstance to formRef
    }

    // ==================== FormRef Tests ====================

    @Test
    fun formRef_touchedMap_initializes_empty() {
        val formRef = FormRef()
        assertTrue(formRef.formFieldTouchedMap.isEmpty())
    }

    @Test
    fun formRef_dirtyMap_initializes_empty() {
        val formRef = FormRef()
        assertTrue(formRef.formFieldDirtyMap.isEmpty())
    }

    @Test
    fun formRef_initialValueMap_initializes_empty() {
        val formRef = FormRef()
        assertTrue(formRef.formFieldInitialValueMap.isEmpty())
    }

    @Test
    fun formRef_touchedMap_can_store_touched_state() {
        val formRef = FormRef()
        formRef.formFieldTouchedMap["email"] = true
        assertTrue(formRef.formFieldTouchedMap["email"] == true)
    }

    @Test
    fun formRef_dirtyMap_can_store_dirty_state() {
        val formRef = FormRef()
        formRef.formFieldDirtyMap["email"] = true
        assertTrue(formRef.formFieldDirtyMap["email"] == true)
    }

    @Test
    fun formRef_initialValueMap_can_store_initial_values() {
        val formRef = FormRef()
        formRef.formFieldInitialValueMap["name"] = "John"
        assertEquals("John", formRef.formFieldInitialValueMap["name"])
    }

    // ==================== FormInstance isTouched Tests ====================

    @Test
    fun formInstance_isTouched_returns_false_initially() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        assertFalse(formInstance.isTouched("email"))
    }

    @Test
    fun formInstance_isTouched_returns_true_when_field_is_touched() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)
        formRef.formFieldTouchedMap["email"] = true

        assertTrue(formInstance.isTouched("email"))
    }

    @Test
    fun formInstance_isTouched_returns_false_for_non_existing_field() {
        val (formInstance, _) = createInitializedFormInstance()

        assertFalse(formInstance.isTouched("nonExistent"))
    }

    // ==================== FormInstance isDirty Tests ====================

    @Test
    fun formInstance_isDirty_returns_false_initially() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        assertFalse(formInstance.isDirty("email"))
    }

    @Test
    fun formInstance_isDirty_returns_true_when_field_is_dirty() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("changed")
        formRef.formFieldDirtyMap["email"] = true

        assertTrue(formInstance.isDirty("email"))
    }

    @Test
    fun formInstance_isDirty_returns_false_for_non_existing_field() {
        val (formInstance, _) = createInitializedFormInstance()

        assertFalse(formInstance.isDirty("nonExistent"))
    }

    // ==================== FormInstance markAsTouched Tests ====================

    @Test
    fun formInstance_markAsTouched_sets_touched_true() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        formInstance.markAsTouched("email")

        assertTrue(formRef.formFieldTouchedMap["email"] == true)
    }

    @Test
    fun formInstance_markAsTouched_ignores_non_existing_field() {
        val (formInstance, formRef) = createInitializedFormInstance()

        formInstance.markAsTouched("nonExistent")

        assertTrue(formRef.formFieldTouchedMap.isEmpty())
    }

    @Test
    fun formInstance_markAllAsTouched_marks_all_fields() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>(null)
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)
        formRef.formFieldMap["phone"] = mutableStateOf<Any?>(null)

        formInstance.markAllAsTouched()

        assertTrue(formRef.formFieldTouchedMap["name"] == true)
        assertTrue(formRef.formFieldTouchedMap["email"] == true)
        assertTrue(formRef.formFieldTouchedMap["phone"] == true)
    }

    // ==================== FormInstance getTouchedFields Tests ====================

    @Test
    fun formInstance_getTouchedFields_returns_empty_set_initially() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        val touched = formInstance.getTouchedFields()

        assertTrue(touched.isEmpty())
    }

    @Test
    fun formInstance_getTouchedFields_returns_touched_field_names() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>(null)
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)
        formRef.formFieldTouchedMap["email"] = true

        val touched = formInstance.getTouchedFields()

        assertEquals(setOf("email"), touched)
    }

    // ==================== FormInstance getDirtyFields Tests ====================

    @Test
    fun formInstance_getDirtyFields_returns_empty_set_initially() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>(null)

        val dirty = formInstance.getDirtyFields()

        assertTrue(dirty.isEmpty())
    }

    @Test
    fun formInstance_getDirtyFields_returns_dirty_field_names() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>(null)
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("changed")
        formRef.formFieldDirtyMap["email"] = true

        val dirty = formInstance.getDirtyFields()

        assertEquals(setOf("email"), dirty)
    }

    // ==================== FormInstance resetFields clears touched/dirty ====================

    @Test
    fun formInstance_resetFields_clears_touched_state() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("test")
        formRef.formFieldTouchedMap["email"] = true

        formInstance.resetFields()

        assertTrue(formRef.formFieldTouchedMap.isEmpty())
    }

    @Test
    fun formInstance_resetFields_clears_dirty_state() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["email"] = mutableStateOf<Any?>("test")
        formRef.formFieldDirtyMap["email"] = true

        formInstance.resetFields()

        assertTrue(formRef.formFieldDirtyMap.isEmpty())
    }

    @Test
    fun formInstance_resetFields_updates_initial_values() {
        val (formInstance, formRef) = createInitializedFormInstance()
        formRef.formFieldMap["name"] = mutableStateOf<Any?>("old")
        formRef.formFieldInitialValueMap["name"] = "old"

        formInstance.resetFields(mapOf("name" to "new"))

        assertEquals("new", formRef.formFieldInitialValueMap["name"])
    }
}