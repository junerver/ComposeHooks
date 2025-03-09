@file:Suppress("UnusedReceiverParameter")

package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.genFormFieldKey
import xyz.junerver.compose.hooks.useContext
import xyz.junerver.compose.hooks.useEventSubscribe

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-10:06
  Email: junerver@gmail.com
  Version: v1.0
*/

/**
 * Object for scoping form-related hooks and functionality.
 * This object serves as a receiver for form hook extensions.
 */
object Form

/**
 * Creates a new form instance for managing form state and validation.
 *
 * This hook provides a way to create and manage form state, validation rules,
 * and form submissions. The form instance can be used to:
 * - Set and get field values
 * - Validate fields with various rules
 * - Reset form fields
 * - Track form validation state
 *
 * @return A new [FormInstance] for managing form state
 *
 * @example
 * ```kotlin
 * // Create a form instance
 * val form = Form.useForm()
 *
 * // Initialize form values
 * useMount {
 *     form.setFieldsValue(
 *         "name" to "default",
 *         "mobile" to "111"
 *     )
 * }
 *
 * // Use in form component
 * Form(form) {
 *     FormItem<String>(
 *         name = "mobile",
 *         Mobile(),
 *         Required()
 *     ) { (state, validate, msgs) ->
 *         var value by state
 *         OutlinedTextField(
 *             value = value ?: "",
 *             onValueChange = { value = it }
 *         )
 *         Text(text = "$validate ${msgs.joinToString()}")
 *     }
 *
 *     // Form controls
 *     Row {
 *         Button(
 *             onClick = { form.resetFields() }
 *         ) {
 *             Text("Reset")
 *         }
 *         Button(
 *             enabled = form.isValidated(),
 *             onClick = { form.submit() }
 *         ) {
 *             Text("Submit")
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun Form.useForm(): FormInstance = remember { FormInstance() }

/**
 * Internal helper function for creating a form instance.
 */
@Composable
private fun useForm(): FormInstance = Form.useForm()

/**
 * A hook for watching form field values outside of a [FormScope].
 *
 * This hook allows you to observe form field values from any component, making it
 * useful for:
 * - Displaying field values outside the form
 * - Creating dependent field logic
 * - Building real-time validation feedback
 *
 * @param fieldName The name of the form field to watch
 * @param formInstance The form instance containing the field
 * @return A [State] containing the current value of the field
 *
 * @example
 * ```kotlin
 * // Watch a field value
 * val name by Form.useWatch<String>("name", form)
 *
 * // Display watched value
 * Text("Current name: $name")
 *
 * // Use with validation
 * FormItem<String>(
 *     name = "email",
 *     Email(),
 *     Required()
 * ) { (state, validate, msgs) ->
 *     var value by state
 *     Column {
 *         OutlinedTextField(
 *             value = value ?: "",
 *             onValueChange = { value = it }
 *         )
 *         // Show validation messages
 *         Text("$validate ${msgs.joinToString()}")
 *     }
 * }
 * ```
 */
@Composable
fun <T> Form.useWatch(fieldName: String, formInstance: FormInstance): State<T?> {
    val state = _useState<T?>(null)
    useEventSubscribe<T?>(fieldName.genFormFieldKey(formInstance)) { value ->
        state.value = value
    }
    return state
}

/**
 * A hook for accessing the current form instance from child components.
 *
 * This hook provides access to the form instance from any component within the form's
 * context. It's particularly useful for:
 * - Creating reusable form components
 * - Accessing form validation state
 * - Implementing form controls
 *
 * @return The current [FormInstance]
 *
 * @example
 * ```kotlin
 * @Composable
 * private fun FormScope.FormControls() {
 *     val form = Form.useFormInstance()
 *
 *     // Get form validation state
 *     val canSubmit by form._isValidated()
 *
 *     Row {
 *         Button(
 *             enabled = canSubmit,
 *             onClick = {
 *                 // Access all form values
 *                 println(form.getAllFields())
 *             }
 *         ) {
 *             Text("Submit")
 *         }
 *
 *         Button(
 *             onClick = {
 *                 // Reset with specific values
 *                 form.resetFields(
 *                     "name" to "User",
 *                     "age" to 18,
 *                     "email" to "user@example.com"
 *                 )
 *             }
 *         ) {
 *             Text("Reset with Values")
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun Form.useFormInstance(): FormInstance = useContext(context = FormContext)
