package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.CharacterInfo
import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object TransactionNoteValidator {
    // Note validation patterns
    private const val MIN_LENGTH = 0  // Notes are optional
    public const val MAX_LENGTH = 280  // Like Twitter character limit
    private const val MAX_LINES = 6

    // Patterns for validation
    private const val VALID_CHARACTERS_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{S}\\p{Z}\\n\\r]*$"
    private const val EXCESSIVE_WHITESPACE_REGEX = "\\s{3,}" // 3+ consecutive spaces
    private const val EXCESSIVE_NEWLINES_REGEX = "\\n{3,}" // 3+ consecutive newlines

    private val VALID_CHARACTERS_PATTERN = Pattern.compile(VALID_CHARACTERS_REGEX)
    private val EXCESSIVE_WHITESPACE_PATTERN = Pattern.compile(EXCESSIVE_WHITESPACE_REGEX)
    private val EXCESSIVE_NEWLINES_PATTERN = Pattern.compile(EXCESSIVE_NEWLINES_REGEX)

    // Common inappropriate content patterns (basic)
    private val INAPPROPRIATE_PATTERNS = listOf(
        "\\b(spam|scam|fraud)\\b".toRegex(RegexOption.IGNORE_CASE),
        "\\b(hack|phishing|steal)\\b".toRegex(RegexOption.IGNORE_CASE),
        "^\\s*test\\s*$".toRegex(RegexOption.IGNORE_CASE), // Just "test"
        "^\\s*\\.+\\s*$".toRegex(), // Just dots
        "^\\s*-+\\s*$".toRegex()  // Just dashes
    )

    /**
     * Validates transaction note content
     * @param note The note string to validate
     * @param isRequired Whether the note is required (false by default)
     * @return ValidationResult containing validation status and error message
     */
    fun validateTransactionNote(note: String?, isRequired: Boolean = false): ValidationResult {
        if (note.isNullOrBlank()) {
            return if (isRequired) {
                ValidationResult(false, "Transaction note is required")
            } else {
                ValidationResult(true, "Valid empty note")
            }
        }

        val trimmedNote = note.trim()

        // Check length constraints
        if (trimmedNote.length > MAX_LENGTH) {
            return ValidationResult(false, "Note cannot exceed $MAX_LENGTH characters")
        }

        if (isRequired && trimmedNote.length < 3) {
            return ValidationResult(false, "Note must be at least 3 characters when provided")
        }

        // Check line count
        val lineCount = trimmedNote.split('\n').size
        if (lineCount > MAX_LINES) {
            return ValidationResult(false, "Note cannot exceed $MAX_LINES lines")
        }

        // Check for valid characters
        if (!VALID_CHARACTERS_PATTERN.matcher(trimmedNote).matches()) {
            return ValidationResult(false, "Note contains invalid characters")
        }

        // Check for excessive whitespace
        if (EXCESSIVE_WHITESPACE_PATTERN.matcher(trimmedNote).find()) {
            return ValidationResult(false, "Avoid excessive spaces in note")
        }

        // Check for excessive newlines
        if (EXCESSIVE_NEWLINES_PATTERN.matcher(trimmedNote).find()) {
            return ValidationResult(false, "Avoid excessive line breaks in note")
        }

        // Check for inappropriate content
        for (pattern in INAPPROPRIATE_PATTERNS) {
            if (pattern.containsMatchIn(trimmedNote)) {
                return ValidationResult(false, "Note contains inappropriate content")
            }
        }

        // Check for notes that are just special characters
        if (trimmedNote.matches(Regex("^[^\\p{L}\\p{N}]+$")) && trimmedNote.length > 10) {
            return ValidationResult(false, "Note should contain meaningful text")
        }

        return ValidationResult(true, "Valid transaction note")
    }

    /**
     * Formats and cleans transaction note
     */
    fun formatTransactionNote(note: String): String {
        if (note.isBlank()) return note

        return note
            .trim()
            .replace(
                EXCESSIVE_WHITESPACE_PATTERN.toRegex(),
                " "
            ) // Replace multiple spaces with single space
            .replace(
                EXCESSIVE_NEWLINES_PATTERN.toRegex(),
                "\n\n"
            ) // Replace multiple newlines with double newline
            .lines()
            .joinToString("\n") { line -> line.trim() } // Trim each line
            .take(MAX_LENGTH) // Ensure max length
    }

    /**
     * Gets character count including formatting information
     */
    fun getCharacterInfo(note: String): CharacterInfo {
        val characterCount = note.length
        val wordCount = if (note.isBlank()) 0 else note.split("\\s+".toRegex()).size
        val lineCount = note.split('\n').size
        val remainingChars = MAX_LENGTH - characterCount

        return CharacterInfo(
            characterCount = characterCount,
            wordCount = wordCount,
            lineCount = lineCount,
            remainingChars = remainingChars,
            isNearLimit = remainingChars <= 20
        )
    }

    /**
     * Suggests common transaction note templates
     */
    fun getCommonNoteTemplates(): List<String> {
        return listOf(
            "Payment for services rendered",
            "Monthly subscription fee",
            "Product purchase - Order #",
            "Refund for cancelled order",
            "Advance payment",
            "Balance adjustment",
            "Service charge",
            "Membership fee",
            "Consultation payment",
            "Equipment rental"
        )
    }
}