package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.coresCampo
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.prazoDigitosParaIso
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.DataVisualTransformation
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.PacienteOpcaoDto
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import java.util.Calendar
import java.util.Locale

private sealed interface EtapaEnvio {
    data class Prazo(val indice: Int) : EtapaEnvio
    object Confirmar : EtapaEnvio
}

/** Tela "Nova tarefa" (também usada para editar rascunho). */
@Composable
internal fun FormularioTarefa(
    estado: TarefasProfissionalUiState,
    form: FormTarefa,
    onVoltar: () -> Unit,
    onVisualizar: () -> Unit,
    onSelecionarAnexo: (Uri?) -> Unit,
    onValidarEnvio: () -> Boolean,
    destinatariosDoEnvio: () -> List<PacienteOpcaoDto>,
    onSalvarRascunho: () -> Unit,
    onEnviar: (prazos: Map<Int, String>) -> Unit
) {
    var etapa by remember { mutableStateOf<EtapaEnvio?>(null) }
    var destinatarios by remember { mutableStateOf<List<PacienteOpcaoDto>>(emptyList()) }
    val prazos = remember { mutableMapOf<Int, String>() }

    LinkVoltar("Voltar às tarefas", onVoltar)
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (form.idRascunhoOriginal != null) "Editar rascunho" else "Nova tarefa",
            fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto, modifier = Modifier.weight(1f)
        )
        BotaoPilula("Visualizar", onVisualizar, primario = false, modifier = Modifier.width(108.dp).height(40.dp))
    }
    Spacer(Modifier.height(16.dp))

    // ---------- Dados da tarefa ----------
    CartaoBranco {
        RotuloForm("Nome da tarefa", obrigatorio = true)
        CampoTexto(form.titulo, { form.titulo = it }, "Ex: Diário de Humor Semanal")

        Spacer(Modifier.height(16.dp))
        RotuloForm("Instruções", opcional = true)
        CampoTexto(form.instrucoes, { form.instrucoes = it }, "Orientações para o paciente...", linhas = 3)

        Spacer(Modifier.height(16.dp))
        RotuloForm("Paciente", opcional = true)
        val disponiveis = estado.pacientes.filter { it.id !in form.pacientes }
        CampoSelecao(
            texto = if (estado.pacientes.isEmpty()) "Nenhum paciente vinculado" else "Selecionar paciente...",
            placeholder = true,
            opcoes = disponiveis.map { it.nome },
            onSelecionar = { nome -> disponiveis.find { it.nome == nome }?.let { form.pacientes.add(it.id) } }
        )
        form.pacientes.forEach { id ->
            val nome = estado.pacientes.find { it.id == id }?.nome ?: "Paciente $id"
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CorPrimariaClara)
                    .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Avatar(nome, corDoAvatar(id), tamanho = 36.dp)
                Spacer(Modifier.width(12.dp))
                Text(text = nome, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CorTexto, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { form.pacientes.remove(id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover paciente", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        RotuloForm("Anexar arquivo", opcional = true)
        CampoAnexo(nome = form.anexoNome, onSelecionar = onSelecionarAnexo, onRemover = { onSelecionarAnexo(null) })
    }

    // ---------- Perguntas ----------
    form.perguntas.forEachIndexed { indice, pergunta ->
        Spacer(Modifier.height(16.dp))
        CartaoPergunta(
            numero = indice + 1,
            pergunta = pergunta,
            somenteLeitura = form.somenteLeitura,
            onDuplicar = { form.duplicar(pergunta) },
            onRemover = { form.perguntas.remove(pergunta) }
        )
    }

    if (!form.somenteLeitura) {
        Spacer(Modifier.height(16.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bordaTracejada()
                .clip(RoundedCornerShape(12.dp))
                .clickable { form.perguntas.add(form.novaPergunta()) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CorPrimaria, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Adicionar pergunta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CorPrimaria)
            }
        }
    }

    if (estado.erroFormulario != null) {
        Spacer(Modifier.height(16.dp))
        CartaoErro(estado.erroFormulario)
    }

    if (!form.somenteLeitura) {
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPilula(
                texto = "Salvar rascunho", onClick = onSalvarRascunho, primario = false,
                modifier = Modifier.weight(1f), enabled = !estado.isSalvandoRascunho && !estado.isEnviando,
                isLoading = estado.isSalvandoRascunho,
                icone = { Icon(Icons.Default.Check, contentDescription = null, tint = CorSecundaria, modifier = Modifier.size(18.dp)) }
            )
            BotaoPilula(
                texto = "Enviar ao paciente",
                onClick = {
                    if (onValidarEnvio()) {
                        prazos.clear()
                        destinatarios = destinatariosDoEnvio()
                        etapa = if (form.pacientes.isNotEmpty() && destinatarios.isNotEmpty()) EtapaEnvio.Prazo(0) else EtapaEnvio.Confirmar
                    }
                },
                primario = true, modifier = Modifier.weight(1.1f),
                enabled = !estado.isSalvandoRascunho && !estado.isEnviando,
                isLoading = estado.isEnviando,
                icone = { Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            )
        }
    }
    Spacer(Modifier.height(24.dp))

    // ---------- Modais ----------
    when (val e = etapa) {
        is EtapaEnvio.Prazo -> {
            val paciente = destinatarios[e.indice]
            val proxima = {
                etapa = if (e.indice + 1 < destinatarios.size) EtapaEnvio.Prazo(e.indice + 1) else EtapaEnvio.Confirmar
            }
            ModalPrazo(
                nomePaciente = paciente.nome,
                onFechar = { etapa = null },
                onSemPrazo = { prazos.remove(paciente.id); proxima() },
                onAdicionar = { iso -> prazos[paciente.id] = iso; proxima() }
            )
        }
        EtapaEnvio.Confirmar -> ModalConfirmarEnvio(
            destino = if (form.pacientes.isEmpty()) "todos os pacientes" else destinatarios.joinToString(", ") { it.nome },
            onCancelar = { etapa = null },
            onEnviar = { etapa = null; onEnviar(prazos.toMap()) }
        )
        null -> Unit
    }
}

// ---------- Campos ----------

@Composable
private fun CampoTexto(valor: String, onChange: (String) -> Unit, placeholder: String, linhas: Int = 1, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        singleLine = linhas == 1,
        minLines = linhas,
        shape = FormatoCampo,
        colors = coresCampo(),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun CampoAnexo(nome: String?, onSelecionar: (Uri?) -> Unit, onRemover: () -> Unit) {
    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) onSelecionar(uri) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .bordaTracejada()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFAF8FD))
            .clickable { seletor.launch("*/*") }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        if (nome == null) {
            Text("+ Anexar arquivo à tarefa", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CorSecundaria)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(nome, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CorTexto, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onRemover, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remover anexo", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------- Pergunta ----------

@Composable
private fun CartaoPergunta(numero: Int, pergunta: PerguntaForm, somenteLeitura: Boolean = false, onDuplicar: () -> Unit, onRemover: () -> Unit) {
    CartaoBranco(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$numero.", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CorSecundaria)
            Spacer(Modifier.width(10.dp))
            if (somenteLeitura) {
                Text(pergunta.tipo.rotulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CorPrimaria, modifier = Modifier.weight(1f))
            } else {
                CampoSelecao(
                    texto = pergunta.tipo.rotulo,
                    placeholder = false,
                    opcoes = TipoPerguntaForm.values().map { it.rotulo },
                    onSelecionar = { r -> TipoPerguntaForm.values().find { it.rotulo == r }?.let { pergunta.tipo = it } },
                    altura = 40.dp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDuplicar, modifier = Modifier.size(36.dp)) { IconeCopiar() }
                IconButton(onClick = onRemover, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover pergunta", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        if (somenteLeitura) {
            Text(pergunta.enunciado.ifBlank { "Sem enunciado" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CorTexto)
        } else {
            CampoTexto(pergunta.enunciado, { pergunta.enunciado = it }, "Enunciado da pergunta")
        }
        Spacer(Modifier.height(10.dp))

        when {
            pergunta.tipo == TipoPerguntaForm.TEXTO -> PrevisaoResposta("Resposta longa do paciente...", altura = 56.dp)
            pergunta.tipo == TipoPerguntaForm.ESCALA -> PrevisaoEscala()
            else -> EditorOpcoes(pergunta)
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = pergunta.obrigatoria,
                onCheckedChange = { pergunta.obrigatoria = it },
                colors = SwitchDefaults.colors(checkedTrackColor = CorPrimaria, uncheckedTrackColor = Color(0xFFE4DDF0), uncheckedThumbColor = Color.White, uncheckedBorderColor = Color.Transparent),
                modifier = Modifier.height(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text("Resposta obrigatória", fontSize = 13.sp, color = CorSecundaria)
        }
    }
}

@Composable
private fun PrevisaoResposta(texto: String, altura: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = altura)
            .clip(RoundedCornerShape(12.dp))
            .background(FundoCampoSuave)
            .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) { Text(texto, fontSize = 14.sp, color = Color(0xFFA79FB5)) }
}

@Composable
private fun PrevisaoEscala() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        (0..10).forEach { n ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FundoCampoSuave)
                    .border(1.dp, CorBorda, RoundedCornerShape(8.dp))
            ) { Text("$n", fontSize = 11.sp, color = CorSecundaria) }
        }
    }
}

