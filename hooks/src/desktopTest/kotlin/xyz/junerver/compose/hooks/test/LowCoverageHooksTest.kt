package xyz.junerver.compose.hooks.test

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks.useredux.createStore
import xyz.junerver.compose.hooks.useredux.combineStores
import xyz.junerver.compose.hooks.useredux.plus
import xyz.junerver.compose.hooks.useredux.ReduxProvider
import xyz.junerver.compose.hooks.useredux.useSelector
import xyz.junerver.compose.hooks.useredux.useDispatch
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Phone
import xyz.junerver.compose.hooks.useform.Mobile
import xyz.junerver.compose.hooks.useform.Regex
import xyz.junerver.compose.hooks.useform.CustomValidator
import xyz.junerver.compose.hooks.useform.ValidationTrigger
import xyz.junerver.compose.hooks.useform.validateField
import xyz.junerver.compose.hooks.usestate.useStateImpl
import xyz.junerver.compose.hooks.usestate._useStateImpl
import xyz.junerver.compose.hooks.useevent.useEventSubscribeImpl
import xyz.junerver.compose.hooks.useevent.useEventPublishImpl
import xyz.junerver.compose.hooks.usethrottle.useThrottleImpl
import xyz.junerver.compose.hooks.usedebounce.useDebounceImpl
import xyz.junerver.compose.hooks.usetimeago.formatTimeAgo
import xyz.junerver.compose.hooks.usetimeago.useTimeAgoImpl
import xyz.junerver.compose.hooks.usetimeago.DefaultEnglishTimeAgoMessages
import xyz.junerver.compose.hooks.usetimeago.DefaultChineseTimeAgoMessages
import xyz.junerver.compose.hooks.usetimeago.FormatTimeAgoOptions
import xyz.junerver.compose.hooks.usetimeago.UseTimeAgoOptions
import xyz.junerver.compose.hooks.usepersistent.usePersistentImpl
import xyz.junerver.compose.hooks.usepersistent.PersistentHolder

/*
  Description: Tests for low-coverage hooks packages
  Author: MiMoCode
  Date: 2026/6/26
  Email: junerver@gmail.com
  Version: v1.0
*/

// region Test action types for Redux

private sealed class LCCounterAction {
    data object Increment : LCCounterAction()
    data object Decrement : LCCounterAction()
    data class Set(val value: Int) : LCCounterAction()
}

private val lcCounterReducer: (Int, LCCounterAction) -> Int = { state, action ->
    when (action) {
        LCCounterAction.Increment -> state + 1
        LCCounterAction.Decrement -> state - 1
        is LCCounterAction.Set -> action.value
    }
}

private sealed class LCTodoAction {
    data class Add(val text: String) : LCTodoAction()
    data class Remove(val index: Int) : LCTodoAction()
}

private data class LCTodoState(val items: List<String> = emptyList())

private val lcTodoReducer: (LCTodoState, LCTodoAction) -> LCTodoState = { state, action ->
    when (action) {
        is LCTodoAction.Add -> state.copy(items = state.items + action.text)
        is LCTodoAction.Remove -> state.copy(items = state.items.toMutableList().apply { removeAt(action.index) })
    }
}

// endregion

@OptIn(ExperimentalTestApi::class)
class LowCoverageHooksTest {

    // region usestate

    @Test
    fun useStateImplReturnsCorrectIntValue() = runComposeUiTest {
        var result: MutableState<Int>? = null
        setContent {
            result = useStateImpl(42)
        }
        waitForIdle()
        assertEquals(42, result!!.value)
    }

    @Test
    fun useStateImplReturnsCorrectFloatValue() = runComposeUiTest {
        var result: MutableState<Float>? = null
        setContent {
            result = useStateImpl(3.14f)
        }
        waitForIdle()
        assertEquals(3.14f, result!!.value)
    }

    @Test
    fun useStateImplReturnsCorrectDoubleValue() = runComposeUiTest {
        var result: MutableState<Double>? = null
        setContent {
            result = useStateImpl(2.718)
        }
        waitForIdle()
        assertEquals(2.718, result!!.value)
    }

    @Test
    fun useStateImplReturnsCorrectLongValue() = runComposeUiTest {
        var result: MutableState<Long>? = null
        setContent {
            result = useStateImpl(100L)
        }
        waitForIdle()
        assertEquals(100L, result!!.value)
    }

    @Test
    fun useStateImplReturnsCorrectStringValue() = runComposeUiTest {
        var result: MutableState<String>? = null
        setContent {
            result = useStateImpl("hello")
        }
        waitForIdle()
        assertEquals("hello", result!!.value)
    }

    @Test
    fun useStateImplReturnsCorrectBooleanValue() = runComposeUiTest {
        var result: MutableState<Boolean>? = null
        setContent {
            result = useStateImpl(true)
        }
        waitForIdle()
        assertTrue(result!!.value)
    }

