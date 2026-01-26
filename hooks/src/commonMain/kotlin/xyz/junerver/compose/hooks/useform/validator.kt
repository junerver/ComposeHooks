package xyz.junerver.compose.hooks.useform

import xyz.junerver.compose.hooks.utils.asBoolean
import xyz.junerver.compose.hooks.utils.isEmail
import xyz.junerver.compose.hooks.utils.isMobile
import xyz.junerver.compose.hooks.utils.isPhone

/**
 * Default validation error messages for common validators
 */
private const val EMAIL_MESSAGE = "invalid email address"
private const val PHONE_MESSAGE = "invalid phone number"
private const val MOBILE_MESSAGE = "invalid mobile number"
private const val REQUIRED_MESSAGE = "this field is required"
private const val REGEX_MESSAGE = "value does not match the regex"

/**
 * Defines when form field validation should be triggered.
 *
 * @property OnChange Validate immediately when the field value changes (default behavior)
 * @property OnBlur Validate when the field loses focus
 * @property OnSubmit Validate only when the form is submitted
 */
enum class ValidationTrigger {
    OnChange,
    OnBlur,
    OnSubmit,
}

/**
 * Base interface for form field validators.
 * Validators are used to check if form field values meet specific criteria.
 * Each validator provides a message that is displayed when validation fails.
 *
 * @see Email
 * @see Phone
 * @see Mobile
 * @see Required
 * @see Regex
 * @see CustomValidator
 */
sealed interface Validator {
    /**
     * Error message to display when validation fails
     */
    val message: String

    /**
     * Verification function of the [Validator]
     */
    val validator: (field: Any?) -> Boolean
}

/**
 * Validates email addresses using a standard email format.
 *
 * @property message Custom error message, defaults to [EMAIL_MESSAGE]
 *
 * @example
 * ```kotlin
 * FormItem<String>(
 *     name = "email",
 *     Email("Please enter a valid email"),
 *     Required()
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
data class Email(override val message: String = EMAIL_MESSAGE) : Validator {
    override val validator: (field: Any?) -> Boolean = { field -> !field.asBoolean() || (field is String && field.isEmail()) }
}

/**
 * Validates phone numbers using a standard phone format.
 *
 * @property message Custom error message, defaults to [PHONE_MESSAGE]
 *
 * @example
 * ```kotlin
 * FormItem<String>(
 *     name = "phone",
 *     Phone("Invalid phone format"),
 *     Required()
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
data class Phone(override val message: String = PHONE_MESSAGE) : Validator {
    override val validator: (field: Any?) -> Boolean = { field -> !field.asBoolean() || (field is String && field.isPhone()) }
}

/**
 * Validates mobile phone numbers using a standard mobile format.
 *
 * @property message Custom error message, defaults to [MOBILE_MESSAGE]
 *
 * @example
 * ```kotlin
 * FormItem<String>(
 *     name = "mobile",
 *     Mobile("Please enter a valid mobile number"),
 *     Required()
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
data class Mobile(override val message: String = MOBILE_MESSAGE) : Validator {
    override val validator: (field: Any?) -> Boolean = { field -> !field.asBoolean() || (field is String && field.isMobile()) }
}

/**
 * Validates that a field has a non-null, non-empty value.
 *
 * @property message Custom error message, defaults to [REQUIRED_MESSAGE]
 *
 * @example
 * ```kotlin
 * FormItem<String>(
 *     name = "username",
 *     Required("Username is required")
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
data class Required(override val message: String = REQUIRED_MESSAGE) : Validator {
    override val validator: (field: Any?) -> Boolean = { field -> field.asBoolean() }
}

/**
 * Validates that a string value matches a specific regular expression pattern.
 *
 * @property message Custom error message, defaults to [REGEX_MESSAGE]
 * @property regex Regular expression pattern to match against
 *
 * @example
 * ```kotlin
 * FormItem<String>(
 *     name = "password",
 *     Regex(
 *         message = "Password must contain at least 8 characters",
 *         regex = "^.{8,}$"
 *     )
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
data class Regex(override val message: String = REGEX_MESSAGE, val regex: String) : Validator {
    override val validator: (field: Any?) -> Boolean =
        { field -> !field.asBoolean() || (field is String && field.matches(regex.toRegex())) }
}

/**
 * Base class for creating custom validators with custom validation logic.
 *
 * @property message Error message to display when validation fails
 * @property validator Function that performs the validation, returns true if valid
 *
 * @example
 * ```kotlin
 * // Custom validator for checking if a number is positive
 * class PositiveNumber : CustomValidator(
 *     message = "Number must be positive",
 *     validator = { field ->
 *         field is Number && field.toDouble() > 0
 *     }
 * )
 *
 * FormItem<Int>(
 *     name = "age",
 *     PositiveNumber(),
 *     Required()
 * ) { (state, isValid, errors) ->
 *     // Form field content
 * }
 * ```
 */
abstract class CustomValidator(
    override val message: String,
    override val validator: (field: Any?) -> Boolean,
) : Validator

/**
 * Extension function that applies a list of validators to a field value.
 * This function processes each validator in sequence and collects validation results.
 *
 * @param fieldValue The value to validate
 * @param pass Callback function called when a validator passes
 * @param fail Callback function called when a validator fails
 * @return true if all validators pass, false otherwise
 *
 * @example
 * ```kotlin
 * val validators = arrayOf(
 *     Required("Field is required"),
 *     Email("Invalid email format")
 * )
 *
 * val isValid = validators.validateField(
 *     fieldValue = "test@example.com",
 *     pass = { /* handle pass */ true },
 *     fail = { /* handle fail */ false }
 * )
 * ```
 */
fun Array<Validator>.validateField(fieldValue: Any?, pass: Validator.() -> Boolean, fail: Validator.() -> Boolean): Boolean = this.map {
    fun Any?.validate(validator: Validator, condition: Any?.() -> Boolean): Boolean = if (this.condition()) {
        validator.pass()
    } else {
        validator.fail()
    }
    fieldValue.validate(it) {
        it.validator(this)
    }
}.all { it }
