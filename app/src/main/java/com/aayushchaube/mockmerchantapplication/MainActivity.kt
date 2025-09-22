package com.aayushchaube.mockmerchantapplication

import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {
    private val deepLinkingURLBase: String = "upi://pay"
    private lateinit var textInputLayoutPayeeVPA: TextInputLayout
    private lateinit var textInputEditTextPayeeVPA: TextInputEditText
    private lateinit var textInputLayoutPayeeName: TextInputLayout
    private lateinit var textInputEditTextPayeeName: TextInputEditText
    private lateinit var textInputLayoutPayeeMCC: TextInputLayout
    private lateinit var textInputEditTextPayeeMCC: TextInputEditText
    private lateinit var materialTextViewTransactionID: MaterialTextView
    private lateinit var textInputLayoutTransactionReference: TextInputLayout
    private lateinit var textInputEditTextTransactionReference: TextInputEditText
    private lateinit var textInputLayoutTransactionNote: TextInputLayout
    private lateinit var textInputEditTextTransactionNote: TextInputEditText
    private lateinit var textInputLayoutPayeeAmount: TextInputLayout
    private lateinit var textInputEditTextPayeeAmount: TextInputEditText
    private lateinit var textInputLayoutPayeeCurrency: TextInputLayout
    private lateinit var textInputEditTextPayeeCurrency: TextInputEditText
    private lateinit var materialTextViewPayeeAppID: MaterialTextView
    private lateinit var materialTextViewPayeeAppName: MaterialTextView
    private lateinit var materialButton: MaterialButton

    private var isVPAValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initializeViews()
        configureVPAKeyboard()
        setupVPAValidation()
        setupButtonState()
    }

    private fun initializeViews() {
        textInputLayoutPayeeVPA = findViewById(R.id.textInputLayout_payee_vpa)
        textInputEditTextPayeeVPA = findViewById(R.id.textInputEditText_payee_vpa)
        textInputLayoutPayeeName = findViewById(R.id.textInputLayout_payee_name)
        textInputEditTextPayeeName = findViewById(R.id.textInputEditText_payee_name)
        textInputLayoutPayeeMCC = findViewById(R.id.textInputLayout_payee_mcc)
        textInputEditTextPayeeMCC = findViewById(R.id.textInputEditText_payee_mcc)
        materialTextViewTransactionID = findViewById(R.id.materialTextView_transaction_id)
        textInputLayoutTransactionReference =
            findViewById(R.id.textInputLayout_transaction_reference)
        textInputEditTextTransactionReference =
            findViewById(R.id.textInputEditText_transaction_reference)
        textInputLayoutTransactionNote = findViewById(R.id.textInputLayout_transaction_note)
        textInputEditTextTransactionNote = findViewById(R.id.textInputEditText_transaction_note)
        textInputLayoutPayeeAmount = findViewById(R.id.textInputLayout_payee_amount)
        textInputEditTextPayeeAmount = findViewById(R.id.textInputEditText_payee_amount)
        textInputLayoutPayeeCurrency = findViewById(R.id.textInputLayout_payee_currency)
        textInputEditTextPayeeCurrency = findViewById(R.id.textInputEditText_payee_currency)
        materialTextViewPayeeAppID = findViewById(R.id.materialTextView_payee_app_id)
        materialTextViewPayeeAppName = findViewById(R.id.materialTextView_payee_app_name)
        materialButton = findViewById(R.id.materialButton)
    }

    private fun setupVPAValidation() {
        // Configure input type for VPA (similar to email but with custom keyboard)
        textInputEditTextPayeeVPA.apply {
            // Set input type for optimal keyboard layout
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Add VPA text watcher for real-time validation
            addTextChangedListener(VPATextWatcher(textInputLayoutPayeeVPA) { isValid ->
                isVPAValid = isValid
                updateButtonState()
            })

            // Set focus change listener for validation when focus is lost
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateVPAOnFocusLoss()
                }
            }
        }
    }

    private fun validateVPAOnFocusLoss() {
        val vpaText = textInputEditTextPayeeVPA.text.toString()
        if (vpaText.isNotEmpty()) {
            val validationResult = VPAValidator.validateVPA(vpaText)
            if (!validationResult.isValid) {
                textInputLayoutPayeeVPA.error = validationResult.errorMessage
                isVPAValid = false
            } else {
                textInputLayoutPayeeVPA.error = null
                isVPAValid = true

                // Format VPA if valid
                val formattedVPA = VPAValidator.formatVPA(vpaText)
                if (formattedVPA != vpaText) {
                    textInputEditTextPayeeVPA.setText(formattedVPA)
                }
            }
            updateButtonState()
        }
    }

    private fun setupButtonState() {
        // Initially disable the button
        materialButton.isEnabled = false
    }

    private fun updateButtonState() {
        // Enable button only when VPA is valid and other validations pass
        materialButton.isEnabled = isVPAValid && areOtherFieldsValid()
    }

    private fun areOtherFieldsValid(): Boolean {
        // Add validation for other fields as needed
        // For now, just check if VPA is valid
        return isVPAValid
    }

    // Method to get validated VPA
    fun getValidatedVPA(): String? {
        return if (isVPAValid) {
            VPAValidator.formatVPA(textInputEditTextPayeeVPA.text.toString())
        } else {
            null
        }
    }

    // Additional keyboard configuration method
    private fun configureVPAKeyboard() {
        textInputEditTextPayeeVPA.apply {
            // Primary input type for VPA
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // IME options for better navigation
            imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set single line
            setSingleLine(true)

            // Set maximum length based on VPA standards
            filters = arrayOf(android.text.InputFilter.LengthFilter(320)) // 256 + @ + 64

            // Disable auto-correct and suggestions
            inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
    }

}