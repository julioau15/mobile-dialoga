package com.example.mobiledialoga.core.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// Mostra os dígitos digitados como "(11) 99999-9999" sem alterar o valor guardado
object MascaraCelular : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val formatado = buildString {
            digitos.forEachIndexed { i, c ->
                when (i) {
                    0 -> append("(")
                    2 -> append(") ")
                    7 -> append("-")
                }
                append(c)
            }
        }

        val mapeamento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val pos = when {
                    offset == 0 -> 0
                    offset <= 2 -> offset + 1
                    offset <= 7 -> offset + 3
                    else -> offset + 4
                }
                return pos.coerceAtMost(formatado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val pos = when {
                    offset <= 1 -> 0
                    offset <= 3 -> offset - 1
                    offset <= 5 -> 2
                    offset <= 10 -> offset - 3
                    offset == 11 -> 7
                    else -> offset - 4
                }
                return pos.coerceIn(0, digitos.length)
            }
        }

        return TransformedText(AnnotatedString(formatado), mapeamento)
    }
}
