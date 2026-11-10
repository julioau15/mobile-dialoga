package com.aulasandroid.mobiledialoga.feature.paciente.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.PacienteSubRota
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FundoTela = Color(0xFFF7F4FC)
private val RoxoEscuro = Color(0xFF2D1B45)
private val RoxoClaro = Color(0xFFF3EDF9)
private val Roxo = Color(0xFF7B4FA6)
private val Verde = Color(0xFF059669)
private val Vermelho = Color(0xFFDC2626)
private val Azul = Color(0xFF3B82F6)
private val Laranja = Color(0xFFF59E0B)
private val Divisor = Color(0xFFEFEAF6)
private val CinzaTexto = Color(0xFF8A8A99)

@Composable
fun PacienteHomeScreen(
    onNavigate: (PacienteSubRota) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PacienteDashboardViewModel = viewModel()
) {
    val authState = useAuth()
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.carregarDados(authState)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FundoTela)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorPrimaria)
        }
        state.erro?.let { Text(text = it, color = Vermelho, fontSize = 12.sp) }

        HeaderSection(state.nomeUsuario)

        IndicadoresHojeSection(
            indicadores = state.indicadoresHoje,
            onRegistrarClick = { onNavigate(PacienteSubRota.HUMOR) }
        )

        SonoSemanaSection(state.sonoSemana)

        EstatisticasGrid(state.estatisticas)

        MedicamentosSection(
            medicamentos = state.medicamentosHoje,
            onVerTodosClick = { onNavigate(PacienteSubRota.TRATAMENTO) }
        )

        ConsultasSection(
            consultas = state.proximasConsultas,
            onAgendaClick = { onNavigate(PacienteSubRota.AGENDA) }
        )

        TarefasSection(
            tarefas = state.tarefasRecentes,
            onVerTodasClick = { onNavigate(PacienteSubRota.TAREFAS) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ---------------------------------------------------------------- blocos base

@Composable
private fun CardSecao(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
private fun CabecalhoSecao(icone: Painter, titulo: String, acao: String, onAcao: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, null, tint = CorPrimaria, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoxoEscuro)
        }
        Row(modifier = Modifier.clickable(onClick = onAcao), verticalAlignment = Alignment.CenterVertically) {
            Text(text = acao, fontSize = 12.sp, color = CorPrimaria, fontWeight = FontWeight.SemiBold)
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = CorPrimaria, modifier = Modifier.size(16.dp)
            )
        }
    }
    HorizontalDivider(color = Divisor)
}

@Composable
private fun TextoVazio(texto: String) {
    Text(
        text = texto,
        fontSize = 13.sp,
        color = CinzaTexto,
        modifier = Modifier.padding(16.dp)
    )
}

// ---------------------------------------------------------------- header

@Composable
private fun HeaderSection(nome: String) {
    val dataAtual = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFE4D8F3), Color(0xFFFAC3C6))))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(text = dataAtual, fontSize = 14.sp, color = Roxo, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Olá, $nome! 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RoxoEscuro
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Como você está se sentindo hoje?", fontSize = 14.sp, color = CorSecundaria)
    }
}

// ---------------------------------------------------------------- indicadores

