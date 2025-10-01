package com.aayushchaube.mockmerchantapplication

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aayushchaube.mockmerchantapplication.adapters.CurrencyDropdownAdapter
import com.aayushchaube.mockmerchantapplication.enums.ReferenceFormat
import com.aayushchaube.mockmerchantapplication.enums.ReferenceType
import com.aayushchaube.mockmerchantapplication.models.CharacterInfo
import com.aayushchaube.mockmerchantapplication.models.CurrencyInfo
import com.aayushchaube.mockmerchantapplication.singletons.AppNameValidator
import com.aayushchaube.mockmerchantapplication.singletons.CurrencyValidator
import com.aayushchaube.mockmerchantapplication.singletons.MCCValidator
import com.aayushchaube.mockmerchantapplication.singletons.NameValidator
import com.aayushchaube.mockmerchantapplication.singletons.SecureTransactionIDGenerator
import com.aayushchaube.mockmerchantapplication.singletons.SecureTransactionReferenceGenerator
import com.aayushchaube.mockmerchantapplication.singletons.TransactionNoteValidator
import com.aayushchaube.mockmerchantapplication.singletons.VPAValidator
import com.aayushchaube.mockmerchantapplication.watchers.AmountTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.AppIDTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.AppNameTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.MCCTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.NameTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.TransactionIDTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.TransactionNoteTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.TransactionReferenceTextWatcher
import com.aayushchaube.mockmerchantapplication.watchers.VPATextWatcher
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private lateinit var textInputLayoutPayeeVPA: TextInputLayout
    private lateinit var textInputEditTextPayeeVPA: TextInputEditText
    private lateinit var textInputLayoutPayeeName: TextInputLayout
    private lateinit var textInputEditTextPayeeName: TextInputEditText
    private lateinit var textInputLayoutPayeeMCC: TextInputLayout
    private lateinit var textInputEditTextPayeeMCC: TextInputEditText
    private lateinit var textInputLayoutTransactionID: TextInputLayout
    private lateinit var textInputEditTextTransactionID: TextInputEditText
    private lateinit var textInputLayoutTransactionReference: TextInputLayout
    private lateinit var textInputEditTextTransactionReference: TextInputEditText
    private lateinit var textInputLayoutTransactionNote: TextInputLayout
    private lateinit var textInputEditTextTransactionNote: TextInputEditText
    private lateinit var textInputLayoutPayeeAmount: TextInputLayout
    private lateinit var textInputEditTextPayeeAmount: TextInputEditText
    private lateinit var textInputLayoutPayeeCurrency: TextInputLayout
    private lateinit var materialAutoCompleteTextViewPayeeCurrency: MaterialAutoCompleteTextView
    private lateinit var textInputLayoutPayeeAppID: TextInputLayout
    private lateinit var textInputEditTextPayeeAppID: TextInputEditText
    private lateinit var textInputLayoutPayeeAppName: TextInputLayout
    private lateinit var textInputEditTextPayeeAppName: TextInputEditText
    private lateinit var materialButton: MaterialButton

    private lateinit var currencyAdapter: CurrencyDropdownAdapter

    private var isVPAValid = false
    private var isNameValid = false
    private var isMCCValid = false
    private var currentMCCCategory: String? = null
    private var isTransactionIDValid = false
    private var isTransactionIDGenerated = false
    private var isTransactionReferenceValid = false
    private var currentReferenceType: ReferenceType? = null
    private var isCurrentReferenceGenerated = false
    private var isTransactionNoteValid = false
    private var currentCharacterInfo: CharacterInfo? = null
    private var isAmountValid = false
    private var isCurrencyValid = false
    private var selectedCurrency: CurrencyInfo? = null
    private var isAppIDValid = true            // starts true because pre-filled is valid
    private var isAppNameValid = true // Start as valid since pre-filled

    private val deepLinkingURLBase: String = "upi://pay"

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
        configureNameKeyboard()
        configureMCCKeyboard()
        configureTransactionIDKeyboard()
        configureTransactionReferenceKeyboard()
        configureTransactionNoteKeyboard()
        setupVPAValidation()
        setupNameValidation()
        setupMCCValidation()
        setupTransactionIDValidation()
        setupTransactionReferenceWithGenerator()
        setupTransactionNoteValidation()
        // Attach TextWatcher
        textInputEditTextPayeeAmount.addTextChangedListener(
            AmountTextWatcher(
                textInputLayoutPayeeAmount
            ) { valid ->
                isAmountValid = valid
                updateButtonState()
            })
        setupCurrencyDropdown()
        setupAppIDValidation()
        setupAppNameValidation()
        setupButtonState()

        // Generate initial ID
        generateNewTransactionID()
        // Generate initial reference
        generateNewTransactionReference()
        // Set default currency (INR for Indian users)
        setDefaultCurrency("INR")
    }

    private fun initializeViews() {
        textInputLayoutPayeeVPA = findViewById(R.id.textInputLayout_payee_vpa)
        textInputEditTextPayeeVPA = findViewById(R.id.textInputEditText_payee_vpa)
        textInputLayoutPayeeName = findViewById(R.id.textInputLayout_payee_name)
        textInputEditTextPayeeName = findViewById(R.id.textInputEditText_payee_name)
        textInputLayoutPayeeMCC = findViewById(R.id.textInputLayout_payee_mcc)
        textInputEditTextPayeeMCC = findViewById(R.id.textInputEditText_payee_mcc)
        textInputLayoutTransactionID = findViewById(R.id.textInputLayout_transaction_id)
        textInputEditTextTransactionID = findViewById(R.id.textInputEditText_transaction_id)
        textInputLayoutTransactionReference =
            findViewById(R.id.textInputLayout_transaction_reference)
        textInputEditTextTransactionReference =
            findViewById(R.id.textInputEditText_transaction_reference)
        textInputLayoutTransactionNote = findViewById(R.id.textInputLayout_transaction_note)
        textInputEditTextTransactionNote = findViewById(R.id.textInputEditText_transaction_note)
        textInputLayoutPayeeAmount = findViewById(R.id.textInputLayout_payee_amount)
        textInputEditTextPayeeAmount = findViewById(R.id.textInputEditText_payee_amount)
        textInputLayoutPayeeCurrency = findViewById(R.id.textInputLayout_payee_currency)
        materialAutoCompleteTextViewPayeeCurrency =
            findViewById(R.id.materialAutoCompleteTextView_payee_currency)
        textInputLayoutPayeeAppID = findViewById(R.id.textInputLayout_payee_app_id)
        textInputEditTextPayeeAppID = findViewById(R.id.textInputEditText_payee_app_id)
        textInputLayoutPayeeAppName = findViewById(R.id.textInputLayout_payee_app_name)
        textInputEditTextPayeeAppName = findViewById(R.id.textInputEditText_payee_app_name)
        materialButton = findViewById(R.id.materialButton)
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
            isSingleLine = true

            // Set maximum length based on VPA standards
            filters = arrayOf(InputFilter.LengthFilter(320)) // 256 + @ + 64

            // Disable auto-correct and suggestions
            inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
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

    // Method to get validated VPA
    fun getValidatedVPA(): String? {
        return if (isVPAValid) {
            VPAValidator.formatVPA(textInputEditTextPayeeVPA.text.toString())
        } else {
            null
        }
    }

    private fun configureNameKeyboard() {
        textInputEditTextPayeeName.apply {
            // Optimal input type for person names
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS or
                    InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // IME options for smooth navigation
            imeOptions = EditorInfo.IME_ACTION_NEXT or
                    EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set single line for names
            isSingleLine = true
            maxLines = 1

            // Enable auto-capitalization of words (for devices that support it)
            setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        }
    }

    private fun setupNameValidation() {
        // Configure input type for name with proper capitalization
        textInputEditTextPayeeName.apply {
            // Set input type for optimal keyboard layout and capitalization
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Set character filter to allow only valid name characters
            filters = arrayOf(
                InputFilter.LengthFilter(100), // Max length
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow only letters, spaces, apostrophes, hyphens, and periods
                    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ .'-"
                    val filtered = StringBuilder()

                    for (i in start until end) {
                        val char = source[i]
                        if (allowedChars.contains(char)) {
                            filtered.append(char)
                        }
                    }

                    if (filtered.length == end - start) {
                        null // Accept all characters
                    } else {
                        filtered.toString() // Return filtered string
                    }
                }
            )

            // Add name text watcher for real-time validation
            addTextChangedListener(
                NameTextWatcher(
                    textInputLayoutPayeeName,
                    onValidationChanged = { isValid ->
                        isNameValid = isValid
                        updateButtonState()
                    },
                    showSingleWordWarning = true,
                    autoFormat = true
                )
            )

            // Set focus change listener for validation when focus is lost
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateNameOnFocusLoss()
                }
            }
        }
    }

    private fun validateNameOnFocusLoss() {
        val nameText = textInputEditTextPayeeName.text.toString()
        if (nameText.isNotEmpty()) {
            val validationResult = NameValidator.validateName(nameText)
            if (!validationResult.isValid) {
                textInputLayoutPayeeName.error = validationResult.errorMessage
                isNameValid = false
            } else {
                textInputLayoutPayeeName.error = null
                isNameValid = true

                // Auto-format name
                val formattedName = NameValidator.formatName(nameText)
                if (formattedName != nameText) {
                    textInputEditTextPayeeName.setText(formattedName)
                    textInputEditTextPayeeName.setSelection(formattedName.length)
                }

                // Show helper text for single word
                if (NameValidator.isSingleWord(nameText)) {
                    textInputLayoutPayeeName.helperText = "Consider adding last name"
                }
            }
            updateButtonState()
        }
    }

    // Method to get validated and formatted name
    fun getValidatedName(): String? {
        return if (isNameValid) {
            NameValidator.formatName(textInputEditTextPayeeName.text.toString())
        } else {
            null
        }
    }

    private fun configureMCCKeyboard() {
        textInputEditTextPayeeMCC.apply {
            // Primary input type for numbers only
            inputType = InputType.TYPE_CLASS_NUMBER

            // IME options for better navigation
            imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set single line
            setSingleLine(true)
            maxLines = 1

            // Set maximum length for MCC (4 digits)
            filters = arrayOf(
                InputFilter.LengthFilter(4),
                // Custom filter to allow only digits
                InputFilter { source, start, end, dest, dstart, dend ->
                    for (i in start until end) {
                        if (!Character.isDigit(source[i])) {
                            return@InputFilter ""
                        }
                    }
                    null
                }
            )

            // Restrict input to digits only
            keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789")
        }
    }

    private fun setupMCCValidation() {
        // Configure input type for numeric-only input
        textInputEditTextPayeeMCC.apply {
            // Set input type for numeric keyboard
            inputType = InputType.TYPE_CLASS_NUMBER

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Set input filters
            filters = arrayOf(
                InputFilter.LengthFilter(4), // Max 4 digits
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow only digits
                    if (source.toString().matches(Regex("[0-9]*"))) {
                        null // Accept input
                    } else {
                        "" // Reject input
                    }
                }
            )

            // Add MCC text watcher for real-time validation
            addTextChangedListener(
                MCCTextWatcher(
                    textInputLayoutPayeeMCC, onValidationChanged = { isValid ->
                        isMCCValid = isValid
                        updateButtonState()
                    },
                    onMCCCategoryFound = { category -> currentMCCCategory = category })
            )

            // Add MCC text watcher for real-time validation
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateMCCOnFocusLoss()
                }
            }
        }
    }

    private fun validateMCCOnFocusLoss() {
        val mccText = textInputEditTextPayeeMCC.text.toString()
        if (mccText.isNotEmpty()) {
            val validationResult = MCCValidator.validateMCC(mccText)
            if (!validationResult.isValid) {
                textInputLayoutPayeeMCC.error = validationResult.errorMessage
                isMCCValid = false
            } else {
                textInputLayoutPayeeMCC.error = null
                isMCCValid = true

                // Format MCC if needed
                val formattedMCC = MCCValidator.formatMCC(mccText)
                if (formattedMCC != mccText) {
                    textInputEditTextPayeeMCC.setText(formattedMCC)
                    textInputEditTextPayeeMCC.setSelection(formattedMCC.length)
                }

                // Show category
                val category = MCCValidator.getMCCCategory(formattedMCC)
                if (category != null) {
                    textInputLayoutPayeeMCC.helperText = "Category: $category"
                    currentMCCCategory = category
                }
            }
            updateButtonState()
        }
    }

    // Method to get validated MCC
    fun getValidatedMCC(): String? {
        return if (isMCCValid) {
            MCCValidator.formatMCC(textInputEditTextPayeeMCC.text.toString())
        } else {
            null
        }
    }

    // Method to get MCC category
    fun getMCCCategory(): String? {
        return currentMCCCategory
    }

    private fun configureTransactionIDKeyboard() {
        textInputEditTextTransactionID.apply {
            // Use visible password input type for alphanumeric keyboard with numbers first
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

            // IME options for better navigation
            imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set single line
            isSingleLine = true
            maxLines = 1

            // Custom key listener for alphanumeric input
            keyListener = android.text.method.DigitsKeyListener.getInstance(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            )
        }
    }

    private fun setupTransactionIDValidation() {
        // Configure input type for alphanumeric input with numbers prioritized
        textInputEditTextTransactionID.apply {
            // Use textVisiblePassword to show alphanumeric keyboard with numbers first
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Set input filters
            filters = arrayOf(
                InputFilter.LengthFilter(35), // Max 35 characters
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow only alphanumeric characters
                    val allowedChars =
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    val filtered = StringBuilder()

                    for (i in start until end) {
                        val char = source[i]
                        if (allowedChars.contains(char)) {
                            // Convert to uppercase for consistency
                            filtered.append(char.uppercaseChar())
                        }
                    }

                    if (filtered.length == end - start) {
                        null // Accept all characters (already filtered)
                    } else {
                        filtered.toString() // Return filtered string
                    }
                }
            )

            // Handle manual editing (mark as no longer generated)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && isTransactionIDGenerated) {
                    // User is about to edit, so it's no longer purely generated
                    textInputLayoutTransactionID.helperText = "Editing generated ID"
                }
            }

            // Add transaction reference text watcher for real-time validation
            addTextChangedListener(
                TransactionIDTextWatcher(
                    textInputLayoutTransactionID,
                    onValidationChanged = { isValid ->
                        isTransactionIDValid = isValid
                        updateButtonState()
                    },
                    isGenerated = { isTransactionIDGenerated },
                )
            )
        }

        // Setup trailing icon click listener for generation
        textInputLayoutTransactionID.setEndIconOnClickListener {
            generateNewTransactionID()
        }
    }

    private fun generateNewTransactionID() {
        try {
            // Generate new secure ID
            val newReference = SecureTransactionIDGenerator.generateID(
                length = 35,
                prefix = "MBTID"
            )

            // Validate the generated reference
            val validationResult =
                SecureTransactionIDGenerator.validateGeneratedID(newReference)

            if (validationResult.isValid) {
                // Set the generated reference
                textInputEditTextTransactionID.setText(newReference)
                textInputEditTextTransactionID.setSelection(newReference.length)

                // Mark as generated
                isTransactionIDGenerated = true

                // Update UI
                textInputLayoutTransactionID.error = null
                textInputLayoutTransactionID.helperText =
                    "Generated secure ID (tap refresh for new)"

                // Update validation state
                isTransactionIDValid = true
                updateButtonState()

                // Show generation success feedback
                showIDGenerationFeedback()
            } else {
                textInputLayoutTransactionID.error = "Failed to generate valid ID"
            }
        } catch (e: Exception) {
            textInputLayoutTransactionID.error = "Error generating ID: ${e.message}"
        }
    }

    private fun showIDGenerationFeedback() {
        // Optional: Add subtle animation or feedback
        textInputLayoutTransactionID.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction {
                textInputLayoutTransactionID.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    // Method to get the current transaction reference
    fun getCurrentTransactionID(): String? {
        return if (isTransactionIDValid) {
            textInputEditTextTransactionID.text.toString()
        } else {
            null
        }
    }

    // Method to check if current reference is generated
    fun isIDGenerated(): Boolean {
        return isTransactionIDGenerated
    }

    /** Returns validated Transaction ID or null */
    fun getTransactionId(): String? =
        if (isTransactionIDValid) textInputEditTextTransactionID.text?.toString()?.trim() else null

    private fun configureTransactionReferenceKeyboard() {
        textInputEditTextTransactionReference.apply {
            // Use visible password input type for alphanumeric keyboard with numbers first
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS

            // IME options for better navigation
            imeOptions = EditorInfo.IME_ACTION_NEXT or EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set single line
            isSingleLine = true
            maxLines = 1

            // Custom key listener for alphanumeric input
            keyListener = android.text.method.DigitsKeyListener.getInstance(
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._-"
            )
        }
    }

    private fun setupTransactionReferenceWithGenerator() {
        // Configure input type for alphanumeric input with numbers prioritized
        textInputEditTextTransactionReference.apply {
            // Use textVisiblePassword to show alphanumeric keyboard with numbers first
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Set input filters
            filters = arrayOf(
                InputFilter.LengthFilter(25), // Max 25 characters
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow only alphanumeric characters, dots, hyphens, underscores
                    val allowedChars =
                        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
                    val filtered = StringBuilder()

                    for (i in start until end) {
                        val char = source[i]
                        if (allowedChars.contains(char)) {
                            // Convert to uppercase for consistency
                            filtered.append(char.uppercaseChar())
                        }
                    }

                    if (filtered.length == end - start) {
                        null // Accept all characters (already filtered)
                    } else {
                        filtered.toString() // Return filtered string
                    }
                }
            )

            // Add transaction reference text watcher for real-time validation
            addTextChangedListener(
                TransactionReferenceTextWatcher(
                    textInputLayoutTransactionReference,
                    onValidationChanged = { isValid ->
                        isTransactionReferenceValid = isValid
                        updateButtonState()
                    },
                    onReferenceTypeDetected = { type ->
                        currentReferenceType = type
                    },
                    isGenerated = { isCurrentReferenceGenerated }
                )
            )

            // Handle manual editing (mark as no longer generated)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && isCurrentReferenceGenerated) {
                    // User is about to edit, so it's no longer purely generated
                    textInputLayoutTransactionReference.helperText = "Editing generated reference"
                }
            }
        }

        // Setup trailing icon click listener for generation
        textInputLayoutTransactionReference.setEndIconOnClickListener {
            generateNewTransactionReference()
        }
    }

    private fun generateNewTransactionReference() {
        try {
            // Generate new secure reference
            val newReference = SecureTransactionReferenceGenerator.generateSecureReference(
                format = ReferenceFormat.ALPHANUMERIC_12,
                prefix = "TXN"
            )

            // Validate the generated reference
            val validationResult =
                SecureTransactionReferenceGenerator.validateGeneratedReference(newReference)

            if (validationResult.isValid) {
                // Set the generated reference
                textInputEditTextTransactionReference.setText(newReference)
                textInputEditTextTransactionReference.setSelection(newReference.length)

                // Mark as generated
                isCurrentReferenceGenerated = true

                // Update UI
                textInputLayoutTransactionReference.error = null
                textInputLayoutTransactionReference.helperText =
                    "Generated secure reference (tap refresh for new)"

                // Update validation state
                isTransactionReferenceValid = true
                updateButtonState()

                // Show generation success feedback
                showGenerationFeedback()
            } else {
                textInputLayoutTransactionReference.error = "Failed to generate valid reference"
            }
        } catch (e: Exception) {
            textInputLayoutTransactionReference.error = "Error generating reference: ${e.message}"
        }
    }

    private fun showGenerationFeedback() {
        // Optional: Add subtle animation or feedback
        textInputLayoutTransactionReference.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction {
                textInputLayoutTransactionReference.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    // Method to get the current transaction reference
    fun getCurrentTransactionReference(): String? {
        return if (isTransactionReferenceValid) {
            textInputEditTextTransactionReference.text.toString()
        } else {
            null
        }
    }

    // Method to check if current reference is generated
    fun isReferenceGenerated(): Boolean {
        return isCurrentReferenceGenerated
    }

    // Method to get reference generation format options
    fun showFormatOptions() {
        val formats = SecureTransactionReferenceGenerator.getFormatExamples()
        // Could show a dialog with different format options
    }

    private fun configureTransactionNoteKeyboard() {
        textInputEditTextTransactionNote.apply {
            // Optimal input type for multiline text notes
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // IME options for multiline text
            imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI

            // Set minimum and maximum lines for better UX
            minLines = 3
            maxLines = 6

            // Proper gravity for multiline text
            gravity = android.view.Gravity.TOP or android.view.Gravity.START

            // Enable scrolling
            isVerticalScrollBarEnabled = true
            scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_INSET

            // Disable horizontal scrolling
            setHorizontallyScrolling(false)
        }
    }

    private fun setupTransactionNoteValidation() {
        // Configure input type for multiline text with proper capitalization
        textInputEditTextTransactionNote.apply {
            // Set input type for multiline text with sentence capitalization
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for multiline (Done action instead of Next)
            imeOptions = EditorInfo.IME_ACTION_DONE

            // Set input filters
            filters = arrayOf(
                InputFilter.LengthFilter(280), // Max 280 characters
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow most characters but filter out some problematic ones
                    val filtered = StringBuilder()

                    for (i in start until end) {
                        val char = source[i]
                        when {
                            char.isLetterOrDigit() -> filtered.append(char)
                            char.isWhitespace() -> filtered.append(char)
                            char in ".,!?;:'-\"()[]{}@#$%&*+=<>/\\|~`^" -> filtered.append(char)
                            // Filter out control characters except newline and carriage return
                            char.code >= 32 || char == '\n' || char == '\r' -> filtered.append(char)
                        }
                    }

                    if (filtered.length == end - start) {
                        null // Accept all characters
                    } else {
                        filtered.toString() // Return filtered string
                    }
                }
            )

            // Enable vertical scrolling
            isVerticalScrollBarEnabled = true
            scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_INSET

            // Set proper gravity for multiline text
            gravity = android.view.Gravity.TOP or android.view.Gravity.START

            // Add transaction note text watcher for real-time validation
            addTextChangedListener(
                TransactionNoteTextWatcher(
                    textInputLayoutTransactionNote,
                    onValidationChanged = { isValid ->
                        isTransactionNoteValid = isValid
                        updateButtonState()
                    },
                    onCharacterInfoChanged = { charInfo ->
                        currentCharacterInfo = charInfo
                    },
                    isRequired = false // Transaction note is optional
                )
            )

            // Set focus change listener
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateTransactionNoteOnFocusLoss()
                }
            }

            // Handle editor action (Done button)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    clearFocus()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun validateTransactionNoteOnFocusLoss() {
        val noteText = textInputEditTextTransactionNote.text.toString()
        val validationResult = TransactionNoteValidator.validateTransactionNote(noteText, false)

        if (!validationResult.isValid) {
            textInputLayoutTransactionNote.error = validationResult.errorMessage
            isTransactionNoteValid = false
        } else {
            textInputLayoutTransactionNote.error = null
            isTransactionNoteValid = true

            // Format note if needed
            if (noteText.isNotBlank()) {
                val formattedNote = TransactionNoteValidator.formatTransactionNote(noteText)
                if (formattedNote != noteText) {
                    textInputEditTextTransactionNote.setText(formattedNote)
                    textInputEditTextTransactionNote.setSelection(formattedNote.length)
                }
            }
        }
        updateButtonState()
    }

    // Method to get validated transaction note
    fun getValidatedTransactionNote(): String? {
        return if (isTransactionNoteValid) {
            val noteText = textInputEditTextTransactionNote.text.toString()
            if (noteText.isBlank()) null else TransactionNoteValidator.formatTransactionNote(
                noteText
            )
        } else {
            null
        }
    }

    // Method to get character information
    fun getTransactionNoteInfo(): CharacterInfo? {
        return currentCharacterInfo
    }

    // Method to show note templates (optional feature)
    private fun showNoteTemplates() {
        val templates = TransactionNoteValidator.getCommonNoteTemplates()
        // Could show a dialog with common note templates for user selection
    }

    private fun setupCurrencyDropdown() {
        // Get all available currencies
        val currencies = CurrencyValidator.getAllCurrencies()

        // Create adapter
        currencyAdapter = CurrencyDropdownAdapter(this, currencies)
        materialAutoCompleteTextViewPayeeCurrency.setAdapter(currencyAdapter)

        // Set item click listener
        materialAutoCompleteTextViewPayeeCurrency.setOnItemClickListener { _, _, position, _ ->
            val currency = currencyAdapter.getItem(position)
            currency?.let {
                selectCurrency(it)
            }
        }

        // Handle manual text input (if user types)
        materialAutoCompleteTextViewPayeeCurrency.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateManualCurrencyInput()
            }
        }

        // Show dropdown when clicked
        materialAutoCompleteTextViewPayeeCurrency.setOnClickListener {
            if (!materialAutoCompleteTextViewPayeeCurrency.isPopupShowing) {
                materialAutoCompleteTextViewPayeeCurrency.showDropDown()
            }
        }
    }

    private fun selectCurrency(currency: CurrencyInfo) {
        selectedCurrency = currency
        materialAutoCompleteTextViewPayeeCurrency.setText(currency.displayName, false)

        // Update validation state
        isCurrencyValid = true
        textInputLayoutPayeeCurrency.error = null
        textInputLayoutPayeeCurrency.helperText = "Selected: ${currency.symbol} (${currency.code})"

        updateButtonState()
    }

    private fun setDefaultCurrency(currencyCode: String) {
        val currency = CurrencyValidator.getCurrencyByCode(currencyCode)
        currency?.let { selectCurrency(it) }
    }

    private fun validateManualCurrencyInput() {
        val inputText = materialAutoCompleteTextViewPayeeCurrency.text.toString().trim()

        if (inputText.isEmpty()) {
            textInputLayoutPayeeCurrency.error = "Please select a currency"
            isCurrencyValid = false
            selectedCurrency = null
            updateButtonState()
            return
        }

        // Try to extract currency code from input
        val currencyCode = extractCurrencyCode(inputText)
        val validationResult = CurrencyValidator.validateCurrencyCode(currencyCode)

        if (validationResult.isValid) {
            val currency = CurrencyValidator.getCurrencyByCode(currencyCode!!)
            if (currency != null) {
                selectCurrency(currency)
            } else {
                textInputLayoutPayeeCurrency.error = "Currency not found"
                isCurrencyValid = false
                selectedCurrency = null
            }
        } else {
            textInputLayoutPayeeCurrency.error = validationResult.errorMessage
            isCurrencyValid = false
            selectedCurrency = null
        }

        updateButtonState()
    }

    private fun extractCurrencyCode(input: String): String? {
        // Try to extract 3-letter currency code from various formats
        return when {
            input.length == 3 && input.matches(Regex("^[A-Za-z]{3}$")) -> input.uppercase()
            input.contains(" - ") -> input.substringBefore(" - ").trim().uppercase()
            input.contains("(") && input.contains(")") -> {
                val match = Regex("\\(([A-Za-z]{3})\\)").find(input)
                match?.groupValues?.get(1)?.uppercase()
            }

            else -> null
        }
    }

    // Method to get selected currency
    fun getSelectedCurrency(): CurrencyInfo? {
        return selectedCurrency
    }

    // Method to get currency code
    fun getSelectedCurrencyCode(): String? {
        return selectedCurrency?.code
    }

    // Method to format amount with selected currency
    fun formatAmountWithCurrency(amount: Double): String? {
        return selectedCurrency?.let { currency ->
            CurrencyValidator.formatAmount(amount, currency.code)
        }
    }

    private fun setupAppIDValidation() {
        // attach watcher
        textInputEditTextPayeeAppID.addTextChangedListener(
            AppIDTextWatcher(textInputLayoutPayeeAppID) { valid ->
                isAppIDValid = valid
                updateButtonState()
            }
        )

        updateButtonState()
    }

    /** Returns validated App-ID or null */
    fun getPayeeAppID(): String? =
        if (isAppIDValid) textInputEditTextPayeeAppID.text?.toString()?.trim() else null

    private fun setupAppNameValidation() {
        // Configure input type for app name with proper capitalization
        textInputEditTextPayeeAppName.apply {
            // Set input type for text with word capitalization and auto-complete
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS or
                    InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

            // Set IME options for better UX
            imeOptions = EditorInfo.IME_ACTION_NEXT

            // Set input filters
            filters = arrayOf(
                InputFilter.LengthFilter(50), // Max 50 characters
                InputFilter { source, start, end, dest, dstart, dend ->
                    // Allow letters, numbers, spaces, and basic punctuation
                    val allowedChars =
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .,!?()-_&+"
                    val filtered = StringBuilder()

                    for (i in start until end) {
                        val char = source[i]
                        if (allowedChars.contains(char)) {
                            filtered.append(char)
                        }
                    }

                    if (filtered.length == end - start) {
                        null // Accept all characters
                    } else {
                        filtered.toString() // Return filtered string
                    }
                }
            )

            // Add app name text watcher for real-time validation
            addTextChangedListener(
                AppNameTextWatcher(
                    textInputLayoutPayeeAppName,
                    onValidationChanged = { isValid ->
                        isAppNameValid = isValid
                        updateButtonState()
                    },
                    onCharacterInfoChanged = { charInfo ->
                        currentCharacterInfo = charInfo
                    }
                ))

            // Set focus change listener for validation when focus is lost
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAppNameOnFocusLoss()
                }
            }
        }
    }

    private fun validateAppNameOnFocusLoss() {
        val nameText = textInputEditTextPayeeAppName.text.toString()
        val validationResult = AppNameValidator.validateAppName(nameText)

        if (!validationResult.isValid) {
            textInputLayoutPayeeAppName.error = validationResult.errorMessage
            isAppNameValid = false
        } else {
            textInputLayoutPayeeAppName.error = null
            isAppNameValid = true

            // Format app name if needed
            val formattedName = AppNameValidator.formatAppName(nameText)
            if (formattedName != nameText) {
                textInputEditTextPayeeAppName.setText(formattedName)
                textInputEditTextPayeeAppName.setSelection(formattedName.length)
            }
        }
        updateButtonState()
    }

    // Method to get validated app name
    fun getValidatedAppName(): String? {
        return if (isAppNameValid) {
            AppNameValidator.formatAppName(textInputEditTextPayeeAppName.text.toString())
        } else {
            null
        }
    }

    // Method to get character information
    fun getAppNameInfo(): CharacterInfo? {
        return currentCharacterInfo
    }

    // Method to show app name suggestions (optional feature)
    private fun showAppNameSuggestions() {
        val suggestions = AppNameValidator.getAppNameSuggestions()
        // Could show a dialog with common app name suggestions for user selection
    }

    private fun updateButtonState() {
        materialButton.isEnabled = areOtherFieldsValid()
    }

    private fun areOtherFieldsValid(): Boolean {
        // Add validation for other fields as needed
        return isVPAValid && isNameValid && isMCCValid && isTransactionIDValid && isTransactionReferenceValid && isTransactionNoteValid && isAmountValid && isCurrencyValid && isAppIDValid && isAppNameValid
    }

    private fun setupButtonState() {
        // Initially disable the button
        materialButton.isEnabled = false
    }
}