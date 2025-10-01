package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.CharacterInfo
import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object AppNameValidator {
    // App name validation constants
    private const val MIN_LENGTH = 3
    const val MAX_LENGTH = 50

    // Regex patterns for validation
    private const val VALID_CHARS_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{S}\\p{Z}&&[^<>]]*$"
    private const val BALANCED_PARENTHESES_REGEX = "^[^()]*$|^[^()]*\\([^()]*\\)[^()]*$"
    private const val NO_EXCESSIVE_SPACES_REGEX = "^(?!.*\\s{3,}).*$"

    private val VALID_CHARS_PATTERN = Pattern.compile(VALID_CHARS_REGEX)
    private val BALANCED_PARENTHESES_PATTERN = Pattern.compile(BALANCED_PARENTHESES_REGEX)
    private val NO_EXCESSIVE_SPACES_PATTERN = Pattern.compile(NO_EXCESSIVE_SPACES_REGEX)

    // Common inappropriate patterns for app names
    private val INAPPROPRIATE_PATTERNS = listOf(
        "\\btest\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\bdemo\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\bsample\\b".toRegex(RegexOption.IGNORE_CASE),
        "^\\s*$".toRegex(), // Only whitespace
        "^[^\\p{L}\\p{N}]*$".toRegex(), // Only special characters
        "\\b(hack|crack|pirate|cheat)\\b".toRegex(RegexOption.IGNORE_CASE)
    )

    // Reserved app names or terms that shouldn't be used
    private val RESERVED_NAMES = setOf(
        "android", "google", "app", "application", "system", "admin", "root",
        "test", "demo", "sample", "example", "default", "null", "undefined"
    )

    /**
     * Validates app name content and format
     * @param name The app name string to validate
     * @return ValidationResult containing validation status and error message
     */
    fun validateAppName(name: String?): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult(false, "App name is required")
        }

        val trimmedName = name.trim()

        // Check length constraints
        if (trimmedName.length < MIN_LENGTH) {
            return ValidationResult(false, "App name must be at least $MIN_LENGTH characters")
        }

        if (trimmedName.length > MAX_LENGTH) {
            return ValidationResult(false, "App name cannot exceed $MAX_LENGTH characters")
        }

        // Check for valid characters (letters, numbers, spaces, basic punctuation)
        if (!VALID_CHARS_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult(false, "App name contains invalid characters")
        }

        // Check for excessive spaces
        if (!NO_EXCESSIVE_SPACES_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult(false, "Avoid excessive spaces in app name")
        }

        // Check for balanced parentheses
        if (!BALANCED_PARENTHESES_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult(false, "Unbalanced parentheses in app name")
        }

        // Check for inappropriate content
        for (pattern in INAPPROPRIATE_PATTERNS) {
            if (pattern.containsMatchIn(trimmedName)) {
                return ValidationResult(false, "App name contains inappropriate content")
            }
        }

        // Check for reserved names
        val lowerName = trimmedName.lowercase()
        if (RESERVED_NAMES.any { reserved -> lowerName.contains(reserved) && lowerName.length <= reserved.length + 2 }) {
            return ValidationResult(false, "App name contains reserved terms")
        }

        // Check if name starts or ends with special characters
        if (trimmedName.matches(Regex("^[\\p{P}\\p{S}].*")) || trimmedName.matches(Regex(".*[\\p{P}\\p{S}]$"))) {
            return ValidationResult(false, "App name cannot start or end with special characters")
        }

        // Check if name is all uppercase (not user-friendly)
        if (trimmedName == trimmedName.uppercase() && trimmedName.length > 5) {
            return ValidationResult(false, "Avoid using all uppercase letters")
        }

        return ValidationResult(true, "Valid app name")
    }

    /**
     * Formats and cleans app name
     */
    fun formatAppName(name: String): String {
        if (name.isBlank()) return name

        return name
            .trim()
            .replace("\\s+".toRegex(), " ") // Replace multiple spaces with single space
            .split(" ")
            .joinToString(" ") { word ->
                // Capitalize first letter of each word while preserving intentional casing
                if (word.isNotEmpty()) {
                    word.first().uppercase() + word.drop(1)
                } else {
                    word
                }
            }
            .take(MAX_LENGTH)
    }

    /**
     * Gets character count information
     */
    fun getCharacterInfo(name: String): CharacterInfo {
        val characterCount = name.length
        val wordCount = if (name.isBlank()) 0 else name.split("\\s+".toRegex()).size
        val remainingChars = MAX_LENGTH - characterCount

        return CharacterInfo(
            characterCount = characterCount,
            wordCount = wordCount,
            lineCount = null,
            remainingChars = remainingChars,
            isNearLimit = remainingChars <= 5
        )
    }

    /**
     * Suggests common app name patterns
     */
    fun getAppNameSuggestions(): List<String> {
        return listOf(
            "Mock Merchant Application",
            "Payment Gateway App",
            "Merchant Payment Portal",
            "Digital Payment Hub",
            "Secure Payment App",
            "Merchant Transaction Center",
            "Payment Processing Tool",
            "Business Payment Suite",
            "Mobile Payment Gateway",
            "Merchant Services App"
        )
    }
}