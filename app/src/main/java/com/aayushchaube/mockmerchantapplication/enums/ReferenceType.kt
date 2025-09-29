package com.aayushchaube.mockmerchantapplication.enums

// Transaction reference types
enum class ReferenceType {
    UPI_REFERENCE,      // 12-digit UPI reference number
    BANK_REFERENCE,     // Bank transaction reference
    IMPS_REFERENCE,     // IMPS transaction reference
    NEFT_REFERENCE,     // NEFT transaction reference
    GENERAL_REFERENCE,  // General alphanumeric reference
    UNKNOWN
}