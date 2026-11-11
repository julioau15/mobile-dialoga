package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.coresCampo
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.RespostaVisualizacaoDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.TarefaDestinatarioProgressoDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.formatarDataBr
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.hojeIso
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import java.util.Calendar
import java.util.Locale

private val MESES = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)

/** Tela "Acompanhamento de respostas". */
@Composable
internal fun AcompanhamentoProfissional(
    estado: TarefasProfissionalUiState,
    onVoltar: () -> Unit,
    onBusca: (String) -> Unit,
    onPeriodo: (String) -> Unit,
    onDia: (String?) -> Unit,
    onMes: (String) -> Unit,
    onAno: (String?) -> Unit,
    onStatus: (String) -> Unit,
    onAbrirRespostas: (TarefaDestinatarioProgressoDto) -> Unit
) {
    val resultados = estado.acompanhamentosFiltrados()

    LinkVoltar("Voltar às tarefas", onVoltar)
    Spacer(Modifier.height(8.dp))
    TituloTela("Acompanhamento de respostas", "Acompanhe o preenchimento das tarefas por paciente")
    Spacer(Modifier.height(20.dp))

    OutlinedTextField(
        value = estado.busca,
        onValueChange = onBusca,
        placeholder = { Text("Buscar por nome da tarefa ou do paciente...", fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        shape = FormatoCampo,
        colors = coresCampo(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(18.dp))
    RotuloSecao("FILTRAR POR PERÍODO")
    LinhaChips(listOf("Todos", "Dia", "Mês", "Ano"), estado.periodo, onPeriodo)

    when (estado.periodo) {
        "Dia" -> {
            Spacer(Modifier.height(10.dp))
            SeletorDia(
                selecionado = estado.diaSelecionado,
                diasComTarefa = estado.acompanhamentos.mapNotNull { it.dataModificacaoIso() }.toSet(),
                onSelecionar = onDia
            )
        }
        "Mês" -> {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = estado.mesDigitos,
                onValueChange = onMes,
                placeholder = { Text("mm/aaaa", fontSize = 14.sp) },
                singleLine = true,
                visualTransformation = MesAnoTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.width(220.dp)
            )
        }
        "Ano" -> {
            Spacer(Modifier.height(10.dp))
            val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
            val anos = (estado.acompanhamentos.mapNotNull { it.dataModificacaoIso()?.take(4)?.toIntOrNull() } + anoAtual)
                .distinct().sortedDescending().map { it.toString() }
            CampoSelecao(
                texto = estado.anoSelecionado ?: "Selecionar ano",
                placeholder = estado.anoSelecionado == null,
                opcoes = anos,
                onSelecionar = onAno,
                altura = 48.dp,
                modifier = Modifier.width(220.dp)
            )
        }
    }

    Spacer(Modifier.height(18.dp))
    RotuloSecao("STATUS DA TAREFA")
    LinhaChips(listOf("Todos", "Em andamento", "Finalizado", "Não preenchido"), estado.statusFiltro, onStatus)

    Spacer(Modifier.height(16.dp))
    Text(text = "${resultados.size} resultado(s)", fontSize = 13.sp, color = CorSecundaria)
    Spacer(Modifier.height(12.dp))

    when {
        estado.isLoading && estado.acompanhamentos.isEmpty() ->
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
            }

        resultados.isEmpty() ->
            EstadoVazio("🔍", "Nenhum resultado encontrado", "Tente ajustar os filtros de busca.", comCartao = true)

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            resultados.forEach { d -> CartaoAcompanhamento(d) { onAbrirRespostas(d) } }
        }
    }
}

@Composable
private fun RotuloSecao(texto: String) {
    Text(
        text = texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CorSecundaria,
        letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun CartaoAcompanhamento(d: TarefaDestinatarioProgressoDto, onClick: () -> Unit) {
    CartaoBranco(padding = 16.dp, modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(d.nomePaciente, corDoAvatar(d.idPaciente))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = d.tituloTarefa, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(text = d.nomePaciente, fontSize = 13.sp, color = CorSecundaria, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "Última modificação: ${d.dataModificacaoIso().formatarDataBr()}",
                    fontSize = 12.sp, color = CorSecundaria, lineHeight = 16.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            SeloStatus(d.rotuloAcompanhamento())
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = CorSecundaria, modifier = Modifier.size(20.dp))
        }
    }
}

// ---------- Calendário (filtro "Dia") ----------

@Composable
private fun SeletorDia(selecionado: String?, diasComTarefa: Set<String>, onSelecionar: (String?) -> Unit) {
    var aberto by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
            .clickable { aberto = !aberto }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = "📅", fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (selecionado != null) selecionado.formatarDataBr() else "Selecionar dia",
            fontSize = 14.sp, color = CorTexto
        )
    }

    if (aberto) {
        Spacer(Modifier.height(8.dp))
        Calendario(
            selecionado = selecionado,
            diasComTarefa = diasComTarefa,
            onSelecionar = { onSelecionar(it); aberto = false }
        )
    }
}

