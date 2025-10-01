package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult

object AppIDValidator {
    /*  Allowed pattern:
        - a.b.c style segments
        - each segment starts with letter
        - only lowercase letters, digits and underscore inside segments
        - 2–255 total chars
     */
    private val SEGMENT_REGEX = Regex("[a-z][a-z0-9_]*")
    private val FULL_REGEX =
        Regex("^(${SEGMENT_REGEX.pattern})(\\.(${SEGMENT_REGEX.pattern}))*$")

    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 255

    fun validate(appId: String?): ValidationResult {
        if (appId.isNullOrBlank()) {
            return ValidationResult(false, "App-ID is required")
        }
        val value = appId.trim()
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            return ValidationResult(false, "Length must be $MIN_LENGTH-$MAX_LENGTH characters")
        }
        if (!FULL_REGEX.matches(value)) {
            return ValidationResult(
                false,
                "Invalid format. Use lowercase letters, digits or _ separated by ‘.’"
            )
        }
        return ValidationResult(true, null)
    }
}