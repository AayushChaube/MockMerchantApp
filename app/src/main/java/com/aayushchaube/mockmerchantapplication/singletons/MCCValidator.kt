package com.aayushchaube.mockmerchantapplication.singletons

import com.aayushchaube.mockmerchantapplication.models.ValidationResult
import java.util.regex.Pattern

object MCCValidator {
    // MCC must be exactly 4 digits
    private const val MCC_REGEX = "^[0-9]{4}$"
    private val MCC_PATTERN = Pattern.compile(MCC_REGEX)

    // Common MCC categories with their ranges based on ISO 18245 standard
    private val MCC_CATEGORIES = mapOf(
        "0001-1499" to "Agricultural Services",
        "1500-2999" to "Contracted Services",
        "3000-3299" to "Airlines",
        "3300-3499" to "Car Rental",
        "3500-3999" to "Lodging",
        "4000-4799" to "Transportation Services",
        "4800-4999" to "Utility Services",
        "5000-5599" to "Retail Outlet Services",
        "5600-5699" to "Clothing Stores",
        "5700-7299" to "Miscellaneous Stores",
        "7300-7999" to "Business Services",
        "8000-8999" to "Professional Services and Membership Organizations",
        "9000-9999" to "Government Services"
    )

    // Popular MCC codes for validation and suggestions
    private val POPULAR_MCC_CODES = mapOf(
        "5411" to "Grocery Stores/Supermarkets",
        "5812" to "Restaurants",
        "5542" to "Automated Fuel Dispensers",
        "5541" to "Service Stations",
        "5999" to "Miscellaneous Retail Stores",
        "5311" to "Department Stores",
        "5732" to "Electronics Stores",
        "5814" to "Fast Food Restaurants",
        "4900" to "Utilities",
        "5912" to "Drug Stores/Pharmacies",
        "5943" to "Stationery/Office Supply Stores",
        "5651" to "Family Clothing Stores",
        "5691" to "Men's/Women's Clothing Stores",
        "5734" to "Computer Software Stores",
        "5941" to "Sporting Goods Stores",
        "5945" to "Hobby/Toy/Game Shops",
        "7011" to "Hotels/Motels/Resorts",
        "3000" to "Airlines",
        "5661" to "Shoe Stores",
        "5947" to "Gift/Card/Novelty Shops"
    )

    /**
     * Validates if the given string is a valid MCC format
     * @param mcc The MCC string to validate
     * @return ValidationResult containing validation status and error message
     */
    fun validateMCC(mcc: String?): ValidationResult {
        if (mcc.isNullOrBlank()) {
            return ValidationResult(false, "MCC cannot be empty")
        }

        val trimmedMCC = mcc.trim()

        // Check if MCC is exactly 4 digits
        if (!MCC_PATTERN.matcher(trimmedMCC).matches()) {
            return ValidationResult(false, "MCC must be exactly 4 digits")
        }

        // Check if MCC is within valid range (0001-9999)
        val mccValue = trimmedMCC.toIntOrNull()
        if (mccValue == null || mccValue < 1 || mccValue > 9999) {
            return ValidationResult(false, "MCC must be between 0001 and 9999")
        }

        // Check for reserved ranges that are not assigned
        if (isReservedMCC(mccValue)) {
            return ValidationResult(false, "This MCC code is in a reserved range")
        }

        return ValidationResult(true, "Valid MCC")
    }

    /**
     * Checks if MCC is in a reserved/unassigned range
     */
    private fun isReservedMCC(mcc: Int): Boolean {
        // Some known reserved ranges (this is not exhaustive)
        val reservedRanges = listOf(
            0..0, // Invalid range
            // Add more reserved ranges as needed based on latest standards
        )

        return reservedRanges.any { range -> mcc in range }
    }

    /**
     * Gets the category description for a given MCC
     */
    fun getMCCCategory(mcc: String): String? {
        if (!validateMCC(mcc).isValid) return null

        val mccValue = mcc.toInt()

        // Check if it's a popular/known MCC
        POPULAR_MCC_CODES[mcc]?.let { return it }

        // Check category ranges
        return MCC_CATEGORIES.entries.find { (range, _) ->
            val (start, end) = range.split("-").map { it.toInt() }
            mccValue in start..end
        }?.value
    }

    /**
     * Formats MCC to ensure it's 4 digits with leading zeros
     */
    fun formatMCC(mcc: String): String {
        val numericMCC = mcc.filter { it.isDigit() }
        return if (numericMCC.isNotEmpty() && numericMCC.length <= 4) {
            numericMCC.padStart(4, '0')
        } else {
            mcc
        }
    }

    /**
     * Gets suggestions for MCC codes based on partial input
     */
    fun getMCCSuggestions(partialMCC: String, limit: Int = 5): List<Pair<String, String>> {
        if (partialMCC.length < 2) return emptyList()

        return POPULAR_MCC_CODES.filter { (code, _) ->
            code.startsWith(partialMCC)
        }.toList().take(limit)  // Convert to List first, then use take()
    }

    /**
     * Checks if MCC is a popular/commonly used code
     */
    fun isPopularMCC(mcc: String): Boolean {
        return POPULAR_MCC_CODES.containsKey(mcc)
    }
}