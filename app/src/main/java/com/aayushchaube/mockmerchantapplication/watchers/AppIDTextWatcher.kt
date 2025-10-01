package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.singletons.AppIDValidator
import com.google.android.material.textfield.TextInputLayout

class AppIDTextWatcher(
    private val layout: TextInputLayout,
    private val onValid: (Boolean) -> Unit
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        layout.error = null                               // clear error while typing
    }

    override fun afterTextChanged(s: Editable?) {
        val text = s?.toString() ?: ""
        val result = AppIDValidator.validate(text)
        if (result.isValid) {
            layout.error = null
        } else {
            layout.error = result.errorMessage
        }
        onValid(result.isValid)
    }
}