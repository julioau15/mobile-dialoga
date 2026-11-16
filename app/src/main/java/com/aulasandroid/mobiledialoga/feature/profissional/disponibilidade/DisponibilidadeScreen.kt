package com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CalendarioAgendaProfissional
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.components.CardMensagemAgenda
import com.aulasandroid.mobiledialoga.feature.vinculos.components.ChipStatus
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloTexto
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeTexto
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.components.CampoDataCalendario
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.DialogaDialogo
import com.aulasandroid.mobiledialoga.components.DialogaOutlinedButton
import com.aulasandroid.mobiledialoga.components.MensagemErroCampo
import com.aulasandroid.mobiledialoga.components.diaParaBr
import com.aulasandroid.mobiledialoga.components.isoParaDia
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.vinculos.components.listaEm
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mensagemErroHttp
import com.aulasandroid.mobiledialoga.feature.vinculos.components.num
import com.aulasandroid.mobiledialoga.feature.vinculos.components.txt
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

// Tela de Gerenciamento de Disponibilidade do Profissional.
// Cada horário de atendimento vira consultas de 1 hora para o paciente escolher (o back divide a janela em horas cheias).

private val NOMES_DIAS = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb") // ids 1..7 do banco

// 06:00, 06:30 ... 22:00
private val OPCOES_HORA = (6..22).flatMap { h -> listOf("%02d:00".format(h), "%02d:30".format(h)) }.dropLast(1)

data class DisponibilidadeUi(
    val id: Int,
    val inicio: String,
    val fim: String,
    val tipo: String,          // semanal | unica | periodo
    val dataInicial: String?,
    val dataFinal: String?,
    val dias: List<Int>        // 1 = Domingo ... 7 = Sábado
) {
    val resumo: String
        get() {
            val horario = "$inicio às $fim"
            val diasTxt = dias.sorted().joinToString(", ") { NOMES_DIAS.getOrElse(it - 1) { "?" } }
            fun br(iso: String?) = isoParaDia(iso)?.let { diaParaBr(it) } ?: "?"
            return when (tipo) {
                "unica" -> "${br(dataInicial)} • $horario"
                "periodo" -> "$diasTxt • de ${br(dataInicial)} a ${br(dataFinal)} • $horario"
                else -> "Toda semana: $diasTxt • $horario"
            }
        }
}

data class DisponibilidadeState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val itens: List<DisponibilidadeUi> = emptyList(),
    val salvando: Boolean = false,
    val aviso: String? = null,
    val avisoErro: Boolean = false
)

class DisponibilidadeViewModel : ViewModel() {

    var state by mutableStateOf(DisponibilidadeState())
        private set

    private class Resp(val conectou: Boolean, val codigo: Int, val corpo: JsonObject?, val mensagem: String?) {
        val ok: Boolean get() = conectou && codigo in 200..299
    }

    private suspend fun chamar(bloco: () -> Call<JsonObject>): Resp = try {
        val r = bloco().awaitResponse()
        val mensagem = if (r.isSuccessful) null else try {
            val texto = r.errorBody()?.string()
            val json = Gson().fromJson(texto, JsonObject::class.java)
            val msg = json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString
            val campo = json?.get("field")?.takeIf { it.isJsonPrimitive }?.asString
            if (!msg.isNullOrBlank()) msg else campo
        } catch (_: Exception) {
            null
        }
        Resp(true, r.code(), if (r.isSuccessful) r.body() else null, mensagem)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Resp(false, 0, null, null)
    }

    fun limparAviso() {
        state = state.copy(aviso = null)
    }

