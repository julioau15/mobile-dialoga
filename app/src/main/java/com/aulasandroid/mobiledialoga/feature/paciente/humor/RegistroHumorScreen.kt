package com.aulasandroid.mobiledialoga.feature.paciente.humor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun RegistroHumorScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Registro de Humor",
        descricao = "Formulário de acompanhamento diário de humor",
        icone = Icons.Default.SentimentSatisfied,
        modifier = modifier
    )
}
