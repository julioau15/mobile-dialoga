package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import kotlinx.coroutines.delay

/**
 * Tarefas do profissional. Telas internas (controladas pelo ViewModel, sem mexer na navegação do app):
 * Lista (Enviadas/Rascunhos) -> Acompanhamento -> Respostas, e Lista -> Nova tarefa -> Pré-visualização.
 */
@Composable
fun TarefasProfissionalScreen(
    modifier: Modifier = Modifier,
    viewModel: TarefasProfissionalViewModel = viewModel()
) {
    val authState = useAuth()
    val context = LocalContext.current
    val estado by viewModel.uiState.observeAsState(TarefasProfissionalUiState())
    val token = authState.auth?.token

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) viewModel.carregarDados(token)
    }

    // o aviso verde some sozinho depois de 3 segundos
    LaunchedEffect(estado.toast) {
        if (estado.toast != null) {
            delay(3000)
            viewModel.limparToast()
        }
    }

    BackHandler(enabled = estado.tela != TelaProfissional.LISTA) { viewModel.voltar() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            when (estado.tela) {
                TelaProfissional.LISTA -> ListaTarefasProfissional(
                    estado = estado,
                    onAcompanhamento = { viewModel.abrirAcompanhamento() },
                    onNovaTarefa = { viewModel.novaTarefa() },
                    onAba = { viewModel.setAbaLista(it) },
                    onAcompanharTarefa = { viewModel.acompanharTarefa(it.titulo) },
                    onAbrirTarefa = { viewModel.abrirTarefa(token, it) },
                    onExcluir = { viewModel.excluirTarefa(token, it.id) },
                    onRecarregar = { viewModel.carregarDados(token) }
                )

                TelaProfissional.ACOMPANHAMENTO -> AcompanhamentoProfissional(
                    estado = estado,
                    onVoltar = { viewModel.voltar() },
                    onBusca = { viewModel.setBusca(it) },
                    onPeriodo = { viewModel.setPeriodo(it) },
                    onDia = { viewModel.setDia(it) },
                    onMes = { viewModel.setMes(it) },
                    onAno = { viewModel.setAno(it) },
                    onStatus = { viewModel.setStatusFiltro(it) },
                    onAbrirRespostas = { viewModel.abrirRespostas(token, it) }
                )

                TelaProfissional.RESPOSTAS -> RespostasPaciente(estado = estado, onVoltar = { viewModel.voltar() })

                TelaProfissional.FORMULARIO -> FormularioTarefa(
                    estado = estado,
                    form = viewModel.form,
                    onVoltar = { viewModel.voltar() },
                    onVisualizar = { viewModel.abrirPreview() },
                    onSelecionarAnexo = { viewModel.selecionarAnexo(context, it) },
                    onValidarEnvio = { viewModel.validarFormulario(paraEnvio = true) == null },
                    destinatariosDoEnvio = { viewModel.destinatariosDoEnvio() },
                    onSalvarRascunho = { viewModel.salvar(context, token, rascunho = true, prazos = emptyMap()) },
                    onEnviar = { prazos -> viewModel.salvar(context, token, rascunho = false, prazos = prazos) }
                )

                TelaProfissional.PREVIEW -> PreviewTarefa(
                    form = viewModel.form,
                    onVoltar = { viewModel.voltar() },
                    onEditar = { viewModel.editarDoPreview() }
                )
            }
        }

        if (estado.toast != null) {
            ToastSucesso(
                texto = estado.toast!!,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 28.dp)
            )
        }
    }
}
