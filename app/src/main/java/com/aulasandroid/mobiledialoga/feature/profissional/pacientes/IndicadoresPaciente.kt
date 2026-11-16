package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.AnotacaoPeriodoItem
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.DatasRelatorio
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.GraficoIndicadores
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.RelatoriosMapper
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.SonoPonto
import com.aulasandroid.mobiledialoga.feature.paciente.relatorios.formatarHorasSono
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalUiState
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.StatusDestinatario
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlin.math.ceil
import kotlin.math.floor

private val VerdeIndicador = Color(0xFF009D78)
private val FundoIndicador = Color(0xFFF3EEFA)

@Composable
internal fun AbaIndicadores(
    paciente: PacienteVinculadoUi,
    estado: PacientesProfissionalState,
    tarefas: TarefasProfissionalUiState,
    onSolicitarAcesso: () -> Unit,
    onRecarregar: () -> Unit
) {
    var dias by rememberSaveable(paciente.id) { mutableIntStateOf(7) }
    when {
        estado.carregandoIndicadores || estado.idIndicadores != paciente.id -> {
            CardIndicador {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = CorPrimaria, strokeWidth = 2.dp)
                }
            }
        }
        estado.erroIndicadores != null -> {
            CardIndicador {
                Column(Modifier.padding(20.dp)) {
                    Text(estado.erroIndicadores, color = Color(0xFFDC2626), fontSize = 13.sp)
                    TextButton(onClick = onRecarregar) { Text("Tentar novamente") }
                }
            }
        }
        estado.acessos[paciente.id] != "aprovada" -> {
            CardIndicador {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Indicadores do paciente", color = CorTexto, fontWeight = FontWeight.Bold)
                    Text("O paciente escolhe quais informações deseja compartilhar.", color = CorSecundaria, fontSize = 13.sp)
                    if (paciente.id in estado.acessoSolicitado) {
                        Text("Aguardando autorização do paciente", color = CorPrimaria, fontSize = 13.sp)
                        TextButton(onClick = onRecarregar) { Text("Atualizar") }
                    } else {
                        Button(onClick = onSolicitarAcesso, enabled = !estado.salvando) { Text("Solicitar acesso") }
                    }
                }
            }
        }
        else -> {
            val hoje = DatasRelatorio.hoje()
            val inicio = DatasRelatorio.somarDias(hoje, 1 - dias)
            val registros = estado.registrosCompartilhados.filter { it.data in inicio..hoje }
            val categorias = estado.categorias
            val medias = RelatoriosMapper.medias(registros).filter { it.rotulo in categorias }
            val pontosSono = RelatoriosMapper.sono(registros)
            val resumoSono = RelatoriosMapper.resumoSono(pontosSono)

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("PERÍODO", color = CorSecundaria, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 14, 30).forEach { periodo ->
                        Box(
                            Modifier.clip(RoundedCornerShape(10.dp))
                                .background(if (dias == periodo) CorPrimaria else FundoIndicador)
                                .clickable { dias = periodo }
                                .padding(horizontal = 13.dp, vertical = 9.dp)
                        ) {
                            Text("${periodo}d", color = if (dias == periodo) Color.White else CorSecundaria, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            CardIndicador {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CabecalhoIndicador("⌁", "Indicadores emocionais", "Média dos últimos $dias dias · dados do paciente", "${registros.size} registro(s)")
                    if (medias.isEmpty()) {
                        TextoIndicadorVazio("Indicadores emocionais não compartilhados.")
                    } else if (registros.isEmpty()) {
                        TextoIndicadorVazio("Nenhum registro neste período.")
                    } else {
                        GraficoIndicadores(medias)
                    }
                }
            }

            if ("Horas de sono" in categorias) {
                CardIndicador {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CabecalhoIndicador("☾", "Sono", "Horas dormidas no período", resumoSono?.let { formatarHorasSono(it.media) }, "média/dia")
                        if (pontosSono.isEmpty()) TextoIndicadorVazio("Nenhum registro de sono neste período.")
                        else GraficoSonoPaciente(pontosSono)
                    }
                }
            }

            CardIndicador {
                Column(Modifier.padding(16.dp)) {
                    CabecalhoIndicador("▤", "Anotações do paciente", detalhe = "${registros.size} registro(s)")
                }
                HorizontalDivider(color = CorBordaCard)
                if (registros.isEmpty()) {
                    Column(Modifier.padding(16.dp)) { TextoIndicadorVazio("Nenhuma anotação neste período.") }
                }
                registros.forEachIndexed { indice, registro ->
                    if (indice > 0) HorizontalDivider(color = CorBordaCard)
                    AnotacaoPeriodoItem(registro)
                }
            }

            CardIndicador {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val idsEnviadas = tarefas.enviadas.map { it.id }.toSet()
                    val acompanhamentos = tarefas.acompanhamentos.filter { it.idPaciente == paciente.id && it.idTarefa in idsEnviadas }
                    val total = acompanhamentos.size
                    CabecalhoIndicador("☑", "Conclusão de tarefas", "Total de tarefas enviadas: $total")
                    when {
                        tarefas.isLoading -> CircularProgressIndicator(Modifier.size(20.dp), color = CorPrimaria, strokeWidth = 2.dp)
                        tarefas.erroLista != null -> TextoIndicadorVazio("Não foi possível carregar as tarefas.")
                        total == 0 -> TextoIndicadorVazio("Nenhuma tarefa enviada a este paciente.")
                        else -> {
                            BarraConclusao("Respondida", acompanhamentos.count { it.status() == StatusDestinatario.CONCLUIDA }, total, VerdeIndicador)
                            BarraConclusao("Em andamento", acompanhamentos.count { it.status() == StatusDestinatario.EM_ANDAMENTO }, total, Color(0xFF539BFF))
                        }
                    }
                }
            }

            CardIndicador {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CabecalhoIndicador("💊", "Aderência ao tratamento", "Dados informados pelo paciente")
                    TextoIndicadorVazio("Não há dados de aderência ao tratamento compartilhados.")
                }
            }

            Text(
                "ℹ Dados fornecidos pelo próprio paciente. Não substituem avaliação clínica.",
                color = CorSecundaria, fontSize = 11.sp, lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEAEA), RoundedCornerShape(10.dp)).padding(12.dp)
            )
        }
    }
}

