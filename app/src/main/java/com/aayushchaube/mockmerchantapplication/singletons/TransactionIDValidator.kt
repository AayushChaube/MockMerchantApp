package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object TransactionIDValidator {
    /**
     * Validates if the given string is a valid transaction ID
     * @param id The transaction ID string to validate
     * @return ValidationResult containing validation status and error message
     */
    fun validateTransactionID(id: String?): ValidationResult {
        if (id.isNullOrBlank()) {
            return ValidationResult(false, "Transaction ID cannot be empty")
        }

        val trimmedID = id.trim().uppercase()

        // Check minimum and maximum length
        if (trimmedID.length != 35) {
            return ValidationResult(false, "Transaction ID must be 35 characters")
        }

        // Check for invalid characters (only alphanumeric allowed)
        if (!trimmedID.matches(Regex("^[A-Za-z0-9]{35}$"))) {
            return ValidationResult(
                false,
                "Reference can only contain letters and numbers"
            )
        }

        return ValidationResult(true, "Valid transaction ID")
    }

    /**
     * Formats transaction ID to uppercase
     */
    fun formatTransactionID(id: String): String {
        return id.trim().uppercase().replace("\\s+".toRegex(), "")
    }
}