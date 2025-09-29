package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object NameValidator {
    // Comprehensive name regex patterns
    private const val BASIC_NAME_REGEX = "^[A-Za-z][A-Za-z\\s.''-]{1,99}$"
    private const val INTERNATIONAL_NAME_REGEX = "^[\\p{L}][\\p{L}\\p{Mn}\\s.''-]{1,99}$"
    private const val SIMPLE_NAME_REGEX = "^[A-Za-z]+(?:[\\s.'-][A-Za-z]+)*$"

    private val BASIC_NAME_PATTERN = Pattern.compile(BASIC_NAME_REGEX)
    private val INTERNATIONAL_NAME_PATTERN = Pattern.compile(INTERNATIONAL_NAME_REGEX)
    private val SIMPLE_NAME_PATTERN = Pattern.compile(SIMPLE_NAME_REGEX)

    // Common invalid patterns to check against
    private val INVALID_PATTERNS = listOf(
        "^\\s+$", // Only whitespace
        ".*\\s{2,}.*", // Multiple consecutive spaces
        ".*[0-9].*", // Contains numbers
        ".*[@#$%^&*()+=\\[\\]{}|;:\"<>?/\\\\].*", // Contains special symbols
        "^[.''-].*", // Starts with punctuation
        ".*[.''-]$" // Ends with punctuation
    )

    /**
     * Validates if the given string is a valid person name
     * @param name The name string to validate
     * @param useInternational Whether to use international character support
     * @return ValidationResult containing validation status and error message
     */
    fun validateName(name: String?, useInternational: Boolean = false): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult(false, "Name cannot be empty")
        }

        val trimmedName = name.trim()

        // Check minimum length
        if (trimmedName.length < 2) {
            return ValidationResult(false, "Name must be at least 2 characters long")
        }

        // Check maximum length
        if (trimmedName.length > 100) {
            return ValidationResult(false, "Name cannot exceed 100 characters")
        }

        // Check for invalid patterns
        for (invalidPattern in INVALID_PATTERNS) {
            if (Pattern.matches(invalidPattern, trimmedName)) {
                return when {
                    invalidPattern.contains("\\s+") -> ValidationResult(
                        false,
                        "Name cannot contain only spaces"
                    )

                    invalidPattern.contains("\\s{2,}") -> ValidationResult(
                        false,
                        "Name cannot contain multiple consecutive spaces"
                    )

                    invalidPattern.contains("[0-9]") -> ValidationResult(
                        false,
                        "Name cannot contain numbers"
                    )

                    invalidPattern.contains("[@#$%^&*()+=") -> ValidationResult(
                        false,
                        "Name contains invalid special characters"
                    )

                    invalidPattern.contains("^[.''-]") -> ValidationResult(
                        false,
                        "Name cannot start with punctuation"
                    )

                    invalidPattern.contains("[.''-]$") -> ValidationResult(
                        false,
                        "Name cannot end with punctuation"
                    )

                    else -> ValidationResult(false, "Invalid name format")
                }
            }
        }

        // Validate using appropriate pattern
        val pattern = if (useInternational) INTERNATIONAL_NAME_PATTERN else SIMPLE_NAME_PATTERN

        if (!pattern.matcher(trimmedName).matches()) {
            return ValidationResult(
                false,
                "Please enter a valid name (letters, spaces, apostrophes, hyphens, and periods only)"
            )
        }

        // Additional checks for common edge cases
        if (trimmedName.count { it == '.' } > 2) {
            return ValidationResult(false, "Name cannot contain more than 2 periods")
        }

        if (trimmedName.count { it == '-' } > 2) {
            return ValidationResult(false, "Name cannot contain more than 2 hyphens")
        }

        if (trimmedName.count { it == '\'' } > 2) {
            return ValidationResult(false, "Name cannot contain more than 2 apostrophes")
        }

        return ValidationResult(true, "Valid name")
    }

    /**
     * Formats name with proper capitalization
     */
    fun formatName(name: String): String {
        if (name.isBlank()) return name

        return name.trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                when {
                    word.isEmpty() -> word
                    word.contains("'") -> formatWordWithApostrophe(word)
                    word.contains("-") -> formatWordWithHyphen(word)
                    word.contains(".") -> formatWordWithPeriod(word)
                    else -> word.lowercase().replaceFirstChar { it.uppercase() }
                }
            }
    }

    private fun formatWordWithApostrophe(word: String): String {
        return word.split("'").joinToString("'") { part ->
            if (part.isNotEmpty()) part.lowercase().replaceFirstChar { it.uppercase() } else part
        }
    }

    private fun formatWordWithHyphen(word: String): String {
        return word.split("-").joinToString("-") { part ->
            if (part.isNotEmpty()) part.lowercase().replaceFirstChar { it.uppercase() } else part
        }
    }

    private fun formatWordWithPeriod(word: String): String {
        // Handle cases like "Jr.", "Sr.", "III."
        return if (word.matches(Regex("^(Jr|Sr|[IVX]+)\\.?$", RegexOption.IGNORE_CASE))) {
            word.uppercase()
        } else {
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Checks if name appears to be a single word (likely incomplete)
     */
    fun isSingleWord(name: String): Boolean {
        return name.trim().split("\\s+".toRegex()).size == 1
    }
}