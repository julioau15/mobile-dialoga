package com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model

import com.aulasandroid.mobiledialoga.data.ApiRotas

data class TarefaItemDto(
    val id: Int,
    val titulo: String,
    val profissionalNome: String = "Seu profissional",
    val tipo: String? = null,
    val instrucoes: String? = null,
    val anexoArquivo: String? = null,
    val dataEnvio: String? = null,
    val idTarefaDestinatario: Int? = null,
    val prazo: String? = null,
    val status: String? = null,
    val dataConclusao: String? = null,
    val progresso: Int = 0,
    val criadaPorMim: Boolean = false
) {
    fun getStatusFormatado(): String {
        return when (status?.lowercase()?.trim()) {
            "concluida", "concluido", "finalizado", "concluída" -> "Concluída"
            "em_andamento", "em andamento" -> "Em andamento"
            "atrasada", "atrasado", "em_atraso", "em atraso" -> "Em atraso"
            else -> "Pendente"
        }
    }

    fun getPrazoFormatado(): String {
        if (prazo.isNullOrBlank()) return "Sem prazo"
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
    val tipo: String?, // "unica_escolha", "multipla_escolha", "escala", "humor", "texto"
    val opcoes: List<OpcaoPerguntaDto> = emptyList(),
    val obrigatoria: Boolean = true
) {
    /**
     * O profissional cria perguntas "unica_escolha" e "texto"; o seed/back também usa "multipla_escolha".
     * Qualquer tipo desconhecido vira "texto".
     */
    fun tipoNormalizado(): String = when (tipo?.lowercase()?.trim()) {
        "humor" -> "humor"
        "unica_escolha", "escolha_unica", "unica" -> "unica_escolha"
        "multipla_escolha", "multipla" -> "multipla_escolha"
        "escala" -> "escala"
        else -> "texto"
    }
}

data class RespostaItemDto(
    val idPergunta: Int,
    val idOpcaoPergunta: Int? = null,
    val opcoesSelecionadasIds: List<Int> = emptyList(),
    val valor: String? = null
)