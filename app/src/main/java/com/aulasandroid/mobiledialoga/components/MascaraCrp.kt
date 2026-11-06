package com.aulasandroid.mobiledialoga.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// Formata o CRP no formato "99/999999-AA" (2 dígitos de região + barra + até 6 números + opcional hífen + até 2 letras)
object MascaraCrp : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.uppercase()
        val digits = StringBuilder()
        val letters = StringBuilder()

        for (c in raw) {
            when {
                c.isDigit() -> {
                    if (digits.length < 8) digits.append(c) // 2 para região + 6 números
                }
                c.isLetter() -> {
                    if (digits.length >= 2 && letters.length < 2) letters.append(c) // até 2 letras no final
                }
            }
        }

        val region = digits.take(2).toString()
        val numbers = digits.drop(2).take(6).toString()
        val suffixLetters = letters.take(2).toString()

        val formatado = buildString {
            append(region)
            if (region.isNotEmpty() && (numbers.isNotEmpty() || raw.contains("/"))) {
                append("/")
            }
            append(numbers)
            if (numbers.isNotEmpty() && (suffixLetters.isNotEmpty() || raw.contains("-"))) {
                append("-")
            }
            append(suffixLetters)
        }

        val safeMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset.coerceAtMost(formatado.length)
            override fun transformedToOriginal(offset: Int): Int = offset.coerceAtMost(raw.length)
        }

        return TransformedText(AnnotatedString(formatado), safeMapping)
    }
}
