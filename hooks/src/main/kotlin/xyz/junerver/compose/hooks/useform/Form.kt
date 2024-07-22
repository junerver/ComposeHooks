package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useState
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
    private val formInstance: FormInstance,
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
        val currentFormRef: FormRef = ref.current
        @Suppress("UNCHECKED_CAST")
        currentFormRef.form[name] = fieldState as MutableState<Any?>
        val publish = useEventPublish<T?>("HOOK_INTERNAL_FORM_FIELD_${formInstance}_$name")
        useEffect(fieldState.value) {
            currentFormRef.opCount.longValue += 1
            @Suppress("UNCHECKED_CAST")
            publish(fieldState.value as? T)
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
                validators.validateField(fieldValue, pass = Validator::pass, fail = Validator::fail)
            set(isValidate)
            currentFormRef.fieldValidatedMap[name] = isValidate
        }
        useEffect(errMsg) {
            currentFormRef.fieldErrorMessagesMap[name] = errMsg.values.toList()
        }
        content(tuple(fieldState, validate, errMsg.values.toList()))
    }

    /**
     * 获取是否验证通过的**状态**，
     *
     * Get the [State] of whether the verification is passed or not
     *
     * @return
     */
    @Composable
    fun FormInstance._isValidated(): State<Boolean> {
        val counter by formRef.current.opCount
        return useState(counter) {
            isValidated()
        }
    }

    companion object {
        internal fun getInstance(ref: Ref<FormRef>, formInstance: FormInstance) =
            FormScope(ref, formInstance)
    }
}

@Stable
internal data class FormRef(
    val form: MutableMap<String, MutableState<Any?>> = mutableMapOf(),
    val fieldValidatedMap: MutableMap<String, Boolean> = mutableMapOf(),
) {
    internal val opCount: MutableLongState = mutableLongStateOf(0L)
    internal val fieldErrorMessagesMap: MutableMap<String, List<String>> = mutableMapOf()

    /** Is all fields in the form are verified successfully */
    val isValidated: Boolean
        get() {
            return fieldValidatedMap.isEmpty() || fieldValidatedMap.entries.map { it.value }
                .all { it }
        }
}

@Composable
fun Form(formInstance: FormInstance = Form.useForm(), children: @Composable FormScope.() -> Unit) {
    val formRef = useCreation { FormRef() }
    formInstance.apply { this.formRef = formRef }
    FormScope.getInstance(formRef, formInstance).children()
}
