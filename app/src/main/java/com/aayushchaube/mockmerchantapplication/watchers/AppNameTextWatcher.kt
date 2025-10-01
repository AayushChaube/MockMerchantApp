package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.models.CharacterInfo
import com.aayushchaube.mockmerchantapplication.singletons.AppNameValidator
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class AppNameTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
    private val onCharacterInfoChanged: (CharacterInfo) -> Unit = {}
) : TextWatcher {
    private var isFormatting = false
    private var timer: Timer? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!isFormatting && textInputLayout.error != null) {
            textInputLayout.error = null
        }
        timer?.cancel()

        // Update character count immediately
        s?.toString()?.let { text ->
            val charInfo = AppNameValidator.getCharacterInfo(text)
            onCharacterInfoChanged(charInfo)
            updateHelperText(charInfo)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return

        val nameText = s.toString()

        // Set timer for delayed validation
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateAppName(s, nameText)
                }
            }, 300) // Wait 300ms after user stops typing
        }
    }

    private fun validateAppName(s: Editable?, nameText: String) {
        val validationResult = AppNameValidator.validateAppName(nameText)

        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null
                // Auto-format if needed (capitalize words)
                if (s != null && nameText.isNotBlank()) {
                    val formattedName = AppNameValidator.formatAppName(nameText)
                    if (formattedName != nameText) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedName)
                        isFormatting = false
                    }
                }

                val charInfo = AppNameValidator.getCharacterInfo(nameText)
                updateHelperText(charInfo)
                onCharacterInfoChanged(charInfo)
                onValidationChanged(true)
            } else {
                textInputLayout.error = validationResult.errorMessage
                onValidationChanged(false)
            }
        }
    }

    private fun updateHelperText(charInfo: CharacterInfo) {
        textInputLayout.post {
            when {
                charInfo.isNearLimit -> {
                    textInputLayout.helperText =
                        "⚠️ ${charInfo.remainingChars} characters remaining"
                }

                charInfo.characterCount == 0 -> {
                    textInputLayout.helperText = "Enter your application name"
                }

                else -> {
                    textInputLayout.helperText =
                        "${charInfo.characterCount}/${AppNameValidator.MAX_LENGTH} • ${charInfo.wordCount} words"
                }
            }
        }
    }
}