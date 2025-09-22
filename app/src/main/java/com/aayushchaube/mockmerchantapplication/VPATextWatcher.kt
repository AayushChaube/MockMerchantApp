package com.aayushchaube.mockmerchantapplication

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

class VPATextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {}
) : TextWatcher {
    private var isValidating = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not needed for this implementation
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Clear error immediately when user starts typing
        if (!isValidating && textInputLayout.error != null) {
            textInputLayout.error = null
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isValidating) return

        isValidating = true

        val vpaText = s.toString()

        // Skip validation for empty text to avoid showing error immediately
        if (vpaText.isEmpty()) {
            textInputLayout.error = null
            onValidationChanged(false)
            isValidating = false
            return
        }

        // Validate VPA
        val validationResult = VPAValidator.validateVPA(vpaText)

        if (validationResult.isValid) {
            textInputLayout.error = null
            // Format the VPA if it's valid
            val formattedVPA = VPAValidator.formatVPA(vpaText)
            if (formattedVPA != vpaText) {
                s?.clear()
                s?.append(formattedVPA)
            }
            onValidationChanged(true)
        } else {
            // Only show error if user has typed something substantial
            if (vpaText.length > 2) {
                textInputLayout.error = validationResult.errorMessage
            }
            onValidationChanged(false)
        }

        isValidating = false
    }
}