package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.isNull
import xyz.junerver.kotlin.tuple

/**
 * Description: Headless Form Component
 *
 * @author Junerver date: 2024/3/25-8:11 Email: junerver@gmail.com Version:
 *     v1.0
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
        ref.current.form[name] = fieldState as MutableState<Any>

        useEffect(fieldState.value) {
            val isValidate = validators.map {
                when (it) {
                    is Required -> {
                        if (fieldState.value.isNull) {
                            errMsg[Required::class] = "field:$name is required!"
                            return@map false
                        }
                        errMsg.remove(Required::class)
                        true
                    }

                    else -> {
                        true
                    }
                }
            }.all { it }
            set(isValidate)
            ref.current.isValidated = isValidate
        }
        content(tuple(fieldState, validate, errMsg.values.toList()))
    }

    companion object {
        fun getInstance(ref: Ref<FormRef>) =
            FormScope(ref)
    }
}

data class FormRef(
    var form: MutableMap<String, MutableState<Any>> = mutableMapOf(),
    var isValidated: Boolean = false,
)

@Composable
fun Form(formInstance: FormInstance = useForm(), children: @Composable FormScope.() -> Unit) {
    val formRef = useRef(default = FormRef())
    formInstance.apply { this.formRef = formRef }
    FormScope.getInstance(formRef).children()
}

/**
 * Validator，used to verify whether form fields are legal
 *
 * @constructor Create empty Validator
 */
private const val EMAIL_MESSAGE = "invalid email address"
private const val REQUIRED_MESSAGE = "this field is required"
private const val REGEX_MESSAGE = "value does not match the regex"

sealed interface Validator
data class Email(var message: String = EMAIL_MESSAGE) : Validator
data class Required(var message: String = REQUIRED_MESSAGE) : Validator
data class Regex(var message: String, var regex: String = REGEX_MESSAGE) : Validator
