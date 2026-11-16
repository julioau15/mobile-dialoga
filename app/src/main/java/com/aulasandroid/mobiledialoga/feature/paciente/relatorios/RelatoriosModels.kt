package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import com.aulasandroid.mobiledialoga.feature.paciente.home.RegistroHumorApi

/** Um dia do calendário de período (sempre ISO yyyy-MM-dd). */
data class MesVisivel(val ano: Int, val mes: Int) // mes: 0-11 (igual ao Calendar)

/** Média de um indicador no período, numa escala 1..maximo (ex.: energia 1.9/3). */
data class IndicadorMedia(
    val rotulo: String,
    val emoji: String,
    val media: Double?,
    val maximo: Int
) {
    val proporcao: Float get() = if (media == null) 0f else (media / maximo).toFloat().coerceIn(0f, 1f)
}

data class SonoPonto(val dataIso: String, val horas: Double)

data class ResumoSono(
    val media: Double,
    val minimo: Double,
    val maximo: Double
) {
    /** Faixa recomendada: 7h a 9h. */
    val abaixo: Boolean get() = media < 7.0
    val acima: Boolean get() = media > 9.0
}

data class SolicitacaoAcessoUi(
    val id: Int,
    val idProfissional: Int,
    val nome: String,
    val especialidade: String,
    val status: String, // "Pendente", "Aprovada", "Recusada"
    val foto: String? = null,
    val categorias: Set<String> = emptySet()
) {
    val iniciais: String
        get() = nome.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "PR" }
    val pendente: Boolean get() = status == "Pendente"
}

data class TarefaRelatorio(
    val id: Int,
    val titulo: String,
    val prazoIso: String?,
    val dataEnvioIso: String?,
    val dataConclusaoIso: String?,
    val status: String, // "Pendente", "Em andamento", "Em atraso", "Concluída"
    val progresso: Int
) {
    val prazoFormatado: String
        get() = prazoIso?.split("-")?.takeIf { it.size == 3 }?.let { "${it[2]}/${it[1]}/${it[0]}" } ?: "Sem prazo"
}

data class RelatoriosState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val registros: List<RegistroHumorApi> = emptyList(),
    val tarefas: List<TarefaRelatorio> = emptyList(),
    val solicitacoes: List<SolicitacaoAcessoUi> = emptyList(),
    /** Período escolhido. Com só o primeiro toque, fim == null e o período é aquele dia. */
    val inicioIso: String,
    val fimIso: String?,
    val mesVisivel: MesVisivel,
    val respondendoSolicitacao: Int? = null,
    val aviso: String? = null
) {
    val periodoFim: String get() = fimIso ?: inicioIso

    val registrosNoPeriodo: List<RegistroHumorApi>
        get() = registros.filter { it.data >= inicioIso && it.data <= periodoFim }

    val diasComRegistro: Set<String> get() = registros.map { it.data }.toSet()
}
