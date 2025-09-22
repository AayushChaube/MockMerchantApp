package com.aayushchaube.mockmerchantapplication

import java.util.regex.Pattern

object VPAValidator {
    // UPI VPA regex pattern based on NPCI guidelines
    private const val VPA_REGEX = "^[a-zA-Z0-9.-]{2,256}@[a-zA-Z]{2,64}$"
    private val VPA_PATTERN = Pattern.compile(VPA_REGEX)

    // List of popular UPI handles for additional validation
    private val POPULAR_UPI_HANDLES = listOf(
        "paytm", "phonepe", "gpay", "ybl", "okaxis", "okicici", "okhdfcbank",
        "oksbi", "okbizaxis", "ibl", "axl", "apl", "fbl", "idfcbank",
        "pnb", "boi", "cnrb", "upi", "allbank", "unionbank", "indianbank"
    )

    /**
     * Validates if the given string is a valid UPI VPA format
     * @param vpa The VPA string to validate
     * @return ValidationResult containing validation status and error message
     */

    fun validateVPA(vpa: String?): ValidationResult {
        if (vpa.isNullOrBlank()) {
            return ValidationResult(false, "VPA cannot be empty")
        }

        val trimmedVPA = vpa.trim()

        // Check basic format using regex
        if (!VPA_PATTERN.matcher(trimmedVPA).matches()) {
            return ValidationResult(false, "Invalid VPA format. Use format: username@bankname")
        }

        // Additional checks
        if (!trimmedVPA.contains("@")) {
            return ValidationResult(false, "VPA must contain @ symbol")
        }

        val parts = trimmedVPA.split("@")
        if (parts.size != 2) {
            return ValidationResult(false, "VPA must have exactly one @ symbol")
        }

        val username = parts[0]
        val handle = parts[1]

        // Validate username part
        if (username.length < 2) {
            return ValidationResult(false, "Username must be at least 2 characters long")
        }

        if (username.length > 256) {
            return ValidationResult(false, "Username cannot exceed 256 characters")
        }

        // Validate handle part
        if (handle.length < 2) {
            return ValidationResult(false, "Bank handle must be at least 2 characters long")
        }

        if (handle.length > 64) {
            return ValidationResult(false, "Bank handle cannot exceed 64 characters")
        }

        // Check if handle contains only alphabets
        if (!handle.matches(Regex("^[a-zA-Z]+$"))) {
            return ValidationResult(false, "Bank handle must contain only alphabets")
        }

        return ValidationResult(true, "Valid VPA")
    }

    /**
     * Checks if the VPA handle is from a popular UPI provider
     */
    fun isPopularHandle(vpa: String): Boolean {
        if (!vpa.contains("@")) return false
        val handle = vpa.split("@")[1].lowercase()
        return POPULAR_UPI_HANDLES.contains(handle)
    }

    /**
     * Formats VPA to lowercase for consistency
     */
    fun formatVPA(vpa: String): String {
        return vpa.trim().lowercase()
    }
}