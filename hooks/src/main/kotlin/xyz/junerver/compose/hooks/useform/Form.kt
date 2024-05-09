package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import java.util.regex.Pattern
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useRef
import xyz.junerver.kotlin.Tuple3
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.isEmail
import xyz.junerver.kotlin.isMobile
import xyz.junerver.kotlin.isPhone
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
                fun Validator.pass(): Boolean {
                    errMsg.remove(this::class)
                    return true
                }

                fun Validator.fail(): Boolean {
                    errMsg[this::class] = this.message
                    return false
                }

                fun Any?.validate(validator: Validator, condition: Any?.() -> Boolean): Boolean {
                    return if (this.condition()) {
                        validator.pass()
                    } else {
                        validator.fail()
                    }
                }

                val fieldValue: Any? = fieldState.value
                when (it) {
                    is Required -> {
                        return@map fieldValue.validate(it) {
                            asBoolean()
                        }
                    }

                    is Email -> {
                        return@map fieldValue.validate(it) {
                            !this.asBoolean() || (this is String && this.isEmail())
                        }
                    }

                    is Phone -> {
                        return@map fieldValue.validate(it) {
                            !this.asBoolean() || (this is String && this.isPhone())
                        }
                    }

                    is Mobile -> {
                        return@map fieldValue.validate(it) {
                            !this.asBoolean() || (this is String && this.isMobile())
                        }
                    }

                    is Regex -> {
                        return@map fieldValue.validate(it) {
                            !this.asBoolean() || (this is String && Pattern.matches(it.regex, this))
                        }
                    }

                    is CustomValidator -> {
                        return@map fieldValue.validate(it) {
                            it.validator(this)
                        }
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
private const val PHONE_MESSAGE = "invalid phone number"
private const val MOBILE_MESSAGE = "invalid mobile number"
private const val REQUIRED_MESSAGE = "this field is required"
private const val REGEX_MESSAGE = "value does not match the regex"

sealed interface Validator {

    /**
     * Prompt message after verification failure
     */
    val message: String
}

data class Email(override val message: String = EMAIL_MESSAGE) : Validator
data class Phone(override val message: String = PHONE_MESSAGE) : Validator
data class Mobile(override val message: String = MOBILE_MESSAGE) : Validator
data class Required(override val message: String = REQUIRED_MESSAGE) : Validator
data class Regex(override val message: String = REGEX_MESSAGE, val regex: String) : Validator
abstract class CustomValidator(override val message: String, val validator: (field: Any?) -> Boolean) :
    Validator