@Composable
private fun EditorOpcoes(pergunta: PerguntaForm) {
    val redondo = pergunta.tipo == TipoPerguntaForm.UNICA
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pergunta.opcoes.forEachIndexed { i, texto ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(18.dp)
                        .border(1.5.dp, BordaTracejada, if (redondo) CircleShape else RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = texto,
                    onValueChange = { pergunta.opcoes[i] = it },
                    placeholder = { Text("Opção ${i + 1}", fontSize = 14.sp) },
                    singleLine = true,
                    shape = FormatoCampo,
                    colors = coresCampo(),
                    modifier = Modifier.weight(1f)
                )
                if (pergunta.opcoes.size > 2) {
                    IconButton(onClick = { pergunta.opcoes.removeAt(i) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remover opção", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Text(
            text = "+ Adicionar opção", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CorPrimaria,
            modifier = Modifier.clickable { pergunta.opcoes.add("") }.padding(vertical = 4.dp)
        )
    }
}

// ---------- Pré-visualização ----------

@Composable
internal fun PreviewTarefa(form: FormTarefa, onVoltar: () -> Unit, onEditar: () -> Unit) {
    LinkVoltar(if (form.somenteLeitura) "Voltar às tarefas" else "Voltar à edição", onVoltar)
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Pré-visualização", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto, modifier = Modifier.weight(1f))
        if (!form.somenteLeitura) {
            BotaoPilula("Editar", onEditar, primario = false, modifier = Modifier.width(96.dp).height(40.dp))
        }
    }
    Spacer(Modifier.height(16.dp))

    CartaoBranco {
        Text(
            text = form.titulo.ifBlank { "Sem título" }, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto
        )
        if (form.instrucoes.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(form.instrucoes, fontSize = 14.sp, color = CorSecundaria, lineHeight = 20.sp)
        }
    }

    form.perguntas.forEachIndexed { i, p ->
        Spacer(Modifier.height(14.dp))
        CartaoBranco(padding = 16.dp) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = CorSecundaria, fontWeight = FontWeight.Bold)) { append("${i + 1}. ") }
                    append(p.enunciado.ifBlank { "(sem enunciado)" })
                    if (p.obrigatoria) withStyle(SpanStyle(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)) { append(" *") }
                },
                fontSize = 15.sp, color = CorTexto, lineHeight = 21.sp
            )
            Spacer(Modifier.height(10.dp))
            when {
                p.tipo == TipoPerguntaForm.TEXTO -> PrevisaoResposta("Resposta longa...", altura = 84.dp)
                p.tipo == TipoPerguntaForm.ESCALA -> PrevisaoEscala()
                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.opcoes.filter { it.isNotBlank() }.forEach { op ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(18.dp).border(1.5.dp, BordaTracejada, if (p.tipo == TipoPerguntaForm.UNICA) CircleShape else RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(10.dp))
                            Text(op, fontSize = 14.sp, color = CorTexto)
                        }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}

