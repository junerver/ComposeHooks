# 表单 Hooks

## 目录

- [Form.useForm](#formuseform)
- [Form.useWatch](#formusewatch)
- [Form.useFormInstance](#formuseforminstance)
- [remember 别名](#remember-别名)
- [FormItem](#formitem)
- [FormInstance](#forminstance)

---

## Form.useForm

创建 `FormInstance`。实例必须传入 `Form(form)` 后，才能调用 `setFieldValue`、`submit`、`resetFields` 等依赖内部 `FormRef` 的方法。
`Form.rememberForm()` 是等价别名。

```kotlin
val form = Form.useForm()

Form(
    formInstance = form,
    onSubmit = { values ->
        println("表单值: $values")
    },
) {
    FormItem<String>(
        name = "username",
        Required("请输入用户名"),
    ) { (state, isValid, errors) ->
        var value by state
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { value = it },
            isError = !isValid,
        )
        errors.firstOrNull()?.let { Text(it) }
    }

    FormItem<String>(
        name = "email",
        Required("请输入邮箱"),
        Email("邮箱格式不正确"),
    ) { (state, isValid, errors) ->
        var value by state
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { value = it },
            isError = !isValid,
        )
        errors.firstOrNull()?.let { Text(it) }
    }

    Button(onClick = { form.submit() }) {
        Text("提交")
    }

    Button(onClick = { form.resetFields() }) {
        Text("重置")
    }
}
```

---

## Form.useWatch

监听指定字段值，返回 `State<T?>`。字段值变化由 `FormItem` 内部发布。
`Form.rememberWatch()` 是等价别名。

```kotlin
val form = Form.useForm()

Form(formInstance = form) {
    val username by Form.useWatch<String>("username", form)
    Text("当前用户名: ${username ?: "未设置"}")

    FormItem<String>("username", Required()) { (state, isValid, errors) ->
        var value by state
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { value = it },
            isError = !isValid,
        )
        errors.firstOrNull()?.let { Text(it) }
    }
}
```

---

## Form.useFormInstance

在 `Form` 子组件中从上下文读取当前 `FormInstance`，避免层层传参。
`Form.rememberFormInstance()` 是等价别名。

```kotlin
val form = Form.useForm()

Form(formInstance = form) {
    UsernameField()
}

@Composable
private fun FormScope.UsernameField() {
    val form = Form.useFormInstance()

    FormItem<String>("username", Required()) { (state, isValid, errors) ->
        var value by state
        OutlinedTextField(
            value = value ?: "",
            onValueChange = { value = it },
            isError = !isValid,
        )
        errors.firstOrNull()?.let { Text(it) }
    }

    Button(onClick = { form.setFieldValue("username", "default") }) {
        Text("填充默认值")
    }
}
```

---

## remember 别名

表单 Hooks 使用 `Form` 对象作为作用域，因此别名也保留相同作用域：

- `Form.rememberForm()` -> `Form.useForm()`
- `Form.rememberWatch(fieldName, formInstance)` -> `Form.useWatch(fieldName, formInstance)`
- `Form.rememberFormInstance()` -> `Form.useFormInstance()`

---

## FormItem

`FormItem<T>` 是 `FormScope` 成员函数，只能在 `Form { }` 作用域内调用。它接收 `Validator` 可变参数，内容 lambda 解构为 `(MutableState<T?>, Boolean, List<String>)`。

```kotlin
FormItem<String>(
    name = "mobile",
    Required("请输入手机号"),
    Mobile("手机号格式不正确"),
) { (state, isValid, errors) ->
    var value by state
    OutlinedTextField(
        value = value ?: "",
        onValueChange = { value = it },
        isError = !isValid,
    )
    errors.forEach { Text(it) }
}
```

需要 touched/dirty 信息时使用 `FormItemWithState`：

```kotlin
FormItemWithState<String>(
    name = "email",
    Required("请输入邮箱"),
    Email("邮箱格式不正确"),
) { field ->
    var value by field.value
    OutlinedTextField(
        value = value ?: "",
        onValueChange = { value = it },
        isError = field.isTouched && !field.isValid,
    )
    if (field.isTouched && !field.isValid) {
        field.errors.forEach { Text(it) }
    }
}
```

内置校验器：

- `Required(message)`
- `Email(message)`
- `Phone(message)`
- `Mobile(message)`
- `Regex(message, regex)`
- 继承 `CustomValidator` 自定义校验

---

## FormInstance

常用方法：

- `getAllFields()`: 获取全部字段值。
- `setFieldValue(name, value)` / `setFieldValue(pair)`: 设置单个字段。
- `setFieldsValue(vararg pairs)` / `setFieldsValue(map)`: 批量设置字段。
- `resetFields()` / `resetFields(vararg pairs)`: 重置字段，可指定重置后的值。
- `isValidated()`: 当前是否全部校验通过。
- `_isValidated()`: `FormScope` 内的响应式校验状态扩展，返回 `State<Boolean>`。
- `getFieldError(name)` / `getAllFieldsErrors()`: 获取错误信息。
- `submit(onSuccess, onError)` 或 `submit()`: 提交表单。
- `isTouched(name)` / `isDirty(name)` / `getTouchedFields()` / `getDirtyFields()`: 字段交互状态。

```kotlin
val form = Form.useForm()

Form(formInstance = form) {
    val canSubmit by form._isValidated()

    Button(
        enabled = canSubmit,
        onClick = {
            form.submit(
                onSuccess = { values -> println(values) },
                onError = { errors -> println(errors) },
            )
        },
    ) {
        Text("提交")
    }
}
```

---

## 依据

- Form hooks: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/hooks.kt`
- Form 组件: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/Form.kt`
- FormInstance: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/FormInstance.kt`
- Validators: `hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/validator.kt`