    fun carregar(token: String) {
        state = state.copy(carregando = true, erro = null)
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getProfissionalService().listarDisponibilidadesMe(token) }
            // 404 = ainda não cadastrou nenhum horário: não é erro
            if (!resp.conectou || (resp.codigo !in 200..299 && resp.codigo != 404)) {
                state = state.copy(carregando = false, erro = resp.mensagem ?: mensagemErroHttp(resp.codigo, !resp.conectou, "carregar seus horários"))
                return@launch
            }
            val itens = resp.corpo.listaEm("disponibilidade").mapNotNull { el ->
                try {
                    val o = el.asJsonObject
                    val id = o.num("id")
                    if (id <= 0) return@mapNotNull null
                    val dias = (o.get("dias_da_semana")?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray())
                        .mapNotNull { d -> if (d.isJsonObject) d.asJsonObject.num("id").takeIf { it > 0 } else null }
                    DisponibilidadeUi(
                        id = id,
                        inicio = (o.txt("hora_inicio") ?: "").take(5),
                        fim = (o.txt("hora_fim") ?: "").take(5),
                        tipo = (o.txt("tipo_repeticao") ?: "semanal").lowercase(),
                        dataInicial = o.txt("data_inicial")?.take(10),
                        dataFinal = o.txt("data_final")?.take(10),
                        dias = dias
                    )
                } catch (_: Exception) {
                    null
                }
            }.sortedWith(compareBy({ it.tipo }, { it.inicio }))
            state = state.copy(carregando = false, itens = itens)
        }
    }

    fun salvar(token: String, corpo: JsonObject, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getProfissionalService().criarDisponibilidadeMe(token, corpo) }
            if (resp.ok) {
                state = state.copy(salvando = false, aviso = "Horário de atendimento salvo.", avisoErro = false)
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    aviso = resp.mensagem ?: mensagemErroHttp(resp.codigo, !resp.conectou, "salvar o horário"),
                    avisoErro = true
                )
            }
        }
    }

    fun excluir(token: String, id: Int) {
        if (state.salvando) return
        state = state.copy(salvando = true)
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getProfissionalService().excluirDisponibilidade(token, id) }
            if (resp.ok) {
                state = state.copy(salvando = false, aviso = "Horário removido.", avisoErro = false)
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    aviso = resp.mensagem ?: mensagemErroHttp(resp.codigo, !resp.conectou, "remover o horário"),
                    avisoErro = true
                )
            }
        }
    }
}

/**
 * Aba "Disponibilidade" da Agenda (protótipo): calendário com os dias em que o profissional atende (verdes) e a
 * lista de horários, com "Adicionar" e a lixeira para remover.
 */
@Composable
fun DisponibilidadeAba(
    token: String?,
    viewModel: DisponibilidadeViewModel,
    modifier: Modifier = Modifier
) {
    val estado = viewModel.state
    var adicionando by remember { mutableStateOf(false) }
    var excluir by remember { mutableStateOf<DisponibilidadeUi?>(null) }
    var mesVisivel by remember { mutableStateOf(Dia.hoje()) }
    var diaSelecionado by remember { mutableStateOf(Dia.hoje()) }

    LaunchedEffect(token) { if (!token.isNullOrBlank()) viewModel.carregar(token) }

    val diasDisponiveis = remember(estado.itens, mesVisivel.ano, mesVisivel.mes) {
        diasComDisponibilidade(estado.itens, mesVisivel.ano, mesVisivel.mes)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.fillMaxWidth()) {
        CalendarioAgendaProfissional(
            mesVisivel = mesVisivel,
            diaSelecionado = diaSelecionado,
            diasDisponiveis = diasDisponiveis,
            legenda = "Dias com disponibilidade",
            onSelecionarDia = { diaSelecionado = it },
            onMudarMes = { mesVisivel = mesVisivel.maisMeses(it) }
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Disponibilidade semanal", color = CorTexto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Horários recorrentes disponíveis para agendamentos",
                    color = CorSecundaria, fontSize = 12.sp
                )
            }
            Button(
                onClick = { viewModel.limparAviso(); adicionando = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        when {
            estado.carregando -> CardMensagemAgenda("Carregando...")
            estado.erro != null -> CardMensagemAgenda(estado.erro)
            estado.itens.isEmpty() -> CardMensagemAgenda(
                "Você ainda não cadastrou horários. Sem eles, nenhum paciente consegue marcar consulta com você."
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                estado.itens.forEach { item ->
                    CardDisponibilidade(item = item, removendo = estado.salvando, onRemover = { excluir = item })
                }
            }
        }
    }

    if (adicionando && !token.isNullOrBlank()) {
        NovoHorarioDialogo(
            salvando = estado.salvando,
            erroServidor = if (estado.avisoErro) estado.aviso else null,
            onSalvar = { corpo -> viewModel.salvar(token, corpo) { adicionando = false } },
            onFechar = { if (!estado.salvando) adicionando = false }
        )
    }

    excluir?.let { alvo ->
        DialogaDialogo(
            titulo = "Remover horário",
            onFechar = { excluir = null },
            rodape = {
                DialogaOutlinedButton(texto = "Cancelar", onClick = { excluir = null })
                DialogaButton(texto = "Remover", onClick = {
                    token?.let { viewModel.excluir(it, alvo.id) }
                    excluir = null
                })
            }
        ) {
            Text(alvo.resumo, color = CorTexto, fontSize = 14.sp)
            Text(
                "Consultas que já estão marcadas não são apagadas.",
                color = CorSecundaria, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// Um horário cadastrado: chip do dia ("Seg"), "08:00 – 12:00", selo (Semanal / Única vez / Período) e lixeira
@Composable
private fun CardDisponibilidade(item: DisponibilidadeUi, removendo: Boolean, onRemover: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CorBranco,
        border = BorderStroke(1.dp, CorBordaCard),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(56.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CorPrimariaClara)
            ) {
                Text(item.rotuloDoDia(), color = CorPrimaria, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${item.inicio} – ${item.fim}", color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.tipo) {
                        "unica" -> ChipStatus("Única vez", CorChipAmareloFundo, CorChipAmareloTexto)
                        "periodo" -> ChipStatus("Período", CorPrimariaClara, CorPrimaria)
                        else -> ChipStatus("Semanal", CorChipVerdeFundo, CorChipVerdeTexto)
                    }
                    val detalhe = item.detalhe()
                    if (detalhe.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(detalhe, color = CorSecundaria, fontSize = 12.sp, maxLines = 2)
                    }
                }
            }
            IconButton(onClick = onRemover, enabled = !removendo) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remover horário", tint = CorSecundaria)
            }
        }
    }
}

