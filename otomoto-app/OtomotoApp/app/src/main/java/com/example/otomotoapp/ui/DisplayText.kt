package com.example.otomotoapp.ui

import java.util.Locale

fun String.toDisplayValue(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return trimmed

    val lowerCased = trimmed.lowercase(Locale.getDefault())
    return lowerCased.replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) {
            firstChar.titlecase(Locale.getDefault())
        } else {
            firstChar.toString()
        }
    }
}

fun String?.toDisplayValueOrDash(): String =
    this?.takeIf { it.isNotBlank() }?.toDisplayValue() ?: "-"
