package com.aulasandroid.mobiledialoga.feature.paciente.notificacoes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.feature.notificacoes.DestinoNotificacao
import com.aulasandroid.mobiledialoga.feature.notificacoes.NotificacoesScreen
import com.aulasandroid.mobiledialoga.feature.notificacoes.PerfilNotificacao
import com.aulasandroid.mobiledialoga.feature.paciente.PacienteSubRota

@Composable
fun NotificacoesPacienteScreen(
    onNavigate: (PacienteSubRota) -> Unit,
    modifier: Modifier = Modifier
) {
    NotificacoesScreen(
        perfil = PerfilNotificacao.PACIENTE,
        modifier = modifier,
        onVer = { destino ->
            onNavigate(
                when (destino) {
                    DestinoNotificacao.AGENDA -> PacienteSubRota.AGENDA
                    DestinoNotificacao.TAREFAS -> PacienteSubRota.TAREFAS
                    DestinoNotificacao.TRATAMENTO -> PacienteSubRota.TRATAMENTO
                    DestinoNotificacao.PESSOAS -> PacienteSubRota.PROFISSIONAIS
                }
            )
        }
    )
}
