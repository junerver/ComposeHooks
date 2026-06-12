package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.useForm

@Composable
fun UseFormExample() {
    val form = Form.useForm()


    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "useForm Example",
            style = MaterialTheme.typography.headlineMedium,
        )

        Form(formInstance = form, onSubmit = { values ->
            println("Form submitted: $values")
        }) {
            FormItem<String>(
                name = "username",
                Required("Username is required"),
            ) { (state, isValid, errors) ->
                var value by state
                Column {
                    OutlinedTextField(
                        value = value ?: "",
                        onValueChange = { value = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!isValid) {
                        Text(errors.joinToString(), color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            FormItem<String>(
                name = "email",
                Required("Email is required"),
                Email("Invalid email format"),
            ) { (state, isValid, errors) ->
                var value by state
                Column {
                    OutlinedTextField(
                        value = value ?: "",
                        onValueChange = { value = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!isValid) {
                        Text(errors.joinToString(), color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                androidx.compose.material3.Button(
                    onClick = { form.resetFields() },
                ) {
                    Text("Reset")
                }
                androidx.compose.material3.Button(
                    enabled = form.isValidated(),
                    onClick = { form.submit() },
                ) {
                    Text("Submit")
                }
            }
        }
    }
}
