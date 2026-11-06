package com.aulasandroid.mobiledialoga.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// Formata o CPF como "000.000.000-00"
object MascaraCpf : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }.take(11)
        val formatado = buildString {
            digitos.forEachIndexed { i, c ->
                when (i) {
                    3, 6 -> append(".")
                    9 -> append("-")
                }
                append(c)
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val pos = when {
                    offset <= 3 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 9 -> offset + 2
                    else -> offset + 3
                }
                return pos.coerceAtMost(formatado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val pos = when {
                    offset <= 3 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 11 -> offset - 2
                    else -> offset - 3
                }
                return pos.coerceIn(0, digitos.length)
            }
        }

        return TransformedText(AnnotatedString(formatado), mapping)
    }
}
