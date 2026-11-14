@file:OptIn(ExperimentalLayoutApi::class)

package com.aulasandroid.mobiledialoga.feature.paciente.tarefas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaOutlinedButton
import com.aulasandroid.mobiledialoga.components.DialogaTextField
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.coresCampo
import com.aulasandroid.mobiledialoga.components.CampoDataCalendario
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.PerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.TarefaItemDto
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

private val FundoAnexo = Color(0xFFFAF8FD)
private val BordaTracejada = Color(0xFFD5C8EA)
private val DivisorModal = Color(0xFFEFEAF6)

@Composable
fun TarefasPacienteScreen(
    modifier: Modifier = Modifier,
    viewModel: TarefasPacienteViewModel = viewModel()
) {
    val vm = viewModel
    val authState = useAuth()
    val estado by vm.uiState.observeAsState(TarefasPacienteUiState())
    val token = authState.auth?.token

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            vm.carregarTarefas(token)
        }
    }

    // O aviso ("Resposta enviada...", "Tarefa adicionada") some sozinho depois de 3 segundos
    LaunchedEffect(estado.toastMensagem) {
        if (estado.toastMensagem != null) {
            delay(3000)
            vm.limparToast()
        }
    }

    val tarefaEmEdicao = estado.tarefaEmEdicao

    if (tarefaEmEdicao != null) {
        // Tela cheia de resposta (usa o mesmo modifier da lista: respeita o padding do topo do app)
        TelaRespostaTarefaDetalhada(
            modifier = modifier,
            tarefa = tarefaEmEdicao,
            estado = estado,
            onVoltar = { vm.fecharEdicaoTarefa() },
            onOpcaoUnicaSelected = { pId, oId -> vm.onOpcaoUnicaSelecionada(pId, oId) },
            onOpcaoMultiplaToggled = { pId, oId -> vm.onOpcaoMultiplaToggled(pId, oId) },
            onTextoChanged = { pId, txt -> vm.onTextoRespostaChanged(pId, txt) },
            onEscalaSelected = { pId, num -> vm.onEscalaRespostaSelected(pId, num) },
            onSolicitarEnvio = { vm.solicitarConfirmacaoEnvio() },
            onMarcarConcluida = { vm.marcarComoConcluida(token, tarefaEmEdicao) }
        )

        if (estado.mostrarModalConfirmarEnvio) {
            ModalConfirmarEnvio(
                enviando = estado.isEnviandoResposta,
                onFechar = { vm.fecharModalConfirmacao() },
                onConfirmar = { vm.confirmarEEnviarResposta(token) }
            )
        }
    } else {
        ListaDeTarefas(
            modifier = modifier,
            estado = estado,
            onAdicionar = { vm.abrirModalNovaTarefa() },
            onFiltro = { vm.setFiltro(it) },
            onAbrir = { tarefa -> vm.abrirTarefa(token, tarefa) },
            onTentarNovamente = { vm.carregarTarefas(token) },
            onFecharToast = { vm.limparToast() },
            onMarcarConcluida = { tarefa -> vm.marcarComoConcluida(token, tarefa) }
        )
    }

    // Modal "+ Nova tarefa"
    if (estado.mostrarModalNovaTarefa) {
        val context = LocalContext.current
        ModalNovaTarefaPaciente(
            criando = estado.isCriandoNovaTarefa,
            erro = estado.erroNovaTarefa,
            onFechar = { vm.fecharModalNovaTarefa() },
            onCriar = { titulo, prazoIso, anexo ->
                vm.criarNovaTarefaPaciente(context, token, titulo, prazoIso, anexo)
            }
        )
    }
}

// ---------- Lista ----------

