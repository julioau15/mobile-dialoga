package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import android.content.Intent
import android.content.ClipData
import androidx.core.content.FileProvider
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.feature.vinculos.components.AvatarIniciais
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.PacienteSubRota
import com.aulasandroid.mobiledialoga.feature.paciente.home.RegistroHumorApi
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria

@Composable
fun RelatoriosPacienteScreen(
    onNavigate: (PacienteSubRota) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RelatoriosPacienteViewModel = viewModel()
) {
    val authState = useAuth()
    val context = LocalContext.current
    val state = viewModel.state
    val scope = rememberCoroutineScope()
    var exportando by remember { mutableStateOf(false) }

    // recarrega ao entrar na tela (os dados mudam ao registrar humor, responder tarefas etc.)
    LaunchedEffect(Unit) { viewModel.carregar(authState, forcar = true) }

    LaunchedEffect(state.aviso) {
        state.aviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limparAviso()
        }
    }

    // tudo abaixo é derivado do período escolhido
    val registros = state.registrosNoPeriodo
    val medias = remember(registros) { RelatoriosMapper.medias(registros) }
    val pontosSono = remember(registros) { RelatoriosMapper.sono(registros) }
    val resumoSono = remember(pontosSono) { RelatoriosMapper.resumoSono(pontosSono) }
    val periodoTexto = "${DatasRelatorio.br(state.inicioIso)} – ${DatasRelatorio.br(state.periodoFim)}"

    var solicitacaoAberta by remember { mutableStateOf<SolicitacaoAcessoUi?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CoresRelatorio.Fundo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.carregando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorPrimaria)
        }
        state.erro?.let { Text(it, color = CoresRelatorio.Vermelho, fontSize = 12.sp) }

        // ------------------------------------------------ cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Relatórios", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = CoresRelatorio.RoxoEscuro)
                Text("Seu histórico de saúde emocional", fontSize = 12.sp, color = CoresRelatorio.Cinza)
            }
            OutlinedButton(
                onClick = {
                    val nome = authState.userProfile?.getNomeExibicao().orEmpty()
                    exportando = true
                    scope.launch {
                        try {
                            val arquivo = withContext(Dispatchers.IO) {
                                RelatorioPdf.gerar(context.applicationContext, nome, state)
                            }
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.relatorios", arquivo
                            )
                            val envio = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_SUBJECT, "Relatório Dialoga – $periodoTexto")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                clipData = ClipData.newRawUri("Relatório Dialoga", uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(envio, "Exportar relatório PDF"))
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(context, "Não foi possível exportar o PDF. Tente novamente.", Toast.LENGTH_LONG).show()
                        } finally {
                            exportando = false
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE4DFED)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                enabled = !exportando && !state.carregando && state.erro == null,
                modifier = Modifier.height(40.dp)
            ) {
                Text(if (exportando) "Gerando..." else "📄 Exportar PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoresRelatorio.RoxoEscuro)
            }
        }

        // ------------------------------------------------ período
        CardRelatorio {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "SELECIONAR PERÍODO",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoresRelatorio.Cinza
                )
                Spacer(Modifier.height(10.dp))
                CalendarioPeriodo(
                    mes = state.mesVisivel,
                    inicioIso = state.inicioIso,
                    fimIso = state.fimIso,
                    diasComRegistro = state.diasComRegistro,
                    onSelecionarDia = viewModel::selecionarDia,
                    onMesAnterior = viewModel::mesAnterior,
                    onProximoMes = viewModel::proximoMes
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Toque no primeiro dia, depois no último para definir o período. Pontos roxos = registros.",
                    fontSize = 11.sp, color = CoresRelatorio.Cinza, lineHeight = 15.sp
                )
            }
        }

        // ------------------------------------------------ indicadores
        CardRelatorio {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Energia · Sentimento · Humor · Pensamentos",
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoresRelatorio.RoxoEscuro
                )
                Text(
                    "${registros.size} registro(s) · $periodoTexto",
                    fontSize = 12.sp, color = CoresRelatorio.Cinza
                )
                Spacer(Modifier.height(16.dp))
                if (registros.isEmpty()) {
                    TextoVazio("Nenhum registro de humor neste período.")
                } else {
                    GraficoIndicadores(medias)
                }
            }
        }

        // ------------------------------------------------ sono
        CardRelatorio {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🌙 Horas de sono", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoresRelatorio.RoxoEscuro)
                Text("Eixo Y: 0h – 24h · Dias do período", fontSize = 12.sp, color = CoresRelatorio.Cinza)
                Spacer(Modifier.height(12.dp))
                GraficoSono(pontosSono, resumoSono)
            }
        }

        // ------------------------------------------------ solicitações de acesso
        CardRelatorio {
            Column {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔑 Solicitações de acesso", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoresRelatorio.RoxoEscuro)
                    Text("Profissionais solicitando ver seus indicadores", fontSize = 12.sp, color = CoresRelatorio.Cinza)
                }
                HorizontalDivider(color = CoresRelatorio.Divisor)
                if (state.solicitacoes.isEmpty()) {
                    TextoVazio("Nenhuma solicitação de acesso por enquanto.", Modifier.padding(16.dp))
                }
                state.solicitacoes.forEachIndexed { i, s ->
                    if (i > 0) HorizontalDivider(color = CoresRelatorio.Divisor)
                    SolicitacaoItem(s, onClick = { solicitacaoAberta = s })
                }
            }
        }

        // ------------------------------------------------ anotações
        CardRelatorio {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_sidebar_relatorios), null, tint = CorPrimaria, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Anotações", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoresRelatorio.RoxoEscuro)
                    }
                    Text("${registros.size} registro(s)", fontSize = 12.sp, color = CoresRelatorio.Cinza)
                }
                HorizontalDivider(color = CoresRelatorio.Divisor)
                if (registros.isEmpty()) {
                    TextoVazio("Nenhuma anotação neste período.", Modifier.padding(16.dp))
                }
                registros.forEachIndexed { i, r ->
                    if (i > 0) HorizontalDivider(color = CoresRelatorio.Divisor)
                    AnotacaoPeriodoItem(r)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    solicitacaoAberta?.let { s ->
        DialogoSolicitacao(
            solicitacao = s,
            respondendo = state.respondendoSolicitacao == s.id,
            onRecusar = {
                viewModel.responderSolicitacao(authState, s.id, aprovar = false, aoConcluir = { solicitacaoAberta = null })
            },
            onAprovar = { categorias ->
                viewModel.responderSolicitacao(
                    authState, s.id, aprovar = true, categorias = categorias,
                    editar = s.status == "Aprovada", aoConcluir = { solicitacaoAberta = null }
                )
            },
            onRemover = { viewModel.removerAcesso(authState, s.id) { solicitacaoAberta = null } },
            onFechar = { solicitacaoAberta = null }
        )
    }
}

