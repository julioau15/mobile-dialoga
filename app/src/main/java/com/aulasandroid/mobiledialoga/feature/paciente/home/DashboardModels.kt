package com.aulasandroid.mobiledialoga.feature.paciente.home

import com.aulasandroid.mobiledialoga.feature.paciente.humor.RegistroHumorInicial

data class DashboardState(
    val isLoading: Boolean = false,
    val nomeUsuario: String = "",
    val indicadoresHoje: IndicadoresHoje? = null,
    val sonoSemana: List<SonoDia> = emptyList(),
    val estatisticas: EstatisticasGerais = EstatisticasGerais(),
    val medicamentosHoje: List<MedicamentoDashboard> = emptyList(),
    val proximasConsultas: List<ConsultaDashboard> = emptyList(),
    val tarefasRecentes: List<TarefaDashboard> = emptyList(),
    val erro: String? = null
)

data class IndicadoresHoje(
    val energia: Int,    // 0-100
    val sentimento: Int, // 0-100
    val humor: Int,      // 0-100
    val pensamentos: Int // 0-100
)

data class SonoDia(
    val diaSemana: String,
    val horas: Double
)

data class EstatisticasGerais(
    val tratamentosAtivos: Int = 0,
    val proximasConsultas: Int = 0,
    val tarefasPendentes: Int = 0,
    val profissionais: Int = 0
)

data class MedicamentoDashboard(
    val id: String,
    val nome: String,
    val dose: String,
    val frequencia: String,
    val horarios: List<String>
)

data class ConsultaDashboard(
    val id: String,
    val data: String, // ISO
    val profissionalNome: String,
    val especialidade: String,
    val horario: String
)

data class TarefaDashboard(
    val id: String,
    val titulo: String,
    val prazo: String,
    val status: String // "Pendente", "Em andamento", "Concluída"
)
