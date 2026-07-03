package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.ValidationTrigger

@Suppress("DEPRECATION")
class UseFormIntegrationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_set_and_get_field_values_through_registered_items() = runComposeUiTest {
        lateinit var formInstance: FormInstance
        var allFields: Map<String, Any?> = emptyMap()

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "username") { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("username" to "testUser")
        waitForIdle()
        allFields = formInstance.getAllFields()

        assertEquals("testUser", allFields["username"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_reset_fields_clears_registered_values() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "field1") { }
                FormItemWithState<String>(name = "field2") { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("field1" to "a", "field2" to "b")
        waitForIdle()
        formInstance.resetFields()
        waitForIdle()

        val fields = formInstance.getAllFields()
        assertEquals(null, fields["field1"])
        assertEquals(null, fields["field2"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_reset_fields_can_seed_new_values() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "name") { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("name" to "old")
        waitForIdle()
        formInstance.resetFields("name" to "new")
        waitForIdle()

        assertEquals("new", formInstance.getAllFields()["name"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_submit_reports_success_when_all_fields_are_valid() = runComposeUiTest {
        lateinit var formInstance: FormInstance
        var submittedValues: Map<String, Any?>? = null

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("name" to "valid")
        waitForIdle()
        formInstance.submit(onSuccess = { submittedValues = it })
        waitForIdle()

        assertNotNull(submittedValues)
        assertEquals("valid", submittedValues!!["name"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_submit_reports_errors_when_validation_fails() = runComposeUiTest {
        lateinit var formInstance: FormInstance
        var errors: Map<String, List<String>>? = null

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "email", Required(), Email()) { }
            }
        }

        waitForIdle()
        formInstance.submit(onSuccess = {}, onError = { errors = it })
        waitForIdle()

        assertNotNull(errors)
        assertTrue(errors!!.containsKey("email"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun required_field_validation_updates_after_setting_value() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "field", Required("required")) { }
            }
        }

        waitForIdle()
        assertFalse(formInstance.isValidated())
        formInstance.setFieldsValue("field" to "value")
        waitForIdle()
        assertTrue(formInstance.isValidated())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun email_field_collects_errors_for_invalid_input() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "email", Email("invalid email")) { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("email" to "not-an-email")
        waitForIdle()

        assertTrue(formInstance.getFieldError("email").isNotEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun email_field_has_no_errors_for_valid_input() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "email", Email()) { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("email" to "test@example.com")
        waitForIdle()

        assertTrue(formInstance.getFieldError("email").isEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mark_as_touched_updates_single_field_touch_state() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "field") { }
            }
        }

        waitForIdle()
        assertFalse(formInstance.isTouched("field"))
        formInstance.markAsTouched("field")
        waitForIdle()
        assertTrue(formInstance.isTouched("field"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mark_all_as_touched_updates_all_registered_fields() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "f1") { }
                FormItemWithState<String>(name = "f2") { }
            }
        }

        waitForIdle()
        formInstance.markAllAsTouched()
        waitForIdle()

        assertTrue(formInstance.isTouched("f1"))
        assertTrue(formInstance.isTouched("f2"))
        assertTrue(formInstance.getTouchedFields().contains("f1"))
        assertTrue(formInstance.getTouchedFields().contains("f2"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun validate_field_checks_single_registered_item() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }

        waitForIdle()
        assertFalse(formInstance.validateField("name"))
        formInstance.setFieldsValue("name" to "value")
        waitForIdle()
        assertTrue(formInstance.validateField("name"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun validate_fields_checks_all_registered_items() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "a", Required()) { }
                FormItemWithState<String>(name = "b", Required()) { }
            }
        }

        waitForIdle()
        assertFalse(formInstance.validateFields())
        formInstance.setFieldsValue("a" to "1", "b" to "2")
        waitForIdle()
        assertTrue(formInstance.validateFields())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun get_all_field_errors_collects_validation_messages() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "email", Required(), Email()) { }
            }
        }

        waitForIdle()
        assertTrue(formInstance.getAllFieldsErrors().containsKey("email"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_submit_without_callbacks_uses_form_on_submit_handler() = runComposeUiTest {
        lateinit var formInstance: FormInstance
        var submittedValues: Map<String, Any?>? = null

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance, onSubmit = { submittedValues = it }) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }

        waitForIdle()
        formInstance.setFieldsValue("name" to "test")
        waitForIdle()
        formInstance.submit()
        waitForIdle()

        assertNotNull(submittedValues)
        assertEquals("test", submittedValues!!["name"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_fields_default_to_on_change_validation_trigger() = runComposeUiTest {
        lateinit var formInstance: FormInstance

        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance) {
                FormItemWithState<String>(name = "field") { }
            }
        }

        waitForIdle()
        assertEquals(ValidationTrigger.OnChange, formInstance.getFieldValidationTrigger("field"))
    }
}
