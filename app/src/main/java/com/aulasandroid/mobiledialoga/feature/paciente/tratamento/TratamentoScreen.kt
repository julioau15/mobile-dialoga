package com.aulasandroid.mobiledialoga.feature.paciente.tratamento

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria

private object Cores {
    val Fundo = Color(0xFFF7F4FC)
    val Escuro = Color(0xFF2D1B45)
    val Claro = Color(0xFFF3EDF9)
    val Chip = Color(0xFFE9E1F4)
    val Roxo = Color(0xFF7B4FA6)
    val Verde = Color(0xFF059669)
    val Vermelho = Color(0xFFDC2626)
    val Cinza = Color(0xFF8A8A99)
    val Texto2 = Color(0xFF4A4458)
    val Borda = Color(0xFFE4DFED)
    val Divisor = Color(0xFFEFEAF6)
}

@Composable
fun TratamentoScreen(
    modifier: Modifier = Modifier,
    viewModel: TratamentoViewModel = viewModel()
) {
    val authState = useAuth()
    val context = LocalContext.current
    val state = viewModel.state

    // null = fechado; editandoId == null com formAberto = novo
    var formAberto by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<TratamentoUi?>(null) }
    var excluir by remember { mutableStateOf<TratamentoUi?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar(authState) }

    LaunchedEffect(state.aviso) {
        state.aviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limparAviso()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cores.Fundo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.carregando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorPrimaria)
        }

        // ------------------------------------------------ cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Tratamentos", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Cores.Escuro)
                Text(
                    when (val n = state.tratamentos.size) {
                        0 -> "Nenhum tratamento cadastrado"
                        1 -> "1 tratamento cadastrado"
                        else -> "$n tratamentos cadastrados"
                    },
                    fontSize = 12.sp, color = Cores.Cinza
                )
            }
            Button(
                onClick = { editando = null; formAberto = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Novo tratamento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        state.erro?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(it, color = Cores.Vermelho, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    "Tentar de novo", color = CorPrimaria, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.carregar(authState) }.padding(8.dp)
                )
            }
        }

        // ------------------------------------------------ lista
        if (state.tratamentos.isEmpty() && !state.carregando && state.erro == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💊", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Você ainda não cadastrou tratamentos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Cores.Escuro)
                    Text(
                        "Toque em \"Novo tratamento\" para adicionar seu primeiro medicamento.",
                        fontSize = 12.sp, color = Cores.Cinza
                    )
                }
            }
        }

        state.tratamentos.forEach { t ->
            TratamentoCard(
                t = t,
                excluindo = state.excluindoId == t.id,
                onEditar = { editando = t; formAberto = true },
                onExcluir = { excluir = t }
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    if (formAberto) {
        FormularioTratamento(
            editando = editando,
            salvando = state.salvando,
            onFechar = { formAberto = false },
            onSalvar = { form, aoConcluir ->
                viewModel.salvar(authState, editando?.id, form) { aoConcluir() }
            },
            onFechouAposSalvar = { formAberto = false }
        )
    }

    excluir?.let { t ->
        AlertDialog(
            onDismissRequest = { excluir = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Excluir tratamento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cores.Escuro) },
            text = {
                Text(
                    "Tem certeza que deseja excluir este tratamento? Esta ação não pode ser desfeita.",
                    fontSize = 13.sp, color = Cores.Texto2
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.excluir(authState, t.id); excluir = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cores.Vermelho)
                ) { Text("Excluir", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { excluir = null },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Cores.Borda)
                ) { Text("Cancelar", color = Cores.Escuro, fontWeight = FontWeight.SemiBold) }
            }
        )
    }
}

// ---------------------------------------------------------------- card

