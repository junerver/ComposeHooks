package xyz.junerver.composehooks.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.regex.Pattern
import xyz.junerver.compose.hooks.useMount
import xyz.junerver.compose.hooks.useform.CustomValidator
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Form
import xyz.junerver.compose.hooks.useform.Mobile
import xyz.junerver.compose.hooks.useform.Phone
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.useForm
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

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
            "name" to "default",
            "mobile" to "111"
        )
    }

    Surface {
        Column {
            Form(form) {
                FormItem<String>(name = "name") { (state, validate, msgs) ->
                    var string by state
                    ItemRow(title = "name") {
                        OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                FormItem<Int>(name = "age", listOf(Required())) { (state, validate, msgs) ->
                    var age by state
                    ItemRow(title = "* age") {
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
                Spacer(modifier = Modifier.height(18.dp))
                FormItem<String>(
                    name = "mobile",
                    listOf(Mobile(), Required())
                ) { (state, validate, msgs) ->
                    var string by state
                    ItemRow(title = "* mobile") {
                        Column {
                            OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                            Text(text = "$validate  ${msgs.joinToString("、")}")
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                FormItem<String>(
                    name = "phone",
                    listOf(Phone())
                ) { (state, validate, msgs) ->
                    var string by state
                    ItemRow(title = "phone") {
                        Column {
                            OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                            Text(text = "$validate  ${msgs.joinToString("、")}")
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
                FormItem<String>(
                    name = "email",
                    listOf(Email(), Required())
                ) { (state, validate, msgs) ->
                    var string by state
                    ItemRow(title = "* email") {
                        Column {
                            OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                            Text(text = "$validate  ${msgs.joinToString("、")}")
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
                FormItem<String>(
                    name = "id",
                    listOf(
                        object : CustomValidator("id number err", {
                            !it.asBoolean() || (it is String && Pattern.matches(CHINA_ID_REGEX, it))
                        }) {}
                    )
                ) { (state, validate, msgs) ->
                    var string by state
                    ItemRow(title = "id") {
                        Column {
                            OutlinedTextField(value = string ?: "", onValueChange = { string = it })
                            Text(text = "$validate  ${msgs.joinToString("、")}")
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
            TButton(text = "submit") {
                toast(form.getAllFields().toString() + "isValidated :" + form.isValidated())
            }
        }
    }
}

@Composable
private fun ItemRow(title: String, content: @Composable () -> Unit) {
    Row {
        Text(text = "$title :")
        Box {
            content()
        }
    }
}

const val CHINA_ID_REGEX =
    """^\d{6}(18|19|20)?\d{2}(0[1-9]|1[12])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$"""
