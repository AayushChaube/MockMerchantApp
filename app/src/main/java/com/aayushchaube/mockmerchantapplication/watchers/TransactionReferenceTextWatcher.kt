package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.enums.ReferenceType
import com.aayushchaube.mockmerchantapplication.singletons.SecureTransactionReferenceGenerator
import com.aayushchaube.mockmerchantapplication.singletons.TransactionReferenceValidator
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class TransactionReferenceTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
    private val onReferenceTypeDetected: (ReferenceType?) -> Unit = {},
    private val isGenerated: () -> Boolean = { false } // Check if current value is generated
) : TextWatcher {
    private var isFormatting = false
    private var timer: Timer? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not needed for this implementation
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Clear error immediately when user starts typing
        if (!isFormatting && textInputLayout.error != null) {
            textInputLayout.error = null
        }

        // Cancel previous timer
        timer?.cancel()
    }

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return

        val referenceText = s.toString()

        // Skip validation for empty text
        if (referenceText.isEmpty()) {
            textInputLayout.error = null
            textInputLayout.helperText = "Tap refresh to generate reference"
            onValidationChanged(false)
            onReferenceTypeDetected(null)
            return
        }

        // Set timer for delayed validation
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateReference(s, referenceText)
                }
            }, 200) // Wait 300ms after user stops typing. Shorter delay for better UX
        }
    }

    private fun validateReference(s: Editable?, referenceText: String) {
        // If it's a generated reference, validate using the generator's validation
        val validationResult = if (isGenerated()) {
            SecureTransactionReferenceGenerator.validateGeneratedReference(referenceText)
        } else {
            TransactionReferenceValidator.validateTransactionReference(referenceText)
        }

        // Update UI on main thread
        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null

                // Auto-format reference (uppercase, remove spaces) if needed
                if (s != null && !isGenerated()) {
                    val formattedRef =
                        TransactionReferenceValidator.formatTransactionReference(referenceText)
                    if (formattedRef != referenceText) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedRef)
                        isFormatting = false
                    }
                }

                // Show reference type information. Show appropriate helper text
                if (isGenerated()) {
                    textInputLayout.helperText = "Generated secure reference (tap refresh for new)"
                } else {
                    val detectedType =
                        TransactionReferenceValidator.validateTransactionReference(referenceText).referenceType
                    detectedType?.let { type ->
                        val description =
                            TransactionReferenceValidator.getReferenceTypeDescription(type)
                        textInputLayout.helperText = "$description (Modified)"
                        onReferenceTypeDetected(type)
                    } ?: run {
                        textInputLayout.helperText = "Custom transaction reference"
                        onReferenceTypeDetected(null)
                    }
                }

                onValidationChanged(true)
            } else {
                // Show error for invalid reference
                if (referenceText.length >= 2) {
                    textInputLayout.error = validationResult.errorMessage
                } else {
                    textInputLayout.helperText = "Enter transaction reference"
                }
                onValidationChanged(false)
                onReferenceTypeDetected(null)
            }
        }
    }
}