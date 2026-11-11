package com.aulasandroid.mobiledialoga.feature.paciente.consultas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CabecalhoAgenda
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CancelarConsultaDialog
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CardAgenda
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CardCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CardDiaSelecionado
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CardProximasConsultas
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.NovaConsultaDialog
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.ToastAgenda
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorFundo
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria

/**
 * Agenda do paciente: calendário (Mês/Semana/Dia), consultas do dia selecionado e próximas consultas,
 * com os pop-ups "Nova consulta" e "Cancelar consulta".
 */
@Composable
fun ConsultasPacienteScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel()
) {
    val token = useAuth().auth?.token
    val estado by viewModel.uiState.observeAsState(AgendaUiState())

    // Carrega ao abrir a tela (e de novo se a sessão mudar)
    LaunchedEffect(token) { viewModel.carregar(token) }

    val consultasDoDia = remember(estado.consultas, estado.diaSelecionado) {
        AgendaRegras.doDia(estado.consultas, estado.diaSelecionado)
    }
    val proximas = remember(estado.consultas) {
        AgendaRegras.proximas(estado.consultas, Dia.hoje(), Dia.horaAgora())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CorFundo)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            CabecalhoAgenda(onNovaConsulta = viewModel::abrirNovaConsulta)

            when {
                estado.carregando -> CardAgenda {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    ) {
                        CircularProgressIndicator(color = CorPrimaria)
                    }
                }

                estado.erroCarregamento != null -> CardAgenda {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Text(
                            text = estado.erroCarregamento ?: "",
                            color = CorErro,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DialogaButton(texto = "Tentar novamente", onClick = { viewModel.carregar(token) })
                    }
                }

                else -> {
                    CardCalendario(
                        visao = estado.visao,
                        diaSelecionado = estado.diaSelecionado,
                        consultas = estado.consultas,
                        onMudarVisao = viewModel::mudarVisao,
                        onNavegar = viewModel::navegar,
                        onSelecionarDia = viewModel::selecionarDia
                    )
                    CardDiaSelecionado(
                        dia = estado.diaSelecionado,
                        consultasDoDia = consultasDoDia,
                        onCancelar = viewModel::pedirCancelamento
                    )
                    CardProximasConsultas(
                        consultas = proximas,
                        onCancelar = viewModel::pedirCancelamento
                    )
                }
            }
        }

        estado.toast?.let { toast ->
            ToastAgenda(
                mensagem = toast,
                onFechar = viewModel::fecharToast,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    estado.formulario?.let { formulario ->
        NovaConsultaDialog(
            formulario = formulario,
            profissionais = estado.profissionais,
            salvando = estado.salvando,
            onProfissionalTexto = viewModel::onProfissionalTextoChange,
            onProfissionalEscolhido = viewModel::onProfissionalEscolhido,
            onEspecialidade = viewModel::onEspecialidadeChange,
            onData = viewModel::onDataChange,
            onHora = viewModel::onHoraChange,
            onObservacoes = viewModel::onObservacoesChange,
            onLembrete = viewModel::onLembreteChange,
            onAdicionarOutra = { viewModel.salvar(token, manterAberto = true) },
            onSalvar = { viewModel.salvar(token, manterAberto = false) },
            onFechar = viewModel::fecharNovaConsulta
        )
    }

    if (estado.consultaParaCancelar != null) {
        CancelarConsultaDialog(
            cancelando = estado.cancelando,
            onVoltar = viewModel::desistirDoCancelamento,
            onConfirmar = { viewModel.confirmarCancelamento(token) }
        )
    }
}
