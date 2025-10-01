package com.aayushchaube.mockmerchantapplication.watchers

import android.R.attr.description
import android.R.attr.text
import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.enums.ReferenceType
import com.aayushchaube.mockmerchantapplication.singletons.SecureTransactionIDGenerator
import com.aayushchaube.mockmerchantapplication.singletons.SecureTransactionReferenceGenerator
import com.aayushchaube.mockmerchantapplication.singletons.TransactionIDValidator
import com.aayushchaube.mockmerchantapplication.singletons.TransactionReferenceValidator
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text
import java.util.Timer
import java.util.TimerTask

class TransactionIDTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
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

        val idText = s.toString()

        // Skip validation for empty text
        if (idText.isEmpty()) {
            textInputLayout.error = null
            textInputLayout.helperText = "Tap refresh to generate ID"
            onValidationChanged(false)
//            onReferenceTypeDetected(null)
            return
        }

        // Set timer for delayed validation
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateID(s, idText)
                }
            }, 200) // Wait 300ms after user stops typing. Shorter delay for better UX
        }
    }

    private fun validateID(s: Editable?, idText: String) {
        // If it's a generated reference, validate using the generator's validation
        val validationResult = if (isGenerated()) {
            SecureTransactionIDGenerator.validateGeneratedID(idText)
        } else {
            TransactionIDValidator.validateTransactionID(idText)
        }

        // Update UI on main thread
        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null

                // Auto-format ID (uppercase, remove spaces) if needed
                if (s != null && !isGenerated()) {
                    val formattedID =
                        TransactionIDValidator.formatTransactionID(idText)
                    if (formattedID != idText) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedID)
                        isFormatting = false
                    }
                }

                // Show appropriate helper text
                if (isGenerated()) {
                    textInputLayout.helperText = "Generated secure ID (tap refresh for new)"
                } else {
                    val validationResult =
                        TransactionReferenceValidator.validateTransactionReference(idText)
                    textInputLayout.helperText = "${validationResult.errorMessage} (Modified)"
                }

                onValidationChanged(true)
            } else {
                // Show error for invalid ID
                if (idText.length >= 2) {
                    textInputLayout.error = validationResult.errorMessage
                } else {
                    textInputLayout.helperText = "Enter transaction ID"
                }

                onValidationChanged(false)
            }
        }
    }
}