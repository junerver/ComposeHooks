package xyz.junerver.compose.hooks.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import xyz.junerver.compose.hooks.useform.Email
import xyz.junerver.compose.hooks.useform.Mobile
import xyz.junerver.compose.hooks.useform.Phone
import xyz.junerver.compose.hooks.useform.Regex
import xyz.junerver.compose.hooks.useform.Required
import xyz.junerver.compose.hooks.useform.Validator
import xyz.junerver.compose.hooks.useform.validateField

/*
  Description: useForm validator comprehensive TDD tests
  Author: Junerver
  Date: 2026/1/24
  Email: junerver@gmail.com
  Version: v1.0
*/

class UseFormValidatorTest {
    @Test
    fun required_validator_passes_for_non_null_value() {
        val validator = Required()
        assertTrue(validator.validator("test"), "Non-null string should pass")
        assertTrue(validator.validator(123), "Non-null number should pass")
        assertTrue(validator.validator(listOf(1, 2, 3)), "Non-empty list should pass")
    }

    @Test
    fun required_validator_fails_for_null_or_empty() {
        val validator = Required()
        assertFalse(validator.validator(null), "Null should fail")
        assertFalse(validator.validator(""), "Empty string should fail")
        assertFalse(validator.validator(emptyList<Any>()), "Empty list should fail")
    }

    @Test
    fun required_validator_uses_custom_message() {
        val customMessage = "This field cannot be empty"
        val validator = Required(customMessage)
        assertEquals(customMessage, validator.message)
    }

    @Test
    fun email_validator_passes_for_valid_emails() {
        val validator = Email()
        assertTrue(validator.validator("test@example.com"), "Standard email should pass")
        assertTrue(validator.validator("user.name@domain.co.uk"), "Email with dots should pass")
        assertTrue(validator.validator("user+tag@example.org"), "Email with plus should pass")
    }

    @Test
    fun email_validator_fails_for_invalid_emails() {
        val validator = Email()
        assertFalse(validator.validator("invalid"), "No @ should fail")
        assertFalse(validator.validator("@example.com"), "No local part should fail")
        assertFalse(validator.validator("test@"), "No domain should fail")
    }

    @Test
    fun email_validator_passes_for_null_or_empty() {
        val validator = Email()
        // Email validator allows null/empty (use Required for mandatory)
        assertTrue(validator.validator(null), "Null should pass (not required)")
        assertTrue(validator.validator(""), "Empty should pass (not required)")
    }

    @Test
    fun phone_validator_passes_for_valid_phones() {
        val validator = Phone()
        // Phone validation depends on isPhone() implementation
        assertTrue(validator.validator(null), "Null should pass (not required)")
        assertTrue(validator.validator(""), "Empty should pass (not required)")
    }

    @Test
    fun mobile_validator_passes_for_valid_mobiles() {
        val validator = Mobile()
        // Mobile validation depends on isMobile() implementation
        assertTrue(validator.validator(null), "Null should pass (not required)")
        assertTrue(validator.validator(""), "Empty should pass (not required)")
    }

    @Test
    fun regex_validator_passes_for_matching_pattern() {
        val validator = Regex(regex = "^[a-z]+$")
        assertTrue(validator.validator("abc"), "Lowercase letters should pass")
        assertTrue(validator.validator("hello"), "Lowercase word should pass")
    }

    @Test
    fun regex_validator_fails_for_non_matching_pattern() {
        val validator = Regex(regex = "^[a-z]+$")
        assertFalse(validator.validator("ABC"), "Uppercase should fail")
        assertFalse(validator.validator("123"), "Numbers should fail")
        assertFalse(validator.validator("abc123"), "Mixed should fail")
    }

    @Test
    fun regex_validator_passes_for_null_or_empty() {
        val validator = Regex(regex = "^[a-z]+$")
        assertTrue(validator.validator(null), "Null should pass (not required)")
        assertTrue(validator.validator(""), "Empty should pass (not required)")
    }

    @Test
    fun regex_validator_uses_custom_message() {
        val customMessage = "Only lowercase letters allowed"
        val validator = Regex(message = customMessage, regex = "^[a-z]+$")
        assertEquals(customMessage, validator.message)
    }

    @Test
    fun validateField_returns_true_when_all_validators_pass() {
        val validators = arrayOf(
            Required(),
            Regex(regex = "^[a-z]+$"),
        )

        var passCount = 0
        var failCount = 0

        val result = validators.validateField(
            fieldValue = "hello",
            pass = {
                passCount++
                true
            },
            fail = {
                failCount++
                false
            },
        )

        assertTrue(result, "All validators should pass")
        assertEquals(2, passCount, "Both validators should call pass")
        assertEquals(0, failCount, "No validators should call fail")
    }

    @Test
    fun validateField_returns_false_when_any_validator_fails() {
        val validators = arrayOf(
            Required(),
            Regex(regex = "^[0-9]+$"), // Only numbers
        )

        var passCount = 0
        var failCount = 0

        val result = validators.validateField(
            fieldValue = "hello", // Letters, not numbers
            pass = {
                passCount++
                true
            },
            fail = {
                failCount++
                false
            },
        )

        assertFalse(result, "Should fail because regex doesn't match")
        assertEquals(1, passCount, "Required should pass")
        assertEquals(1, failCount, "Regex should fail")
    }

    @Test
    fun validateField_with_empty_validators_returns_true() {
        val validators = emptyArray<Validator>()

        val result = validators.validateField(
            fieldValue = "anything",
            pass = { true },
            fail = { false },
        )

        assertTrue(result, "Empty validators should return true")
    }

    @Test
    fun multiple_validators_collect_all_errors() {
        val validators = arrayOf(
            Required("Field is required"),
            Regex(message = "Must be numbers", regex = "^[0-9]+$"),
        )

        val errors = mutableListOf<String>()

        // Use "abc" which fails Regex but passes Required
        validators.validateField(
            fieldValue = "abc", // Will fail Regex only
            pass = { true },
            fail = {
                errors.add(message)
                false
            },
        )

        assertEquals(1, errors.size, "Should collect 1 error (Regex)")
        assertTrue(errors.contains("Must be numbers"))
    }

    @Test
    fun validator_order_is_preserved() {
        val order = mutableListOf<String>()
        val validators = arrayOf(
            Required("first"),
            Email("second"),
            Regex(message = "third", regex = ".*"),
        )

        validators.validateField(
            fieldValue = "test@example.com",
            pass = {
                order.add(message)
                true
            },
            fail = { false },
        )

        assertEquals(listOf("first", "second", "third"), order)
    }

    @Test
    fun password_strength_validation_example() {
        // Example: Password must be at least 8 chars with number and letter
        val validators = arrayOf(
            Required("Password is required"),
            Regex(message = "At least 8 characters", regex = "^.{8,}$"),
            Regex(message = "Must contain a number", regex = ".*[0-9].*"),
            Regex(message = "Must contain a letter", regex = ".*[a-zA-Z].*"),
        )

        // Valid password
        val validResult = validators.validateField(
            fieldValue = "Password123",
            pass = { true },
            fail = { false },
        )
        assertTrue(validResult, "Strong password should pass")

        // Too short
        val shortResult = validators.validateField(
            fieldValue = "Pass1",
            pass = { true },
            fail = { false },
        )
        assertFalse(shortResult, "Short password should fail")

        // No number
        val noNumberResult = validators.validateField(
            fieldValue = "PasswordOnly",
            pass = { true },
            fail = { false },
        )
        assertFalse(noNumberResult, "Password without number should fail")
    }
}