    @Test
    fun useStateImplCanBeUpdated() = runComposeUiTest {
        var state: MutableState<Int>? = null
        setContent {
            state = useStateImpl(0)
        }
        waitForIdle()
        assertEquals(0, state!!.value)
        state!!.value = 10
        waitForIdle()
        assertEquals(10, state!!.value)
    }

    @Test
    fun useStateImplIntCanBeUpdated() = runComposeUiTest {
        var state: MutableState<Int>? = null
        setContent {
            state = useStateImpl(5)
        }
        waitForIdle()
        assertEquals(5, state!!.value)
        state!!.value = 20
        waitForIdle()
        assertEquals(20, state!!.value)
    }

    @Test
    fun useStateImplFloatCanBeUpdated() = runComposeUiTest {
        var state: MutableState<Float>? = null
        setContent {
            state = useStateImpl(1.0f)
        }
        waitForIdle()
        assertEquals(1.0f, state!!.value)
        state!!.value = 9.9f
        waitForIdle()
        assertEquals(9.9f, state!!.value)
    }

    @Test
    fun useStateImplDoubleCanBeUpdated() = runComposeUiTest {
        var state: MutableState<Double>? = null
        setContent {
            state = useStateImpl(1.0)
        }
        waitForIdle()
        assertEquals(1.0, state!!.value)
        state!!.value = 9.9
        waitForIdle()
        assertEquals(9.9, state!!.value)
    }

    @Test
    fun useStateImplLongCanBeUpdated() = runComposeUiTest {
        var state: MutableState<Long>? = null
        setContent {
            state = useStateImpl(1L)
        }
        waitForIdle()
        assertEquals(1L, state!!.value)
        state!!.value = 999L
        waitForIdle()
        assertEquals(999L, state!!.value)
    }

    @Test
    fun usePrivateStateImplWithNull() = runComposeUiTest {
        var result: MutableState<String?>? = null
        setContent {
            result = _useStateImpl<String?>(null)
        }
        waitForIdle()
        assertEquals(null, result!!.value)
    }

    @Test
    fun usePrivateStateImplWithValue() = runComposeUiTest {
        var result: MutableState<String?>? = null
        setContent {
            result = _useStateImpl<String?>("test")
        }
        waitForIdle()
        assertEquals("test", result!!.value)
    }

    @Test
    fun usePrivateStateImplCanBeUpdated() = runComposeUiTest {
        var result: MutableState<String?>? = null
        setContent {
            result = _useStateImpl<String?>("initial")
        }
        waitForIdle()
        assertEquals("initial", result!!.value)
        result!!.value = "updated"
        waitForIdle()
        assertEquals("updated", result!!.value)
    }

    @Test
    fun usePrivateStateImplSetToNull() = runComposeUiTest {
        var result: MutableState<String?>? = null
        setContent {
            result = _useStateImpl<String?>("value")
        }
        waitForIdle()
        assertEquals("value", result!!.value)
        result!!.value = null
        waitForIdle()
        assertEquals(null, result!!.value)
    }

    // endregion

    // region useevent

    @Test
    fun eventPublishAndSubscribe() = runComposeUiTest {
        var received: String? = null
        var publishFn: ((String) -> Unit)? = null
        setContent {
            useEventSubscribeImpl<String> { received = it }
            publishFn = useEventPublishImpl()
        }
        waitForIdle()
        publishFn!!.invoke("hello")
        waitForIdle()
        assertEquals("hello", received)
    }

    @Test
    fun eventPublishMultipleMessages() = runComposeUiTest {
        val received = mutableListOf<String>()
        var publishFn: ((String) -> Unit)? = null
        setContent {
            useEventSubscribeImpl<String> { received.add(it) }
            publishFn = useEventPublishImpl()
        }
        waitForIdle()
        publishFn!!.invoke("one")
        waitForIdle()
        publishFn!!.invoke("two")
        waitForIdle()
        publishFn!!.invoke("three")
        waitForIdle()
        assertEquals(listOf("one", "two", "three"), received)
    }

    @Test
    fun eventDifferentTypes() = runComposeUiTest {
        var stringReceived: String? = null
        var intReceived: Int? = null
        var publishString: ((String) -> Unit)? = null
        var publishInt: ((Int) -> Unit)? = null
        setContent {
            useEventSubscribeImpl<String> { stringReceived = it }
            useEventSubscribeImpl<Int> { intReceived = it }
            publishString = useEventPublishImpl()
            publishInt = useEventPublishImpl()
        }
        waitForIdle()
        publishString!!.invoke("test")
        publishInt!!.invoke(42)
        waitForIdle()
        assertEquals("test", stringReceived)
        assertEquals(42, intReceived)
    }

    // endregion

    // region useform