// ---------- Modais ----------

@Composable
private fun CascaModal(titulo: String, onFechar: () -> Unit, corpo: @Composable () -> Unit, rodape: @Composable () -> Unit) {
    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
                IconButton(onClick = onFechar) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CorSecundaria) }
            }
            HorizontalDivider(color = DivisorModal)
            Column(modifier = Modifier.padding(20.dp)) { corpo() }
            HorizontalDivider(color = DivisorModal)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) { rodape() }
        }
    }
}

@Composable
private fun ModalPrazo(
    nomePaciente: String,
    onFechar: () -> Unit,
    onSemPrazo: () -> Unit,
    onAdicionar: (iso: String) -> Unit
) {
    val context = LocalContext.current
    var digitos by remember { mutableStateOf("") }
    val iso = prazoDigitosParaIso(digitos)

    CascaModal(
        titulo = "Adicionar prazo?",
        onFechar = onFechar,
        corpo = {
            Text(
                text = buildAnnotatedString {
                    append("Deseja definir um prazo para ")
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = CorTexto)) { append(nomePaciente) }
                    append("?")
                },
                fontSize = 14.sp, color = CorSecundaria, lineHeight = 20.sp
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = digitos,
                onValueChange = { digitos = it.filter { c -> c.isDigit() }.take(8) },
                placeholder = { Text("dd/mm/aaaa", fontSize = 14.sp) },
                singleLine = true,
                visualTransformation = DataVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = {
                        val hoje = Calendar.getInstance()
                        DatePickerDialog(context, { _, a, m, d ->
                            digitos = String.format(Locale.US, "%02d%02d%04d", d, m + 1, a)
                        }, hoje.get(Calendar.YEAR), hoje.get(Calendar.MONTH), hoje.get(Calendar.DAY_OF_MONTH))
                            .apply { datePicker.minDate = hoje.timeInMillis - 1000 }
                            .show()
                    }) { Icon(Icons.Default.DateRange, contentDescription = "Escolher data", tint = CorTexto) }
                },
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
            if (digitos.length == 8 && iso == null) {
                Text("Data inválida ou já passou.", fontSize = 12.sp, color = Color(0xFFDC2626), modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }
        },
        rodape = {
            BotaoPilula("Sem prazo", onSemPrazo, primario = false, modifier = Modifier.height(42.dp))
            BotaoPilula("Adicionar prazo", { iso?.let(onAdicionar) }, primario = true, enabled = iso != null, modifier = Modifier.height(42.dp))
        }
    )
}

@Composable
private fun ModalConfirmarEnvio(destino: String, onCancelar: () -> Unit, onEnviar: () -> Unit) {
    CascaModal(
        titulo = "Confirmar envio",
        onFechar = onCancelar,
        corpo = {
            Text(
                text = buildAnnotatedString {
                    append("Enviar a tarefa para: ")
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = CorTexto)) { append(destino) }
                    append("?")
                },
                fontSize = 14.sp, color = CorSecundaria, lineHeight = 20.sp
            )
        },
        rodape = {
            BotaoPilula("Cancelar", onCancelar, primario = false, modifier = Modifier.height(42.dp))
            BotaoPilula("Enviar", onEnviar, primario = true, modifier = Modifier.height(42.dp))
        }
    )
}