@Composable
private fun IndicadoresHojeSection(indicadores: IndicadoresHoje?, onRegistrarClick: () -> Unit) {
    val dataHoje = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")).format(Date())
    }

    CardSecao {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_sidebar_relatorios), null, tint = CorPrimaria, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Indicadores de hoje", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoxoEscuro)
                    }
                    Text(text = dataHoje, fontSize = 12.sp, color = CinzaTexto)
                }

                // O botão só aparece se não houver indicadores registrados para hoje
                if (indicadores == null) {
                    Button(
                        onClick = onRegistrarClick,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = "😊", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Registrar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (indicadores == null) {
                Text(
                    text = "Você ainda não registrou como está hoje. Toque em Registrar.",
                    fontSize = 12.sp,
                    color = CinzaTexto
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IndicadorBar("Energia", "⚡", indicadores?.energia ?: 0, Modifier.weight(1f))
                IndicadorBar("Sentimento", "💭", indicadores?.sentimento ?: 0, Modifier.weight(1f))
                IndicadorBar("Humor", "😊", indicadores?.humor ?: 0, Modifier.weight(1f))
                IndicadorBar("Pensamentos", "🧠", indicadores?.pensamentos ?: 0, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendaPonto(Verde, "Positivo")
                Spacer(modifier = Modifier.width(16.dp))
                LegendaPonto(Roxo, "Moderado")
                Spacer(modifier = Modifier.width(16.dp))
                LegendaPonto(Vermelho, "Atenção")
            }
        }
    }
}

@Composable
private fun IndicadorBar(label: String, emoji: String, valor: Int, modifier: Modifier = Modifier) {
    val cor = when {
        valor > 70 -> Verde
        valor > 40 -> Roxo
        else -> Vermelho
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(valor.coerceIn(0, 100) / 100f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(cor)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = emoji, fontSize = 12.sp)
        Text(text = label, fontSize = 11.sp, color = CorSecundaria, maxLines = 1)
    }
}

@Composable
private fun LegendaPonto(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(cor))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = texto, fontSize = 11.sp, color = CorSecundaria)
    }
}

// ---------------------------------------------------------------- sono

@Composable
private fun SonoSemanaSection(sono: List<SonoDia>) {
    val media = if (sono.isEmpty()) null else sono.map { it.horas }.average()

    CardSecao {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = "🌙 Sono da semana", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoxoEscuro)
                    Text(text = "Horas dormidas nos últimos 7 dias", fontSize = 12.sp, color = CinzaTexto)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = media?.let { String.format(Locale.US, "%.1fh", it) } ?: "--",
                        fontWeight = FontWeight.Bold, fontSize = 22.sp, color = RoxoEscuro
                    )
                    Text(text = "média/dia", fontSize = 11.sp, color = CinzaTexto)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sono.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFF9F7FD), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Registre seu humor por alguns dias para ver o gráfico", fontSize = 12.sp, color = CorSecundaria)
                }
            } else {
                SonoChart(sono)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(CinzaTexto))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Ideal (7h)", fontSize = 11.sp, color = CorSecundaria)
                Spacer(modifier = Modifier.width(16.dp))
                LegendaPonto(Vermelho, "Abaixo do ideal")
                Spacer(modifier = Modifier.width(16.dp))
                LegendaPonto(Roxo, "Hoje")
            }
        }
    }
}

private fun formatarHoras(v: Double): String =
    if (v % 1.0 == 0.0) "${v.toInt()}h" else String.format(Locale.US, "%.1fh", v)

