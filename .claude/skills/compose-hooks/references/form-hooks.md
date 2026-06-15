# 表单 Hooks

## 目录

- [Form.useForm](#formuseform)
- [Form.useWatch](#formusewatch)
- [Form.useFormInstance](#formuseforminstance)

---

## Form.useForm

创建表单实例，用于管理表单状态和验证。

```kotlin
val form = Form.useForm()

// 表单布局
Form(form) {
    FormItem(name = "username", label = "用户名") {
        OutlinedTextField(
            value = form.getFieldValue("username") ?: "",
            onValueChange = { form.setFieldValue("username", it) }
        )
    }

    FormItem(name = "password", label = "密码") {
        OutlinedTextField(
            value = form.getFieldValue("password") ?: "",
            onValueChange = { form.setFieldValue("password", it) }
        )
    }
}

// 表单操作
Button(onClick = {
    form.validate().then { values ->
        println("表单值: $values")
    }
}) {
    Text("提交")
}

Button(onClick = { form.resetFields() }) {
    Text("重置")
}
```

### 验证规则

```kotlin
val form = Form.useForm()

Form(form) {
    FormItem(
        name = "email",
        label = "邮箱",
        rules = listOf(
            Rule.Required("请输入邮箱"),
            Rule.Email("邮箱格式不正确"),
        )
    ) {
        OutlinedTextField(
            value = form.getFieldValue("email") ?: "",
            onValueChange = { form.setFieldValue("email", it) }
        )
    }
}
```

---

## Form.useWatch

监听表单中指定字段的值变化。

```kotlin
val form = Form.useForm()
val username = Form.useWatch("username", form)

// username 会随表单中 username 字段的变化而更新
Text("当前用户名: ${username ?: "未设置"}")
```

### 监听多个字段

```kotlin
val form = Form.useForm()
val password = Form.useWatch("password", form)

// 密码强度提示
val strength = when {
    (password?.length ?: 0) < 6 -> "弱"
    (password?.length ?: 0) < 10 -> "中"
    else -> "强"
}
Text("密码强度: $strength")
```

---

## Form.useFormInstance

在 Form 子组件中获取当前表单实例，无需通过参数传递。

```kotlin
// 父组件
val form = Form.useForm()
Form(form) {
    UsernameField()  // 子组件可以直接获取 form
}

// 子组件
@Composable
fun UsernameField() {
    val form = Form.useFormInstance()
    FormItem(name = "username", label = "用户名") {
        OutlinedTextField(
            value = form.getFieldValue("username") ?: "",
            onValueChange = { form.setFieldValue("username", it) }
        )
    }
}
```

---

## 依据

- Form hooks: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/hooks.kt
- Form 组件: hooks/src/commonMain/kotlin/xyz/junerver/compose/hooks/useform/Form.kt
