package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.singletons.NameValidator
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class NameTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
    private val showSingleWordWarning: Boolean = true,
    private val autoFormat: Boolean = true
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

        val nameText = s.toString()

        // Skip validation for empty text
        if (nameText.isEmpty()) {
            textInputLayout.error = null
            onValidationChanged(false)
            return
        }

        // Set timer for delayed validation (better UX)
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateName(s, nameText)
                }
            }, 500) // Wait 500ms after user stops typing
        }
    }

    private fun validateName(s: Editable?, nameText: String) {
        val validationResult = NameValidator.validateName(nameText)

        // Update UI on main thread
        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null

                // Auto-format if enabled
                if (autoFormat && s != null) {
                    val formattedName = NameValidator.formatName(nameText)
                    if (formattedName != nameText) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedName)
                        isFormatting = false
                    }
                }

                // Show helper text for single word names
                if (showSingleWordWarning && NameValidator.isSingleWord(nameText)) {
                    textInputLayout.helperText = "Consider adding last name for better verification"
                } else {
                    textInputLayout.helperText = "Enter full name (First Last)"
                }

                onValidationChanged(true)
            } else {
                // Only show error if user has typed something substantial
                if (nameText.length > 1) {
                    textInputLayout.error = validationResult.errorMessage
                    textInputLayout.helperText = null
                }
                onValidationChanged(false)
            }
        }
    }
}