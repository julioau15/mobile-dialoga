package com.aulasandroid.mobiledialoga.core.error

object ErrorTranslator {

    /**
     * Traduz erros exclusivos da tela de Login.
     */
    fun traduzirLogin(mensagemBackend: String?): String {
        if (mensagemBackend.isNullOrBlank()) {
            return "E-mail ou senha incorretos."
        }
        val lower = mensagemBackend.lowercase()
        return when {
            lower.contains("conexão") || lower.contains("connect") || lower.contains("timeout") ||
            lower.contains("unable to resolve host") || lower.contains("failed to connect") ->
                "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."

            else ->
                "E-mail ou senha incorretos."
        }
    }

    /**
     * Traduz erros exclusivos da tela de Cadastro.
     */
    fun traduzirCadastro(mensagemBackend: String?): String {
        if (mensagemBackend.isNullOrBlank()) {
            return "Não foi possível concluir o cadastro. Verifique seus dados e tente novamente."
        }
        val lower = mensagemBackend.lowercase()
        return when {
            lower.contains("conexão") || lower.contains("connect") || lower.contains("timeout") ||
            lower.contains("unable to resolve host") || lower.contains("failed to connect") ->
                "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."

            lower.contains("e-mail") || lower.contains("email") ->
                "Este e-mail já está cadastrado no sistema."

            lower.contains("cpf") ->
                "Este CPF já está cadastrado no sistema."

            lower.contains("crp") ->
                "Este CRP já está cadastrado no sistema."

            lower.contains("celular") || lower.contains("telefone") ->
                "Este celular já está cadastrado no sistema."

            lower.contains("já cadastrado") || lower.contains("conflict") || lower.contains("409") ||
            lower.contains("existe") || lower.contains("duplicado") ->
                "Os dados informados já estão cadastrados no sistema."

            else ->
                mensagemBackend
        }
    }

    fun traduzir(mensagemBackend: String?): String {
        return traduzirLogin(mensagemBackend)
    }
}
