@file:Suppress("UNCHECKED_CAST", "USELESS_CAST")

package xyz.junerver.compose.hooks.useform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import kotlin.reflect.KClass
import xyz.junerver.compose.hooks.Ref
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.createContext
import xyz.junerver.compose.hooks.genFormFieldKey
import xyz.junerver.compose.hooks.useBoolean
import xyz.junerver.compose.hooks.useCreation
import xyz.junerver.compose.hooks.useEffect
import xyz.junerver.compose.hooks.useEventPublish
import xyz.junerver.compose.hooks.useMap
import xyz.junerver.compose.hooks.useState

/*
  Description: Headless Form Component
  Author: Junerver
  Date: 2024/3/25-8:11
  Email: junerver@gmail.com
  Version: v1.0
*/

internal val FormContext by lazy { createContext(FormInstance()) }

/**
 * Headless Form Component
 *
 * @param formInstance
 * @param content
 * @receiver
 */
@Composable
fun Form(formInstance: FormInstance = Form.useForm(), content: @Composable FormScope.() -> Unit) {
    val formRef = useCreation { FormRef() }
    formInstance.apply { this.formRef = formRef }
    FormContext.Provider(formInstance) {
        FormScope.getInstance(formRef, formInstance).content()
    }
}

/**
 * Form Component scope
 *
 * @constructor Create empty Form scope
 * @property formRefRef
 * @property formInstance
 */
class FormScope private constructor(
    private val formRefRef: Ref<FormRef>,
    private val formInstance: FormInstance,
) {
    @Deprecated(
        "use vararg params",
        ReplaceWith("FormItem(name = name, validators = validators.toTypedArray(), content = content)")
    )
    @Composable
    fun <T : Any> FormItem(
        name: String,
        validators: List<Validator> = emptyList(),
        content: @Composable (Triple<MutableState<T?>, Boolean, List<String>>) -> Unit,
    ) = FormItem(
        name = name,
        validators = validators.toTypedArray(),
        content = content
    )

    /**
     * 表单字段容器组件
     *
     * FormItem Component
     *
     * @param name
     * @param validators
     * @param content 子组件函数，通过参数提供字段状态、是否通过校验、错误信息列表
     * @param T
     * @receiver
     */
    @Composable
    fun <T : Any> FormItem(
        name: String,
        vararg validators: Validator,
        content: @Composable (Triple<MutableState<T?>, Boolean, List<String>>) -> Unit,
    ) {
        val fieldState = _useState<T?>(default = null)
        val (validate, _, set) = useBoolean()
        val errMsg = useMap<KClass<*>, String>()
        val currentFormRef: FormRef = formRefRef.current
        @Suppress("UNCHECKED_CAST")
        currentFormRef.formFieldMap[name] = fieldState as MutableState<Any?>
        val publish = useEventPublish<T?>(name.genFormFieldKey(formInstance))
        useEffect(fieldState) {
            currentFormRef.formOperationCount.longValue += 1
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
                (validators as Array<Validator>).validateField(fieldValue, pass = Validator::pass, fail = Validator::fail)
            set(isValidate)
            currentFormRef.formFieldValidationMap[name] = isValidate
        }
        useEffect(errMsg) {
            currentFormRef.formFieldErrorMessagesMap[name] = errMsg.values.toList()
        }
        content(Triple(fieldState, validate.value, errMsg.values.toList()))
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
        internal fun getInstance(ref: Ref<FormRef>, formInstance: FormInstance) = FormScope(ref, formInstance)
    }
}