@OptIn(ExperimentalTextApi::class)
@Composable
private fun SonoChart(dados: List<SonoDia>) {
    val measurer = rememberTextMeasurer()
    val estiloEixo = TextStyle(fontSize = 11.sp, color = CinzaTexto)
    val estiloIdeal = TextStyle(fontSize = 11.sp, color = Roxo, fontWeight = FontWeight.Bold)
    val estiloValor = TextStyle(fontSize = 11.sp, color = RoxoEscuro, fontWeight = FontWeight.Bold)
    val estiloHoje = TextStyle(fontSize = 11.sp, color = Roxo, fontWeight = FontWeight.Bold)

    Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
        val n = dados.size
        val esq = 40.dp.toPx()
        val dir = 18.dp.toPx()
        val topo = 22.dp.toPx()
        val base = 28.dp.toPx()
        val larguraUtil = size.width - esq - dir
        val alturaUtil = size.height - topo - base
        val minY = 4.0
        val maxY = 10.0

        fun px(i: Int) = esq + larguraUtil * i / (n - 1)
        fun py(v: Double) = topo + alturaUtil * (1f - ((v.coerceIn(minY, maxY) - minY) / (maxY - minY)).toFloat())

        // grade + rótulos do eixo Y
        listOf(4, 6, 8, 10).forEach { h ->
            val y = py(h.toDouble())
            drawLine(Color(0xFFEDE7F6), Offset(esq, y), Offset(size.width - dir, y), 1.dp.toPx())
            val t = measurer.measure("${h}h", estiloEixo)
            drawText(t, topLeft = Offset(esq - 8.dp.toPx() - t.size.width, y - t.size.height / 2f))
        }

        // linha ideal (7h)
        val yIdeal = py(7.0)
        drawLine(
            color = Color(0xFFB9A3D6),
            start = Offset(esq, yIdeal),
            end = Offset(size.width - dir, yIdeal),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
        val tIdeal = measurer.measure("7h ✓", estiloIdeal)
        drawText(tIdeal, topLeft = Offset(esq - 8.dp.toPx() - tIdeal.size.width, yIdeal - tIdeal.size.height / 2f))

        // curva suavizada
        val linha = Path().apply {
            moveTo(px(0), py(dados[0].horas))
            for (i in 1 until n) {
                val cx = (px(i - 1) + px(i)) / 2f
                cubicTo(cx, py(dados[i - 1].horas), cx, py(dados[i].horas), px(i), py(dados[i].horas))
            }
        }
        val area = Path().apply {
            addPath(linha)
            lineTo(px(n - 1), topo + alturaUtil)
            lineTo(px(0), topo + alturaUtil)
            close()
        }
        drawPath(
            area,
            Brush.verticalGradient(
                listOf(Roxo.copy(alpha = 0.18f), Roxo.copy(alpha = 0f)),
                startY = topo, endY = topo + alturaUtil
            )
        )
        drawPath(
            linha, Roxo,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // pontos, valores e dias da semana
        dados.forEachIndexed { i, d ->
            val hoje = i == n - 1
            val centro = Offset(px(i), py(d.horas))
            val cor = if (!hoje && d.horas < 7.0) Vermelho else Roxo

            if (hoje) drawCircle(Roxo.copy(alpha = 0.25f), 11.dp.toPx(), centro)
            drawCircle(Color.White, (if (hoje) 8f else 6f).dp.toPx(), centro)
            drawCircle(cor, (if (hoje) 6f else 4.5f).dp.toPx(), centro)

            val tv = measurer.measure(formatarHoras(d.horas), if (hoje) estiloHoje else estiloValor)
            val xv = (centro.x - tv.size.width / 2f).coerceIn(0f, size.width - tv.size.width)
            drawText(tv, topLeft = Offset(xv, centro.y - tv.size.height - 8.dp.toPx()))

            val td = measurer.measure(d.diaSemana, if (hoje) estiloHoje else estiloEixo)
            val xd = (centro.x - td.size.width / 2f).coerceIn(0f, size.width - td.size.width)
            drawText(td, topLeft = Offset(xd, size.height - base + 10.dp.toPx()))
        }
    }
}

// ---------------------------------------------------------------- estatísticas

@Composable
private fun EstatisticasGrid(e: EstatisticasGerais) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Tratamentos ativos", e.tratamentosAtivos, painterResource(R.drawable.ic_sidebar_tratamentos), CorPrimaria, RoxoClaro, Modifier.weight(1f))
            StatCard("Próximas consultas", e.proximasConsultas, painterResource(R.drawable.ic_sidebar_agenda), Azul, Color(0xFFEAF2FF), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Tarefas pendentes", e.tarefasPendentes, painterResource(R.drawable.ic_sidebar_tarefas), Verde, Color(0xFFE6F6EF), Modifier.weight(1f))
            StatCard("Profissionais", e.profissionais, painterResource(R.drawable.ic_sidebar_profissionais), Laranja, Color(0xFFFFF3DC), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String, valor: Int, icone: Painter,
    corIcone: Color, fundoIcone: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(fundoIcone),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, null, tint = corIcone, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = valor.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = RoxoEscuro)
            Text(text = label, fontSize = 12.sp, color = CorSecundaria)
        }
    }
}

