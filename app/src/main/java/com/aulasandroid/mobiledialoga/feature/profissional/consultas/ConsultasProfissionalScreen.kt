package com.aulasandroid.mobiledialoga.feature.profissional.consultas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.MensagemToast
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.ToastAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.AbasAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CabecalhoAgendaProfissional
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CalendarioAgendaProfissional
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CardConsultaDoDia
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CardMensagemAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CardProximoAtendimento
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.DetalhesConsultaDialog
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.ExcluirConsultaDialog
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.FormularioConsultaDialog
import com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade.DisponibilidadeAba
import com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade.DisponibilidadeViewModel
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CampoBusca
import com.aulasandroid.mobiledialoga.feature.vinculos.components.RotuloSecao
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorFundo
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria

/**
 * Agenda do profissional (protótipo "Agenda - Profissional"):
 *  - aba Eventos: calendário do mês, busca por paciente, consultas do dia e próximos atendimentos,
 *    com "Nova consulta", detalhes, edição e exclusão;
 *  - aba Disponibilidade: dias e horários em que os pacientes podem marcar consulta.
 */
@Composable
fun ConsultasProfissionalScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaProfissionalViewModel = viewModel(),
    disponibilidadeViewModel: DisponibilidadeViewModel = viewModel()
) {
    val token = useAuth().auth?.token
    val estado = viewModel.state
    val disponibilidade = disponibilidadeViewModel.state

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            viewModel.carregar(token)
            disponibilidadeViewModel.carregar(token)
        }
    }

    // Avisos do cadastro de horários também aparecem como o aviso verde/vermelho no canto da tela.
    // remember: o aviso tem um id próprio, e um id novo a cada recomposição reiniciaria o tempo para sumir.
    val avisoDisponibilidade = remember(disponibilidade.aviso, disponibilidade.avisoErro) {
        disponibilidade.aviso?.let { MensagemToast(it, disponibilidade.avisoErro) }
    }
    val toast = estado.toast ?: avisoDisponibilidade

    Box(modifier = modifier.fillMaxSize().background(CorFundo)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CabecalhoAgendaProfissional(onNovaConsulta = viewModel::abrirNovaConsulta)
            AbasAgenda(aba = estado.aba, onSelecionar = viewModel::selecionarAba)

            when (estado.aba) {
                AbaAgenda.EVENTOS -> EventosConteudo(
                    estado = estado,
                    viewModel = viewModel,
                    onTentarNovamente = { token?.let(viewModel::carregar) }
                )
                AbaAgenda.DISPONIBILIDADE -> DisponibilidadeAba(token = token, viewModel = disponibilidadeViewModel)
            }

            // folga para o aviso não cobrir o último card
            if (toast != null) Spacer(modifier = Modifier.height(48.dp))
        }

        if (toast != null) {
            ToastAgenda(
                mensagem = toast,
                onFechar = {
                    viewModel.fecharToast()
                    disponibilidadeViewModel.limparAviso()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    // ---------- Pop-ups ----------

    estado.detalhe?.let { consulta ->
        DetalhesConsultaDialog(
            consulta = consulta,
            onFechar = viewModel::fecharDetalhe,
            onEditar = { viewModel.abrirEdicao(consulta) },
            onExcluir = { viewModel.pedirExclusao(consulta) }
        )
    }

    estado.excluindo?.let { consulta ->
        ExcluirConsultaDialog(
            consulta = consulta,
            removendo = estado.removendo,
            onCancelar = viewModel::cancelarExclusao,
            onConfirmar = { token?.let(viewModel::confirmarExclusao) }
        )
    }

    estado.formulario?.let { formulario ->
        FormularioConsultaDialog(
            formulario = formulario,
            pacientes = estado.pacientes,
            disponibilidades = disponibilidade.itens,
            onPacienteTexto = viewModel::alterarPacienteTexto,
            onPacienteEscolhido = viewModel::escolherPaciente,
            onData = viewModel::alterarData,
            onHora = viewModel::alterarHora,
            onSalvar = { token?.let(viewModel::salvar) },
            onFechar = viewModel::fecharFormulario
        )
    }
}

@Composable
private fun EventosConteudo(
    estado: AgendaProfissionalState,
    viewModel: AgendaProfissionalViewModel,
    onTentarNovamente: () -> Unit
) {
    CalendarioAgendaProfissional(
        mesVisivel = estado.mesVisivel,
        diaSelecionado = estado.diaSelecionado,
        diasComConsulta = estado.diasComConsulta,
        onSelecionarDia = viewModel::selecionarDia,
        onMudarMes = viewModel::mudarMes
    )

    CampoBusca(
        valor = estado.busca,
        onValorChange = viewModel::alterarBusca,
        placeholder = "Buscar paciente...",
        modifier = Modifier.fillMaxWidth()
    )

    when {
        estado.carregando -> CardMensagemAgenda("Carregando...")
        estado.erro != null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = estado.erro, color = CorErro, fontSize = 13.sp)
            TextButton(onClick = onTentarNovamente) { Text("Tentar novamente") }
        }
        else -> {
            // ----- Consultas do dia selecionado -----
            RotuloSecao(AgendaCalendario.tituloDiaSelecionado(estado.diaSelecionado))
            val doDia = estado.doDia(estado.diaSelecionado)
            if (doDia.isEmpty()) {
                CardMensagemAgenda(if (estado.busca.isBlank()) "Nenhuma consulta neste dia." else "Nenhum paciente encontrado neste dia.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    doDia.forEach { consulta ->
                        CardConsultaDoDia(consulta = consulta, onClick = { viewModel.abrirDetalhe(consulta) })
                    }
                }
            }

            // ----- Próximos atendimentos -----
            RotuloSecao("Próximos atendimentos")
            val proximas = estado.proximas()
            if (proximas.isEmpty()) {
                CardMensagemAgenda(if (estado.busca.isBlank()) "Nenhuma consulta marcada." else "Nenhum paciente encontrado.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    proximas.forEach { consulta ->
                        CardProximoAtendimento(consulta = consulta, onClick = { viewModel.abrirDetalhe(consulta) })
                    }
                }
            }
        }
    }
}
