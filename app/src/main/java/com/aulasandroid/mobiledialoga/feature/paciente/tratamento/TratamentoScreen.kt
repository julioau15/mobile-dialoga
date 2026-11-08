package com.aulasandroid.mobiledialoga.feature.paciente.tratamento

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun TratamentoScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Tratamento",
        descricao = "Acompanhamento do tratamento do paciente",
        icone = Icons.Default.Healing,
        modifier = modifier
    )
}
