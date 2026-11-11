package com.aulasandroid.mobiledialoga.feature.paciente

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.components.DialogaLayout
import com.aulasandroid.mobiledialoga.components.DialogaMenuItem
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.ConsultasPacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.home.PacienteDashboardViewModel
import com.aulasandroid.mobiledialoga.feature.paciente.home.PacienteHomeScreen
import com.aulasandroid.mobiledialoga.feature.paciente.humor.RegistroHumorDialog
import com.aulasandroid.mobiledialoga.feature.paciente.notificacoes.NotificacoesPacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.perfil.PerfilPacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.profissionais.BuscaProfissionaisScreen
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.RelatoriosPacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.TarefasPacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.tratamento.TratamentoScreen

enum class PacienteSubRota(val titulo: String) {
    DASHBOARD("Dashboard"),
    RELATORIOS("Relatórios"),
    TRATAMENTO("Tratamentos"),
    AGENDA("Agenda"),
    PROFISSIONAIS("Profissionais"),
    TAREFAS("Tarefas"),
    NOTIFICACOES("Notificações"),
    PERFIL("Perfil"),
    HUMOR("Registro de Humor")
}

@Composable
fun PacienteScreen(onLogout: () -> Unit) {
    val authState = useAuth()
    // Mesmo ViewModel para o dashboard e para o modal de humor (salvar -> recarregar)
    val dashboardViewModel: PacienteDashboardViewModel = viewModel()
    var subRotaAtual by rememberSaveable { mutableStateOf(PacienteSubRota.DASHBOARD) }

    LaunchedEffect(Unit) {
        authState.carregarPerfilMeu()
    }

    val profile = authState.userProfile
    val nomeExibicao = profile?.getNomeExibicao() ?: "Paciente"
    val iniciais = profile?.getIniciais() ?: "PA"
    val fotoUrl = profile?.getFotoUrlCompleta()

    val itensMenu = remember {
        listOf(
            DialogaMenuItem("dashboard", "Dashboard", iconeRes = R.drawable.ic_sidebar_dashboard),
            DialogaMenuItem("relatorios", "Relatórios", iconeRes = R.drawable.ic_sidebar_relatorios),
            DialogaMenuItem("tratamento", "Tratamentos", iconeRes = R.drawable.ic_sidebar_tratamentos),
            DialogaMenuItem("agenda", "Agenda", iconeRes = R.drawable.ic_sidebar_agenda),
            DialogaMenuItem("profissionais", "Profissionais", iconeRes = R.drawable.ic_sidebar_profissionais),
            DialogaMenuItem("tarefas", "Tarefas", iconeRes = R.drawable.ic_sidebar_tarefas),
            DialogaMenuItem("notificacoes", "Notificações", iconeRes = R.drawable.ic_sidebar_notificacao),
            DialogaMenuItem("perfil", "Perfil", iconeRes = R.drawable.ic_sidebar_perfil)
        )
    }

    // O modal de humor abre por cima do dashboard, então título e menu continuam em "Dashboard".
    val rotaVisivel = if (subRotaAtual == PacienteSubRota.HUMOR) PacienteSubRota.DASHBOARD else subRotaAtual

    val itemSelecionadoId = when (rotaVisivel) {
        PacienteSubRota.DASHBOARD -> "dashboard"
        PacienteSubRota.RELATORIOS -> "relatorios"
        PacienteSubRota.TRATAMENTO -> "tratamento"
        PacienteSubRota.AGENDA -> "agenda"
        PacienteSubRota.PROFISSIONAIS -> "profissionais"
        PacienteSubRota.TAREFAS -> "tarefas"
        PacienteSubRota.NOTIFICACOES -> "notificacoes"
        PacienteSubRota.PERFIL -> "perfil"
        PacienteSubRota.HUMOR -> "dashboard"
    }

    DialogaLayout(
        tituloTela = rotaVisivel.titulo,
        nomeUsuario = nomeExibicao,
        iniciaisUsuario = iniciais,
        fotoUrl = fotoUrl,
        itensMenu = itensMenu,
        itemSelecionadoId = itemSelecionadoId,
        onSelecionarItem = { id ->
            subRotaAtual = when (id) {
                "dashboard" -> PacienteSubRota.DASHBOARD
                "relatorios" -> PacienteSubRota.RELATORIOS
                "tratamento" -> PacienteSubRota.TRATAMENTO
                "agenda" -> PacienteSubRota.AGENDA
                "profissionais" -> PacienteSubRota.PROFISSIONAIS
                "tarefas" -> PacienteSubRota.TAREFAS
                "notificacoes" -> PacienteSubRota.NOTIFICACOES
                "perfil" -> PacienteSubRota.PERFIL
                else -> PacienteSubRota.DASHBOARD
            }
        },
        onLogout = {
            authState.logout()
            onLogout()
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (rotaVisivel) {
            PacienteSubRota.DASHBOARD, PacienteSubRota.HUMOR -> PacienteHomeScreen(
                onNavigate = { subRotaAtual = it },
                modifier = modifier,
                viewModel = dashboardViewModel
            )
            PacienteSubRota.RELATORIOS -> RelatoriosPacienteScreen(modifier = modifier)
            PacienteSubRota.TRATAMENTO -> TratamentoScreen(modifier = modifier)
            PacienteSubRota.AGENDA -> ConsultasPacienteScreen(modifier = modifier)
            PacienteSubRota.PROFISSIONAIS -> BuscaProfissionaisScreen(modifier = modifier)
            PacienteSubRota.TAREFAS -> TarefasPacienteScreen(modifier = modifier)
            PacienteSubRota.NOTIFICACOES -> NotificacoesPacienteScreen(onNavigate = { subRotaAtual = it }, modifier = modifier)
            PacienteSubRota.PERFIL -> PerfilPacienteScreen(modifier = modifier)
        }

        if (subRotaAtual == PacienteSubRota.HUMOR) {
            RegistroHumorDialog(
                salvando = dashboardViewModel.salvandoHumor,
                erro = dashboardViewModel.erroHumor,
                onCancelar = {
                    dashboardViewModel.limparErroHumor()
                    subRotaAtual = PacienteSubRota.DASHBOARD
                },
                onSalvar = { dados ->
                    dashboardViewModel.registrarHumor(authState, dados) {
                        subRotaAtual = PacienteSubRota.DASHBOARD
                    }
                }
            )
        }
    }
}