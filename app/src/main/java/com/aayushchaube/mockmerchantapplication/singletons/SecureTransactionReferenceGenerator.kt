package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.enums.ReferenceFormat
import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SecureTransactionReferenceGenerator {
    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure transaction reference
     * @param format The format type for the reference
     * @param prefix Optional prefix for the reference
     * @return Generated secure transaction reference
     */
    fun generateSecureReference(
        format: ReferenceFormat = ReferenceFormat.ALPHANUMERIC_12,
        prefix: String? = null
    ): String {
        return when (format) {
            ReferenceFormat.ALPHANUMERIC_12 -> generateAlphanumericReference(12, prefix)
            ReferenceFormat.ALPHANUMERIC_16 -> generateAlphanumericReference(16, prefix)
            ReferenceFormat.NUMERIC_12 -> generateNumericReference(12, prefix)
            ReferenceFormat.TIMESTAMPED -> generateTimestampedReference(prefix)
            ReferenceFormat.UUID_SHORT -> generateUUIDShortReference(prefix)
        }
    }

    /**
     * Generates alphanumeric reference with secure random
     */
    private fun generateAlphanumericReference(length: Int, prefix: String?): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val basePrefix = prefix ?: "TXN"
        val remainingLength = length - basePrefix.length

        if (remainingLength <= 0) {
            throw IllegalArgumentException("Length must be greater than prefix length")
        }

        val randomPart = StringBuilder()
        repeat(remainingLength) {
            randomPart.append(chars[secureRandom.nextInt(chars.length)])
        }

        return basePrefix + randomPart.toString()
    }

    /**
     * Generates numeric reference with secure random
     */
    private fun generateNumericReference(length: Int, prefix: String?): String {
        val basePrefix = prefix ?: ""
        val remainingLength = length - basePrefix.length

        if (remainingLength <= 0) {
            throw IllegalArgumentException("Length must be greater than prefix length")
        }

        val randomPart = StringBuilder()
        repeat(remainingLength) {
            randomPart.append(secureRandom.nextInt(10))
        }

        return basePrefix + randomPart.toString()
    }

    /**
     * Generates timestamped reference with current time
     */
    private fun generateTimestampedReference(prefix: String?): String {
        val basePrefix = prefix ?: "TXN"
        val timestamp = SimpleDateFormat("yyMMddHHmmss", Locale.US).format(Date())

        // Add 4 random characters for uniqueness
        val randomSuffix = StringBuilder()
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        repeat(4) {
            randomSuffix.append(chars[secureRandom.nextInt(chars.length)])
        }

        return "$basePrefix$timestamp$randomSuffix"
    }

    /**
     * Generates shortened UUID-based reference
     */
    private fun generateUUIDShortReference(prefix: String?): String {
        val basePrefix = prefix ?: "REF"
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()

        // Take first 12 characters of UUID for shorter reference
        val shortUuid = uuid.substring(0, 12)

        return basePrefix + shortUuid
    }

    /**
     * Validates if a transaction reference meets security requirements
     */
    fun validateGeneratedReference(reference: String): ValidationResult {
        if (reference.length < 8) {
            return ValidationResult(false, "Generated reference too short")
        }

        if (reference.length > 25) {
            return ValidationResult(false, "Generated reference too long")
        }

        if (!reference.matches(Regex("^[A-Z0-9]+$"))) {
            return ValidationResult(false, "Generated reference contains invalid characters")
        }

        return ValidationResult(true, "Valid generated reference")
    }

    fun getFormatExamples(): Map<ReferenceFormat, String> {
        return mapOf(
            ReferenceFormat.ALPHANUMERIC_12 to "TXN123ABC456",
            ReferenceFormat.ALPHANUMERIC_16 to "TXNX1234ABCD5678",
            ReferenceFormat.NUMERIC_12 to "123456789012",
            ReferenceFormat.TIMESTAMPED to "TXN240929123456AB",
            ReferenceFormat.UUID_SHORT to "REFABC123DEF456G"
        )
    }
}