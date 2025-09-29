package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.models.CharacterInfo
import com.aayushchaube.mockmerchantapplication.singletons.TransactionNoteValidator
import com.google.android.material.textfield.TextInputLayout
import java.util.Timer
import java.util.TimerTask

class TransactionNoteTextWatcher(
    private val textInputLayout: TextInputLayout,
    private val onValidationChanged: (Boolean) -> Unit = {},
    private val onCharacterInfoChanged: (CharacterInfo) -> Unit = {},
    private val isRequired: Boolean = false
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
            val charInfo = TransactionNoteValidator.getCharacterInfo(text)
            onCharacterInfoChanged(charInfo)

            // Update helper text with character info
            updateHelperText(charInfo)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return

        val noteText = s.toString()

        // Set timer for delayed validation
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    validateNote(s, noteText)
                }
            }, 500) // Wait 500ms after user stops typing
        }
    }

    private fun validateNote(s: Editable?, noteText: String) {
        val validationResult =
            TransactionNoteValidator.validateTransactionNote(noteText, isRequired)

        textInputLayout.post {
            if (validationResult.isValid) {
                textInputLayout.error = null

                // Auto-format if needed
                if (s != null && noteText.isNotBlank()) {
                    val formattedNote = TransactionNoteValidator.formatTransactionNote(noteText)
                    if (formattedNote != noteText) {
                        isFormatting = true
                        s.clear()
                        s.append(formattedNote)
                        isFormatting = false
                    }
                }

                val charInfo = TransactionNoteValidator.getCharacterInfo(noteText)
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
                    textInputLayout.helperText =
                        if (isRequired) "Transaction note is required" else "Add optional note about this payment"
                }

                else -> {
                    textInputLayout.helperText =
                        "${charInfo.characterCount}/${TransactionNoteValidator.MAX_LENGTH} • ${charInfo.wordCount} words • ${charInfo.lineCount} lines"
                }
            }
        }
    }
}