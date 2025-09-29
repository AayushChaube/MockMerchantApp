package com.aayushchaube.mockmerchantapplication.watchers

import android.text.Editable
import android.text.TextWatcher
import com.aayushchaube.mockmerchantapplication.singletons.AmountValidator
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AmountTextWatcher(
    private val editLayout: TextInputLayout,
    private val onValidChanged: (Boolean) -> Unit = {}
) : TextWatcher {
    private var isEditing = false
    private val formatter: DecimalFormat = DecimalFormat(
        "#,##0.##", DecimalFormatSymbols(
            Locale(
                "en",
                "IN"
            )
        )
    )

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // clear error on new typing
        if (!isEditing) editLayout.error = null
    }

    override fun afterTextChanged(s: Editable?) {
        if (isEditing) return
        val original = s.toString()
        // Skip empty
        if (original.isBlank()) {
            onValidChanged(false)
            return
        }
        // Validate raw input
        val validation = AmountValidator.validate(original)
        if (!validation.isValid) {
            editLayout.error = validation.errorMessage
            onValidChanged(false)
            return
        }
        // Format with grouping separators
        val value = original.toDoubleOrNull() ?: return
        val formatted = formatter.format(value)
        if (formatted != original) {
            isEditing = true
            s?.replace(0, s.length, formatted)
            isEditing = false
        }
        // Clear error and notify valid
        editLayout.error = null
        onValidChanged(true)
    }
}