package com.aayushchaube.mockmerchantapplication.enums

// Different reference formats supported
enum class ReferenceFormat {
    ALPHANUMERIC_12,     // TXN123ABC456 (12 chars)
    ALPHANUMERIC_16,     // TXNX1234ABCD5678 (16 chars)
    NUMERIC_12,          // 123456789012 (12 digits)
    TIMESTAMPED,         // TXN240929123456 (with timestamp)
    UUID_SHORT
}