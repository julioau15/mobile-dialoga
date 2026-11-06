package com.aulasandroid.mobiledialoga.core.error

object ErrorTranslator {
    fun traduzir(mensagemBackend: String?): String {
        if (mensagemBackend.isNullOrBlank()) {
            return "Ocorreu um erro inesperado. Tente novamente mais tarde."
        }
        val lower = mensagemBackend.lowercase()
        return when {
            lower.contains("senha") || lower.contains("email") || lower.contains("credencial") || lower.contains("unauthorized") || lower.contains("401") ->
                "E-mail ou senha incorretos."
            lower.contains("já cadastrado") || lower.contains("conflict") || lower.contains("409") ->
                "Os dados informados já estão cadastrados no sistema."
            lower.contains("em análise") || lower.contains("pendente") ->
                "Seu cadastro está em análise pela equipe."
            lower.contains("recusado") || lower.contains("não aprovado") ->
                "Seu cadastro não foi aprovado pela equipe."
            lower.contains("conexão") || lower.contains("connect") || lower.contains("timeout") || lower.contains("unable to resolve host") || lower.contains("failed to connect") ->
                "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."
            else ->
                "Não foi possível concluir a operação. Tente novamente."
        }
    }
}