@Composable
private fun ListaDeTarefas(
    modifier: Modifier,
    estado: TarefasPacienteUiState,
    onAdicionar: () -> Unit,
    onFiltro: (String) -> Unit,
    onAbrir: (TarefaItemDto) -> Unit,
    onTentarNovamente: () -> Unit,
    onFecharToast: () -> Unit,
    onMarcarConcluida: (TarefaItemDto) -> Unit
) {
    val tarefasFiltradas = when (estado.filtroAtivo) {
        "Pendentes" -> estado.tarefas.filter { it.getStatusFormatado() == "Pendente" }
        "Em andamento" -> estado.tarefas.filter { it.getStatusFormatado() == "Em andamento" }
        "Em atraso" -> estado.tarefas.filter { it.getStatusFormatado() == "Em atraso" }
        "Concluídas" -> estado.tarefas.filter { it.getStatusFormatado() == "Concluída" }
        else -> estado.tarefas
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Título + descrição + botão "+ Adicionar tarefa"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tarefas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CorTexto
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Questionários e atividades do seu profissional",
                        fontSize = 14.sp,
                        color = CorSecundaria,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CorPrimaria)
                        .clickable(onClick = onAdicionar)
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Adicionar tarefa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros centralizados
            RowFiltroChipsBadges(
                estado = estado,
                filtroAtivo = estado.filtroAtivo,
                onSelecionarFiltro = onFiltro
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (estado.erroGeral != null) {
                CartaoErroLista(mensagem = estado.erroGeral, onTentarNovamente = onTentarNovamente)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (estado.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
                }
            } else if (tarefasFiltradas.isEmpty()) {
                if (estado.erroGeral == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, CorBordaCard, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (estado.filtroAtivo == "Todas") {
                                "Você ainda não tem tarefas."
                            } else {
                                "Nenhuma tarefa encontrada neste filtro."
                            },
                            fontSize = 14.sp,
                            color = CorSecundaria,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    tarefasFiltradas.forEach { tarefa ->
                        CartaoTarefaPrototipo(
                            tarefa = tarefa,
                            onAcaoClick = { onAbrir(tarefa) },
                            onMarcarConcluida = { onMarcarConcluida(tarefa) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Aviso flutuante (some sozinho; toque para fechar)
        if (estado.toastMensagem != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (estado.toastErro) Color(0xFFB91C1C) else Color(0xFF047857))
                    .clickable(onClick = onFecharToast)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = if (estado.toastErro) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = estado.toastMensagem,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartaoErroLista(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = mensagem, fontSize = 13.sp, color = CorErro, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tentar novamente",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CorPrimaria,
            modifier = Modifier.clickable(onClick = onTentarNovamente)
        )
    }
}

@Composable
private fun RowFiltroChipsBadges(
    estado: TarefasPacienteUiState,
    filtroAtivo: String,
    onSelecionarFiltro: (String) -> Unit
) {
    val filtrosComContagem = listOf(
        Pair("Todas", estado.todasCount),
        Pair("Em andamento", estado.emAndamentoCount),
        Pair("Em atraso", estado.emAtrasoCount),
        Pair("Concluídas", estado.concluidasCount),
        Pair("Pendentes", estado.pendentesCount)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filtrosComContagem.forEach { (nome, count) ->
            val selecionado = nome == filtroAtivo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selecionado) CorPrimaria else Color.White)
                    .border(1.dp, if (selecionado) CorPrimaria else CorBorda, RoundedCornerShape(12.dp))
                    .clickable { onSelecionarFiltro(nome) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nome,
                        fontSize = 13.sp,
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
                        color = if (selecionado) Color.White else CorTexto,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selecionado) Color.White.copy(alpha = 0.25f) else Color(0xFFEEE8F6))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selecionado) Color.White else CorSecundaria
                        )
                    }
                }
            }
        }
    }
}

private fun coresStatus(status: String): Pair<Color, Color> = when (status) {
    "Concluída" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
    "Em andamento" -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
    "Em atraso" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
    else -> Pair(Color(0xFFF3E8FF), CorPrimaria)
}

