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
import xyz.junerver.compose.hooks.createContext
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
    private val formRefRef: Ref<FormRef>,
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
        val currentFormRef: FormRef = formRefRef.current
        @Suppress("UNCHECKED_CAST")
        currentFormRef.formFieldMap[name] = fieldState as MutableState<Any?>
        val publish = useEventPublish<T?>("HOOK_INTERNAL_FORM_FIELD_${formInstance}_$name")
        useEffect(fieldState.value) {
            currentFormRef.formOperationCount.longValue += 1
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
            currentFormRef.formFieldValidationMap[name] = isValidate
        }
        useEffect(errMsg) {
            currentFormRef.formFieldErrorMessagesMap[name] = errMsg.values.toList()
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
        val counter by formRef.current.formOperationCount
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
    /**
     * Corresponds to the data map in a [Form] component
     */
    val formFieldMap: MutableMap<String, MutableState<Any?>> = mutableMapOf(),
    /**
     * A map corresponding to whether each field in a [Form] component passes the verification
     */
    val formFieldValidationMap: MutableMap<String, Boolean> = mutableMapOf(),
) {
    // Counter that records data changes in the form
    internal val formOperationCount: MutableLongState = mutableLongStateOf(0L)
    // Record the error message of each field verification failure in the form
    internal val formFieldErrorMessagesMap: MutableMap<String, List<String>> = mutableMapOf()

    /** Is all fields in the form are verified successfully */
    val isValidated: Boolean
        get() {
            return formFieldValidationMap.isEmpty() || formFieldValidationMap.entries.map { it.value }
                .all { it }
        }
}

internal val FormContext = createContext(FormInstance())

@Composable
fun Form(formInstance: FormInstance = Form.useForm(), children: @Composable FormScope.() -> Unit) {
    val formRef = useCreation { FormRef() }
    formInstance.apply { this.formRef = formRef }
    FormContext.Provider(formInstance) {
        FormScope.getInstance(formRef, formInstance).children()
    }
}