// Dia da semana no chip: "Seg"; com vários dias, o primeiro e quantos mais ("Seg +2")
private fun DisponibilidadeUi.rotuloDoDia(): String {
    if (tipo == "unica") {
        return isoParaDia(dataInicial)?.let { NOMES_DIAS[it.diaDaSemana()] } ?: "—"
    }
    val ordenados = dias.sorted()
    val primeiro = ordenados.firstOrNull()?.let { NOMES_DIAS.getOrNull(it - 1) } ?: "—"
    return if (ordenados.size > 1) "$primeiro +${ordenados.size - 1}" else primeiro
}

// Texto ao lado do selo: dias (se mais de um), a data da "Única vez" ou o intervalo do "Período"
private fun DisponibilidadeUi.detalhe(): String {
    fun br(iso: String?) = isoParaDia(iso)?.let { diaParaBr(it) } ?: "?"
    val diasTexto = dias.sorted().joinToString(", ") { NOMES_DIAS.getOrElse(it - 1) { "?" } }
    return when (tipo) {
        "unica" -> br(dataInicial)
        "periodo" -> "$diasTexto • ${br(dataInicial)} a ${br(dataFinal)}"
        else -> if (dias.size > 1) diasTexto else ""
    }
}

fun temDisponibilidade(dia: Dia, item: DisponibilidadeUi): Boolean {
    val idDoDia = dia.diaDaSemana() + 1 // 1 = Domingo ... 7 = Sábado, como no banco
    val iso = dia.paraIso()
    return when (item.tipo) {
        "unica" -> item.dataInicial == iso
        "periodo" -> idDoDia in item.dias &&
                (item.dataInicial.isNullOrEmpty() || iso >= item.dataInicial) &&
                (item.dataFinal.isNullOrEmpty() || iso <= item.dataFinal)
        else -> idDoDia in item.dias &&
                (item.dataInicial.isNullOrEmpty() || iso >= item.dataInicial) &&
                (item.dataFinal.isNullOrEmpty() || iso <= item.dataFinal)
    }
}

fun temDisponibilidade(dia: Dia, disponibilidades: List<DisponibilidadeUi>): Boolean =
    disponibilidades.any { temDisponibilidade(dia, it) }

fun horariosDoDia(dia: Dia, disponibilidades: List<DisponibilidadeUi>): List<String> {
    val resultado = mutableSetOf<String>()
    val itensDoDia = disponibilidades.filter { temDisponibilidade(dia, it) }
    for (item in itensDoDia) {
        val minInicio = horaParaMinutos(item.inicio) ?: continue
        val minFim = horaParaMinutos(item.fim) ?: continue
        var m = minInicio
        while (m <= minFim) {
            resultado.add(minutosParaHora(m))
            m += 30
        }
    }
    return resultado.sorted()
}

private fun horaParaMinutos(hora: String): Int? {
    val partes = hora.split(":")
    if (partes.size < 2) return null
    val h = partes[0].toIntOrNull() ?: return null
    val m = partes[1].toIntOrNull() ?: return null
    return h * 60 + m
}

private fun minutosParaHora(minutos: Int): String {
    val h = minutos / 60
    val m = minutos % 60
    return String.format(java.util.Locale.US, "%02d:%02d", h, m)
}

