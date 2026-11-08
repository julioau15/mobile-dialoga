package com.aulasandroid.mobiledialoga.feature.profissional.notificacoes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun NotificacoesProfissionalScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Notificações",
        descricao = "Alertas e avisos do profissional",
        icone = Icons.Default.Notifications,
        modifier = modifier
    )
}
