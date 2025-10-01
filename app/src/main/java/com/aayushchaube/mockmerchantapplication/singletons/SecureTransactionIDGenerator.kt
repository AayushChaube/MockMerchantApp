package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.security.SecureRandom

object SecureTransactionIDGenerator {
    private val secureRandom = SecureRandom()
    private const val ALLOWED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val LENGTH = 35

    fun generateID(length: Int = LENGTH, prefix: String?): String {
        val basePrefix = prefix ?: "MBTID"
        val remainingLength = length - basePrefix.length

        if (remainingLength <= 0) {
            throw IllegalArgumentException("Length must be greater than prefix length")
        }

        val sb = StringBuilder(basePrefix)

        repeat(remainingLength) {
            sb.append(ALLOWED[secureRandom.nextInt(ALLOWED.length)])
        }

        return sb.toString()
    }

    /**
     * Validates if a transaction reference meets security requirements
     */
    fun validateGeneratedID(id: String): ValidationResult {
        if (id.length != 35) {
            return ValidationResult(false, "Generated ID must be 35 characters long")
        }

        if (!id.matches(Regex("^[A-Za-z0-9]{35}\$"))) {
            return ValidationResult(false, "Generated ID contains invalid characters")
        }

        return ValidationResult(true, "Valid generated reference")
    }
}