@Composable
private fun CardIndicador(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
private fun CabecalhoIndicador(icone: String, titulo: String, subtitulo: String? = null, detalhe: String? = null, legendaDetalhe: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("$icone  $titulo", color = CorTexto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            subtitulo?.let { Text(it, color = CorSecundaria, fontSize = 11.sp, lineHeight = 16.sp) }
        }
        detalhe?.let {
            Column(horizontalAlignment = Alignment.End) {
                Text(it, color = if (legendaDetalhe != null) CorTexto else CorSecundaria, fontSize = if (legendaDetalhe != null) 16.sp else 10.sp, fontWeight = if (legendaDetalhe != null) FontWeight.Bold else FontWeight.Normal)
                legendaDetalhe?.let { texto -> Text(texto, color = CorSecundaria, fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun TextoIndicadorVazio(texto: String) {
    Text(texto, color = CorSecundaria, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
private fun BarraConclusao(titulo: String, quantidade: Int, total: Int, cor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, color = CorTexto, fontSize = 11.sp)
            Text("$quantidade de $total", color = CorSecundaria, fontSize = 11.sp)
        }
        LinearProgressIndicator(
            progress = { quantidade.toFloat() / total }, color = cor, trackColor = Color(0xFFE8E0F0),
            drawStopIndicator = {}, strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun GraficoSonoPaciente(pontos: List<SonoPonto>) {
    val medidor = rememberTextMeasurer()
    val estilo = TextStyle(fontSize = 10.sp, color = CorSecundaria)
    Canvas(Modifier.fillMaxWidth().height(190.dp)) {
        val esquerda = 32.dp.toPx()
        val direita = size.width - 16.dp.toPx()
        val topo = 22.dp.toPx()
        val base = size.height - 28.dp.toPx()
        val minimo = minOf(4.0, floor(pontos.minOf { it.horas } / 2) * 2).coerceAtLeast(0.0)
        val maximo = maxOf(10.0, ceil(pontos.maxOf { it.horas } / 2) * 2)
        fun x(indice: Int) = if (pontos.size == 1) (esquerda + direita) / 2 else esquerda + (direita - esquerda) * indice / (pontos.size - 1)
        fun y(horas: Double) = base - ((horas - minimo) / (maximo - minimo)).toFloat() * (base - topo)
        val passoHoras = if (maximo - minimo > 12) 4 else 2
        val intervalos = ceil((maximo - minimo) / passoHoras).toInt()
        for (indice in 0..intervalos) {
            val horas = minOf(maximo, minimo + passoHoras * indice)
            val altura = y(horas)
            drawLine(Color(0xFFEDE7F6), Offset(esquerda, altura), Offset(direita, altura), 1.dp.toPx())
            val rotulo = medidor.measure("${horas.toInt()}h", estilo)
            drawText(rotulo, topLeft = Offset(esquerda - rotulo.size.width - 6.dp.toPx(), altura - rotulo.size.height / 2))
        }
        val media = pontos.map { it.horas }.average()
        drawLine(
            CorPrimaria.copy(alpha = 0.35f), Offset(esquerda, y(media)), Offset(direita, y(media)),
            strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
        )
        val caminho = Path()
        pontos.forEachIndexed { indice, ponto ->
            if (indice == 0) caminho.moveTo(x(indice), y(ponto.horas))
            else caminho.lineTo(x(indice), y(ponto.horas))
        }
        drawPath(caminho, CorPrimaria, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        val passo = ceil(pontos.size / 5.0).toInt().coerceAtLeast(1)
        pontos.forEachIndexed { indice, ponto ->
            val centro = Offset(x(indice), y(ponto.horas))
            drawCircle(Color.White, 4.5.dp.toPx(), centro)
            drawCircle(if (ponto.horas < 6) Color(0xFFEF4444) else CorPrimaria, 3.dp.toPx(), centro)
            if (indice % passo == 0 || indice == pontos.lastIndex) {
                val texto = medidor.measure(DatasRelatorio.diaMes(ponto.dataIso), estilo)
                drawText(texto, topLeft = Offset(centro.x - texto.size.width / 2, base + 10.dp.toPx()))
            }
        }
    }
}
