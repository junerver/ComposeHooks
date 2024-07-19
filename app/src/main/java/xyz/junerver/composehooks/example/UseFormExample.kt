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
import xyz.junerver.compose.hooks.useform.useWatch
import xyz.junerver.composehooks.ui.component.TButton
import xyz.junerver.kotlin.asBoolean

/*
  Description:
  Author: Junerver
  Date: 2024/3/25-8:48
  Email: junerver@gmail.com
  Version: v1.0
*/
@Composable
fun UseFormExample() {
    val form = Form.useForm()
    useMount {
        form.setFieldsValue(
            "name" to "default",
            "mobile" to "111"
        )
    }
    val name by Form.useWatch<String>(fieldName = "name", formInstance = form)

    Surface {
        Column {
            Form(form) {
                val canSubmit by form._isValidated()
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
                            TButton(text = "1", enabled = age != 1) {
//                                age = 1
                                form.setFieldValue("age" to 1)
                            }
                            TButton(text = "3", enabled = age != 3) {
//                                age = 3
                                form.setFieldValue("age" to 3)
                            }
                            TButton(text = "5", enabled = age != 5) {
//                                age = 5
                                form.setFieldValue("age" to 5)
                            }
                            TButton(text = "null", enabled = age != null) {
//                                age = null
                                form.setFieldValue("age" to null)
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
                Row {
                    TButton(text = "submit", enabled = canSubmit) {
                        toast(form.getAllFields().toString() + "\nisValidated :" + form.isValidated())
                    }
                    TButton(text = "reset") {
                        form.resetFields()
                    }
                }
            }
            Text(text = "by use `Form.useWatch(fieldName,formInstance)`can watch a field\nname: $name")
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
