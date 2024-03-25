package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.useForm
import xyz.junerver.composehooks.ui.component.TButton

/**
 * Description:
 *
 * @author Junerver date: 2024/3/25-8:48 Email: junerver@gmail.com Version:
 *     v1.0
 */
@Composable
fun UseFormExample() {
    val form = useForm()
    useMount {
        form.setFieldsValue(
            mapOf(
                "name" to "default"
            )
        )
    }
    Surface {
        Column {
            Form(form) {
                FormItem<String>(name = "name") {(state,validate,msgs)->
                    var string by state
                    OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                }
                FormItem<Int>(name = "age", listOf(Required())) { (state,validate,msgs)->
                    var age by state
                    Row {
                        TButton(text = "1") {
                            age = 1
                        }
                        TButton(text = "3") {
                            age = 3
                        }
                        TButton(text = "5") {
                            age = 5
                        }
                        TButton(text = "null") {
                            age = null
                        }
                        Text(text = "$validate  ${msgs.joinToString("、")}")
                    }
                }
            }
            TButton(text = "submit") {
                toast(form.getAllFields().toString() +"isValidated :"+form.isValidated())
            }
        }
    }
}
