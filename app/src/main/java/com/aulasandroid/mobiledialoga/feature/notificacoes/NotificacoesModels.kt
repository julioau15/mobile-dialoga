package com.aulasandroid.mobiledialoga.feature.notificacoes

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class PerfilNotificacao { PACIENTE, PROFISSIONAL }

/** Para onde o "Ver →" leva. Cada perfil traduz para a sua própria subrota. */
enum class DestinoNotificacao { AGENDA, TAREFAS, TRATAMENTO, PESSOAS }

data class NotificacaoItem(
    val id: Int,
    val mensagem: String,
    val lida: Boolean,
    val dataCriacao: String,
    val referencia: String?,
    val idCategoria: Int
)

/** Visual de cada tipo de notificação (emoji, rótulo do chip e cores), igual ao protótipo. */
enum class CategoriaVisual(
    val emoji: String,
    val corIcone: Color,
    val corChip: Color,
    val corTextoChip: Color
) {
    TAREFA("✅", Color(0xFFF0E9F9), Color(0xFFEFE6F8), Color(0xFF6B4A93)),
    AGENDA("📅", Color(0xFFEAF2FF), Color(0xFFE3EEFD), Color(0xFF3B6BA5)),
    TRATAMENTO("💊", Color(0xFFFFF6DD), Color(0xFFFFF1CC), Color(0xFF8A6A12)),
    PESSOA("🩺", Color(0xFFE8F7EF), Color(0xFFDDF3E7), Color(0xFF2F7A52)),
    SISTEMA("🔔", Color(0xFFF0EEF4), Color(0xFFEAE7F0), Color(0xFF5E5870))
}

// ids de tbl_categoria no script-inserts.sql:
// 1 agenda/lembrete_consulta, 2 agenda/consulta_cancelada, 3 tarefa/nova_tarefa,
// 4 vinculo/solicitacao_recebida, 5 medicacao/hora_da_dose, 6 sistema
fun NotificacaoItem.categoriaVisual(): CategoriaVisual = when (idCategoria) {
    1, 2 -> CategoriaVisual.AGENDA
    3 -> CategoriaVisual.TAREFA
    4 -> CategoriaVisual.PESSOA
    5 -> CategoriaVisual.TRATAMENTO
    else -> CategoriaVisual.SISTEMA
}

fun NotificacaoItem.rotuloCategoria(perfil: PerfilNotificacao): String = when (idCategoria) {
    1, 2 -> "Agenda"
    3 -> "Tarefa"
    4 -> if (perfil == PerfilNotificacao.PACIENTE) "Profissional" else "Paciente"
    5 -> "Tratamento"
    else -> "Sistema"
}

fun NotificacaoItem.titulo(perfil: PerfilNotificacao): String = when (idCategoria) {
    1 -> when (referencia) {
        "nova_consulta" -> "Nova consulta"
        "consulta_alterada" -> "Consulta reagendada"
        else -> "Lembrete de consulta"
    }
    2 -> "Consulta cancelada"
    3 -> if (perfil == PerfilNotificacao.PACIENTE) "Nova tarefa recebida" else "Atualização de tarefa"
    4 -> if (perfil == PerfilNotificacao.PACIENTE) "Atualização de vínculo" else "Solicitação de acompanhamento"
    5 -> "Hora do medicamento"
    else -> "Aviso do Dialoga"
}

/** referencia_evento vem como "consulta:1", "tarefa:3", "vinculo:5", "dose:1" ou "nova_tarefa", "vinculo_aceito", etc. */
fun NotificacaoItem.destino(): DestinoNotificacao? {
    val ref = (referencia ?: "").lowercase().trim()
    val prefixo = ref.substringBefore(":")
    return when {
        prefixo in listOf("consulta", "lembrete_consulta", "nova_consulta") || ref.contains("consulta") || idCategoria in listOf(1, 2) -> DestinoNotificacao.AGENDA
        prefixo in listOf("tarefa", "nova_tarefa", "tarefa_respondida", "tarefa_atrasada") || ref.contains("tarefa") || idCategoria == 3 -> DestinoNotificacao.TAREFAS
        prefixo in listOf("dose", "tratamento", "hora_da_dose") || ref.contains("dose") || ref.contains("tratamento") || idCategoria == 5 -> DestinoNotificacao.TRATAMENTO
        prefixo in listOf("vinculo", "solicitacao_vinculo", "vinculo_aceito", "novo_vinculo") || ref.contains("vinculo") || idCategoria == 4 -> DestinoNotificacao.PESSOAS
        else -> null
    }
}

