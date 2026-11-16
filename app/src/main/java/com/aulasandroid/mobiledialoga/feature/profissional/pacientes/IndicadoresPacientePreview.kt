package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.feature.paciente.home.RegistroHumorApi
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.DatasRelatorio
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalUiState

@Preview(showBackground = true, widthDp = 360, heightDp = 1450)
@Composable
private fun IndicadoresPacientePreview() {
    val paciente = PacienteVinculadoUi(
        id = 1, idVinculo = 1, nome = "Ana Beatriz Ferreira",
        ativo = true, statusVinculo = 1, origemCadastro = 1,
        desdeIso = null, email = null, celular = null
    )
    val registros = listOf(
        RegistroHumorApi(DatasRelatorio.somarDias(DatasRelatorio.hoje(), -2), "Feliz", "Alta", "Muito bem", 8.0, "Positivos", "Fim de semana ótimo. Exercício e leitura durante a tarde."),
        RegistroHumorApi(DatasRelatorio.somarDias(DatasRelatorio.hoje(), -1), "Ansioso", "Baixa", "Neutro", 5.5, "Acelerados", "Reunião de trabalho complicada. Dificuldade de concentração."),
        RegistroHumorApi(DatasRelatorio.hoje(), "Calmo", "Média", "Bem", 7.5, "Positivos", "Consulta com o profissional. Boa conversa sobre metas da semana.")
    )
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F0FF)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AbaIndicadores(
            paciente = paciente,
            estado = PacientesProfissionalState(
                idIndicadores = 1, acessos = mapOf(1 to "aprovada"),
                categorias = setOf("Sentimento", "Energia", "Humor", "Pensamentos", "Horas de sono", "Acontecimentos"),
                registrosCompartilhados = registros
            ),
            tarefas = TarefasProfissionalUiState(),
            onSolicitarAcesso = {},
            onRecarregar = {}
        )
    }
}
