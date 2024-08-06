package xyz.junerver.compose.hooks.useform

import java.util.regex.Pattern
import xyz.junerver.kotlin.asBoolean
import xyz.junerver.kotlin.isEmail
import xyz.junerver.kotlin.isMobile
import xyz.junerver.kotlin.isPhone

private const val EMAIL_MESSAGE = "invalid email address"
private const val PHONE_MESSAGE = "invalid phone number"
private const val MOBILE_MESSAGE = "invalid mobile number"
private const val REQUIRED_MESSAGE = "this field is required"
private const val REGEX_MESSAGE = "value does not match the regex"

/**
 * Validator，used to verify whether form fields are legal
 *
 * @constructor Create empty Validator
 */
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
abstract class CustomValidator(
    override val message: String,
    val validator: (field: Any?) -> Boolean,
) : Validator

/**
 * Verify the value of a field using a list of validators
 *
 * @param fieldValue Field that need to be verified
 * @param pass Verification success callback
 * @param fail Verification failure callback
 * @receiver
 * @receiver
 * @return Is pass all validators
 */
fun Array<Validator>.validateField(
    fieldValue: Any?,
    pass: Validator.() -> Boolean,
    fail: Validator.() -> Boolean,
): Boolean {
    return this.map {
        fun Any?.validate(validator: Validator, condition: Any?.() -> Boolean): Boolean {
            return if (this.condition()) {
                validator.pass()
            } else {
                validator.fail()
            }
        }

        when (it) {
            is Required -> fieldValue.validate(it) {
                asBoolean()
            }

            is Email -> fieldValue.validate(it) {
                !this.asBoolean() || (this is String && this.isEmail())
            }

            is Phone -> fieldValue.validate(it) {
                !this.asBoolean() || (this is String && this.isPhone())
            }

            is Mobile -> fieldValue.validate(it) {
                !this.asBoolean() || (this is String && this.isMobile())
            }

            is Regex -> fieldValue.validate(it) {
                !this.asBoolean() || (this is String && Pattern.matches(it.regex, this))
            }

            is CustomValidator -> fieldValue.validate(it) {
                it.validator(this)
            }
        }
    }.all { it }
}
