package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.tuple

/*
  Description: Headless Form Component
  Author: Junerver
  Date: 2024/3/25-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

class FormScope private constructor(
    private val ref: Ref<FormRef>,
) {

    @Composable
    fun <T : Any> FormItem(
        name: String,
        validators: List<Validator> = emptyList(),
        content: @Composable (Tuple3<MutableState<T?>, Boolean, List<String>>) -> Unit,
    ) {
        val fieldState = _useState<T?>(default = null)
        val (validate, _, set) = useBoolean()
        val errMsg = useMap<KClass<*>, String>()
        @Suppress("UNCHECKED_CAST")
        ref.current.form[name] = fieldState as MutableState<Any>

        useEffect(fieldState.value) {
            fun Validator.pass(): Boolean {
                errMsg.remove(this::class)
                return true
            }

            fun Validator.fail(): Boolean {
                errMsg[this::class] = this.message
                return false
            }

            @Suppress("RedundantNullableReturnType")
            val fieldValue: Any? = fieldState.value
            val isValidate =
                validators.validateField(fieldValue, pass = Validator::pass, fail = Validator::fail)
            set(isValidate)
            ref.current.fieldValidatedMap[name] = isValidate
        }
        content(tuple(fieldState, validate, errMsg.values.toList()))
    }

    companion object {
        internal fun getInstance(ref: Ref<FormRef>) =
            FormScope(ref)
    }
}

@Stable
internal data class FormRef(
    val form: MutableMap<String, MutableState<Any>> = mutableMapOf(),
    val fieldValidatedMap: MutableMap<String, Boolean> = mutableMapOf(),
) {
    /**
     * Is all fields in the form are verified successfully
     */
    val isValidated: Boolean
        get() {
            return fieldValidatedMap.isEmpty() || fieldValidatedMap.entries.map { it.value }
                .all { it }
        }
}

@Composable
fun Form(formInstance: FormInstance = useForm(), children: @Composable FormScope.() -> Unit) {
    val formRef = useCreation { FormRef() }
    formInstance.apply { this.formRef = formRef }
    FormScope.getInstance(formRef).children()
}
