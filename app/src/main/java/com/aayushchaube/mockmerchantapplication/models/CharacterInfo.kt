package com.aayushchaube.mockmerchantapplication.models

data class CharacterInfo(
    val characterCount: Int,
    val wordCount: Int,
    val lineCount: Int,
    val remainingChars: Int,
    val isNearLimit: Boolean
)
