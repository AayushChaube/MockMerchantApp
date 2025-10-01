package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.CurrencyInfo
import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.Currency
import java.util.Locale

object CurrencyValidator {
    /**
     * Popular currencies for quick access
     */
    private val POPULAR_CURRENCIES = listOf(
        "USD", "EUR", "INR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "SEK"
    )

    /**
     * Gets all available currencies from the system
     */
    fun getAllCurrencies(): List<CurrencyInfo> {
        return try {
            Currency.getAvailableCurrencies()
                .filter { it.currencyCode.length == 3 } // Valid ISO 4217 codes
                .map { currency ->
                    CurrencyInfo(
                        code = currency.currencyCode,
                        name = getCurrencyName(currency),
                        symbol = getCurrencySymbol(currency),
                        numericCode = currency.numericCode,
                        defaultFractionDigits = currency.defaultFractionDigits,
                        displayName = "${currency.currencyCode} - ${getCurrencyName(currency)}"
                    )
                }.sortedWith(compareBy<CurrencyInfo> { !POPULAR_CURRENCIES.contains(it.code) }
                    .thenBy { it.code })
        } catch (e: Exception) {
            getDefaultCurrencies()
        }
    }

    /**
     * Gets popular currencies for quick selection
     */
    fun getPopularCurrencies(): List<CurrencyInfo> {
        return getAllCurrencies().filter { it.code in POPULAR_CURRENCIES }
    }

    /**
     * Gets currency by code
     */
    fun getCurrencyByCode(code: String): CurrencyInfo? {
        return try {
            val currency = Currency.getInstance(code)
            CurrencyInfo(
                code = currency.currencyCode,
                name = getCurrencyName(currency),
                symbol = getCurrencySymbol(currency),
                numericCode = currency.numericCode,
                defaultFractionDigits = currency.defaultFractionDigits,
                displayName = "${currency.currencyCode} - ${getCurrencyName(currency)}"
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates currency code
     */
    fun validateCurrencyCode(code: String?): ValidationResult {
        if (code.isNullOrBlank()) {
            return ValidationResult(false, "Currency is required")
        }

        if (code.length != 3) {
            return ValidationResult(false, "Currency code must be 3 characters")
        }

        if (!code.matches(Regex("^[A-Z]{3}$"))) {
            return ValidationResult(false, "Currency code must be 3 uppercase letters")
        }

        return try {
            Currency.getInstance(code)
            ValidationResult(true, "Valid currency code")
        } catch (e: IllegalArgumentException) {
            ValidationResult(false, "Invalid currency code: $code")
        }
    }

    /**
     * Formats amount with currency
     */
    fun formatAmount(amount: Double, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val format = java.text.NumberFormat.getCurrencyInstance()
            format.currency = currency
            format.format(amount)
        } catch (e: Exception) {
            "$currencyCode ${"%.2f".format(amount)}"
        }
    }

    /**
     * Gets currency name with fallback
     */
    private fun getCurrencyName(currency: Currency): String {
        return try {
            currency.getDisplayName(Locale.getDefault())
        } catch (e: Exception) {
            currency.currencyCode
        }
    }

    /**
     * Gets currency symbol with fallback
     */
    private fun getCurrencySymbol(currency: Currency): String {
        return try {
            currency.getSymbol(Locale.getDefault())
        } catch (e: Exception) {
            currency.currencyCode
        }
    }

    /**
     * Default currencies as fallback
     */
    private fun getDefaultCurrencies(): List<CurrencyInfo> {
        return listOf(
            CurrencyInfo("USD", "US Dollar", "$", 840, 2, "USD - US Dollar"),
            CurrencyInfo("EUR", "Euro", "€", 978, 2, "EUR - Euro"),
            CurrencyInfo("INR", "Indian Rupee", "₹", 356, 2, "INR - Indian Rupee"),
            CurrencyInfo("GBP", "British Pound", "£", 826, 2, "GBP - British Pound"),
            CurrencyInfo("JPY", "Japanese Yen", "¥", 392, 0, "JPY - Japanese Yen"),
            CurrencyInfo("CNY", "Chinese Yuan", "¥", 156, 2, "CNY - Chinese Yuan"),
            CurrencyInfo("AUD", "Australian Dollar", "A$", 36, 2, "AUD - Australian Dollar"),
            CurrencyInfo("CAD", "Canadian Dollar", "C$", 124, 2, "CAD - Canadian Dollar"),
            CurrencyInfo("CHF", "Swiss Franc", "CHF", 756, 2, "CHF - Swiss Franc"),
            CurrencyInfo("SEK", "Swedish Krona", "kr", 752, 2, "SEK - Swedish Krona")
        )
    }
}