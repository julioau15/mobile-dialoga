package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Consulta
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// Quadradinho roxo claro com o dia e o mês: "10 / Set."
@Composable
fun ChipDeData(dia: Dia, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .width(54.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CorPrimariaClara)
    ) {
        Text(
            text = dia.dia.toString(),
            color = CorPrimaria,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = AgendaCalendario.mesCurto(dia.mes),
            color = CorPrimaria,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Uma consulta em lista: [chip de data] + nome e horário + lixeira.
 * [mostrarData] liga o chip (usado em "Próximas consultas"); [onCancelar] null esconde a lixeira.
 */
@Composable
fun LinhaDeConsulta(
    consulta: Consulta,
    mostrarData: Boolean,
    onCancelar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (mostrarData) {
            ChipDeData(consulta.dia)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = consulta.titulo,
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = CorSecundaria,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = consulta.hora, color = CorSecundaria, fontSize = 12.sp)
            }
        }
        if (onCancelar != null) {
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Cancelar consulta",
                    tint = CorSecundaria
                )
            }
        }
    }
}
