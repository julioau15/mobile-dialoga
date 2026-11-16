package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta da tela de relatórios (mesmos valores do dashboard do paciente). */
internal object CoresRelatorio {
    val Fundo = Color(0xFFF7F4FC)
    val RoxoEscuro = Color(0xFF2D1B45)
    val RoxoClaro = Color(0xFFF3EDF9)
    val RoxoPeriodo = Color(0xFFE3D5F0)
    val Roxo = Color(0xFF7B4FA6)
    val Verde = Color(0xFF059669)
    val Vermelho = Color(0xFFDC2626)
    val Azul = Color(0xFF3B82F6)
    val Laranja = Color(0xFFD97706)
    val LaranjaClaro = Color(0xFFFFF7E0)
    val Divisor = Color(0xFFEFEAF6)
    val Cinza = Color(0xFF8A8A99)
    val Texto2 = Color(0xFF4A4458)
}

private val DIAS_SEMANA = listOf("D", "S", "T", "Q", "Q", "S", "S")

/**
 * Calendário do protótipo: toque no primeiro dia e depois no último para definir o período.
 * Pontos nos dias que têm registro de humor.
 */
@Composable
fun CalendarioPeriodo(
    mes: MesVisivel,
    inicioIso: String,
    fimIso: String?,
    diasComRegistro: Set<String>,
    onSelecionarDia: (String) -> Unit,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hoje = DatasRelatorio.hoje()
    val fim = fimIso ?: inicioIso

    Column(modifier = modifier.fillMaxWidth()) {
        // cabeçalho: < Agosto 2026 >
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mês anterior",
                tint = CoresRelatorio.Texto2,
                modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onMesAnterior)
            )
            Text(
                text = DatasRelatorio.tituloMes(mes),
                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CoresRelatorio.RoxoEscuro
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo mês",
                tint = CoresRelatorio.Texto2,
                modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onProximoMes)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth()) {
            DIAS_SEMANA.forEach {
                Text(
                    text = it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CoresRelatorio.Cinza
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // células: espaços vazios até o 1º dia + os dias do mês, em linhas de 7
        val vazios = DatasRelatorio.diaDaSemanaDoPrimeiro(mes)
        val total = DatasRelatorio.diasNoMes(mes)
        val celulas: List<Int?> = List(vazios) { null } + (1..total).toList()

        celulas.chunked(7).forEach { semana ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                for (i in 0 until 7) {
                    val dia = semana.getOrNull(i)
                    if (dia == null) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                        continue
                    }
                    val iso = DatasRelatorio.doMes(mes.ano, mes.mes, dia)
                    val ehExtremo = iso == inicioIso || (fimIso != null && iso == fim)
                    DiaCelula(
                        dia = dia,
                        noPeriodo = iso >= inicioIso && iso <= fim,
                        extremo = ehExtremo,
                        temRegistro = iso in diasComRegistro,
                        futuro = iso > hoje,
                        onClick = { onSelecionarDia(iso) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider(color = CoresRelatorio.Divisor, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

        Text(
            text = if (fimIso == null) DatasRelatorio.br(inicioIso)
            else "${DatasRelatorio.br(inicioIso)} – ${DatasRelatorio.br(fim)}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoresRelatorio.RoxoEscuro
        )
    }
}

@Composable
private fun DiaCelula(
    dia: Int,
    noPeriodo: Boolean,
    extremo: Boolean,
    temRegistro: Boolean,
    futuro: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fundo = when {
        extremo -> CoresRelatorio.Roxo
        noPeriodo -> CoresRelatorio.RoxoPeriodo
        else -> Color.Transparent
    }
    val corTexto = when {
        extremo -> Color.White
        futuro -> CoresRelatorio.Cinza.copy(alpha = 0.5f)
        else -> CoresRelatorio.RoxoEscuro
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(fundo)
            .clickable(enabled = !futuro, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dia.toString(), fontSize = 13.sp, color = corTexto,
                fontWeight = if (extremo) FontWeight.Bold else FontWeight.Medium
            )
            // ponto = existe registro de humor naquele dia
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !temRegistro -> Color.Transparent
                            extremo -> Color.White
                            else -> CoresRelatorio.Roxo
                        }
                    )
            )
        }
    }
}
