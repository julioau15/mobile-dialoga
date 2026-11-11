package com.aulasandroid.mobiledialoga.feature.profissional.notificacoes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.feature.notificacoes.DestinoNotificacao
import com.aulasandroid.mobiledialoga.feature.notificacoes.NotificacoesScreen
import com.aulasandroid.mobiledialoga.feature.notificacoes.PerfilNotificacao
import com.aulasandroid.mobiledialoga.feature.profissional.ProfissionalSubRota

@Composable
fun NotificacoesProfissionalScreen(
    onNavigate: (ProfissionalSubRota) -> Unit,
    modifier: Modifier = Modifier
) {
    NotificacoesScreen(
        perfil = PerfilNotificacao.PROFISSIONAL,
        modifier = modifier,
        onVer = { destino ->
            when (destino) {
                DestinoNotificacao.AGENDA -> onNavigate(ProfissionalSubRota.AGENDA)
                DestinoNotificacao.TAREFAS -> onNavigate(ProfissionalSubRota.TAREFAS)
                DestinoNotificacao.PESSOAS -> onNavigate(ProfissionalSubRota.PACIENTES)
                DestinoNotificacao.TRATAMENTO -> Unit // profissional não tem tela de tratamento
            }
        }
    )
}
