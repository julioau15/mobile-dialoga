package com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model

import com.aulasandroid.mobiledialoga.data.ApiRotas

data class TarefaItemDto(
    val id: Int,
    val titulo: String,
    val tipo: String?,
    val instrucoes: String?,
    val anexoArquivo: String?,
    val dataEnvio: String?,
    val idTarefaDestinatario: Int?,
    val prazo: String?,
    val status: String?,
    val dataConclusao: String?
) {
    fun getStatusFormatado(): String {
        return when (status?.lowercase()?.trim()) {
            "concluida", "concluido", "finalizado", "concluída" -> "Concluída"
            "em_andamento", "em andamento" -> "Em andamento"
            "atrasada", "atrasado" -> "Atrasada"
            else -> "Pendente"
        }
    }

    fun getPrazoFormatado(): String {
        if (prazo.isNullOrBlank()) return "Sem prazo definido"
        return try {
            val limpo = prazo.take(10)
            val partes = limpo.split("-")
            if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else prazo
        } catch (e: Exception) { prazo }
    }

    fun getAnexoUrlCompleta(): String? {
        val a = anexoArquivo?.trim() ?: return null
        if (a.isBlank()) return null
        if (a.startsWith("http://") || a.startsWith("https://")) return a
        val base = ApiRotas.BASE_URL.substringBefore("/v1/dialoga").removeSuffix("/")
        val path = if (a.startsWith("/")) a else "/$a"
        return "$base$path"
    }
}

data class OpcaoPerguntaDto(
    val id: Int,
    val texto: String
)

data class PerguntaDto(
    val id: Int,
    val enunciado: String,
    val tipo: String?, // "unica_escolha", "multipla_escolha", "texto", etc.
    val opcoes: List<OpcaoPerguntaDto> = emptyList()
)

data class RespostaItemDto(
    val idPergunta: Int,
    val idOpcaoPergunta: Int? = null,
    val valor: String? = null
)
