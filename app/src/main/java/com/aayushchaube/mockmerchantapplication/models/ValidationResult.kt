package com.aayushchaube.mockmerchantapplication.models

import com.aayushchaube.mockmerchantapplication.enums.ReferenceType

/**
 * Data class to hold validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val referenceType: ReferenceType? = null
)