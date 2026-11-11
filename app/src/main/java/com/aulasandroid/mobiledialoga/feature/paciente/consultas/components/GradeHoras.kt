package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Consulta
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

private val LarguraRotuloHora = 48.dp
private val LarguraColunaDia = 46.dp
private val AlturaLinhaHora = 52.dp
private val AlturaGradeDia = 240.dp
private val AlturaGradeSemana = 360.dp

// Linhas finas da grade: à direita e/ou embaixo da célula
private fun Modifier.linhasDaGrade(direita: Boolean = false, embaixo: Boolean = true): Modifier =
    this.drawBehind {
        val espessura = 1.dp.toPx()
        if (embaixo) drawLine(CorBordaCard, Offset(0f, size.height), Offset(size.width, size.height), espessura)
        if (direita) drawLine(CorBordaCard, Offset(size.width, 0f), Offset(size.width, size.height), espessura)
    }

@Composable
private fun RotuloDaHora(hora: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(LarguraRotuloHora)
            .heightIn(min = AlturaLinhaHora)
            .linhasDaGrade(direita = true)
            .padding(top = 6.dp, start = 8.dp)
    ) {
        Text(text = AgendaCalendario.rotuloDaHora(hora), color = CorSecundaria, fontSize = 11.sp)
    }
}

// Bloco roxo claro de uma consulta dentro da grade
@Composable
private fun BlocoDaConsulta(consulta: Consulta, compacto: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CorPrimaria.copy(alpha = 0.16f))
            .padding(horizontal = if (compacto) 3.dp else 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (compacto) consulta.hora else "${consulta.hora} · ${consulta.titulo}",
            color = CorPrimaria,
            fontSize = if (compacto) 9.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (compacto) {
            Text(
                text = consulta.titulo,
                color = CorPrimaria,
                fontSize = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------- Visão "Dia" ----------

@Composable
fun GradeDia(
    dia: Dia,
    consultasDoDia: List<Consulta>,
    horas: IntRange,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Faixa com o dia por extenso e a quantidade de consultas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CorPrimariaClara)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = AgendaCalendario.tituloDiaCompleto(dia),
                color = CorTexto,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AgendaCalendario.quantidadeDeConsultas(consultasDoDia.size),
                color = CorSecundaria,
                fontSize = 11.sp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(AlturaGradeDia)
                .verticalScroll(rememberScrollState())
        ) {
            horas.forEach { hora ->
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    RotuloDaHora(hora, modifier = Modifier.fillMaxHeight())
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = AlturaLinhaHora)
                            .linhasDaGrade()
                    ) {
                        consultasDoDia.filter { it.horaCheia == hora }.forEach { BlocoDaConsulta(it, compacto = false) }
                    }
                }
            }
        }
    }
}

// ---------- Visão "Semana" (segunda a domingo; rola para o lado, com o cabeçalho junto) ----------

@Composable
fun GradeSemana(
    dias: List<Dia>,
    diaSelecionado: Dia,
    consultasDaSemana: List<Consulta>,
    horas: IntRange,
    onSelecionarDia: (Dia) -> Unit,
    modifier: Modifier = Modifier
) {
    // O mesmo estado de rolagem no cabeçalho e no corpo mantém os dois alinhados
    val rolagemHorizontal = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(LarguraRotuloHora))
            Row(modifier = Modifier.weight(1f).horizontalScroll(rolagemHorizontal)) {
                dias.forEachIndexed { indice, dia ->
                    CabecalhoDaColuna(
                        nome = AgendaCalendario.DIAS_CURTOS_SEGUNDA_PRIMEIRO[indice],
                        dia = dia,
                        selecionado = dia == diaSelecionado,
                        onClick = onSelecionarDia
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AlturaGradeSemana)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                horas.forEach { hora -> RotuloDaHora(hora) }
            }
            Column(modifier = Modifier.weight(1f).horizontalScroll(rolagemHorizontal)) {
                horas.forEach { hora ->
                    Row {
                        dias.forEach { dia ->
                            CelulaDaSemana(
                                selecionado = dia == diaSelecionado,
                                consultas = consultasDaSemana.filter { it.dia == dia && it.horaCheia == hora }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabecalhoDaColuna(
    nome: String,
    dia: Dia,
    selecionado: Boolean,
    onClick: (Dia) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(LarguraColunaDia)
            .background(if (selecionado) CorPrimariaClara else Color.Transparent)
            .clickable { onClick(dia) }
            .linhasDaGrade(direita = true)
            .padding(vertical = 6.dp)
    ) {
        Text(text = nome, color = CorSecundaria, fontSize = 10.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selecionado) CorPrimaria else Color.Transparent)
        ) {
            Text(
                text = dia.dia.toString(),
                color = if (selecionado) CorBranco else CorTexto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CelulaDaSemana(
    selecionado: Boolean,
    consultas: List<Consulta>,
    largura: Dp = LarguraColunaDia
) {
    Column(
        modifier = Modifier
            .width(largura)
            .height(AlturaLinhaHora)
            .clipToBounds()
            .background(if (selecionado) CorPrimariaClara.copy(alpha = 0.55f) else Color.Transparent)
            .linhasDaGrade(direita = true)
    ) {
        consultas.forEach { BlocoDaConsulta(it, compacto = true) }
    }
}