@Composable
private fun Calendario(selecionado: String?, diasComTarefa: Set<String>, onSelecionar: (String) -> Unit) {
    val inicial = (selecionado ?: diasComTarefa.maxOrNull() ?: hojeIso())
    var ano by remember { mutableIntStateOf(inicial.substring(0, 4).toInt()) }
    var mes by remember { mutableIntStateOf(inicial.substring(5, 7).toInt()) } // 1..12

    val primeiroDiaSemana = Calendar.getInstance().apply { clear(); set(ano, mes - 1, 1) }.get(Calendar.DAY_OF_WEEK) - 1 // 0 = domingo
    val diasNoMes = Calendar.getInstance().apply { clear(); set(ano, mes - 1, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Default.KeyboardArrowLeft, contentDescription = "Mês anterior", tint = CorSecundaria,
                modifier = Modifier.size(28.dp).clip(CircleShape).clickable { if (mes == 1) { mes = 12; ano-- } else mes-- }
            )
            Text(
                text = "${MESES[mes - 1]} $ano", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowRight, contentDescription = "Próximo mês", tint = CorSecundaria,
                modifier = Modifier.size(28.dp).clip(CircleShape).clickable { if (mes == 12) { mes = 1; ano++ } else mes++ }
            )
        }
        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                Text(text = it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CorSecundaria, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))

        val celulas = primeiroDiaSemana + diasNoMes
        val linhas = (celulas + 6) / 7
        for (l in 0 until linhas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val dia = l * 7 + c - primeiroDiaSemana + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dia in 1..diasNoMes) {
                            val iso = String.format(Locale.US, "%04d-%02d-%02d", ano, mes, dia)
                            val ativo = iso == selecionado
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (ativo) CorPrimaria else Color.Transparent)
                                    .clickable { onSelecionar(iso) }
                                    .padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "$dia", fontSize = 13.sp,
                                    color = if (ativo) Color.White else CorTexto,
                                    fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal
                                )
                                if (iso in diasComTarefa) {
                                    Box(Modifier.padding(top = 1.dp).size(4.dp).clip(CircleShape).background(if (ativo) Color.White else CorPrimaria))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Digita só os números (mmaaaa) e mostra mm/aaaa. */
private object MesAnoTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val d = text.text.take(6)
        val saida = if (d.length > 2) d.substring(0, 2) + "/" + d.substring(2) else d
        val mapa = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset <= 2) offset else offset + 1
            override fun transformedToOriginal(offset: Int) = (if (offset <= 2) offset else offset - 1).coerceIn(0, d.length)
        }
        return TransformedText(AnnotatedString(saida), mapa)
    }
}

// ---------- Respostas de um paciente ----------

@Composable
internal fun RespostasPaciente(estado: TarefasProfissionalUiState, onVoltar: () -> Unit) {
    val d = estado.destinatarioSelecionado

    LinkVoltar("Voltar ao acompanhamento", onVoltar)
    Spacer(Modifier.height(8.dp))
    TituloTela("Respostas", if (d != null) "${d.nomePaciente} · ${d.tituloTarefa}" else "")
    Spacer(Modifier.height(20.dp))

    when {
        estado.isCarregandoRespostas ->
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
            }

        estado.erroRespostas != null -> CartaoErro(estado.erroRespostas)

        estado.respostas.isEmpty() ->
            EstadoVazio("🗒️", "Sem respostas", "O paciente ainda não respondeu esta tarefa.", comCartao = true)

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            estado.respostas.forEachIndexed { i, r -> CartaoResposta(i + 1, r) }
        }
    }
}

@Composable
private fun CartaoResposta(numero: Int, r: RespostaVisualizacaoDto) {
    val semResposta = r.respostaValor == "Sem resposta"
    CartaoBranco(padding = 16.dp) {
        Text(text = "$numero. ${r.enunciadoPergunta}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto, lineHeight = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = r.respostaValor, fontSize = 14.sp, lineHeight = 20.sp,
            fontWeight = if (semResposta) FontWeight.Normal else FontWeight.SemiBold,
            color = if (semResposta) CorSecundaria else CorPrimaria
        )
    }
}