// ---------------------------------------------------------------- medicamentos

@Composable
private fun MedicamentosSection(medicamentos: List<MedicamentoDashboard>, onVerTodosClick: () -> Unit) {
    CardSecao {
        CabecalhoSecao(painterResource(R.drawable.ic_sidebar_tratamentos), "Medicamentos de hoje", "Ver todos", onVerTodosClick)
        if (medicamentos.isEmpty()) TextoVazio("Nenhum medicamento para hoje")
        medicamentos.forEachIndexed { i, med ->
            if (i > 0) HorizontalDivider(color = Divisor)
            MedicamentoItem(med)
        }
    }
}

@Composable
private fun MedicamentoItem(med: MedicamentoDashboard) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = med.nome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RoxoEscuro)
            Text(text = med.frequencia, fontSize = 12.sp, color = CinzaTexto)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            med.horarios.forEach { hora ->
                Box(
                    modifier = Modifier
                        .background(RoxoClaro, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = hora, fontSize = 12.sp, color = CorPrimaria, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- consultas

@Composable
private fun ConsultasSection(consultas: List<ConsultaDashboard>, onAgendaClick: () -> Unit) {
    CardSecao {
        CabecalhoSecao(painterResource(R.drawable.ic_sidebar_agenda), "Próximas consultas", "Agenda", onAgendaClick)
        if (consultas.isEmpty()) TextoVazio("Nenhuma consulta agendada")
        consultas.forEachIndexed { i, c ->
            if (i > 0) HorizontalDivider(color = Divisor)
            ConsultaItem(c)
        }
    }
}

@Composable
private fun ConsultaItem(consulta: ConsultaDashboard) {
    val (dia, mes) = remember(consulta.data) {
        val data = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(consulta.data) }.getOrNull()
        if (data == null) {
            consulta.data.takeLast(2) to ""
        } else {
            val ptBr = Locale("pt", "BR")
            val d = SimpleDateFormat("d", ptBr).format(data)
            val m = SimpleDateFormat("MMM", ptBr).format(data)
                .replace(".", "").replaceFirstChar { it.uppercase() } + "."
            d to m
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(RoxoClaro, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = dia, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CorPrimaria)
                Text(text = mes, fontSize = 10.sp, color = CorPrimaria)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = consulta.profissionalNome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RoxoEscuro)
            if (consulta.especialidade.isNotBlank()) {
                Text(text = consulta.especialidade, fontSize = 12.sp, color = CinzaTexto)
            }
            if (consulta.horario.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🕒", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = consulta.horario, fontSize = 12.sp, color = CinzaTexto)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- tarefas

@Composable
private fun TarefasSection(tarefas: List<TarefaDashboard>, onVerTodasClick: () -> Unit) {
    CardSecao {
        CabecalhoSecao(painterResource(R.drawable.ic_sidebar_tarefas), "Tarefas recentes", "Ver todas", onVerTodasClick)
        if (tarefas.isEmpty()) TextoVazio("Nenhuma tarefa por enquanto")
        tarefas.forEachIndexed { i, t ->
            if (i > 0) HorizontalDivider(color = Divisor)
            TarefaItem(t)
        }
    }
}

@Composable
private fun TarefaItem(tarefa: TarefaDashboard) {
    val corStatus = when (tarefa.status) {
        "Concluída" -> Verde
        "Em andamento" -> Azul
        else -> Roxo
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = tarefa.titulo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = RoxoEscuro,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "Prazo: ${tarefa.prazo}", fontSize = 12.sp, color = CinzaTexto)
        }
        Box(
            modifier = Modifier
                .background(corStatus.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = tarefa.status, fontSize = 11.sp, color = corStatus, fontWeight = FontWeight.Bold)
        }
    }
}