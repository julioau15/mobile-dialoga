package com.example.mobiledialoga.core.validation

object PhoneValidator {

    const val TAMANHO_MAXIMO = 11

    // Mantém só os dígitos (DDD + número), no máximo 11
    fun limpar(celular: String): String = celular.filter { it.isDigit() }.take(TAMANHO_MAXIMO)

    // Retorna a mensagem de erro, ou null se o celular for válido
    fun validar(celular: String): String? = when {
        celular.isEmpty() -> "Informe seu celular"
        celular.length < TAMANHO_MAXIMO -> "Celular inválido"
        else -> null
    }
}
