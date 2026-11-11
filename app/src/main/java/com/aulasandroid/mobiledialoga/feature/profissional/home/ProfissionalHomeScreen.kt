package com.aulasandroid.mobiledialoga.feature.profissional.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalUiState
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalViewModel
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.ProfissionalTarefaDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.StatusDestinatario
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.StatusTarefa
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.formatarDataBr
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import java.util.Calendar

private val CorLinha = Color(0xFFEDE9F5)
private val CorAvatarAtendimentoFundo = Color(0xFFE4DAF3)
private val CorAvatarPacienteFundo = Color(0xFFFDE4E4)
private val CorAvatarPacienteTexto = Color(0xFFC0504D)
private val CorVerdeFundo = Color(0xFFDCF5E6)
private val CorVerdeTexto = Color(0xFF16A34A)
private val CorAzulFundo = Color(0xFFDDEAFE)
private val CorAzulTexto = Color(0xFF2563EB)
private val CorAmareloFundo = Color(0xFFFEF3C7)
private val CorAmareloTexto = Color(0xFFD97706)
private val CorVermelhoFundo = Color(0xFFFEE2E2)
private val CorVermelhoTexto = Color(0xFFDC2626)
private val CorLilasFundo = Color(0xFFEDE7F6)

@Composable
fun ProfissionalHomeScreen(
    modifier: Modifier = Modifier,
    onAbrirAgenda: () -> Unit = {},
    onAbrirPacientes: () -> Unit = {},
    onAbrirTarefas: () -> Unit = {},
    viewModel: ProfissionalDashboardViewModel = viewModel(),
    // Mesmo ViewModel da tela de Tarefas: os dados já carregados lá são reaproveitados aqui
    tarefasViewModel: TarefasProfissionalViewModel = viewModel()
) {
    val authState = useAuth()
    val token = authState.auth?.token
    val estado = viewModel.state
    val tarefas by tarefasViewModel.uiState.observeAsState(TarefasProfissionalUiState())

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            viewModel.carregar(token)
            tarefasViewModel.carregarDados(token)
        }
    }

    val primeiroNome = authState.userProfile?.getNomeExibicao()
        ?.trim()?.substringBefore(" ").orEmpty().ifBlank { "Profissional" }

    val enviadas = tarefas.enviadas
    val pendentesResposta = enviadas.sumOf { t ->
        t.destinatarios.count { it.status() != StatusDestinatario.CONCLUIDA }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        CartaoBoasVindas(primeiroNome, estado.atendimentosHoje.size, estado.carregou)

        if (estado.erro != null) {
            CartaoErro(estado.erro) { token?.let { viewModel.carregar(it); tarefasViewModel.carregarDados(it) } }
        }

        // Indicadores (2 x 2)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CartaoIndicador(
                valor = estado.pacientesAtivos,
                rotulo = "Pacientes ativos",
                icone = Icons.Outlined.Group,
                corIcone = CorPrimaria,
                fundoIcone = CorLilasFundo,
                modifier = Modifier.weight(1f)
            )
            CartaoIndicador(
                valor = estado.atendimentosHoje.size,
                rotulo = "Atendimentos hoje",
                icone = Icons.Outlined.CalendarToday,
                corIcone = CorAzulTexto,
                fundoIcone = CorAzulFundo,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CartaoIndicador(
                valor = enviadas.size,
                rotulo = "Tarefas enviadas",
                icone = Icons.Outlined.CheckBox,
                corIcone = CorVerdeTexto,
                fundoIcone = CorVerdeFundo,
                modifier = Modifier.weight(1f)
            )
            CartaoIndicador(
                valor = pendentesResposta,
                rotulo = "Pendentes resp.",
                icone = Icons.Outlined.ShowChart,
                corIcone = CorAmareloTexto,
                fundoIcone = CorAmareloFundo,
                modifier = Modifier.weight(1f)
            )
        }

        // Atendimentos de hoje
        CartaoSecao(
            titulo = "Atendimentos de hoje",
            icone = Icons.Outlined.CalendarToday,
            acao = "Agenda",
            onAcao = onAbrirAgenda
        ) {
            if (estado.atendimentosHoje.isEmpty()) {
                TextoVazio(if (estado.carregou) "Nenhum atendimento hoje." else "Carregando…")
            } else {
                estado.atendimentosHoje.forEachIndexed { i, a ->
                    if (i > 0) HorizontalDivider(color = CorLinha)
                    LinhaPessoa(
                        iniciais = iniciaisDe(a.paciente),
                        fundoAvatar = CorAvatarAtendimentoFundo,
                        corAvatar = CorPrimaria,
                        titulo = a.paciente,
                        subtitulo = a.titulo
                    ) {
                        if (a.hora.isNotBlank()) SeloHorario(a.hora)
                    }
                }
            }
        }

        // Pacientes recentes
        CartaoSecao(
            titulo = "Pacientes recentes",
            icone = Icons.Outlined.Group,
            acao = "Ver todos",
            onAcao = onAbrirPacientes
        ) {
            val recentes = estado.pacientes.take(4)
            if (recentes.isEmpty()) {
                TextoVazio(if (estado.carregou) "Nenhum paciente vinculado ainda." else "Carregando…")
            } else {
                recentes.forEachIndexed { i, p ->
                    if (i > 0) HorizontalDivider(color = CorLinha)
                    LinhaPessoa(
                        iniciais = iniciaisDe(p.nome),
                        fundoAvatar = CorAvatarPacienteFundo,
                        corAvatar = CorAvatarPacienteTexto,
                        titulo = p.nome,
                        subtitulo = p.proximaIso?.let { "Próx. ${it.formatarDataBr()}" } ?: "Sem consulta agendada"
                    ) {
                        if (p.ativo) Selo("Ativo", CorVerdeFundo, CorVerdeTexto)
                        else Selo("Inativo", CorLilasFundo, CorSecundaria)
                    }
                }
            }
        }

        // Atividades enviadas
        CartaoSecao(
            titulo = "Atividades enviadas",
            icone = Icons.Outlined.CheckBox,
            acao = "Gerenciar",
            onAcao = onAbrirTarefas
        ) {
            val recentes = enviadas.sortedByDescending { it.dataEnvio.orEmpty() }.take(4)
            when {
                recentes.isEmpty() -> TextoVazio(
                    if (tarefas.isLoading) "Carregando…" else "Nenhuma atividade enviada ainda."
                )
                else -> recentes.forEachIndexed { i, t ->
                    if (i > 0) HorizontalDivider(color = CorLinha)
                    LinhaAtividade(t)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// Blocos
// ---------------------------------------------------------------------------

@Composable
private fun CartaoBoasVindas(nome: String, atendimentosHoje: Int, carregou: Boolean) {
    val resumo = when {
        !carregou -> "Preparando o seu dia…"
        atendimentosHoje == 0 -> "Você não tem atendimentos hoje."
        atendimentosHoje == 1 -> "Você tem 1 atendimento hoje."
        else -> "Você tem $atendimentosHoje atendimentos hoje."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF7B4FA6), Color(0xFF5F3A86))))
            .padding(horizontal = 24.dp, vertical = 22.dp)
    ) {
        Text(text = dataPorExtenso(), color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Olá, $nome! \uD83D\uDC4B",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = resumo, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
    }
}