// ---------------------------------------------------------------- blocos base

@Composable
private fun CardRelatorio(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
private fun TextoVazio(texto: String, modifier: Modifier = Modifier) {
    Text(texto, fontSize = 13.sp, color = CoresRelatorio.Cinza, modifier = modifier)
}



@Composable
private fun Selo(texto: String, cor: Color, fundo: Color = cor.copy(alpha = 0.12f)) {
    Box(
        modifier = Modifier
            .background(fundo, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = cor, fontWeight = FontWeight.Bold)
    }
}

private fun corStatusSolicitacao(status: String) = when (status) {
    "Aprovada" -> CoresRelatorio.Verde
    "Recusada" -> CoresRelatorio.Vermelho
    else -> CoresRelatorio.Laranja
}

// ---------------------------------------------------------------- solicitações

@Composable
private fun SolicitacaoItem(s: SolicitacaoAcessoUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIniciais(s.nome, tamanho = 40.dp, fotoUrl = s.foto)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                s.nome.ifBlank { "Carregando..." },
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CoresRelatorio.RoxoEscuro,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (s.especialidade.isNotBlank()) {
                Text(s.especialidade, fontSize = 12.sp, color = CoresRelatorio.Cinza, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(if (s.pendente) "Toque para responder" else "Gerenciar compartilhamento", fontSize = 11.sp, color = CorPrimaria)
        }
        val cor = corStatusSolicitacao(s.status)
        Selo(s.status, cor, if (s.pendente) CoresRelatorio.LaranjaClaro else cor.copy(alpha = 0.12f))
    }
}

/**
 * Diálogo do protótipo "Solicitação de acesso". As caixas marcam o que o paciente quer compartilhar,
 * enviando as categorias escolhidas ao aprovar.
 */
@Composable
private fun DialogoSolicitacao(
    solicitacao: SolicitacaoAcessoUi,
    respondendo: Boolean,
    onRecusar: () -> Unit,
    onAprovar: (List<String>) -> Unit,
    onRemover: () -> Unit,
    onFechar: () -> Unit
) {
    val opcoes = listOf("Sentimento", "Energia", "Humor", "Pensamentos", "Horas de sono", "Acontecimentos")
    var marcadas by remember(solicitacao.id) {
        mutableStateOf(if (solicitacao.pendente) setOf("Sentimento", "Energia", "Humor", "Horas de sono") else solicitacao.categorias)
    }
    var confirmarRemocao by remember(solicitacao.id) { mutableStateOf(false) }
    if (confirmarRemocao) {
        AlertDialog(
            onDismissRequest = { if (!respondendo) confirmarRemocao = false },
            title = { Text("Remover solicitação e acesso?") },
            text = { Text("O profissional deixará de acessar seus indicadores. Um novo acesso dependerá de outra autorização sua.") },
            confirmButton = {
                Button(onClick = onRemover, enabled = !respondendo) { Text(if (respondendo) "Removendo..." else "Remover") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmarRemocao = false }, enabled = !respondendo) { Text("Cancelar") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = { if (!respondendo) onFechar() },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciais(solicitacao.nome, tamanho = 40.dp, fotoUrl = solicitacao.foto)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(solicitacao.nome, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CoresRelatorio.RoxoEscuro)
                    if (solicitacao.especialidade.isNotBlank()) {
                        Text(solicitacao.especialidade, fontSize = 12.sp, color = CoresRelatorio.Cinza)
                    }
                }
                IconButton(onClick = onFechar, enabled = !respondendo) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CoresRelatorio.Cinza)
                }
            }
        },
        text = {
            Column {
                Text(
                    if (solicitacao.status == "Recusada") "Solicitação recusada. Nenhum dado está sendo compartilhado." else "O que deseja compartilhar com este profissional?",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoresRelatorio.RoxoEscuro
                )
                Spacer(Modifier.height(12.dp))
                if (solicitacao.pendente || solicitacao.status == "Aprovada") opcoes.chunked(2).forEach { linha ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        linha.forEach { opcao ->
                            val marcada = opcao in marcadas
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (marcada) CoresRelatorio.RoxoClaro else Color.White)
                                    .clickable(enabled = !respondendo) { marcadas = if (marcada) marcadas - opcao else marcadas + opcao }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (marcada) CoresRelatorio.Roxo else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (marcada) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(opcao, fontSize = 12.sp, color = CoresRelatorio.RoxoEscuro, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (solicitacao.pendente || solicitacao.status == "Aprovada") {
                    Button(
                        onClick = { onAprovar(marcadas.toList()) },
                        enabled = !respondendo,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria)
                    ) {
                        if (respondendo) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text(if (solicitacao.pendente) "Aprovar" else "Salvar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OutlinedButton(
                    onClick = { if (solicitacao.pendente) onRecusar() else confirmarRemocao = true },
                    enabled = !respondendo,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE4DFED))
                ) {
                    Text(
                        if (solicitacao.pendente) "Recusar" else "Remover",
                        color = if (solicitacao.pendente) CoresRelatorio.Texto2 else CoresRelatorio.Vermelho,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

// ------------------------------------------------ anotações

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnotacaoPeriodoItem(r: RegistroHumorApi) {
    val nivelHumor = EscalasRelatorio.humor(r.humor)
    val emoji = when (nivelHumor) {
        5 -> "😄"
        4 -> "😊"
        3 -> "😐"
        2 -> "🙁"
        1 -> "😢"
        else -> "📝"
    }
    val corHumor = when (nivelHumor) {
        4, 5 -> CoresRelatorio.Verde
        1, 2 -> CoresRelatorio.Vermelho
        else -> CoresRelatorio.Roxo
    }
    var expandida by remember(r) { mutableStateOf(false) }
    var textoLongo by remember(r) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(corHumor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = DatasRelatorio.diaMes(r.data),
                fontSize = 11.sp,
                color = CoresRelatorio.Cinza
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                r.sentimento?.takeIf { it.isNotBlank() }?.let { SeloChip(it) }
                r.energia?.takeIf { it.isNotBlank() }?.let { SeloChip(it) }
                r.humor?.takeIf { it.isNotBlank() }?.let { SeloChip(it) }
                r.pensamento?.takeIf { it.isNotBlank() }?.let { SeloChip(it) }
            }
            r.horasSono?.let {
                Spacer(Modifier.height(4.dp))
                SeloChip("🌙 ${formatarHorasSono(it)} sono", sono = true)
            }
            if (!r.relato.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = r.relato,
                    fontSize = 13.sp,
                    color = CoresRelatorio.Texto2,
                    lineHeight = 19.sp,
                    maxLines = if (expandida) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { if (!expandida) textoLongo = it.hasVisualOverflow }
                )
                if (textoLongo) {
                    Text(
                        text = if (expandida) "Ver menos" else "Ver mais",
                        color = CorPrimaria,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { expandida = !expandida }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeloChip(texto: String, sono: Boolean = false) {
    Box(
        modifier = Modifier
            .background(if (sono) Color(0xFFEFF6FF) else Color(0xFFF3EEFA), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = if (sono) Color(0xFF539BFF) else CoresRelatorio.Roxo, fontWeight = FontWeight.SemiBold)
    }
}


