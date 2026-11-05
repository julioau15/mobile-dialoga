package com.aulasandroid.mobiledialoga.core.validation

object PasswordValidator {

    const val TAMANHO_MINIMO = 6

    // Retorna a mensagem de erro, ou null se a senha for válida
    fun validar(senha: String): String? = when {
        senha.isEmpty() -> "Informe a senha"
        senha.length < TAMANHO_MINIMO -> "A senha deve ter no mínimo $TAMANHO_MINIMO caracteres"
        else -> null
    }

    fun validarConfirmacao(senha: String, confirmacao: String): String? = when {
        confirmacao.isEmpty() -> "Repita a senha"
        confirmacao != senha -> "As senhas não coincidem"
        else -> null
    }
}