@Composable
private fun CartaoErro(mensagem: String, onTentarNovamente: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorVermelhoFundo),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Text(text = mensagem, color = CorErro, fontSize = 13.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onTentarNovamente) {
                Text("Tentar de novo", color = CorErro, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CartaoIndicador(
    valor: Int,
    rotulo: String,
    icone: ImageVector,
    corIcone: Color,
    fundoIcone: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(fundoIcone)
            ) {
                Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = valor.toString(), color = CorTexto, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = rotulo, color = CorSecundaria, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CartaoSecao(
    titulo: String,
    icone: ImageVector,
    acao: String,
    onAcao: () -> Unit,
    conteudo: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Icon(icone, contentDescription = null, tint = CorPrimaria, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titulo,
                    color = CorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onAcao)
                ) {
                    Text(text = acao, color = CorPrimaria, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = CorSecundaria,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = CorLinha)
            conteudo()
        }
    }
}

@Composable
private fun LinhaPessoa(
    iniciais: String,
    fundoAvatar: Color,
    corAvatar: Color,
    titulo: String,
    subtitulo: String,
    fim: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(fundoAvatar)
        ) {
            Text(text = iniciais, color = corAvatar, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = CorTexto,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitulo,
                color = CorSecundaria,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        fim()
    }
}

@Composable
private fun LinhaAtividade(tarefa: ProfissionalTarefaDto) {
    val nomes = tarefa.destinatarios.map { it.nomePaciente }.distinct()
    val para = when {
        nomes.isEmpty() -> "—"
        nomes.size == 1 -> nomes[0]
        else -> "${nomes[0]} +${nomes.size - 1}"
    }
    val status = tarefa.statusGeral()
    val (fundo, texto) = when (status) {
        StatusTarefa.EM_ANDAMENTO -> CorAzulFundo to CorAzulTexto
        StatusTarefa.PENDENTE -> CorLilasFundo to CorPrimaria
        StatusTarefa.RESPONDIDA -> CorVerdeFundo to CorVerdeTexto
        StatusTarefa.EM_ATRASO -> CorVermelhoFundo to CorVermelhoTexto
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tarefa.titulo,
                color = CorTexto,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Para: $para",
                color = CorSecundaria,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Selo(status.rotulo, fundo, texto)
    }
}

@Composable
private fun Selo(texto: String, fundo: Color, corTexto: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = texto, color = corTexto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SeloHorario(hora: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CorLilasFundo)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = CorPrimaria, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = hora, color = CorPrimaria, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TextoVazio(texto: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp)
    ) {
        if (texto == "Carregando…") {
            CircularProgressIndicator(color = CorPrimaria, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(text = texto, color = CorSecundaria, fontSize = 13.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

private fun iniciaisDe(nome: String): String {
    val partes = nome.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        partes.isEmpty() -> "?"
        partes.size == 1 -> partes[0].take(2).uppercase()
        else -> "${partes.first().first()}${partes.last().first()}".uppercase()
    }
}

/** "Quarta-feira, 11 de novembro" (sem java.time, como o resto do app). */
private fun dataPorExtenso(): String {
    val dias = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
    val meses = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    )
    val c = Calendar.getInstance()
    return "${dias[c.get(Calendar.DAY_OF_WEEK) - 1]}, ${c.get(Calendar.DAY_OF_MONTH)} de ${meses[c.get(Calendar.MONTH)]}"
}