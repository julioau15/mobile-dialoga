package com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model

import com.aulasandroid.mobiledialoga.data.ApiRotas
import java.util.Calendar

/** Status de um paciente em uma tarefa. */
enum class StatusDestinatario { PENDENTE, EM_ANDAMENTO, CONCLUIDA, ATRASADA }

/** Status único exibido no card da tela "Tarefas" (resumo de todos os pacientes). */
enum class StatusTarefa(val rotulo: String) {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    RESPONDIDA("Respondida"),
    EM_ATRASO("Em atraso")
}

data class ProfissionalTarefaDto(
    val id: Int,
    val titulo: String,
    val tipo: String?,
    val instrucoes: String?,
    val isRascunho: Boolean = false,
    val anexoArquivo: String? = null,
    val dataEnvio: String? = null,
    val prazo: String? = null,
    val totalPerguntas: Int = 0,
    val destinatarios: List<TarefaDestinatarioProgressoDto> = emptyList()
) {
    /** Resume o andamento de todos os pacientes em um único status. */
    fun statusGeral(): StatusTarefa {
        if (destinatarios.isEmpty()) return StatusTarefa.PENDENTE
        val status = destinatarios.map { it.status() }
        return when {
            status.all { it == StatusDestinatario.CONCLUIDA } -> StatusTarefa.RESPONDIDA
            status.any { it == StatusDestinatario.ATRASADA } -> StatusTarefa.EM_ATRASO
            status.any { it == StatusDestinatario.EM_ANDAMENTO || it == StatusDestinatario.CONCLUIDA } -> StatusTarefa.EM_ANDAMENTO
            else -> StatusTarefa.PENDENTE
        }
    }

    /** Menor prazo entre os pacientes que ainda não concluíram (ISO yyyy-MM-dd). */
    fun prazoIso(): String? =
        prazo.paraIso()
            ?: destinatarios.filter { it.status() != StatusDestinatario.CONCLUIDA }
                .mapNotNull { it.prazo.paraIso() }
                .minOrNull()
            ?: destinatarios.mapNotNull { it.prazo.paraIso() }.minOrNull()

    fun getAnexoUrlCompleta(): String? {
        val a = anexoArquivo?.trim() ?: return null
        if (a.isBlank()) return null
        if (a.startsWith("http://") || a.startsWith("https://")) return a
        val base = ApiRotas.BASE_URL.substringBefore("/v1/dialoga").removeSuffix("/")
        val path = if (a.startsWith("/")) a else "/$a"
        return "$base$path"
    }
}

data class TarefaDestinatarioProgressoDto(
    val idTarefaDestinatario: Int,
    val idTarefa: Int,
    val tituloTarefa: String,
    val idPaciente: Int,
    val nomePaciente: String,
    val prazo: String?,
    val statusBruto: String?,
    val dataConclusao: String?,
    val ultimaModificacao: String? = null
) {
    fun status(): StatusDestinatario {
        val bruto = statusBruto?.lowercase()?.trim()
        return when (bruto) {
            "concluida", "concluido", "finalizado", "concluída", "respondida" -> StatusDestinatario.CONCLUIDA
            "atrasada", "atrasado", "em_atraso", "em atraso" -> StatusDestinatario.ATRASADA
            "em_andamento", "em andamento" -> StatusDestinatario.EM_ANDAMENTO
            else -> {
                val p = prazo.paraIso()
                if (p != null && p < hojeIso()) StatusDestinatario.ATRASADA else StatusDestinatario.PENDENTE
            }
        }
    }

    /** Rótulo do selo na tela de acompanhamento. */
    fun rotuloAcompanhamento(): String = when (status()) {
        StatusDestinatario.CONCLUIDA -> "Respondida"
        StatusDestinatario.EM_ANDAMENTO -> "Em andamento"
        else -> "Não preenchido"
    }

    /** Data usada em "Última modificação" e nos filtros de período (ISO yyyy-MM-dd). */
    fun dataModificacaoIso(): String? =
        ultimaModificacao.paraIso() ?: dataConclusao.paraIso()
}

data class PacienteOpcaoDto(val id: Int, val nome: String)

data class RespostaVisualizacaoDto(
    val id: Int,
    val enunciadoPergunta: String,
    val respostaValor: String
)

// ---------- Datas (sem java.time para não exigir API 26) ----------

/** "2026-09-07T00:00:00.000Z" -> "2026-09-07"; devolve null se não parecer data. */
fun String?.paraIso(): String? {
    val s = this?.trim()?.take(10) ?: return null
    return if (Regex("""\d{4}-\d{2}-\d{2}""").matches(s)) s else null
}

/** "2026-09-07" -> "07/09/2026" */
fun String?.formatarDataBr(vazio: String = "—"): String {
    val iso = paraIso() ?: return vazio
    return "${iso.substring(8, 10)}/${iso.substring(5, 7)}/${iso.substring(0, 4)}"
}

fun hojeIso(): String {
    val c = Calendar.getInstance()
    return String.format(java.util.Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}