    @Test
    fun formInstanceCreation() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
        }
        waitForIdle()
        assertNotNull(formInstance)
    }

    @Test
    fun formInstanceRequiresFormRef() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
        }
        waitForIdle()
        assertFailsWith<IllegalArgumentException> {
            formInstance!!.getAllFields()
        }
    }

    @Test
    fun formSetAndGetFieldValues() = runComposeUiTest {
        var formInstance: FormInstance? = null
        var allFields: Map<String, Any?> = emptyMap()
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "username") { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("username" to "testUser")
        waitForIdle()
        allFields = formInstance!!.getAllFields()
        assertEquals("testUser", allFields["username"])
    }

    @Test
    fun formResetFields() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "field1") { }
                FormItemWithState<String>(name = "field2") { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("field1" to "a", "field2" to "b")
        waitForIdle()
        formInstance!!.resetFields()
        waitForIdle()
        val fields = formInstance!!.getAllFields()
        assertEquals(null, fields["field1"])
        assertEquals(null, fields["field2"])
    }

    @Test
    fun formResetFieldsWithValues() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "name") { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("name" to "old")
        waitForIdle()
        formInstance!!.resetFields("name" to "new")
        waitForIdle()
        val fields = formInstance!!.getAllFields()
        assertEquals("new", fields["name"])
    }

    @Test
    fun formSubmitOnSuccess() = runComposeUiTest {
        var formInstance: FormInstance? = null
        var submittedValues: Map<String, Any?>? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("name" to "valid")
        waitForIdle()
        formInstance!!.submit(onSuccess = { submittedValues = it })
        waitForIdle()
        assertNotNull(submittedValues)
        assertEquals("valid", submittedValues!!["name"])
    }

    @Test
    fun formSubmitOnError() = runComposeUiTest {
        var formInstance: FormInstance? = null
        var errors: Map<String, List<String>>? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "email", Required(), Email()) { }
            }
        }
        waitForIdle()
        formInstance!!.submit(onSuccess = {}, onError = { errors = it })
        waitForIdle()
        assertNotNull(errors)
        assertTrue(errors!!.containsKey("email"))
    }

    @Test
    fun formValidationWithRequired() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "field", Required("required")) { }
            }
        }
        waitForIdle()
        assertFalse(formInstance!!.isValidated())
        formInstance!!.setFieldsValue("field" to "value")
        waitForIdle()
        assertTrue(formInstance!!.isValidated())
    }

    @Test
    fun formValidationWithEmail() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "email", Email("invalid email")) { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("email" to "not-an-email")
        waitForIdle()
        val errors = formInstance!!.getFieldError("email")
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun formValidationWithValidEmail() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "email", Email()) { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("email" to "test@example.com")
        waitForIdle()
        val errors = formInstance!!.getFieldError("email")
        assertTrue(errors.isEmpty())
    }

    @Test
    fun formMarkAsTouched() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "field") { }
            }
        }
        waitForIdle()
        assertFalse(formInstance!!.isTouched("field"))
        formInstance!!.markAsTouched("field")
        waitForIdle()
        assertTrue(formInstance!!.isTouched("field"))
    }

    @Test
    fun formMarkAllAsTouched() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "f1") { }
                FormItemWithState<String>(name = "f2") { }
            }
        }
        waitForIdle()
        formInstance!!.markAllAsTouched()
        waitForIdle()
        assertTrue(formInstance!!.isTouched("f1"))
        assertTrue(formInstance!!.isTouched("f2"))
        assertTrue(formInstance!!.getTouchedFields().contains("f1"))
        assertTrue(formInstance!!.getTouchedFields().contains("f2"))
    }

    @Test
    fun formValidateField() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }
        waitForIdle()
        assertFalse(formInstance!!.validateField("name"))
        formInstance!!.setFieldsValue("name" to "value")
        waitForIdle()
        assertTrue(formInstance!!.validateField("name"))
    }

    @Test
    fun formValidateFields() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "a", Required()) { }
                FormItemWithState<String>(name = "b", Required()) { }
            }
        }
        waitForIdle()
        assertFalse(formInstance!!.validateFields())
        formInstance!!.setFieldsValue("a" to "1", "b" to "2")
        waitForIdle()
        assertTrue(formInstance!!.validateFields())
    }

    @Test
    fun formGetAllFieldsErrors() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "email", Required(), Email()) { }
            }
        }
        waitForIdle()
        val allErrors = formInstance!!.getAllFieldsErrors()
        assertTrue(allErrors.containsKey("email"))
    }

    @Test
    fun formWithOnSubmitCallback() = runComposeUiTest {
        var formInstance: FormInstance? = null
        var submittedValues: Map<String, Any?>? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!, onSubmit = { submittedValues = it }) {
                FormItemWithState<String>(name = "name", Required()) { }
            }
        }
        waitForIdle()
        formInstance!!.setFieldsValue("name" to "test")
        waitForIdle()
        formInstance!!.submit()
        waitForIdle()
        assertNotNull(submittedValues)
        assertEquals("test", submittedValues!!["name"])
    }

    @Test
    fun formValidationTriggerDefault() = runComposeUiTest {
        var formInstance: FormInstance? = null
        setContent {
            formInstance = remember { FormInstance() }
            Form(formInstance!!) {
                FormItemWithState<String>(name = "field") { }
            }
        }
        waitForIdle()
        assertEquals(ValidationTrigger.OnChange, formInstance!!.getFieldValidationTrigger("field"))
    }

    // region validators

    @Test
    fun validatorRequiredPassesWithValue() {
        val required = Required()
        assertTrue(required.validator("test"))
    }

    @Test
    fun validatorRequiredFailsWithNull() {
        val required = Required()
        assertFalse(required.validator(null))
    }

    @Test
    fun validatorRequiredFailsWithEmpty() {
        val required = Required()
        assertFalse(required.validator(""))
    }

    @Test
    fun validatorEmailPassesWithValid() {
        val email = Email()
        assertTrue(email.validator("test@example.com"))
    }

    @Test
    fun validatorEmailFailsWithInvalid() {
        val email = Email()
        assertFalse(email.validator("not-email"))
    }

    @Test
    fun validatorEmailPassesWithNull() {
        val email = Email()
        assertTrue(email.validator(null))
    }

    @Test
    fun validatorPhonePassesWithValid() {
        val phone = Phone()
        assertTrue(phone.validator("010-12345678"))
    }

    @Test
    fun validatorPhoneFailsWithInvalid() {
        val phone = Phone()
        assertFalse(phone.validator("abc"))
    }

    @Test
    fun validatorMobilePassesWithValid() {
        val mobile = Mobile()
        assertTrue(mobile.validator("13800138000"))
    }

    @Test
    fun validatorMobileFailsWithInvalid() {
        val mobile = Mobile()
        assertFalse(mobile.validator("123"))
    }

    @Test
    fun validatorRegexPassesWithMatch() {
        val regex = Regex(regex = "^\\d{3}$")
        assertTrue(regex.validator("123"))
    }

    @Test
    fun validatorRegexFailsWithNoMatch() {
        val regex = Regex(regex = "^\\d{3}$")
        assertFalse(regex.validator("abc"))
    }

    @Test
    fun validatorRegexPassesWithNull() {
        val regex = Regex(regex = "^\\d+$")
        assertTrue(regex.validator(null))
    }

    @Test
    fun validatorCustomPasses() {
        val custom = object : CustomValidator(
            message = "must be positive",
            validator = { field -> field is Int && field > 0 },
        ) {}
        assertTrue(custom.validator(5))
    }

    @Test
    fun validatorCustomFails() {
        val custom = object : CustomValidator(
            message = "must be positive",
            validator = { field -> field is Int && field > 0 },
        ) {}
        assertFalse(custom.validator(-1))
    }

    @Test
    fun validatorCustomMessage() {
        val custom = object : CustomValidator(
            message = "custom error",
            validator = { false },
        ) {}
        assertEquals("custom error", custom.message)
    }

    @Test
    fun validatorArrayValidateFieldAllPass() {
        val validators = arrayOf(
            Required(),
            Email(),
        )
        val result = validators.validateField(
            "test@example.com",
            pass = { true },
            fail = { false },
        )
        assertTrue(result)
    }

    @Test
    fun validatorArrayValidateFieldOneFails() {
        val validators = arrayOf(
            Required(),
            Email(),
        )
        val result = validators.validateField(
            "not-email",
            pass = { true },
            fail = { false },
        )
        assertFalse(result)
    }

    @Test
    fun validatorArrayValidateFieldEmptyFails() {
        val validators = arrayOf(
            Required(),
            Email(),
        )
        val result = validators.validateField(
            "",
            pass = { true },
            fail = { false },
        )
        assertFalse(result)
    }

    @Test
    fun validatorCustomMessageEmail() {
        val email = Email("custom email error")
        assertEquals("custom email error", email.message)
    }

    @Test
    fun validatorCustomMessagePhone() {
        val phone = Phone("custom phone error")
        assertEquals("custom phone error", phone.message)
    }

    @Test
    fun validatorCustomMessageMobile() {
        val mobile = Mobile("custom mobile error")
        assertEquals("custom mobile error", mobile.message)
    }

    @Test
    fun validatorCustomMessageRequired() {
        val required = Required("custom required error")
        assertEquals("custom required error", required.message)
    }

    @Test
    fun validatorRegexCustomMessage() {
        val regex = Regex(message = "custom regex error", regex = "^.*$")
        assertEquals("custom regex error", regex.message)
    }

    // endregion

    // endregion

    // region useredux

    @Test
    fun createStoreWithSingleReducer() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                lcCounterReducer with 0
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatchFn = useDispatch()
            }
        }
        waitForIdle()
        assertEquals(0, count)
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        assertEquals(1, count)
    }

    @Test
    fun createStoreWithMultipleReducers() = runComposeUiTest {
        var count by mutableStateOf(0)
        var todoCount by mutableStateOf(0)
        setContent {
            val store = createStore {
                lcCounterReducer with 0
                lcTodoReducer with LCTodoState()
            }
            ReduxProvider(store = store) {
                val counter by useSelector<Int>()
                val todos by useSelector<LCTodoState>()
                count = counter
                todoCount = todos.items.size
            }
        }
        waitForIdle()
        assertEquals(0, count)
        assertEquals(0, todoCount)
    }

    @Test
    fun storeDispatchIncrement() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                lcCounterReducer with 0
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatchFn = useDispatch()
            }
        }
        waitForIdle()
        assertEquals(0, count)
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        assertEquals(1, count)
    }

    @Test
    fun storeDispatchDecrement() = runComposeUiTest {
        var count by mutableStateOf(10)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                lcCounterReducer with 10
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatchFn = useDispatch()
            }
        }
        waitForIdle()
        assertEquals(10, count)
        dispatchFn!!(LCCounterAction.Decrement)
        waitForIdle()
        assertEquals(9, count)
    }

    @Test
    fun storeDispatchSetValue() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                lcCounterReducer with 0
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatchFn = useDispatch()
            }
        }
        waitForIdle()
        dispatchFn!!(LCCounterAction.Set(100))
        waitForIdle()
        assertEquals(100, count)
    }

    @Test
    fun storeWithAlias() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                named<Int, LCCounterAction>("counter") {
                    lcCounterReducer with 0
                }
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>(alias = "counter")
                count = state
                dispatchFn = useDispatch(alias = "counter")
            }
        }
        waitForIdle()
        assertEquals(0, count)
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        assertEquals(1, count)
    }

    @Test
    fun storeSelectorTransform() = runComposeUiTest {
        var display by mutableStateOf("")
        setContent {
            val store = createStore {
                lcCounterReducer with 42
            }
            ReduxProvider(store = store) {
                val text by useSelector<Int, String> { "Count: $this" }
                display = text
            }
        }
        waitForIdle()
        assertEquals("Count: 42", display)
    }

    @Test
    fun storePlusOperator() {
        val store1 = createStore { lcCounterReducer with 0 }
        val store2 = createStore { lcTodoReducer with LCTodoState() }
        val combined = store1 + store2
        assertEquals(2, combined.records.size)
    }

    @Test
    fun combineStoresFunction() {
        val store1 = createStore { lcCounterReducer with 0 }
        val store2 = createStore { lcTodoReducer with LCTodoState() }
        val combined = combineStores(store1, store2)
        assertEquals(2, combined.records.size)
    }

    @Test
    fun storeDispatchMultipleActions() = runComposeUiTest {
        var count by mutableStateOf(0)
        var dispatchFn: ((LCCounterAction) -> Unit)? = null
        setContent {
            val store = createStore {
                lcCounterReducer with 0
            }
            ReduxProvider(store = store) {
                val state by useSelector<Int>()
                count = state
                dispatchFn = useDispatch()
            }
        }
        waitForIdle()
        assertEquals(0, count)
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        dispatchFn!!(LCCounterAction.Increment)
        waitForIdle()
        assertEquals(3, count)
    }

    // endregion

    // region usethrottle

    @Test
    fun throttleValueWithDefaultOptions() = runComposeUiTest {
        var result: State<String>? = null
        setContent {
            result = useThrottleImpl("initial")
        }
        waitForIdle()
        assertEquals("initial", result!!.value)
    }

    @Test
    fun throttleOptionsDefaults() {
        val options = xyz.junerver.compose.hooks.usethrottle.UseThrottleOptions.optionOf {}
        assertEquals(1.seconds, options.wait)
        assertTrue(options.leading)
        assertTrue(options.trailing)
    }

    @Test
    fun throttleOptionsCustom() {
        val options = xyz.junerver.compose.hooks.usethrottle.UseThrottleOptions.optionOf {
            wait = 200.milliseconds
            leading = false
            trailing = false
        }
        assertEquals(200.milliseconds, options.wait)
        assertFalse(options.leading)
        assertFalse(options.trailing)
    }

    // endregion

    // region usedebounce

    @Test
    fun debounceValueWithDefaultOptions() = runComposeUiTest {
        var result: State<String>? = null
        setContent {
            result = useDebounceImpl("initial")
        }
        waitForIdle()
        assertEquals("initial", result!!.value)
    }

    @Test
    fun debounceOptionsDefaults() {
        val options = xyz.junerver.compose.hooks.usedebounce.UseDebounceOptions.optionOf {}
        assertEquals(1.seconds, options.wait)
        assertFalse(options.leading)
        assertTrue(options.trailing)
        assertEquals(kotlin.time.Duration.ZERO, options.maxWait)
    }

    @Test
    fun debounceOptionsCustom() {
        val options = xyz.junerver.compose.hooks.usedebounce.UseDebounceOptions.optionOf {
            wait = 200.milliseconds
            leading = true
            trailing = false
            maxWait = 1.seconds
        }
        assertEquals(200.milliseconds, options.wait)
        assertTrue(options.leading)
        assertFalse(options.trailing)
        assertEquals(1.seconds, options.maxWait)
    }

    // endregion

    // region usetimeago

    @Test
    fun formatTimeAgoJustNow() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val recent = Instant.fromEpochMilliseconds(990000) // 10 seconds ago
        val result = formatTimeAgo(recent, now = now)
        assertEquals("just now", result)
    }

    @Test
    fun formatTimeAgoMinutesAgo() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val fiveMinAgo = Instant.fromEpochMilliseconds(700000) // 5 minutes ago
        val result = formatTimeAgo(fiveMinAgo, now = now)
        assertTrue(result.contains("minute"), "Expected 'minute' in result, got: $result")
        assertTrue(result.contains("ago"), "Expected 'ago' in result, got: $result")
    }

    @Test
    fun formatTimeAgoHoursAgo() {
        val now = Instant.fromEpochMilliseconds(100000000)
        val twoHoursAgo = Instant.fromEpochMilliseconds(92800000) // ~2 hours ago
        val result = formatTimeAgo(twoHoursAgo, now = now)
        assertTrue(result.contains("hour"), "Expected 'hour' in result, got: $result")
        assertTrue(result.contains("ago"), "Expected 'ago' in result, got: $result")
    }

    @Test
    fun formatTimeAgoDaysAgo() {
        val now = Instant.fromEpochMilliseconds(1000000000)
        val threeDaysAgo = Instant.fromEpochMilliseconds(740800000) // ~3 days ago
        val result = formatTimeAgo(threeDaysAgo, now = now)
        assertTrue(result.contains("day"), "Expected 'day' in result, got: $result")
        assertTrue(result.contains("ago"), "Expected 'ago' in result, got: $result")
    }

    @Test
    fun formatTimeAgoFuture() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val future = Instant.fromEpochMilliseconds(1300000) // 5 minutes later
        val result = formatTimeAgo(future, now = now)
        assertTrue(result.contains("in "), "Expected 'in ' in result, got: $result")
        assertTrue(result.contains("minute"), "Expected 'minute' in result, got: $result")
    }

    @Test
    fun formatTimeAgoWithChineseMessages() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val fiveMinAgo = Instant.fromEpochMilliseconds(700000)
        val options = FormatTimeAgoOptions.optionOf {
            messages = DefaultChineseTimeAgoMessages
        }
        val result = formatTimeAgo(fiveMinAgo, options, now)
        assertTrue(result.contains("分钟"), "Expected '分钟' in result, got: $result")
        assertTrue(result.contains("前"), "Expected '前' in result, got: $result")
    }

    @Test
    fun formatTimeAgoWithChineseJustNow() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val recent = Instant.fromEpochMilliseconds(990000)
        val options = FormatTimeAgoOptions.optionOf {
            messages = DefaultChineseTimeAgoMessages
        }
        val result = formatTimeAgo(recent, options, now)
        assertEquals("刚刚", result)
    }

    @Test
    fun formatTimeAgoWithChineseFuture() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val future = Instant.fromEpochMilliseconds(1300000)
        val options = FormatTimeAgoOptions.optionOf {
            messages = DefaultChineseTimeAgoMessages
        }
        val result = formatTimeAgo(future, options, now)
        assertTrue(result.contains("分钟"), "Expected '分钟' in result, got: $result")
        assertTrue(result.contains("后"), "Expected '后' in result, got: $result")
    }

    @Test
    fun formatTimeAgoShowSeconds() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val fiveSecAgo = Instant.fromEpochMilliseconds(995000)
        val options = FormatTimeAgoOptions.optionOf {
            showSecond = true
        }
        val result = formatTimeAgo(fiveSecAgo, options, now)
        assertTrue(result.contains("second"), "Expected 'second' in result, got: $result")
    }

    @Test
    fun formatTimeAgoRoundingFloor() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val ninetySecAgo = Instant.fromEpochMilliseconds(910000) // 90 seconds = 1.5 minutes
        val options = FormatTimeAgoOptions.optionOf {
            rounding = "floor"
        }
        val result = formatTimeAgo(ninetySecAgo, options, now)
        assertTrue(result.contains("1 minute"), "Expected '1 minute' in result, got: $result")
    }

    @Test
    fun formatTimeAgoRoundingCeil() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val ninetySecAgo = Instant.fromEpochMilliseconds(910000)
        val options = FormatTimeAgoOptions.optionOf {
            rounding = "ceil"
        }
        val result = formatTimeAgo(ninetySecAgo, options, now)
        assertTrue(result.contains("2 minutes"), "Expected '2 minutes' in result, got: $result")
    }

    @Test
    fun formatTimeAgoRoundingRound() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val ninetySecAgo = Instant.fromEpochMilliseconds(910000)
        val options = FormatTimeAgoOptions.optionOf {
            rounding = "round"
        }
        val result = formatTimeAgo(ninetySecAgo, options, now)
        assertTrue(result.contains("2 minutes"), "Expected '2 minutes' in result, got: $result")
    }

    @Test
    fun formatTimeAgoMaxExceeded() {
        val now = Instant.fromEpochMilliseconds(1000000000)
        val veryOld = Instant.fromEpochMilliseconds(1000000) // very far in the past
        val options = FormatTimeAgoOptions.optionOf {
            max = 100000000L // 100 seconds max
            fullDateFormatter = { "custom date" }
        }
        val result = formatTimeAgo(veryOld, options, now)
        assertEquals("custom date", result)
    }

    @Test
    fun formatTimeAgoMaxExceededDefaultFormat() {
        val now = Instant.fromEpochMilliseconds(1000000000)
        val veryOld = Instant.fromEpochMilliseconds(1000000)
        val options = FormatTimeAgoOptions.optionOf {
            max = 100000000L
        }
        val result = formatTimeAgo(veryOld, options, now)
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatTimeAgoWeeksAgo() {
        val now = Instant.fromEpochMilliseconds(1000000000)
        val twoWeeksAgo = Instant.fromEpochMilliseconds(878400000) // ~14 days ago
        val result = formatTimeAgo(twoWeeksAgo, now = now)
        assertTrue(result.contains("week") || result.contains("day"), "Expected 'week' or 'day' in result, got: $result")
    }

    @Test
    fun formatTimeAgoMonthsAgo() {
        val now = Instant.fromEpochMilliseconds(10000000000)
        val threeMonthsAgo = Instant.fromEpochMilliseconds(2200000000) // ~90 days ago
        val result = formatTimeAgo(threeMonthsAgo, now = now)
        assertTrue(result.contains("month") || result.contains("day"), "Expected 'month' or 'day' in result, got: $result")
    }

    @Test
    fun formatTimeAgoYearsAgo() {
        val now = Instant.fromEpochMilliseconds(100000000000)
        val twoYearsAgo = Instant.fromEpochMilliseconds(36500000000) // ~1 year ago
        val result = formatTimeAgo(twoYearsAgo, now = now)
        assertTrue(result.contains("year") || result.contains("month"), "Expected 'year' or 'month' in result, got: $result")
    }

    @Test
    fun formatTimeAgoExactSameTime() {
        val now = Instant.fromEpochMilliseconds(1000000)
        val result = formatTimeAgo(now, now = now)
        assertEquals("just now", result)
    }

    @Test
    fun timeAgoDefaultEnglishMessages() {
        val msgs = DefaultEnglishTimeAgoMessages
        assertEquals("just now", msgs.justNow)
        assertEquals("invalid date", msgs.invalid)
        assertEquals("1 second", msgs.second(1))
        assertEquals("5 seconds", msgs.second(5))
        assertEquals("1 minute", msgs.minute(1))
        assertEquals("3 minutes", msgs.minute(3))
        assertEquals("1 hour", msgs.hour(1))
        assertEquals("2 hours", msgs.hour(2))
        assertEquals("1 day", msgs.day(1))
        assertEquals("7 days", msgs.day(7))
        assertEquals("1 week", msgs.week(1))
        assertEquals("4 weeks", msgs.week(4))
        assertEquals("1 month", msgs.month(1))
        assertEquals("6 months", msgs.month(6))
        assertEquals("1 year", msgs.year(1))
        assertEquals("10 years", msgs.year(10))
        assertEquals("5 minutes ago", msgs.past("5 minutes"))
        assertEquals("in 5 minutes", msgs.future("5 minutes"))
    }

    @Test
    fun timeAgoDefaultChineseMessages() {
        val msgs = DefaultChineseTimeAgoMessages
        assertEquals("刚刚", msgs.justNow)
        assertEquals("无效日期", msgs.invalid)
        assertEquals("1秒", msgs.second(1))
        assertEquals("5秒", msgs.second(5))
        assertEquals("1分钟", msgs.minute(1))
        assertEquals("3分钟", msgs.minute(3))
        assertEquals("1小时", msgs.hour(1))
        assertEquals("2小时", msgs.hour(2))
        assertEquals("1天", msgs.day(1))
        assertEquals("7天", msgs.day(7))
        assertEquals("1周", msgs.week(1))
        assertEquals("4周", msgs.week(4))
        assertEquals("1个月", msgs.month(1))
        assertEquals("6个月", msgs.month(6))
        assertEquals("1年", msgs.year(1))
        assertEquals("10年", msgs.year(10))
        assertEquals("5分钟前", msgs.past(msgs.minute(5)))
        assertEquals("5分钟后", msgs.future(msgs.minute(5)))
    }

    @Test
    fun timeAgoOptionsDefaults() {
        val options = UseTimeAgoOptions.optionOf {}
        assertEquals(30.seconds, options.updateInterval)
        assertEquals(null, options.max)
        assertEquals(null, options.fullDateFormatter)
        assertEquals(false, options.showSecond)
        assertEquals("round", options.rounding)
        assertEquals(null, options.units)
    }

    @Test
    fun formatTimeAgoOptionsDefaults() {
        val options = FormatTimeAgoOptions.optionOf {}
        assertEquals(null, options.max)
        assertEquals(null, options.fullDateFormatter)
        assertEquals(false, options.showSecond)
        assertEquals("round", options.rounding)
        assertEquals(null, options.units)
    }

    @Test
    fun useTimeAgoImplReturnsState() = runComposeUiTest {
        var result: State<String>? = null
        val fiveMinAgo = kotlin.time.Clock.System.now() - 300.toLong().seconds
        setContent {
            result = useTimeAgoImpl(fiveMinAgo, optionsOf = {
                updateInterval = kotlin.time.Duration.ZERO
            })
        }
        waitForIdle()
        assertNotNull(result)
        assertTrue(result!!.value.contains("minute"), "Expected 'minute' in result, got: ${result!!.value}")
    }

    // endregion

    // region usepersistent

    @Test
    fun persistentBasicSaveAndGet() = runComposeUiTest {
        var holder: PersistentHolder<String>? = null
        setContent {
            holder = usePersistentImpl("test-key-1", "default")
        }
        waitForIdle()
        assertEquals("default", holder!!.state.value)
        holder!!.save("saved")
        waitForIdle()
        assertEquals("saved", holder!!.state.value)
    }

    @Test
    fun persistentClearResetsToDefault() = runComposeUiTest {
        var holder: PersistentHolder<String>? = null
        setContent {
            holder = usePersistentImpl("test-key-2", "default")
        }
        waitForIdle()
        holder!!.save("saved")
        waitForIdle()
        assertEquals("saved", holder!!.state.value)
        holder!!.clear()
        waitForIdle()
        assertEquals("default", holder!!.state.value)
    }

    @Test
    fun persistentWithIntValue() = runComposeUiTest {
        var holder: PersistentHolder<Int>? = null
        setContent {
            holder = usePersistentImpl("test-key-int", 0)
        }
        waitForIdle()
        assertEquals(0, holder!!.state.value)
        holder!!.save(42)
        waitForIdle()
        assertEquals(42, holder!!.state.value)
    }

    @Test
    fun persistentWithBooleanValue() = runComposeUiTest {
        var holder: PersistentHolder<Boolean>? = null
        setContent {
            holder = usePersistentImpl("test-key-bool", false)
        }
        waitForIdle()
        assertFalse(holder!!.state.value)
        holder!!.save(true)
        waitForIdle()
        assertTrue(holder!!.state.value)
    }

    @Test
    fun persistentDifferentKeysIsolated() = runComposeUiTest {
        var holder1: PersistentHolder<String>? = null
        var holder2: PersistentHolder<String>? = null
        setContent {
            holder1 = usePersistentImpl("test-isolated-1", "default1")
            holder2 = usePersistentImpl("test-isolated-2", "default2")
        }
        waitForIdle()
        assertEquals("default1", holder1!!.state.value)
        assertEquals("default2", holder2!!.state.value)
        holder1!!.save("value1")
        waitForIdle()
        assertEquals("value1", holder1!!.state.value)
        assertEquals("default2", holder2!!.state.value)
    }

    @Test
    fun persistentPropertyDelegation() = runComposeUiTest {
        var holder: PersistentHolder<String>? = null
        setContent {
            holder = usePersistentImpl("test-delegation", "default")
        }
        waitForIdle()
        assertEquals("default", holder!!.state.value)
        holder!!.save("delegated")
        waitForIdle()
        assertEquals("delegated", holder!!.state.value)
    }

    @Test
    fun persistentForceUseMemory() = runComposeUiTest {
        var holder: PersistentHolder<String>? = null
        setContent {
            holder = usePersistentImpl("test-memory", "default", forceUseMemory = true)
        }
        waitForIdle()
        assertEquals("default", holder!!.state.value)
        holder!!.save("memory-value")
        waitForIdle()
        assertEquals("memory-value", holder!!.state.value)
    }

    @Test
    fun persistentWithListValue() = runComposeUiTest {
        var holder: PersistentHolder<List<String>>? = null
        setContent {
            holder = usePersistentImpl("test-list", emptyList())
        }
        waitForIdle()
        assertTrue(holder!!.state.value.isEmpty())
        holder!!.save(listOf("a", "b", "c"))
        waitForIdle()
        assertEquals(listOf("a", "b", "c"), holder!!.state.value)
    }

    // endregion
}
