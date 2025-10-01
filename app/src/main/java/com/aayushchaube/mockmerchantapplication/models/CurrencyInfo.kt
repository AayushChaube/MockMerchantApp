package com.aayushchaube.mockmerchantapplication.models

/**
 * Data class representing a currency with all relevant information
 */
data class CurrencyInfo(
    val code: String,           // ISO 4217 code (e.g., "USD")
    val name: String,           // Full name (e.g., "US Dollar")
    val symbol: String,         // Symbol (e.g., "$")
    val numericCode: Int,       // ISO numeric code
    val defaultFractionDigits: Int, // Decimal places
    val displayName: String     // Display format for dropdown
)
