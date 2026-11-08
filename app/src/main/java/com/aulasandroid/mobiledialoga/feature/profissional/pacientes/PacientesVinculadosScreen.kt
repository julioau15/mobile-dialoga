package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun PacientesVinculadosScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Pacientes",
        descricao = "Lista de pacientes vinculados ao profissional",
        icone = Icons.Default.People,
        modifier = modifier
    )
}