/** "2026-09-02T12:15:00.000Z" ou "2026-09-02 09:15:00" -> "02/09/2026 às 09:15". */
fun formatarDataNotificacao(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val saida = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    try {
        if (raw.endsWith("Z")) {
            val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            entrada.timeZone = TimeZone.getTimeZone("UTC")
            val data = entrada.parse(raw.take(19))
            if (data != null) return saida.format(data)
        }
    } catch (e: Exception) { }
    val m = Regex("(\\d{4})-(\\d{2})-(\\d{2})[T ](\\d{2}):(\\d{2})").find(raw) ?: return raw
    val (ano, mes, dia, hora, min) = m.destructured
    return "$dia/$mes/$ano às $hora:$min"
}

// ---------------------------------------------------------------------------------------------
// Configurações (tbl_preferencia_notificacao: categoria + tipo_especifico + ativo)
// ---------------------------------------------------------------------------------------------

data class ItemPreferencia(
    val tipo: String,
    val titulo: String,
    val descricao: String,
    val padrao: Boolean = true
)

data class GrupoPreferencia(
    val categoria: String,
    val emoji: String,
    val titulo: String,
    val itens: List<ItemPreferencia>
)

fun chavePreferencia(categoria: String, tipo: String) = "$categoria|$tipo"

fun gruposPreferencia(perfil: PerfilNotificacao): List<GrupoPreferencia> =
    if (perfil == PerfilNotificacao.PACIENTE) listOf(
        GrupoPreferencia(
            "tarefa", "✅", "Tarefas", listOf(
                ItemPreferencia("nova_tarefa", "Novas tarefas", "Quando seu profissional enviar uma nova atividade"),
                ItemPreferencia("lembrete_prazo", "Lembretes de prazo", "Alertas 24h antes do vencimento"),
                ItemPreferencia("tarefa_atrasada", "Alertas de atraso", "Notificação quando uma tarefa vencer sem resposta")
            )
        ),
        GrupoPreferencia(
            "medicacao", "💊", "Tratamentos", listOf(
                ItemPreferencia("hora_da_dose", "Lembrete de dose", "Notificação no horário de cada medicamento"),
                ItemPreferencia("dose_nao_confirmada", "Dose não confirmada", "Aviso quando uma dose não for registrada")
            )
        ),
        GrupoPreferencia(
            "agenda", "📅", "Agenda", listOf(
                ItemPreferencia("lembrete_consulta", "Lembrete de consulta", "Notificação 1h antes da consulta"),
                ItemPreferencia("lembrete_dia_anterior", "Lembrete no dia anterior", "Aviso na véspera da consulta")
            )
        )
    ) else listOf(
        GrupoPreferencia(
            "agenda", "📅", "Agenda", listOf(
                ItemPreferencia("nova_consulta", "Nova consulta agendada", "Quando um paciente confirmar ou solicitar agendamento"),
                ItemPreferencia("consulta_cancelada", "Cancelamento de consulta", "Alerta de consulta cancelada pelo paciente"),
                ItemPreferencia("lembrete_atendimento", "Lembrete de atendimento", "Notificação 30 min antes da próxima consulta"),
                ItemPreferencia("alteracoes_agenda", "Alterações na agenda", "Quando houver qualquer mudança nos agendamentos", padrao = false)
            )
        ),
        GrupoPreferencia(
            "tarefa", "✅", "Tarefas", listOf(
                ItemPreferencia("tarefa_respondida", "Tarefa respondida", "Quando um paciente enviar um questionário preenchido"),
                ItemPreferencia("tarefa_atrasada", "Tarefas em atraso", "Alerta quando uma tarefa vencer sem resposta"),
                ItemPreferencia("atividade_andamento", "Atividade em andamento", "Quando um paciente começar a preencher uma tarefa", padrao = false)
            )
        ),
        GrupoPreferencia(
            "vinculo", "👥", "Pacientes", listOf(
                ItemPreferencia("novo_vinculo", "Novo vínculo", "Quando um paciente aceitar o convite de acompanhamento"),
                ItemPreferencia("atualizacao_perfil", "Atualização de perfil", "Quando um paciente atualizar seus dados", padrao = false),
                ItemPreferencia("paciente_inativo", "Paciente inativo", "Alerta de pacientes sem atividade há mais de 30 dias")
            )
        )
    )

fun preferenciasPadrao(grupos: List<GrupoPreferencia>): Map<String, Boolean> =
    grupos.flatMap { g -> g.itens.map { chavePreferencia(g.categoria, it.tipo) to it.padrao } }.toMap()
