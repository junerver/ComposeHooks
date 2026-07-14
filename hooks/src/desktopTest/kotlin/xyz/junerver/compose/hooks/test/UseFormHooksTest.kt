/*
  Description: Form hook lifecycle and integration tests
  Author: Junerver
  Date: 2026/07/14
  Email: junerver@gmail.com
  Version: v1.0
*/

package xyz.junerver.compose.hooks.test

import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.rememberForm
import xyz.junerver.compose.hooks.rememberFormInstance
import xyz.junerver.compose.hooks.rememberWatch
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.FormItemState
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.Validator
import xyz.junerver.compose.hooks.useform.useForm
import xyz.junerver.compose.hooks.useform.useFormInstance
import xyz.junerver.compose.hooks.useform.useWatch

@Suppress("DEPRECATION")
class UseFormHooksTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun form_hooks_are_stable_and_context_aliases_return_provider_instance() = runComposeUiTest {
        val phase = mutableStateOf(0)
        lateinit var createdByUse: FormInstance
        lateinit var createdByAlias: FormInstance
        lateinit var contextByUse: FormInstance
        lateinit var contextByAlias: FormInstance
        setContent {
            createdByUse = Form.useForm()
            createdByAlias = Form.rememberForm()
            Form(createdByUse) {
                contextByUse = Form.useFormInstance()
                contextByAlias = Form.rememberFormInstance()
                Text("phase=${phase.value}")
            }
        }

        waitForIdle()
        val initialUse = createdByUse
        val initialAlias = createdByAlias
        assertSame(createdByUse, contextByUse)
        assertSame(createdByUse, contextByAlias)

        runOnIdle { phase.value = 1 }
        waitForIdle()

