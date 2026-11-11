package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaRegras
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Consulta
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// Card "quarta-feira, 2 de setembro / 0 consulta(s)" com as consultas do dia selecionado
@Composable
fun CardDiaSelecionado(
    dia: Dia,
    consultasDoDia: List<Consulta>,
    onCancelar: (Consulta) -> Unit,
    modifier: Modifier = Modifier
) {
    CardAgenda(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = AgendaCalendario.tituloDiaSelecionado(dia),
                color = CorTexto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AgendaCalendario.quantidadeDeConsultas(consultasDoDia.size),
                color = CorSecundaria,
                fontSize = 12.sp
            )
        }
        HorizontalDivider(color = CorBordaCard)
        if (consultasDoDia.isEmpty()) {
            TextoVazio("Nenhuma consulta neste dia.")
        } else {
            consultasDoDia.forEachIndexed { indice, consulta ->
                if (indice > 0) HorizontalDivider(color = CorBordaCard)
                LinhaDeConsulta(
                    consulta = consulta,
                    mostrarData = false,
                    onCancelar = if (AgendaRegras.podeCancelar(consulta)) ({ onCancelar(consulta) }) else null
                )
            }
        }
    }
}

// Card "Próximas consultas": chip de data, nome, horário e lixeira
@Composable
fun CardProximasConsultas(
    consultas: List<Consulta>,
    onCancelar: (Consulta) -> Unit,
    modifier: Modifier = Modifier
) {
    CardAgenda(modifier = modifier) {
        Text(
            text = "Próximas consultas",
            color = CorTexto,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
        HorizontalDivider(color = CorBordaCard)
        if (consultas.isEmpty()) {
            TextoVazio("Você não tem consultas agendadas.")
        } else {
            consultas.forEachIndexed { indice, consulta ->
                if (indice > 0) HorizontalDivider(color = CorBordaCard)
                LinhaDeConsulta(
                    consulta = consulta,
                    mostrarData = true,
                    onCancelar = { onCancelar(consulta) }
                )
            }
        }
    }
}

@Composable
private fun TextoVazio(texto: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 16.dp)
    ) {
        Text(text = texto, color = CorSecundaria, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
