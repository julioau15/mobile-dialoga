package com.aulasandroid.mobiledialoga.feature.profissional.consultas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun ConsultasProfissionalScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Agenda",
        descricao = "Gestão de consultas e disponibilidade da agenda",
        icone = Icons.Default.Event,
        modifier = modifier
    )
}