@Composable
private fun SeloStatus(status: String) {
    val (fundo, texto) = coresStatus(status)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = texto,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Card branco com o espaçamento do protótipo (20dp). */
@Composable
private fun CartaoBranco(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun CartaoTarefaPrototipo(
    tarefa: TarefaItemDto,
    onAcaoClick: () -> Unit,
    onMarcarConcluida: (() -> Unit)? = null
) {
    val statusTxt = tarefa.getStatusFormatado()

    CartaoBranco {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = tarefa.titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CorTexto,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            SeloStatus(statusTxt)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tarefa.profissionalNome,
            fontSize = 13.sp,
            color = CorSecundaria,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = CorSecundaria,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Prazo: ${tarefa.getPrazoFormatado()}",
                fontSize = 12.sp,
                color = CorSecundaria
            )
        }

        // Progresso para tarefas ainda não concluídas
        if (statusTxt != "Concluída") {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progresso", fontSize = 12.sp, color = CorSecundaria)
                Text(text = "${tarefa.progresso}%", fontSize = 12.sp, color = CorSecundaria, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { tarefa.progresso / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = CorPrimaria,
                trackColor = Color(0xFFEFE8F8)
            )
        }

        if (tarefa.criadaPorMim && statusTxt != "Concluída") {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECFDF5))
                    .clickable { onMarcarConcluida?.invoke() }
            ) {
                Text(
                    text = "Marcar como concluída",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (statusTxt == "Concluída") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECFDF5))
                    .clickable(onClick = onAcaoClick)
            ) {
                Text(
                    text = "Ver respostas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        } else {
            val txtBotao = if (statusTxt == "Em andamento") "Continuar" else "Abrir tarefa"
            DialogaButton(
                texto = txtBotao,
                onClick = onAcaoClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------- Tela cheia de resposta / visualização da tarefa ----------

@Composable
private fun TelaRespostaTarefaDetalhada(
    modifier: Modifier,
    tarefa: TarefaItemDto,
    estado: TarefasPacienteUiState,
    onVoltar: () -> Unit,
    onOpcaoUnicaSelected: (Int, Int) -> Unit,
    onOpcaoMultiplaToggled: (Int, Int) -> Unit,
    onTextoChanged: (Int, String) -> Unit,
    onEscalaSelected: (Int, Int) -> Unit,
    onSolicitarEnvio: () -> Unit,
    onMarcarConcluida: () -> Unit
) {
    val statusTxt = tarefa.getStatusFormatado()
    val jaConcluida = statusTxt == "Concluída"
    val uriHandler = LocalUriHandler.current
    val anexoUrl = tarefa.getAnexoUrlCompleta()

    // "modifier" já traz o padding do Scaffold: sem ele o topo ficava escondido atrás da barra do app
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // "← Voltar às tarefas"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onVoltar)
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = CorSecundaria,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Voltar às tarefas", fontSize = 14.sp, color = CorSecundaria, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card do cabeçalho da tarefa
        CartaoBranco {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tarefa.titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorTexto,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SeloStatus(statusTxt)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (tarefa.profissionalNome == "Você") "Criada por você" else "Enviado por: ${tarefa.profissionalNome}",
                fontSize = 13.sp,
                color = CorSecundaria
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = CorSecundaria,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Prazo: ${tarefa.getPrazoFormatado()}", fontSize = 12.sp, color = CorSecundaria)
            }

            if (!tarefa.instrucoes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = tarefa.instrucoes, fontSize = 13.sp, color = CorTexto, lineHeight = 19.sp)
            }

            if (anexoUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "📎 Ver anexo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CorPrimaria,
                    modifier = Modifier.clickable { runCatching { uriHandler.openUri(anexoUrl) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            estado.isLoadingDetalhes -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
                }
            }

            estado.perguntas.isEmpty() -> {
                MensagemInformativa(
                    texto = estado.erroDetalhes ?: "Esta tarefa não tem perguntas para responder.",
                    erro = estado.erroDetalhes != null
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (!jaConcluida) {
                    DialogaButton(
                        texto = "Marcar como concluída",
                        onClick = {
                            onMarcarConcluida()
                            onVoltar()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                DialogaOutlinedButton(texto = "Voltar às tarefas", onClick = onVoltar, modifier = Modifier.fillMaxWidth())
            }

            else -> {
                if (jaConcluida && !estado.algumaRespostaPreenchida) {
                    MensagemInformativa(texto = "Nenhuma resposta registrada para esta tarefa.", erro = false)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    estado.perguntas.forEachIndexed { index, pergunta ->
                        CartaoPergunta(
                            numero = index + 1,
                            pergunta = pergunta,
                            estado = estado,
                            somenteLeitura = jaConcluida,
                            onOpcaoUnicaSelected = onOpcaoUnicaSelected,
                            onOpcaoMultiplaToggled = onOpcaoMultiplaToggled,
                            onTextoChanged = onTextoChanged,
                            onEscalaSelected = onEscalaSelected
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (estado.erroDetalhes != null) {
                    MensagemInformativa(texto = estado.erroDetalhes, erro = true)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!jaConcluida) {
                    val habilitado = estado.podeEnviar && !estado.isEnviandoResposta

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (habilitado) CorPrimaria else CorPrimaria.copy(alpha = 0.4f))
                            .clickable(enabled = habilitado, onClick = onSolicitarEnvio)
                            .padding(horizontal = 22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (estado.isEnviandoResposta) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Enviando...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Responder atividade",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    if (!estado.podeEnviar) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Responda as perguntas obrigatórias (*) para enviar.",
                            fontSize = 12.sp,
                            color = CorSecundaria
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DialogaOutlinedButton(
                        texto = "Marcar como concluída",
                        onClick = onMarcarConcluida,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DialogaOutlinedButton(
                        texto = "Voltar às tarefas",
                        onClick = onVoltar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MensagemInformativa(texto: String, erro: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (erro) Color(0xFFFEF2F2) else Color.White)
            .border(1.dp, if (erro) Color(0xFFFECACA) else CorBordaCard, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = texto,
            fontSize = 13.sp,
            color = if (erro) CorErro else CorSecundaria,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CartaoPergunta(
    numero: Int,
    pergunta: PerguntaDto,
    estado: TarefasPacienteUiState,
    somenteLeitura: Boolean,
    onOpcaoUnicaSelected: (Int, Int) -> Unit,
    onOpcaoMultiplaToggled: (Int, Int) -> Unit,
    onTextoChanged: (Int, String) -> Unit,
    onEscalaSelected: (Int, Int) -> Unit
) {
    CartaoBranco {
        // "1. Enunciado *" em um único texto, para o asterisco acompanhar a quebra de linha
        Text(
            text = buildAnnotatedString {
                append("$numero. ${pergunta.enunciado}")
                if (pergunta.obrigatoria) {
                    withStyle(SpanStyle(color = CorErro, fontWeight = FontWeight.Bold)) { append(" *") }
                }
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = CorTexto,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (pergunta.tipoNormalizado()) {
            "humor" -> {
                val emojis = listOf("😔", "😐", "🙂")
                val padrao = listOf(Triple(1, "😔", "Mal"), Triple(2, "😐", "Neutro"), Triple(3, "🙂", "Bem"))
                val opcoesHumor = if (pergunta.opcoes.size == 3) {
                    pergunta.opcoes.mapIndexed { i, o -> Triple(o.id, emojis[i], o.texto) }
                } else {
                    padrao
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    opcoesHumor.forEach { (oId, emoji, rotulo) ->
                        val sel = estado.respostasOpcaoUnica[pergunta.id] == oId
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (sel) Color(0xFFF3EDFD) else Color.White)
                                .border(
                                    if (sel) 2.dp else 1.dp,
                                    if (sel) CorPrimaria else CorBorda,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !somenteLeitura) { onOpcaoUnicaSelected(pergunta.id, oId) }
                                .padding(vertical = 12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rotulo,
                                    fontSize = 12.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (sel) CorPrimaria else CorSecundaria,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            "unica_escolha" -> {
                val selecionada = estado.respostasOpcaoUnica[pergunta.id]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    pergunta.opcoes.forEach { opt ->
                        val sel = selecionada == opt.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !somenteLeitura) { onOpcaoUnicaSelected(pergunta.id, opt.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = CorPrimaria)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt.texto,
                                fontSize = 14.sp,
                                color = CorTexto,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            "multipla_escolha" -> {
                val marcadas = estado.respostasMultiplasOpcoes[pergunta.id] ?: emptyList()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    pergunta.opcoes.forEach { opt ->
                        val sel = marcadas.contains(opt.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !somenteLeitura) { onOpcaoMultiplaToggled(pergunta.id, opt.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = sel,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = CorPrimaria)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt.texto,
                                fontSize = 14.sp,
                                color = CorTexto,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            "escala" -> {
                val selNum = estado.respostasEscala[pergunta.id]
                Column {
                    // quadrados de tamanho fixo que quebram de linha (como no protótipo), em vez de 11 colunas apertadas
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (num in 0..10) {
                            val sel = selNum == num
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) CorPrimaria else Color.White)
                                    .border(1.dp, if (sel) CorPrimaria else CorBorda, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !somenteLeitura) { onEscalaSelected(pergunta.id, num) }
                            ) {
                                Text(
                                    text = "$num",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else CorTexto
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Nenhum", fontSize = 11.sp, color = CorSecundaria)
                        Text(text = "Máximo", fontSize = 11.sp, color = CorSecundaria)
                    }
                }
            }

            else -> {
                // Texto aberto (área maior, como no protótipo)
                OutlinedTextField(
                    value = estado.respostasTexto[pergunta.id] ?: "",
                    onValueChange = { novo -> if (!somenteLeitura) onTextoChanged(pergunta.id, novo) },
                    readOnly = somenteLeitura,
                    placeholder = { Text(text = "Escreva sua resposta aqui...", fontSize = 14.sp) },
                    minLines = 4,
                    shape = FormatoCampo,
                    colors = coresCampo(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ---------- Botões compactos dos modais ----------

/**
 * Botões dos modais: largura do próprio texto, 1 linha só.
 * (antes cada botão ocupava metade do modal e "Confirmar envio" quebrava em duas linhas)
 */
@Composable
private fun BotaoModal(
    texto: String,
    onClick: () -> Unit,
    primario: Boolean,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val forma = RoundedCornerShape(10.dp)
    val padding = PaddingValues(horizontal = 18.dp)

    if (primario) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            shape = forma,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = CorPrimaria,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE8E1F2),
                disabledContentColor = CorPrimaria.copy(alpha = 0.7f)
            ),
            modifier = modifier.height(44.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = forma,
            border = BorderStroke(1.dp, CorBorda),
            contentPadding = padding,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = CorTexto),
            modifier = modifier.height(44.dp)
        ) {
            Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        }
    }
}

// ---------- Modal "Confirmar envio" ----------

@Composable
private fun ModalConfirmarEnvio(
    enviando: Boolean,
    onFechar: () -> Unit,
    onConfirmar: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!enviando) onFechar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Confirmar envio", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
                IconButton(onClick = onFechar, enabled = !enviando) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = CorSecundaria)
                }
            }
            HorizontalDivider(color = DivisorModal)

            Text(
                text = "Após o envio, as respostas serão compartilhadas com seu profissional e não poderão ser alteradas.",
                fontSize = 14.sp,
                color = CorSecundaria,
                lineHeight = 20.sp,
                modifier = Modifier.padding(20.dp)
            )

            HorizontalDivider(color = DivisorModal)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotaoModal(texto = "Cancelar", onClick = onFechar, primario = false, enabled = !enviando)
                BotaoModal(texto = "Confirmar envio", onClick = onConfirmar, primario = true, isLoading = enviando)
            }
        }
    }
}

// ---------- Modal "+ Adicionar tarefa" ----------

@Composable
private fun ModalNovaTarefaPaciente(
    criando: Boolean,
    erro: String?,
    onFechar: () -> Unit,
    onCriar: (titulo: String, prazoIso: String?, anexo: Uri?) -> Unit
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var prazoIso by remember { mutableStateOf<String?>(null) }
    var anexoUri by remember { mutableStateOf<Uri?>(null) }

    val nomeAnexo = remember(anexoUri) { anexoUri?.let { nomeDoArquivo(context, it) } }
    val seletorDeArquivo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) anexoUri = uri
    }

    Dialog(
        onDismissRequest = { if (!criando) onFechar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Nova tarefa", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
                IconButton(onClick = onFechar, enabled = !criando) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = CorSecundaria)
                }
            }
            HorizontalDivider(color = DivisorModal)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                DialogaTextField(
                    rotulo = "Título *",
                    placeholder = "Ex: Leitura indicada pelo profissional",
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                CampoDataCalendario(
                    rotulo = "Data de entrega *",
                    prazoIso = prazoIso,
                    onPrazoChange = { prazoIso = it },
                    enabled = !criando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = CorTexto)) { append("Arquivo ") }
                        withStyle(SpanStyle(color = CorSecundaria)) { append("(opcional)") }
                    },
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val anexo = anexoUri
                if (anexo == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FundoAnexo)
                            .clickable(enabled = !criando) { seletorDeArquivo.launch("*/*") }
                            .bordaTracejada(BordaTracejada, 10.dp)
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "+ Anexar arquivo",
                            fontSize = 13.sp,
                            color = CorSecundaria,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FundoAnexo)
                            .border(1.dp, CorBorda, RoundedCornerShape(10.dp))
                            .padding(start = 14.dp, top = 2.dp, bottom = 2.dp, end = 4.dp)
                    ) {
                        Text(
                            text = "📎 ${nomeAnexo ?: "arquivo selecionado"}",
                            fontSize = 13.sp,
                            color = CorTexto,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { anexoUri = null }, enabled = !criando) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover arquivo",
                                tint = CorSecundaria,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (erro != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = erro, fontSize = 12.sp, color = CorErro, lineHeight = 16.sp)
                }
            }

            HorizontalDivider(color = DivisorModal)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotaoModal(texto = "Cancelar", onClick = onFechar, primario = false, enabled = !criando)
                BotaoModal(
                    texto = "Criar",
                    onClick = { onCriar(titulo, prazoIso, anexoUri) },
                    primario = true,
                    isLoading = criando
                )
            }
        }
    }
}

/** Borda tracejada do campo "+ Anexar arquivo" (o protótipo usa tracejado lilás). */
private fun Modifier.bordaTracejada(cor: Color, raio: Dp): Modifier = this.drawBehind {
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f), 0f)
        )
    )
}