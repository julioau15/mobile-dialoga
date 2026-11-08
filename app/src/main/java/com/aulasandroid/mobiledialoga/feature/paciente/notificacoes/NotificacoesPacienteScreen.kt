package com.aulasandroid.mobiledialoga.feature.paciente.notificacoes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun NotificacoesPacienteScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Notificações",
        descricao = "Avisos e lembretes para o paciente",
        icone = Icons.Default.Notifications,
        modifier = modifier
    )
}
