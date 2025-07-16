package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useform.CustomValidator
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.FormInstance
import xyz.junerver.compose.hooks.useform.FormScope
import xyz.junerver.compose.hooks.useform.Mobile
import xyz.junerver.compose.hooks.useform.Phone
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.useForm
import xyz.junerver.compose.hooks.useform.useFormInstance
import xyz.junerver.compose.hooks.useform.useWatch
import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.composehooks.ui.component.ExampleCard
import xyz.junerver.composehooks.ui.component.ScrollColumn
import xyz.junerver.composehooks.ui.component.TButton

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-8:48
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseFormExample() {
    Surface {
        ScrollColumn(modifier = Modifier.padding(16.dp)) {
            // Page title
            Text(
                text = "useForm Examples",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Basic form example
            ExampleCard(title = "Basic Form Example") {
                BasicFormExample()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form operations example
            ExampleCard(title = "Form Operations") {
                FormOperationsExample()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field watching example
            ExampleCard(title = "Field Watching") {
                FieldWatchingExample()
            }
        }
    }
}

/**
 * Basic form example
 * Demonstrates the basic usage and validation of form fields
 */
@Composable
private fun BasicFormExample() {
    val form = Form.useForm()

    // Initialize form fields
    useMount {
        form.setFieldsValue(
            "name" to "default",
            "mobile" to "111",
        )
    }

    Form(form) {
        // Name field - no validation
        FormItem<String>(name = "name") { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "Name",
                isRequired = false,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Age field - required validation
        FormItem<Int>(name = "age", Required()) { (state, validate, msgs) ->
            var age by state
            FormField(
                title = "Age",
                isRequired = true,
                isValid = validate,
                errorMessages = msgs,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TButton(text = "1", enabled = age != 1) {
                        form.setFieldValue("age" to 1)
                    }
                    TButton(text = "3", enabled = age != 3) {
                        form.setFieldValue("age" to 3)
                    }
                    TButton(text = "5", enabled = age != 5) {
                        form.setFieldValue("age" to 5)
                    }
                    TButton(text = "Clear", enabled = age != null) {
                        form.setFieldValue("age" to null)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mobile field - mobile format validation + required validation
        FormItem<String>(
            name = "mobile",
            Mobile(),
            Required(),
        ) { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "Mobile",
                isRequired = true,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone field - phone format validation
        FormItem<String>(
            name = "phone",
            Phone(),
        ) { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "Phone",
                isRequired = false,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email field - email format validation + required validation
        FormItem<String>(
            name = "email",
            Email(),
            Required(),
        ) { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "Email",
                isRequired = true,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ID number field - custom validator
        FormItem<String>(
            name = "id",
            object : CustomValidator(
                "Invalid ID number format",
                {
                    !it.asBoolean() || (it is String && it.matches(Regex(CHINA_ID_REGEX)))
                },
            ) {},
        ) { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "ID Number",
                isRequired = false,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter Chinese ID number") },
                )
            }
        }

        // Form action buttons
        Spacer(modifier = Modifier.height(24.dp))
        FormActions()
    }
}

/**
 * Form field component
 * Unifies the layout and error messages of form fields
 */
@Composable
private fun FormField(
    title: String,
    isRequired: Boolean,
    isValid: Boolean,
    errorMessages: List<String>,
    content: @Composable () -> Unit,
) {
    Column {
        // Field title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Field content
        content()

        // Error messages
        if (!isValid && errorMessages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessages.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Form action buttons component
 */
@Composable
private fun FormScope.FormActions() {
    val formInstance: FormInstance = Form.useFormInstance()
    val canSubmit by formInstance._isValidated()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TButton(
            text = "Submit",
            enabled = canSubmit,
            modifier = Modifier.weight(1f),
        ) {
            println(
                "Form data: ${formInstance.getAllFields()}\n" +
                    "Is validated: ${formInstance.isValidated()}",
            )
        }

        TButton(
            text = "Reset",
            modifier = Modifier.weight(1f),
        ) {
            formInstance.resetFields()
        }
    }
}

/**
 * Form operations example
 * Demonstrates form reset and submit operations
 */
@Composable
private fun FormOperationsExample() {
    val form = Form.useForm()

    Form(form) {
        // Name field
        FormItem<String>(name = "name") { (state, validate, msgs) ->
            var string by state
            FormField(
                title = "Name",
                isRequired = false,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = string ?: "",
                    onValueChange = { string = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Age field - required validation
        FormItem<Int>(name = "age", Required()) { (state, validate, msgs) ->
            var age by state
            FormField(
                title = "Age",
                isRequired = true,
                isValid = validate,
                errorMessages = msgs,
            ) {
                OutlinedTextField(
                    value = age?.toString() ?: "",
                    onValueChange = {
                        try {
                            age = if (it.isEmpty()) {
                                null
                            } else {
                                it.toInt()
                            }
                        } catch (_: NumberFormatException) {
                            // Ignore non-numeric input
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form operations
        val formInstance: FormInstance = Form.useFormInstance()
        val canSubmit by formInstance._isValidated()

        Text(
            text = "Form Operations",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TButton(
                text = "Submit",
                enabled = canSubmit,
                modifier = Modifier.weight(1f),
            ) {
                println("Form data: ${formInstance.getAllFields()}")
            }

            TButton(
                text = "Reset",
                modifier = Modifier.weight(1f),
            ) {
                formInstance.resetFields()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TButton(
            text = "Reset with Values",
            modifier = Modifier.fillMaxWidth(),
        ) {
            formInstance.resetFields(
                "name" to "Junerver",
                "age" to 5,
            )
        }
    }
}

/**
 * Field watching example
 * Demonstrates how to use Form.useWatch to monitor form field changes
 */
@Composable
private fun FieldWatchingExample() {
    val form = Form.useForm()

    // Use Form.useWatch to monitor field changes
    val name by Form.useWatch<String>(fieldName = "name", formInstance = form)
    val age by Form.useWatch<Int>(fieldName = "age", formInstance = form)

    Column {
        Form(form) {
            // Name field
            FormItem<String>(name = "name") { (state, validate, msgs) ->
                var string by state
                FormField(
                    title = "Name",
                    isRequired = false,
                    isValid = validate,
                    errorMessages = msgs,
                ) {
                    OutlinedTextField(
                        value = string ?: "",
                        onValueChange = { string = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Age field
            FormItem<Int>(name = "age") { (state, validate, msgs) ->
                var age by state
                FormField(
                    title = "Age",
                    isRequired = false,
                    isValid = validate,
                    errorMessages = msgs,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TButton(text = "1", enabled = age != 1) {
                            form.setFieldValue("age" to 1)
                        }
                        TButton(text = "3", enabled = age != 3) {
                            form.setFieldValue("age" to 3)
                        }
                        TButton(text = "5", enabled = age != 5) {
                            form.setFieldValue("age" to 5)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display monitoring results
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Field Watching Results",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Using Form.useWatch<T>(fieldName, formInstance)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Watched name: ${name ?: "(not set)"}")
                Text("Watched age: ${age ?: "(not set)"}")
            }
        }
    }
}

const val CHINA_ID_REGEX =
    """^\d{6}(18|19|20)?\d{2}(0[1-9]|1[12])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$"""