// Dias do mês em que o profissional atende (para pintar o calendário de verde)
fun diasComDisponibilidade(itens: List<DisponibilidadeUi>, ano: Int, mes: Int): Set<Dia> {
    val resultado = mutableSetOf<Dia>()
    for (numero in 1..Dia.diasNoMes(ano, mes)) {
        val dia = Dia(ano, mes, numero)
        if (temDisponibilidade(dia, itens)) resultado.add(dia)
    }
    return resultado
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun NovoHorarioDialogo(
    salvando: Boolean,
    erroServidor: String?,
    onSalvar: (JsonObject) -> Unit,
    onFechar: () -> Unit
) {
    var tipo by remember { mutableStateOf("semanal") }
    var dias by remember { mutableStateOf(setOf<Int>()) }
    var inicio by remember { mutableStateOf("08:00") }
    var fim by remember { mutableStateOf("12:00") }
    var dataInicial by remember { mutableStateOf<String?>(null) }
    var dataFinal by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    fun validar(): String? = when {
        fim <= inicio -> "O horário final precisa ser depois do inicial."
        tipo != "unica" && dias.isEmpty() -> "Marque pelo menos um dia da semana."
        tipo != "semanal" && dataInicial == null -> "Escolha a data${if (tipo == "periodo") " inicial" else ""}."
        tipo == "periodo" && (dataFinal == null || dataFinal!! < dataInicial!!) -> "A data final precisa ser igual ou depois da inicial."
        else -> null
    }

    DialogaDialogo(
        titulo = "Novo horário de atendimento",
        onFechar = onFechar,
        rodape = {
            DialogaOutlinedButton(texto = "Cancelar", onClick = onFechar, corConteudo = CorTexto)
            DialogaButton(
                texto = if (salvando) "Salvando..." else "Salvar",
                enabled = !salvando,
                onClick = {
                    erro = validar()
                    if (erro != null) return@DialogaButton
                    val corpo = JsonObject().apply {
                        addProperty("hora_inicio", inicio)
                        addProperty("hora_fim", fim)
                        addProperty("tipo_repeticao", tipo)
                        if (tipo != "semanal") addProperty("data_inicial", dataInicial)
                        if (tipo == "periodo") addProperty("data_final", dataFinal)
                        if (tipo != "unica") add("dias_da_semana", JsonArray().also { a -> dias.sorted().forEach { a.add(it) } })
                    }
                    onSalvar(corpo)
                }
            )
        }
    ) {
        Text("Repetição", color = CorTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            listOf("semanal" to "Toda semana", "unica" to "Data única", "periodo" to "Período").forEach { (valor, rotulo) ->
                FilterChip(selected = tipo == valor, onClick = { tipo = valor; erro = null }, label = { Text(rotulo, fontSize = 12.sp) })
            }
        }

        if (tipo != "unica") {
            Text("Dias da semana", color = CorTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                NOMES_DIAS.forEachIndexed { i, nome ->
                    val id = i + 1
                    FilterChip(
                        selected = id in dias,
                        onClick = { dias = if (id in dias) dias - id else dias + id; erro = null },
                        label = { Text(nome, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (tipo == "unica") {
            CampoDataCalendario(prazoIso = dataInicial, onPrazoChange = { dataInicial = it }, rotulo = "Data")
        }
        if (tipo == "periodo") {
            CampoDataCalendario(prazoIso = dataInicial, onPrazoChange = { dataInicial = it }, rotulo = "De")
            Spacer(modifier = Modifier.height(8.dp))
            CampoDataCalendario(prazoIso = dataFinal, onPrazoChange = { dataFinal = it }, rotulo = "Até")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SeletorHora("Das", inicio, { inicio = it }, Modifier.weight(1f))
            SeletorHora("Até as", fim, { fim = it }, Modifier.weight(1f))
        }

        MensagemErroCampo(erro ?: erroServidor)
    }
}

@Composable
private fun SeletorHora(rotulo: String, valor: String, onEscolher: (String) -> Unit, modifier: Modifier = Modifier) {
    var aberto by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(rotulo, color = CorTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        androidx.compose.foundation.layout.Box {
            DialogaOutlinedButton(texto = valor, onClick = { aberto = true }, corConteudo = CorTexto, modifier = Modifier.fillMaxWidth())
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                OPCOES_HORA.forEach { h ->
                    DropdownMenuItem(text = { Text(h) }, onClick = { onEscolher(h); aberto = false })
                }
            }
        }
    }
}
