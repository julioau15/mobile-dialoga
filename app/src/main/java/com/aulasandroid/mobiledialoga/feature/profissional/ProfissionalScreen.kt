package com.aulasandroid.mobiledialoga.feature.profissional

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.components.DialogaLayout
import com.aulasandroid.mobiledialoga.components.DialogaMenuItem
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.ConsultasProfissionalScreen
import com.aulasandroid.mobiledialoga.feature.profissional.home.ProfissionalHomeScreen
import com.aulasandroid.mobiledialoga.feature.profissional.notificacoes.NotificacoesProfissionalScreen
import com.aulasandroid.mobiledialoga.feature.profissional.pacientes.PacientesVinculadosScreen
import com.aulasandroid.mobiledialoga.feature.profissional.perfil.PerfilProfissionalScreen
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalScreen

enum class ProfissionalSubRota(val titulo: String) {
    DASHBOARD("Dashboard"),
    TAREFAS("Tarefas"),
    PACIENTES("Pacientes"),
    AGENDA("Agenda"),
    NOTIFICACOES("Notificações"),
    PERFIL("Perfil")
}

@Composable
fun ProfissionalScreen(onLogout: () -> Unit) {
    val authState = useAuth()
    var subRotaAtual by rememberSaveable { mutableStateOf(ProfissionalSubRota.DASHBOARD) }

    // Busca as informações atualizadas do usuário da API (/profissionais/me) ao carregar a tela
    LaunchedEffect(Unit) {
        authState.carregarPerfilMeu()
    }

    val profile = authState.userProfile
    val nomeExibicao = profile?.getNomeExibicao() ?: "Profissional"
    val iniciais = profile?.getIniciais() ?: "PR"
    val fotoUrl = profile?.getFotoUrlCompleta()

    val itensMenu = remember {
        listOf(
            DialogaMenuItem("dashboard", "Dashboard", iconeRes = R.drawable.ic_sidebar_dashboard),
            DialogaMenuItem("tarefas", "Tarefas", iconeRes = R.drawable.ic_sidebar_tarefas),
            DialogaMenuItem("pacientes", "Pacientes", iconeRes = R.drawable.ic_sidebar_profissionais),
            DialogaMenuItem("agenda", "Agenda", iconeRes = R.drawable.ic_sidebar_agenda),
            DialogaMenuItem("notificacoes", "Notificações", iconeRes = R.drawable.ic_sidebar_notificacao),
            DialogaMenuItem("perfil", "Perfil", iconeRes = R.drawable.ic_sidebar_perfil)
        )
    }

    val itemSelecionadoId = when (subRotaAtual) {
        ProfissionalSubRota.DASHBOARD -> "dashboard"
        ProfissionalSubRota.TAREFAS -> "tarefas"
        ProfissionalSubRota.PACIENTES -> "pacientes"
        ProfissionalSubRota.AGENDA -> "agenda"
        ProfissionalSubRota.NOTIFICACOES -> "notificacoes"
        ProfissionalSubRota.PERFIL -> "perfil"
    }

    DialogaLayout(
        tituloTela = subRotaAtual.titulo,
        nomeUsuario = nomeExibicao,
        iniciaisUsuario = iniciais,
        fotoUrl = fotoUrl,
        itensMenu = itensMenu,
        itemSelecionadoId = itemSelecionadoId,
        onSelecionarItem = { id ->
            subRotaAtual = when (id) {
                "dashboard" -> ProfissionalSubRota.DASHBOARD
                "tarefas" -> ProfissionalSubRota.TAREFAS
                "pacientes" -> ProfissionalSubRota.PACIENTES
                "agenda" -> ProfissionalSubRota.AGENDA
                "notificacoes" -> ProfissionalSubRota.NOTIFICACOES
                "perfil" -> ProfissionalSubRota.PERFIL
                else -> ProfissionalSubRota.DASHBOARD
            }
        },
        onLogout = {
            authState.logout()
            onLogout()
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (subRotaAtual) {
            ProfissionalSubRota.DASHBOARD -> ProfissionalHomeScreen(
                modifier = modifier,
                onAbrirAgenda = { subRotaAtual = ProfissionalSubRota.AGENDA },
                onAbrirPacientes = { subRotaAtual = ProfissionalSubRota.PACIENTES },
                onAbrirTarefas = { subRotaAtual = ProfissionalSubRota.TAREFAS }
            )
            ProfissionalSubRota.TAREFAS -> TarefasProfissionalScreen(modifier = modifier)
            ProfissionalSubRota.PACIENTES -> PacientesVinculadosScreen(modifier = modifier)
            ProfissionalSubRota.AGENDA -> ConsultasProfissionalScreen(modifier = modifier)
            ProfissionalSubRota.NOTIFICACOES -> NotificacoesProfissionalScreen(onNavigate = { subRotaAtual = it }, modifier = modifier)
            ProfissionalSubRota.PERFIL -> PerfilProfissionalScreen(modifier = modifier)
        }
    }
}