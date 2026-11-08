package com.aulasandroid.mobiledialoga.feature.paciente.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun PacienteHomeScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Dashboard",
        descricao = "Tela principal do paciente",
        icone = Icons.Default.Home,
        modifier = modifier
    )
}
