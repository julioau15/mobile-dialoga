package com.aulasandroid.mobiledialoga.feature.paciente.consultas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun ConsultasPacienteScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Agenda",
        descricao = "Agendamento e consultas do paciente",
        icone = Icons.Default.Event,
        modifier = modifier
    )
}
