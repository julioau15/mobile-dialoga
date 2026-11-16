package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.ceil

internal fun formatarMedia(v: Double): String = String.format(Locale.US, "%.1f", v)
internal fun formatarHorasSono(v: Double): String = String.format(Locale.US, "%.1fh", v)

// ---------------------------------------------------------------- indicadores (barras)

/** Barras com a média de Energia · Sentimento · Humor · Pensamentos no período. */
@Composable
fun GraficoIndicadores(medias: List<IndicadorMedia>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            medias.forEach { BarraIndicador(it, Modifier.weight(1f)) }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendaPonto(CoresRelatorio.Verde, "Positivo")
            Spacer(Modifier.width(16.dp))
            LegendaPonto(CoresRelatorio.Roxo, "Moderado")
            Spacer(Modifier.width(16.dp))
            LegendaPonto(CoresRelatorio.Vermelho, "Atenção")
        }
    }
}

@Composable
private fun BarraIndicador(indicador: IndicadorMedia, modifier: Modifier = Modifier) {
    val p = indicador.proporcao
    val cor = when {
        indicador.media == null -> CoresRelatorio.Divisor
        p > 0.7f -> CoresRelatorio.Verde
        p > 0.4f -> CoresRelatorio.Roxo
        else -> CoresRelatorio.Vermelho
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(p.coerceAtLeast(0.03f)) // sem dado fica só um traço
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(cor)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(indicador.emoji, fontSize = 12.sp)
        Text(
            indicador.rotulo, fontSize = if (indicador.rotulo.length > 10) 10.sp else 11.sp, fontWeight = FontWeight.Bold,
            color = CoresRelatorio.RoxoEscuro, textAlign = TextAlign.Center, maxLines = 2
        )
        Text(
            text = indicador.media?.let { "${formatarMedia(it)}/${indicador.maximo}" } ?: "--",
            fontSize = 11.sp, color = CoresRelatorio.Cinza
        )
    }
}

@Composable
private fun LegendaPonto(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 11.sp, color = CoresRelatorio.Texto2)
    }
}

// ---------------------------------------------------------------- sono (linha)

/** Linha de horas de sono por dia, eixo Y de 0h a 24h (como no protótipo) + resumo. */
@Composable
fun GraficoSono(pontos: List<SonoPonto>, resumo: ResumoSono?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (pontos.isEmpty() || resumo == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(120.dp)
                    .background(Color(0xFFF9F7FD), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sem registros de sono neste período",
                    fontSize = 12.sp, color = CoresRelatorio.Texto2, textAlign = TextAlign.Center
                )
            }
        } else {
            LinhaSono(pontos)
            Spacer(Modifier.height(12.dp))
            ResumoSonoBox(resumo)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun LinhaSono(pontos: List<SonoPonto>) {
    val medidor = rememberTextMeasurer()
    val estiloEixo = TextStyle(fontSize = 10.sp, color = CoresRelatorio.Cinza)

    Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
        val n = pontos.size
        val esq = 34.dp.toPx()
        val dir = 14.dp.toPx()
        val topo = 10.dp.toPx()
        val base = 26.dp.toPx()
        val largura = size.width - esq - dir
        val altura = size.height - topo - base
        val maxY = 24.0

        fun px(i: Int) = if (n == 1) esq + largura / 2f else esq + largura * i / (n - 1)
        fun py(v: Double) = topo + altura * (1f - (v.coerceIn(0.0, maxY) / maxY).toFloat())

        // grade e rótulos do eixo Y
        listOf(0, 6, 12, 18, 24).forEach { h ->
            val y = py(h.toDouble())
            drawLine(Color(0xFFEDE7F6), Offset(esq, y), Offset(size.width - dir, y), 1.dp.toPx())
            val t = medidor.measure("${h}h", estiloEixo)
            drawText(t, topLeft = Offset(esq - 6.dp.toPx() - t.size.width, y - t.size.height / 2f))
        }

        // linha + área (só com 2 ou mais pontos)
        if (n >= 2) {
            val linha = Path().apply {
                moveTo(px(0), py(pontos[0].horas))
                for (i in 1 until n) {
                    val cx = (px(i - 1) + px(i)) / 2f
                    cubicTo(cx, py(pontos[i - 1].horas), cx, py(pontos[i].horas), px(i), py(pontos[i].horas))
                }
            }
            val area = Path().apply {
                addPath(linha)
                lineTo(px(n - 1), topo + altura)
                lineTo(px(0), topo + altura)
                close()
            }
            drawPath(
                area,
                Brush.verticalGradient(
                    listOf(CoresRelatorio.Roxo.copy(alpha = 0.16f), CoresRelatorio.Roxo.copy(alpha = 0f)),
                    startY = topo, endY = topo + altura
                )
            )
            drawPath(
                linha, CoresRelatorio.Roxo,
                style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // pontos + datas no eixo X (rótulos espaçados para não encavalar)
        val passo = ceil(n / 6.0).toInt().coerceAtLeast(1)
        pontos.forEachIndexed { i, p ->
            val centro = Offset(px(i), py(p.horas))
            drawCircle(Color.White, 5.5f.dp.toPx(), centro)
            drawCircle(CoresRelatorio.Roxo, 4f.dp.toPx(), centro)

            if (i % passo == 0 || i == n - 1) {
                val t = medidor.measure(DatasRelatorio.diaMes(p.dataIso), estiloEixo)
                val x = (centro.x - t.size.width / 2f).coerceIn(0f, size.width - t.size.width)
                drawText(t, topLeft = Offset(x, size.height - base + 8.dp.toPx()))
            }
        }
    }
}

@Composable
private fun ResumoSonoBox(resumo: ResumoSono) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CoresRelatorio.RoxoClaro, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Resumo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoresRelatorio.RoxoEscuro)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ItemResumo("Média", "${formatarHorasSono(resumo.media)}/dia", CoresRelatorio.RoxoEscuro)
            ItemResumo("Mínimo", formatarHorasSono(resumo.minimo), CoresRelatorio.Vermelho)
            ItemResumo("Máximo", formatarHorasSono(resumo.maximo), CoresRelatorio.Verde)
        }

        if (resumo.abaixo || resumo.acima) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CoresRelatorio.LaranjaClaro, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (resumo.abaixo) "⚠️ Abaixo do recomendado (7–9h)" else "⚠️ Acima do recomendado (7–9h)",
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CoresRelatorio.Laranja
                )
            }
        }
    }
}

@Composable
private fun ItemResumo(rotulo: String, valor: String, cor: Color) {
    Column {
        Text(rotulo, fontSize = 11.sp, color = CoresRelatorio.Cinza)
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cor)
    }
}
