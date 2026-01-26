@file:Suppress("UNCHECKED_CAST", "USELESS_CAST")

package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.internal.genFormFieldKey
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useMap

/*
  Description: Headless Form Component
  Author: Junerver
  Date: 2024/3/25-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Internal form context for managing form state across components.
 * This context provides access to the form instance throughout the component tree.
 */
internal val FormContext by lazy { createContext(FormInstance()) }

/**
 * Data class representing the complete state of a form field.
 * Provides more detailed state information than the basic Triple.
 *
 * @param T The type of the form field value
 * @property value The mutable state holding the field's current value
 * @property isValid Whether the field passes all validation rules
 * @property errors List of validation error messages
 * @property isTouched Whether the user has interacted with this field
 * @property isDirty Whether the field value differs from its initial value
 */
@Stable
data class FormItemState<T>(
    val value: MutableState<T?>,
    val isValid: Boolean,
    val errors: List<String>,
    val isTouched: Boolean,
    val isDirty: Boolean,
)

/**
 * A headless form component that provides form state management and validation.
 *
 * This component creates a form context and provides form functionality to its children
 * without imposing any UI constraints. It allows you to:
 * - Manage form field values
 * - Handle field validation
 * - Track form state
 * - Create custom form layouts
 *
 * @param formInstance The form instance to use. If not provided, a new instance will be created
 * @param onSubmit Optional callback invoked when form.submit() is called and validation passes
 * @param content The form content with access to form functionality through [FormScope]
 *
 * @example
 * ```kotlin
 * Form { // Uses default form instance
 *     FormItem<String>(
 *         name = "username",
 *         Required("Username is required"),
 *         MinLength(3, "Username must be at least 3 characters")
 *     ) { (state, isValid, errors) ->
 *         var value by state
 *         TextField(
 *             value = value ?: "",
 *             onValueChange = { value = it }
 *         )
 *         if (!isValid) {
 *             Text(errors.first(), color = Color.Red)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun Form(
    formInstance: FormInstance = Form.useForm(),
    onSubmit: ((Map<String, Any?>) -> Unit)? = null,
    content: @Composable FormScope.() -> Unit,
) {
    val formRef = useCreation { FormRef() }
    formInstance.apply { this.formRef = formRef }
    // Register onSubmit callback
    formRef.current.onSubmitCallback = onSubmit
    val formScope = remember(formRef, formInstance) { FormScope.getInstance(formRef, formInstance) }
    FormContext.Provider(formInstance) {
        formScope.content()
    }
}

/**
 * Scope class for form components that provides form-specific functionality.
 * This scope gives access to form operations and state management within the form content.
 *
 * @property formRefRef Reference to the form's internal state
 * @property formInstance The current form instance being used
 */
