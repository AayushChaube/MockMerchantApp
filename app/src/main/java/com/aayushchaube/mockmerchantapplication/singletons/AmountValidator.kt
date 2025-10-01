package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult

object AmountValidator {
    private val DECIMAL_PATTERN = Regex("^\\d{1,7}(\\.\\d{0,2})?$")
    private const val MIN_AMOUNT = 0.01
    private const val MAX_AMOUNT = 1_000_000.00

    /** Validates amount string. */
    fun validate(amountText: String?): ValidationResult {
        if (amountText.isNullOrBlank()) {
            return ValidationResult(false, "Amount is required")
        }
        // Must match up to two decimals
        if (!DECIMAL_PATTERN.matches(amountText)) {
            return ValidationResult(false, "Enter a valid amount (max 2 decimals)")
        }
        val value = amountText.toDoubleOrNull()
            ?: return ValidationResult(false, "Invalid number format")
        if (value < MIN_AMOUNT) {
            return ValidationResult(false, "Amount must be at least ₹${"%.2f".format(MIN_AMOUNT)}")
        }
        if (value > MAX_AMOUNT) {
            return ValidationResult(false, "Amount cannot exceed ₹${"%,.2f".format(MAX_AMOUNT)}")
        }
        return ValidationResult(true, null)
    }
}