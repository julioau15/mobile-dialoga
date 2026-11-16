package com.aulasandroid.mobiledialoga.feature.profissional.consultas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.CardAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.AbaAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.ConsultaProfissional
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

private val CorFundoAbas = Color(0xFFE9E3F2)
private val CorDiaDisponivel = Color(0xFFDDF3E8)

// ---------------------------------------------------------------------------
// Topo: título + "Nova consulta" e as abas "Eventos | Disponibilidade"
// ---------------------------------------------------------------------------

@Composable
fun CabecalhoAgendaProfissional(onNovaConsulta: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Agenda", color = CorTexto, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "Gerencie seus atendimentos", color = CorSecundaria, fontSize = 13.sp)
        }
        Button(
            onClick = onNovaConsulta,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Nova consulta", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun AbasAgenda(aba: AbaAgenda, onSelecionar: (AbaAgenda) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CorFundoAbas)
            .padding(3.dp)
    ) {
        ItemDaAba("Eventos", Icons.Outlined.CalendarToday, aba == AbaAgenda.EVENTOS, Modifier.weight(1f)) {
            onSelecionar(AbaAgenda.EVENTOS)
        }
        ItemDaAba("Disponibilidade", Icons.Outlined.Schedule, aba == AbaAgenda.DISPONIBILIDADE, Modifier.weight(1f)) {
            onSelecionar(AbaAgenda.DISPONIBILIDADE)
        }
    }
}

@Composable
private fun ItemDaAba(
    rotulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    selecionada: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selecionada) CorBranco else Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = if (selecionada) CorPrimaria else CorSecundaria,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = rotulo,
            color = if (selecionada) CorPrimaria else CorSecundaria,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Calendário (‹ Setembro 2026 ›). Eventos: bolinha vermelha nos dias com consulta.
// Disponibilidade: dias em que o profissional atende ficam verdes.
// ---------------------------------------------------------------------------

private val LETRAS_DA_SEMANA = listOf("D", "S", "T", "Q", "Q", "S", "S")

@Composable
fun CalendarioAgendaProfissional(
    mesVisivel: Dia,
    diaSelecionado: Dia?,
    onSelecionarDia: (Dia) -> Unit,
    onMudarMes: (Int) -> Unit,
    modifier: Modifier = Modifier,
    diasComConsulta: Set<Dia> = emptySet(),
    diasDisponiveis: Set<Dia> = emptySet(),
    legenda: String? = null
) {
    val semanas = remember(mesVisivel.ano, mesVisivel.mes) {
        AgendaCalendario.gradeDoMes(mesVisivel.ano, mesVisivel.mes)
    }

    CardAgenda(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SetaDoMes("‹", "Mês anterior") { onMudarMes(-1) }
                Text(
                    text = AgendaCalendario.tituloMes(mesVisivel),
                    color = CorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                SetaDoMes("›", "Próximo mês") { onMudarMes(1) }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                LETRAS_DA_SEMANA.forEach { letra ->
                    Text(
                        text = letra,
                        color = CorSecundaria,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            semanas.forEach { semana ->
                Row(modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    semana.forEach { dia ->
                        CelulaDoDia(
                            dia = dia,
                            selecionado = dia != null && dia == diaSelecionado,
                            disponivel = dia != null && dia in diasDisponiveis,
                            temConsulta = dia != null && dia in diasComConsulta,
                            onClick = onSelecionarDia,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (legenda != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CorDiaDisponivel)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = legenda, color = CorSecundaria, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SetaDoMes(simbolo: String, descricao: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = descricao, onClick = onClick)
    ) {
        Text(text = simbolo, color = CorSecundaria, fontSize = 24.sp)
    }
}

@Composable
private fun CelulaDoDia(
    dia: Dia?,
    selecionado: Boolean,
    disponivel: Boolean,
    temConsulta: Boolean,
    onClick: (Dia) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxHeight()
    ) {
        // Dias de fora do mês ficam em branco
        if (dia == null) return@Column

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(40.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        selecionado -> CorPrimaria
                        disponivel -> CorDiaDisponivel
                        else -> Color.Transparent
                    }
                )
                .clickable { onClick(dia) }
        ) {
            Text(
                text = dia.dia.toString(),
                color = when {
                    selecionado -> CorBranco
                    disponivel -> CorSucesso
                    else -> CorTexto
                },
                fontSize = 14.sp,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        if (temConsulta) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(CorErro)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cards de consulta
// ---------------------------------------------------------------------------

// Consulta do dia selecionado: borda roxa e barra lateral, com o horário em destaque
@Composable
fun CardConsultaDoDia(consulta: ConsultaProfissional, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CorBranco,
        border = BorderStroke(1.dp, CorPrimaria),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(CorPrimaria)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CorPrimariaClara)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = consulta.hora, color = CorPrimaria, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = consulta.paciente,
                        color = CorTexto,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = consulta.subtitulo, color = CorSecundaria, fontSize = 12.sp, fontStyle = FontStyle.Normal)
                }
            }
        }
    }
}

// "Próximos atendimentos": chip com a data, nome e "🕒 09:00 · Psicologia Clínica"
@Composable
fun CardProximoAtendimento(consulta: ConsultaProfissional, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CorBranco,
        border = BorderStroke(1.dp, CorBordaCard),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CorPrimariaClara)
                    .padding(vertical = 6.dp)
            ) {
                Text(text = consulta.dia.dia.toString(), color = CorPrimaria, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = AgendaCalendario.mesCurto(consulta.dia.mes), color = CorPrimaria, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = consulta.paciente,
                    color = CorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = CorSecundaria,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${consulta.hora} · ${consulta.subtitulo}",
                        color = CorSecundaria,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Mensagem dentro de um card branco ("Nenhuma consulta neste dia.")
@Composable
fun CardMensagemAgenda(texto: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CorBranco,
        border = BorderStroke(1.dp, CorBordaCard),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp)) {
            Text(text = texto, color = CorSecundaria, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
