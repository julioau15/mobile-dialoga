package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun RelatoriosPacienteScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Relatórios",
        descricao = "Relatórios e estatísticas de progresso do paciente",
        icone = Icons.Default.BarChart,
        modifier = modifier
    )
}
