package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// Visão "Mês": Dom a Sáb, o dia selecionado em roxo e uma bolinha nos dias que têm consulta
@Composable
fun CalendarioMes(
    diaSelecionado: Dia,
    diasComConsulta: Set<Dia>,
    onSelecionarDia: (Dia) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanas = remember(diaSelecionado.ano, diaSelecionado.mes) {
        AgendaCalendario.gradeDoMes(diaSelecionado.ano, diaSelecionado.mes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AgendaCalendario.DIAS_CURTOS_DOMINGO_PRIMEIRO.forEach { nome ->
                Text(
                    text = nome,
                    color = CorSecundaria,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        semanas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                semana.forEach { dia ->
                    CelulaDoDia(
                        dia = dia,
                        selecionado = dia != null && dia == diaSelecionado,
                        temConsulta = dia != null && dia in diasComConsulta,
                        onClick = onSelecionarDia,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CelulaDoDia(
    dia: Dia?,
    selecionado: Boolean,
    temConsulta: Boolean,
    onClick: (Dia) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxHeight()
    ) {
        // Dias de fora do mês ficam em branco (como no protótipo)
        if (dia == null) return@Column

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(42.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selecionado) CorPrimaria else Color.Transparent)
                .clickable { onClick(dia) }
        ) {
            Text(
                text = dia.dia.toString(),
                color = if (selecionado) CorBranco else CorTexto,
                fontSize = 15.sp,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        if (temConsulta) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(CorPrimaria)
            )
        }
    }
}
