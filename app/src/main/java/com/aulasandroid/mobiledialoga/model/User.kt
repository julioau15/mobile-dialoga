package com.aulasandroid.mobiledialoga.model

import com.aulasandroid.mobiledialoga.data.ApiRotas

data class UserProfile(
    val id: Int? = null,
    val nome: String? = null,
    val apelido: String? = null,
    val email: String? = null,
    val foto: String? = null,
    val primeiroAcesso: Boolean = false
) {
    /**
     * Retorna o nome de exibição:
     * 1. Se tiver apelido válido, utiliza o apelido.
     * 2. Se não tiver, utiliza os dois primeiros nomes do nome completo.
     */
    fun getNomeExibicao(): String {
        val ap = apelido?.trim()
        if (!ap.isNullOrBlank() && ap.lowercase() != "null" && ap.lowercase() != "undefined") {
            return ap
        }
        val nomeLimpo = nome?.trim()
        if (nomeLimpo.isNullOrBlank() || nomeLimpo.lowercase() == "null" || nomeLimpo.lowercase() == "undefined") {
            return "Usuário"
        }
        val partes = nomeLimpo.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            partes.size >= 2 -> "${partes[0]} ${partes[1]}"
            partes.size == 1 -> partes[0]
            else -> "Usuário"
        }
    }

    /**
     * Retorna as iniciais dos dois primeiros nomes para desenhar o círculo de perfil
     */
    fun getIniciais(): String {
        val ap = apelido?.trim()
        val nomeCompletoOuApelido = if (!ap.isNullOrBlank() && ap.lowercase() != "null" && ap.lowercase() != "undefined") {
            ap
        } else {
            nome?.trim() ?: ""
        }
        val partes = nomeCompletoOuApelido.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            partes.size >= 2 -> {
                val i1 = partes[0].firstOrNull()?.uppercaseChar() ?: ""
                val i2 = partes[1].firstOrNull()?.uppercaseChar() ?: ""
                "$i1$i2"
            }
            partes.size == 1 -> {
                partes[0].take(2).uppercase()
            }
            else -> "UD"
        }
    }

    /**
     * Retorna a URL completa da foto (se for caminho relativo, adiciona o servidor base)
     */
    fun getFotoUrlCompleta(): String? {
        val f = foto?.trim() ?: return null
        if (f.isBlank() || f.lowercase() == "null" || f.lowercase() == "undefined") return null
        if (f.startsWith("http://") || f.startsWith("https://") || f.startsWith("data:image")) {
            return f
        }
        val base = ApiRotas.BASE_URL.substringBefore("/v1/dialoga").removeSuffix("/")
        val path = if (f.startsWith("/")) f else "/$f"
        return "$base$path"
    }
}