@Stable
class FormScope private constructor(
    private val formRefRef: Ref<FormRef>,
    private val formInstance: FormInstance,
) {
    /**
     * Creates a form field container with validation support.
     * This is an internal implementation that converts the validators list to an array.
     *
     * @param T The type of the form field value
     * @param name Unique identifier for the form field
     * @param validators List of validators to apply to the field
     * @param content Composable content that receives the field state, validation state, and error messages
     */
    @Composable
    private fun <T : Any> FormItem(
        name: String,
        validators: List<Validator> = emptyList(),
        content: @Composable (Triple<MutableState<T?>, Boolean, List<String>>) -> Unit,
    ) = FormItem(
        name = name,
        validators = validators.toTypedArray(),
        content = content,
    )

    /**
     * Creates a form field container with validation support.
     * This component manages the state and validation of a single form field.
     *
     * @param T The type of the form field value
     * @param name Unique identifier for the form field
     * @param validators Array of validators to apply to the field
     * @param content Composable content that receives:
     *                - MutableState<T?>: The field's value state
     *                - Boolean: Whether the field passes validation
     *                - List<String>: List of error messages if validation fails
     *
     * @example
     * ```kotlin
     * FormItem<String>(
     *     name = "email",
     *     Email("Invalid email format"),
     *     Required("Email is required")
     * ) { (state, isValid, errors) ->
     *     var value by state
     *     Column {
     *         TextField(
     *             value = value ?: "",
     *             onValueChange = { value = it }
     *         )
     *         if (!isValid) {
     *             Text(errors.joinToString(), color = Color.Red)
     *         }
     *     }
     * }
     * ```
     */
    @Composable
    fun <T : Any> FormItem(
        name: String,
        vararg validators: Validator,
        content: @Composable (Triple<MutableState<T?>, Boolean, List<String>>) -> Unit,
    ) {
        val fieldState = _useState<T?>(default = null)
        val (validate, _, set) = useBoolean()
        val errMsg = useMap<KClass<*>, String>()
        val currentFormRef: FormRef = formRefRef.current
        @Suppress("UNCHECKED_CAST")
        currentFormRef.formFieldMap[name] = fieldState as MutableState<Any?>
        val publish = useEventPublish<T?>(name.genFormFieldKey(formInstance))
        useEffect(fieldState) {
            currentFormRef.formOperationCount.longValue += 1
            publish(fieldState.value as? T)

            // Track touched state - mark as touched when value changes
            if (fieldState.value != null) {
                currentFormRef.formFieldTouchedMap[name] = true
            }

            // Track dirty state - compare with initial value
            val initialValue = currentFormRef.formFieldInitialValueMap[name]
            currentFormRef.formFieldDirtyMap[name] = fieldState.value != initialValue

            fun Validator.pass(): Boolean {
                errMsg.remove(this::class)
                return true
            }

            fun Validator.fail(): Boolean {
                errMsg[this::class] = this.message
                return false
            }

            val fieldValue: Any? = fieldState.value
            val isValidate =
                (validators as Array<Validator>).validateField(fieldValue, pass = Validator::pass, fail = Validator::fail)
            set(isValidate)
            currentFormRef.formFieldValidationMap[name] = isValidate
        }
        useEffect(errMsg) {
            currentFormRef.formFieldErrorMessagesMap[name] = errMsg.values.toList()
        }
        val errorMessages = errMsg.values.toList()
        content(Triple(fieldState, validate.value, errorMessages))
    }

    /**
     * Creates a form field container with enhanced state information.
     * This component provides more detailed state including touched and dirty status.
     *
     * @param T The type of the form field value
     * @param name Unique identifier for the form field
     * @param validators Array of validators to apply to the field
     * @param content Composable content that receives [FormItemState] with complete field information
     *
     * @example
     * ```kotlin
     * FormItemWithState<String>(
     *     name = "email",
     *     Email("Invalid email format"),
     *     Required("Email is required")
     * ) { state ->
     *     var value by state.value
     *     Column {
     *         TextField(
     *             value = value ?: "",
     *             onValueChange = { value = it }
     *         )
     *         // Only show errors after user has interacted with the field
     *         if (state.isTouched && !state.isValid) {
     *             Text(state.errors.joinToString(), color = Color.Red)
     *         }
     *     }
     * }
     * ```
     */
    @Composable
    fun <T : Any> FormItemWithState(
        name: String,
        vararg validators: Validator,
        content: @Composable (FormItemState<T>) -> Unit,
    ) {
        val fieldState = _useState<T?>(default = null)
        val (validate, _, set) = useBoolean()
        val (touched, _, setTouched) = useBoolean(false)
        val (dirty, _, setDirty) = useBoolean(false)
        val errMsg = useMap<KClass<*>, String>()
        val currentFormRef: FormRef = formRefRef.current
        @Suppress("UNCHECKED_CAST")
        currentFormRef.formFieldMap[name] = fieldState as MutableState<Any?>
        val publish = useEventPublish<T?>(name.genFormFieldKey(formInstance))
        useEffect(fieldState) {
            currentFormRef.formOperationCount.longValue += 1
            publish(fieldState.value as? T)

            // Track touched state
            if (fieldState.value != null) {
                currentFormRef.formFieldTouchedMap[name] = true
                setTouched(true)
            }

            // Track dirty state
            val initialValue = currentFormRef.formFieldInitialValueMap[name]
            val isDirty = fieldState.value != initialValue
            currentFormRef.formFieldDirtyMap[name] = isDirty
            setDirty(isDirty)

            fun Validator.pass(): Boolean {
                errMsg.remove(this::class)
                return true
            }

            fun Validator.fail(): Boolean {
                errMsg[this::class] = this.message
                return false
            }

            val fieldValue: Any? = fieldState.value
            val isValidate =
                (validators as Array<Validator>).validateField(fieldValue, pass = Validator::pass, fail = Validator::fail)
            set(isValidate)
            currentFormRef.formFieldValidationMap[name] = isValidate
        }
        useEffect(errMsg) {
            currentFormRef.formFieldErrorMessagesMap[name] = errMsg.values.toList()
        }
        val errorMessages = errMsg.values.toList()
        content(
            FormItemState(
                value = fieldState,
                isValid = validate.value,
                errors = errorMessages,
                isTouched = touched.value,
                isDirty = dirty.value,
            ),
        )
    }

    /**
     * Extension function to get the form's validation state as a [State].
     * This allows for reactive updates to the UI based on the form's validation status.
     *
     * @return [State] containing whether all form fields are currently valid
     *
     * @example
     * ```kotlin
     * val isValid by form._isValidated()
     * Button(
     *     enabled = isValid,
     *     onClick = { /* handle submission */ }
     * ) {
     *     Text("Submit")
     * }
     * ```
     */
    @Composable
    fun FormInstance._isValidated(): State<Boolean> {
        val counterRef = formRef.current.formOperationCount
        val (isValidated, _, setValidated) = useBoolean(isValidated())
        useEffect(counterRef) {
            setValidated(isValidated())
        }
        return isValidated
    }

    companion object {
        /**
         * Internal factory method for creating FormScope instances.
         * This ensures proper initialization of the form scope with required references.
         *
         * @param ref Reference to the form's internal state
         * @param formInstance The form instance to associate with this scope
         * @return A new [FormScope] instance
         */
        internal fun getInstance(ref: Ref<FormRef>, formInstance: FormInstance) = FormScope(ref, formInstance)
    }
}
