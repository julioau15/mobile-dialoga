package com.example.mobiledialoga.core.validation

import android.util.Patterns

object EmailValidator {

    // Retorna a mensagem de erro, ou null se o e-mail for válido
    fun validar(email: String): String? = when {
        email.isBlank() -> "Informe seu e-mail"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "E-mail inválido"
        else -> null
    }
}
