package com.aulasandroid.mobiledialoga.feature.paciente.tarefas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.CabecalhoCard
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.DialogaOutlinedButton
import com.aulasandroid.mobiledialoga.components.DialogaTextField
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.PerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.RespostaItemDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.TarefaItemDto
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorSucessoClara
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

@Composable
fun TarefasPacienteScreen(
    modifier: Modifier = Modifier,
    viewModel: TarefasPacienteViewModel = viewModel()
) {
    val authState = useAuth()
    val estado by viewModel.uiState.observeAsState(TarefasPacienteUiState())
    val token = authState.auth?.token

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            viewModel.carregarTarefas(token)
        }
    }

    val tarefasFiltradas = when (estado.filtroAtivo) {
        "Pendentes" -> estado.tarefas.filter { it.getStatusFormatado() == "Pendente" }
        "Em andamento" -> estado.tarefas.filter { it.getStatusFormatado() == "Em andamento" }
        "Concluídas" -> estado.tarefas.filter { it.getStatusFormatado() == "Concluída" }
        else -> estado.tarefas
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Card do topo com título e atualização
        DialogaCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CabecalhoCard(
                        titulo = "Minhas Tarefas",
                        subtitulo = "Acompanhe e responda as atividades passadas por seu profissional de saúde."
                    )
                }
                IconButton(onClick = { viewModel.carregarTarefas(token) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Atualizar tarefas",
                        tint = CorPrimaria
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner de erro se houver
        if (estado.erroGeral != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFDEDED))
                    .border(1.dp, CorErro, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = estado.erroGeral!!,
                    color = CorErro,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Banner de sucesso se houver
        if (estado.mensagemSucesso != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CorSucessoClara)
                    .border(1.dp, CorSucesso, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = estado.mensagemSucesso!!,
                        color = CorSucesso,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.limparMensagemSucesso() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CorSucesso)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Filtros (Todas, Pendentes, Em andamento, Concluídas)
        FiltrosTarefas(
            opcoes = listOf("Todas", "Pendentes", "Em andamento", "Concluídas"),
            selecionado = estado.filtroAtivo,
            onSelecionar = { filtro -> viewModel.setFiltro(filtro) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (estado.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
            }
        } else if (tarefasFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, CorBorda, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma tarefa encontrada neste filtro. 📋",
                    fontSize = 14.sp,
                    color = CorSecundaria,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tarefasFiltradas.forEach { tarefa ->
                    CartaoTarefaItem(
                        tarefa = tarefa,
                        onAbrir = { viewModel.abrirTarefa(token, tarefa) }
                    )
                }
            }
        }
    }

    // Modal de Detalhes / Responder Tarefa
    if (estado.tarefaSelecionada != null) {
        DialogoRespostaTarefa(
            tarefa = estado.tarefaSelecionada!!,
            perguntas = estado.perguntas,
            respostasFormulario = estado.respostasFormulario,
            carregandoDetalhes = estado.isLoadingDetalhes,
            enviando = estado.isEnviandoResposta,
            onFechar = { viewModel.fecharTarefaDetalhes() },
            onOpcaoSelecionada = { pId, oId -> viewModel.onOpcaoSelecionada(pId, oId) },
            onTextoChange = { pId, texto -> viewModel.onTextoRespostaChanged(pId, texto) },
            onSalvarRascunho = { viewModel.submeterRespostas(token, false) },
            onEnviarDefinitivo = { viewModel.submeterRespostas(token, true) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltrosTarefas(
    opcoes: List<String>,
    selecionado: String,
    onSelecionar: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcoes.forEach { opcao ->
            val ativa = opcao == selecionado
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (ativa) CorPrimaria else Color.White)
                    .border(1.dp, if (ativa) CorPrimaria else CorBorda, RoundedCornerShape(10.dp))
                    .clickable { onSelecionar(opcao) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = opcao,
                    fontSize = 13.sp,
                    color = if (ativa) Color.White else CorTexto,
                    fontWeight = if (ativa) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CartaoTarefaItem(
    tarefa: TarefaItemDto,
    onAbrir: () -> Unit
) {
    val statusTxt = tarefa.getStatusFormatado()
    val (fundoStatus, bordaStatus, corTextoStatus) = when (statusTxt) {
        "Concluída" -> Triple(Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF047857))
        "Em andamento" -> Triple(Color(0xFFEFF6FF), Color(0xFFBFDBFE), Color(0xFF1D4ED8))
        "Atrasada" -> Triple(Color(0xFFFEF2F2), Color(0xFFFECACA), Color(0xFFB91C1C))
        else -> Triple(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFB45309))
    }

    DialogaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(fundoStatus)
                    .border(1.dp, bordaStatus, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusTxt,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = corTextoStatus
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = CorSecundaria,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tarefa.getPrazoFormatado(),
                    fontSize = 12.sp,
                    color = CorSecundaria,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = tarefa.titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CorTexto
        )

        if (!tarefa.instrucoes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tarefa.instrucoes,
                fontSize = 13.sp,
                color = CorSecundaria,
                maxLines = 2,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val textoBotao = if (statusTxt == "Concluída") "Ver Tarefa / Respostas" else "Responder Tarefa →"
        DialogaButton(
            texto = textoBotao,
            onClick = onAbrir,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DialogoRespostaTarefa(
    tarefa: TarefaItemDto,
    perguntas: List<PerguntaDto>,
    respostasFormulario: Map<Int, RespostaItemDto>,
    carregandoDetalhes: Boolean,
    enviando: Boolean,
    onFechar: () -> Unit,
    onOpcaoSelecionada: (Int, Int) -> Unit,
    onTextoChange: (Int, String) -> Unit,
    onSalvarRascunho: () -> Unit,
    onEnviarDefinitivo: () -> Unit
) {
    Dialog(onDismissRequest = onFechar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Cabeçalho do modal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tarefa.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorTexto,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFechar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CorSecundaria)
                }
            }

            if (!tarefa.instrucoes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tarefa.instrucoes,
                    fontSize = 13.sp,
                    color = CorSecundaria,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (carregandoDetalhes) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
                }
            } else if (perguntas.isEmpty()) {
                // Tarefa sem perguntas cadastradas: resposta aberta simples
                val rItem = respostasFormulario[0]
                val textoResp = rItem?.valor ?: ""

                Text(
                    text = "Sua resposta / comentário:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CorTexto
                )
                Spacer(modifier = Modifier.height(6.dp))
                DialogaTextField(
                    rotulo = "",
                    placeholder = "Digite sua resposta aqui...",
                    value = textoResp,
                    onValueChange = { nova -> onTextoChange(0, nova) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Lista de Perguntas
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    perguntas.forEachIndexed { index, pergunta ->
                        val pNum = index + 1
                        val respAtual = respostasFormulario[pergunta.id]

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "$pNum. ${pergunta.enunciado}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CorTexto
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (pergunta.opcoes.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    pergunta.opcoes.forEach { opcao ->
                                        val selecionada = respAtual?.idOpcaoPergunta == opcao.id
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpcaoSelecionada(pergunta.id, opcao.id) }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            RadioButton(
                                                selected = selecionada,
                                                onClick = { onOpcaoSelecionada(pergunta.id, opcao.id) },
                                                colors = RadioButtonDefaults.colors(selectedColor = CorPrimaria)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = opcao.texto,
                                                fontSize = 13.sp,
                                                color = CorTexto,
                                                fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            } else {
                                DialogaTextField(
                                    rotulo = "",
                                    placeholder = "Sua resposta...",
                                    value = respAtual?.valor ?: "",
                                    onValueChange = { nova -> onTextoChange(pergunta.id, nova) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botões do Modal
            val jaConcluida = tarefa.getStatusFormatado() == "Concluída"
            if (!jaConcluida) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DialogaOutlinedButton(
                        texto = "Salvar Rascunho",
                        onClick = onSalvarRascunho,
                        modifier = Modifier.weight(1f)
                    )
                    DialogaButton(
                        texto = "Enviar Resposta",
                        onClick = onEnviarDefinitivo,
                        isLoading = enviando,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                DialogaOutlinedButton(
                    texto = "Fechar",
                    onClick = onFechar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
