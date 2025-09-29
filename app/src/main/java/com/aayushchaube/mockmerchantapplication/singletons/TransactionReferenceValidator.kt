package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.enums.ReferenceType
import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object TransactionReferenceValidator {
    // Different transaction reference formats
    private const val UPI_REF_REGEX = "^[0-9]{12}$" // 12-digit UPI reference
    private const val BANK_REF_REGEX = "^[A-Z0-9]{6,25}$" // Bank transaction reference
    private const val GENERAL_REF_REGEX = "^[A-Z0-9._-]{4,50}$" // General alphanumeric reference
    private const val IMPS_REF_REGEX = "^[A-Z0-9]{12,16}$" // IMPS reference format
    private const val NEFT_REF_REGEX = "^[A-Z]{4}[0-9]{6,10}$" // NEFT reference format

    private val UPI_REF_PATTERN = Pattern.compile(UPI_REF_REGEX)
    private val BANK_REF_PATTERN = Pattern.compile(BANK_REF_REGEX)
    private val GENERAL_REF_PATTERN = Pattern.compile(GENERAL_REF_REGEX)
    private val IMPS_REF_PATTERN = Pattern.compile(IMPS_REF_REGEX)
    private val NEFT_REF_PATTERN = Pattern.compile(NEFT_REF_REGEX)

    /**
     * Validates if the given string is a valid transaction reference
     * @param reference The transaction reference string to validate
     * @param allowedTypes List of allowed reference types (null means all types allowed)
     * @return ValidationResult containing validation status and error message
     */
    fun validateTransactionReference(
        reference: String?,
        allowedTypes: List<ReferenceType>? = null
    ): ValidationResult {
        if (reference.isNullOrBlank()) {
            return ValidationResult(false, "Transaction reference cannot be empty")
        }

        val trimmedRef = reference.trim().uppercase()

        // Check minimum and maximum length
        if (trimmedRef.length < 4) {
            return ValidationResult(false, "Transaction reference must be at least 4 characters")
        }

        if (trimmedRef.length > 50) {
            return ValidationResult(false, "Transaction reference cannot exceed 50 characters")
        }

        // Check for invalid characters (only alphanumeric, dots, hyphens, underscores allowed)
        if (!trimmedRef.matches(Regex("^[A-Z0-9._-]+$"))) {
            return ValidationResult(false, "Reference can only contain letters, numbers, dots, hyphens, and underscores")
        }

        // Determine reference type and validate accordingly
        val referenceType = determineReferenceType(trimmedRef)

        // Check if reference type is allowed
        if (allowedTypes != null && referenceType !in allowedTypes) {
            return ValidationResult(false, "This reference format is not accepted")
        }

        // Validate based on specific type
        val typeValidation = validateByType(trimmedRef, referenceType)
        if (!typeValidation.isValid) {
            return typeValidation
        }

        return ValidationResult(true, "Valid transaction reference", referenceType)
    }

    /**
     * Determines the type of transaction reference
     */
    private fun determineReferenceType(reference: String): ReferenceType {
        return when {
            UPI_REF_PATTERN.matcher(reference).matches() -> ReferenceType.UPI_REFERENCE
            NEFT_REF_PATTERN.matcher(reference).matches() -> ReferenceType.NEFT_REFERENCE
            IMPS_REF_PATTERN.matcher(reference).matches() -> ReferenceType.IMPS_REFERENCE
            BANK_REF_PATTERN.matcher(reference).matches() -> ReferenceType.BANK_REFERENCE
            GENERAL_REF_PATTERN.matcher(reference).matches() -> ReferenceType.GENERAL_REFERENCE
            else -> ReferenceType.UNKNOWN
        }
    }

    /**
     * Validates reference based on its specific type
     */
    private fun validateByType(reference: String, type: ReferenceType): ValidationResult {
        return when (type) {
            ReferenceType.UPI_REFERENCE -> {
                if (reference.length == 12 && reference.all { it.isDigit() }) {
                    ValidationResult(true, "Valid UPI reference number")
                } else {
                    ValidationResult(false, "UPI reference must be exactly 12 digits")
                }
            }

            ReferenceType.NEFT_REFERENCE -> {
                if (reference.matches(Regex("^[A-Z]{4}[0-9]{6,10}$"))) {
                    ValidationResult(true, "Valid NEFT reference")
                } else {
                    ValidationResult(false, "NEFT reference should start with 4 letters followed by 6-10 digits")
                }
            }

            ReferenceType.IMPS_REFERENCE -> {
                if (reference.length in 12..16 && reference.all { it.isLetterOrDigit() }) {
                    ValidationResult(true, "Valid IMPS reference")
                } else {
                    ValidationResult(false, "IMPS reference should be 12-16 alphanumeric characters")
                }
            }

            ReferenceType.BANK_REFERENCE -> {
                ValidationResult(true, "Valid bank reference")
            }

            ReferenceType.GENERAL_REFERENCE -> {
                ValidationResult(true, "Valid reference format")
            }

            ReferenceType.UNKNOWN -> {
                ValidationResult(false, "Unknown reference format")
            }
        }
    }

    /**
     * Formats transaction reference to uppercase
     */
    fun formatTransactionReference(reference: String): String {
        return reference.trim().uppercase().replace("\\s+".toRegex(), "")
    }

    /**
     * Gets a description of the reference type
     */
    fun getReferenceTypeDescription(type: ReferenceType): String {
        return when (type) {
            ReferenceType.UPI_REFERENCE -> "UPI Transaction Reference (12 digits)"
            ReferenceType.BANK_REFERENCE -> "Bank Transaction Reference"
            ReferenceType.IMPS_REFERENCE -> "IMPS Transaction Reference"
            ReferenceType.NEFT_REFERENCE -> "NEFT Transaction Reference"
            ReferenceType.GENERAL_REFERENCE -> "General Transaction Reference"
            ReferenceType.UNKNOWN -> "Unknown Reference Format"
        }
    }

    /**
     * Generates example references for different types
     */
    fun getExampleReferences(): Map<ReferenceType, String> {
        return mapOf(
            ReferenceType.UPI_REFERENCE to "123456789012",
            ReferenceType.NEFT_REFERENCE to "NEFT12345678",
            ReferenceType.IMPS_REFERENCE to "IMPS123456789012",
            ReferenceType.BANK_REFERENCE to "TXN123456789",
            ReferenceType.GENERAL_REFERENCE to "REF-2024-001234"
        )
    }
}