@Composable
private fun TratamentoCard(
    t: TratamentoUi,
    excluindo: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            t.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cores.Escuro,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (t.dosagem.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.background(Cores.Claro, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(t.dosagem, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Cores.Roxo)
                            }
                        }
                    }
                    if (t.frequencia.isNotBlank()) {
                        Text(t.frequencia, fontSize = 13.sp, color = Cores.Cinza, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (excluindo) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CorPrimaria)
                } else {
                    Icon(
                        Icons.Default.Edit, "Editar ${t.nome}", tint = Cores.Cinza,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onEditar).padding(7.dp)
                    )
                    Icon(
                        Icons.Default.Delete, "Excluir ${t.nome}", tint = Cores.Cinza,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onExcluir).padding(7.dp)
                    )
                }
            }

            if (t.horarios.isNotEmpty() || t.lembreteAtivo) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    t.horarios.forEach { h -> ChipHorario(h) }
                    if (t.lembreteAtivo) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = Cores.Verde, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Lembrete ativo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Cores.Verde)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipHorario(hora: String) {
    Row(
        modifier = Modifier.background(Cores.Chip, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AccessTime, null, tint = Cores.Texto2, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(hora, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Cores.Escuro)
    }
}

// ---------------------------------------------------------------- formulário

/**
 * Novo tratamento / Editar tratamento. Em "novo" há o botão "Adicionar outro": salva e limpa o formulário
 * sem fechar. [onSalvar] recebe o formulário e um callback que é chamado quando o back confirma.
 */
@Composable
private fun FormularioTratamento(
    editando: TratamentoUi?,
    salvando: Boolean,
    onFechar: () -> Unit,
    onSalvar: (FormTratamento, () -> Unit) -> Unit,
    onFechouAposSalvar: () -> Unit
) {
    val context = LocalContext.current
    var form by remember { mutableStateOf(editando?.let(FormTratamento::de) ?: FormTratamento()) }
    var tentouSalvar by remember { mutableStateOf(false) }
    var menuAberto by remember { mutableStateOf(false) }

    val nomeInvalido = form.nome.isBlank()
    val dosagemInvalida = form.dosagem.isBlank()
    val horarioFaltando = form.horarios.any { it == null }
    val valido = !nomeInvalido && !dosagemInvalida && !horarioFaltando

    fun enviar(adicionarOutro: Boolean) {
        tentouSalvar = true
        if (!valido) return
        onSalvar(form) {
            if (adicionarOutro) {
                form = FormTratamento()
                tentouSalvar = false
            } else {
                onFechouAposSalvar()
            }
        }
    }

    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (editando == null) "Novo tratamento" else "Editar tratamento",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Cores.Escuro
                    )
                    Icon(
                        Icons.Default.Close, "Fechar", tint = Cores.Texto2,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onFechar).padding(6.dp)
                    )
                }
                HorizontalDivider(color = Cores.Divisor)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoTexto(
                        rotulo = "Nome do medicamento / tratamento", obrigatorio = true,
                        valor = form.nome, dica = "Ex: Fluoxetina, Terapia cognitivo-comportamental",
                        erro = tentouSalvar && nomeInvalido,
                        onChange = { form = form.copy(nome = it.take(150)) }
                    )
                    CampoTexto(
                        rotulo = "Dosagem", obrigatorio = true,
                        valor = form.dosagem, dica = "Ex: 50mg, 1 comprimido",
                        erro = tentouSalvar && dosagemInvalida,
                        onChange = { form = form.copy(dosagem = it.take(150)) }
                    )

                    // frequência
                    Column {
                        Text("Frequência", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cores.Escuro)
                        Spacer(Modifier.height(6.dp))
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .border(1.dp, Cores.Borda, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { menuAberto = true }
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(form.frequencia.rotulo, fontSize = 14.sp, color = Cores.Escuro)
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Cores.Texto2)
                            }
                            DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                                FrequenciaTratamento.values().forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f.rotulo, fontSize = 14.sp) },
                                        onClick = { form = form.comFrequencia(f); menuAberto = false }
                                    )
                                }
                            }
                        }
                    }

                    // horários (um por dose)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = Cores.Texto2, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (form.frequencia.doses == 1) "Horário" else "Horários",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cores.Escuro
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            form.horarios.forEachIndexed { i, h ->
                                val invalido = tentouSalvar && h == null
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, if (invalido) Cores.Vermelho else Cores.Borda, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            val (h0, m0) = h?.split(":")?.let { it[0].toInt() to it[1].toInt() } ?: (8 to 0)
                                            TimePickerDialog(context, { _, hh, mm ->
                                                val nova = "%02d:%02d".format(hh, mm)
                                                form = form.copy(horarios = form.horarios.toMutableList().also { it[i] = nova })
                                            }, h0, m0, true).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        h ?: "--:--", fontSize = 14.sp,
                                        fontWeight = if (h != null) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (h != null) Cores.Escuro else Cores.Cinza
                                    )
                                }
                            }
                        }
                        if (tentouSalvar && horarioFaltando) {
                            Text("Escolha o horário de cada dose.", fontSize = 11.sp, color = Cores.Vermelho, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    // lembretes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = form.lembreteAtivo,
                            onCheckedChange = { form = form.copy(lembreteAtivo = it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = CorPrimaria)
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Default.Notifications, null, tint = Cores.Texto2, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ativar lembretes e notificações", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cores.Escuro)
                    }
                }

                HorizontalDivider(color = Cores.Divisor)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editando == null) {
                        OutlinedButton(
                            onClick = { enviar(adicionarOutro = true) },
                            enabled = !salvando,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Cores.Borda),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) { Text("Adicionar outro", fontSize = 13.sp, color = Cores.Escuro, fontWeight = FontWeight.SemiBold) }
                    }
                    Button(
                        onClick = { enviar(adicionarOutro = false) },
                        enabled = !salvando,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = Color.White)
                    ) {
                        if (salvando) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        else Text("Salvar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoTexto(
    rotulo: String,
    obrigatorio: Boolean,
    valor: String,
    dica: String,
    erro: Boolean,
    onChange: (String) -> Unit
) {
    Column {
        Row {
            Text(rotulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cores.Escuro)
            if (obrigatorio) Text(" *", fontSize = 13.sp, color = Cores.Vermelho)
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(dica, fontSize = 13.sp, color = Cores.Cinza) },
            singleLine = true,
            isError = erro,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Cores.Escuro,
                unfocusedTextColor = Cores.Escuro,
                focusedBorderColor = CorPrimaria,
                unfocusedBorderColor = Cores.Borda,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = CorPrimaria,
                focusedPlaceholderColor = Cores.Cinza,
                unfocusedPlaceholderColor = Cores.Cinza
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (erro) Text("Campo obrigatório.", fontSize = 11.sp, color = Cores.Vermelho, modifier = Modifier.padding(top = 4.dp))
    }
}
