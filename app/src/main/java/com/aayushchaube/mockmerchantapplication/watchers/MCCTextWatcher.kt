package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.singletons.MCCValidator
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class MCCTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
    private val onMCCCategoryFound: (String?) -> Unit = {}
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

        val mccText = s.toString()

        // Skip validation for empty text
        if (mccText.isEmpty()) {
            textInputLayout.error = null
            textInputLayout.helperText = "Enter 4-digit Merchant Category Code"
            onValidationChanged(false)
            onMCCCategoryFound(null)
            return
        }

        // Set timer for delayed validation and category lookup
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateMCC(s, mccText)
                }
            }, 300) // Wait 300ms after user stops typing
        }
    }

    private fun validateMCC(s: Editable?, mccText: String) {
        val validationResult = MCCValidator.validateMCC(mccText)

        // Update UI on main thread
        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null

                // Auto-format if needed (add leading zeros)
                if (s != null && mccText.length < 4) {
                    val formattedMCC = MCCValidator.formatMCC(mccText)
                    if (formattedMCC != mccText && mccText.length == 4) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedMCC)
                        isFormatting = false
                    }
                }

                // Show category information
                val category = MCCValidator.getMCCCategory(mccText)
                if (category != null) {
                    textInputLayout.helperText = "Category: $category"
                    onMCCCategoryFound(category)
                } else {
                    textInputLayout.helperText = "Valid MCC code"
                    onMCCCategoryFound(null)
                }

                onValidationChanged(true)
            } else {
                // Show error for invalid MCC
                if (mccText.length >= 2) {
                    textInputLayout.error = validationResult.errorMessage
                    textInputLayout.helperText = null
                } else {
                    textInputLayout.helperText = "Enter 4-digit Merchant Category Code"
                }
                onValidationChanged(false)
                onMCCCategoryFound(null)
            }
        }
    }
}