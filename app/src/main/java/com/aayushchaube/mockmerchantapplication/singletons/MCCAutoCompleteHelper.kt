package com.aayushchaube.mockmerchantapplication.singletons

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class MCCAutoCompleteHelper {
    companion object {
        fun setupMCCAutoComplete(
            textInputEditText: TextInputEditText,
            context: android.content.Context
        ) {
            textInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val input = s.toString()
                    if (input.length >= 2) {
                        val suggestions = MCCValidator.getMCCSuggestions(input, 3)
                        if (suggestions.isNotEmpty()) {
                            showMCCSuggestions(context, textInputEditText, suggestions)
                        }
                    }
                }
            })
        }

        private fun showMCCSuggestions(
            context: android.content.Context,
            editText: TextInputEditText,
            suggestions: List<Pair<String, String>>
        ) {
            // Implementation for showing suggestions popup
            // This could be a PopupWindow or AlertDialog with suggestions
        }
    }
}