        onNodeWithText("phase=1").assertExists()
        assertSame(initialUse, createdByUse)
        assertSame(initialAlias, createdByAlias)
        assertSame(createdByUse, contextByUse)
        assertSame(createdByUse, contextByAlias)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun watch_hooks_replay_existing_value_and_follow_updates() = runComposeUiTest {
        val showWatchers = mutableStateOf(false)
        lateinit var form: FormInstance
        setContent {
            form = Form.useForm()
            Form(form) {
                FormItemWithState<String>(name = "name") { }
            }
            if (showWatchers.value) {
                val watched = Form.useWatch<String>("name", form)
                val remembered = Form.rememberWatch<String>("name", form)
                Text("watched=${watched.value} remembered=${remembered.value}")
            }
        }

        waitForIdle()
        runOnIdle { form.setFieldValue("name", "before-watch") }
        waitForIdle()
        runOnIdle { showWatchers.value = true }
        waitForIdle()

        onNodeWithText("watched=before-watch remembered=before-watch").assertExists()

        runOnIdle { form.setFieldValue("name", "after-watch") }
        waitForIdle()

        onNodeWithText("watched=after-watch remembered=after-watch").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun watch_rebinds_and_replays_value_when_field_name_changes() = runComposeUiTest {
        val watchedName = mutableStateOf("first")
        lateinit var form: FormInstance
        lateinit var watchedState: State<String?>
        setContent {
            form = Form.useForm()
            Form(form) {
                FormItemWithState<String>(name = "first") { }
                FormItemWithState<String>(name = "second") { }
            }
            watchedState = Form.useWatch(watchedName.value, form)
            Text("${watchedName.value}=${watchedState.value}")
        }

        waitForIdle()
        runOnIdle { form.setFieldsValue("first" to "one", "second" to "two") }
        waitForIdle()
        onNodeWithText("first=one").assertExists()
        val initialState = watchedState

        runOnIdle { watchedName.value = "second" }
        waitForIdle()
        onNodeWithText("second=two").assertExists()
        assertSame(initialState, watchedState)

        runOnIdle { form.setFieldValue("first", "ignored") }
        waitForIdle()
        onNodeWithText("second=two").assertExists()

        runOnIdle { form.setFieldValue("second", "updated") }
        waitForIdle()
        onNodeWithText("second=updated").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun watch_rebinds_and_replays_value_when_form_instance_changes() = runComposeUiTest {
        val useSecondForm = mutableStateOf(false)
        lateinit var firstForm: FormInstance
        lateinit var secondForm: FormInstance
        setContent {
            firstForm = Form.useForm()
            secondForm = Form.useForm()
            Form(firstForm) {
                FormItemWithState<String>(name = "shared") { }
            }
            Form(secondForm) {
                FormItemWithState<String>(name = "shared") { }
            }
            val selectedForm = if (useSecondForm.value) secondForm else firstForm
            val watched = Form.useWatch<String>("shared", selectedForm)
            Text("selected=${if (useSecondForm.value) "second" else "first"} value=${watched.value}")
        }

        waitForIdle()
        runOnIdle {
            firstForm.setFieldValue("shared", "one")
            secondForm.setFieldValue("shared", "two")
        }
        waitForIdle()
        onNodeWithText("selected=first value=one").assertExists()

        runOnIdle { useSecondForm.value = true }
        waitForIdle()
        onNodeWithText("selected=second value=two").assertExists()

        runOnIdle { firstForm.setFieldValue("shared", "ignored") }
        waitForIdle()
        onNodeWithText("selected=second value=two").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun conditional_field_unregisters_and_clears_watcher_when_disposed() = runComposeUiTest {
        val showField = mutableStateOf(true)
        lateinit var form: FormInstance
        setContent {
            form = Form.useForm()
            Form(form) {
                if (showField.value) {
                    FormItemWithState<String>(name = "conditional", Required("required")) { }
                }
            }
            val watched = Form.useWatch<String>("conditional", form)
            Text("watched=${watched.value}")
        }

        waitForIdle()
        runOnIdle { form.setFieldValue("conditional", "value") }
        waitForIdle()
        onNodeWithText("watched=value").assertExists()
        assertEquals(setOf("conditional"), form.getAllFields().keys)

        runOnIdle { showField.value = false }
        waitForIdle()

        onNodeWithText("watched=null").assertExists()
        assertTrue(form.getAllFields().isEmpty())
        assertTrue(form.isValidated())
        assertTrue(form.getAllFieldsErrors().isEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun validator_changes_revalidate_and_clear_stale_errors() = runComposeUiTest {
        val required = mutableStateOf(false)
        lateinit var form: FormInstance
        setContent {
            form = Form.useForm()
            Form(form) {
                val validators: Array<Validator> =
                    if (required.value) arrayOf(Required("required")) else emptyArray()
                FormItemWithState<String>(name = "field", *validators) { field ->
                    Text("valid=${field.isValid} errors=${field.errors.joinToString()}")
                }
            }
        }

        waitForIdle()
        assertTrue(form.isValidated())
        assertTrue(form.getFieldError("field").isEmpty())

        runOnIdle { required.value = true }
        waitForIdle()

        onNodeWithText("valid=false errors=required").assertExists()
        assertFalse(form.isValidated())
        assertEquals(listOf("required"), form.getFieldError("field"))

        runOnIdle { required.value = false }
        waitForIdle()

        onNodeWithText("valid=true errors=").assertExists()
        assertTrue(form.isValidated())
        assertTrue(form.getFieldError("field").isEmpty())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun touched_and_dirty_distinguish_programmatic_user_and_reset_changes() = runComposeUiTest {
        lateinit var form: FormInstance
        lateinit var field: FormItemState<String>
        setContent {
            form = Form.useForm()
            Form(form) {
                FormItemWithState<String>(name = "name") { currentField ->
                    field = currentField
                    Text(
                        "value=${currentField.value.value} touched=${currentField.isTouched} dirty=${currentField.isDirty}",
                    )
                }
            }
        }

        waitForIdle()
        assertFalse(form.isTouched("name"))
        assertFalse(form.isDirty("name"))

        runOnIdle { form.setFieldValue("name", "programmatic") }
        waitForIdle()

        assertFalse(form.isTouched("name"))
        assertTrue(form.isDirty("name"))
        assertFalse(field.isTouched)
        assertTrue(field.isDirty)

        runOnIdle { form.resetFields("name" to "seed") }
        waitForIdle()

        onNodeWithText("value=seed touched=false dirty=false").assertExists()
        assertFalse(form.isTouched("name"))
        assertFalse(form.isDirty("name"))

        runOnIdle { form.markAsTouched("name") }
        waitForIdle()
        onNodeWithText("value=seed touched=true dirty=false").assertExists()

        runOnIdle { form.resetFields("name" to "reset") }
        waitForIdle()
        onNodeWithText("value=reset touched=false dirty=false").assertExists()

        runOnIdle { field.onValueChange("user") }
        waitForIdle()

        onNodeWithText("value=user touched=true dirty=true").assertExists()
        assertTrue(form.isTouched("name"))
        assertTrue(form.isDirty("name"))
    }
}
