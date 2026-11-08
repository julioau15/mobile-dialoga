package com.aulasandroid.mobiledialoga.feature.paciente.profissionais

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun BuscaProfissionaisScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Profissionais",
        descricao = "Busca e acompanhamento de profissionais da saúde",
        icone = Icons.Default.Psychology,
        modifier = modifier
    )
}
