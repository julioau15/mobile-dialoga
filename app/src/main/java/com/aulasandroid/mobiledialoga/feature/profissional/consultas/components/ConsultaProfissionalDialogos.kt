package com.aulasandroid.mobiledialoga.feature.profissional.consultas.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.aulasandroid.mobiledialoga.components.CampoDataCalendario
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.MensagemErroCampo
import com.aulasandroid.mobiledialoga.components.coresCampo
import com.aulasandroid.mobiledialoga.components.diaParaBr
import com.aulasandroid.mobiledialoga.components.isoParaDia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade.DisponibilidadeUi
import com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade.horariosDoDia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.components.RotuloObrigatorio
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.ConsultaProfissional
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.FormularioConsulta
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.PacienteDaAgenda
import com.aulasandroid.mobiledialoga.feature.profissional.consultas.semAcento
import com.aulasandroid.mobiledialoga.feature.vinculos.components.BotaoModalPrincipal
import com.aulasandroid.mobiledialoga.feature.vinculos.components.BotaoModalSecundario
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorPerigo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.ModalDialoga
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// 06:00, 06:30 ... 22:00 (mesmas opções do cadastro de disponibilidade)
private val OPCOES_HORARIO = (6..21).flatMap { h -> listOf("%02d:00".format(h), "%02d:30".format(h)) } + "22:00"

// "quarta-feira, 2 de setembro" -> "Quarta-Feira, 2 De Setembro" (cada palavra com inicial maiúscula, como no protótipo)
private fun dataPorExtenso(dia: Dia): String =
    AgendaCalendario.tituloDiaSelecionado(dia)
        .split(" ")
        .joinToString(" ") { palavra ->
            palavra.split("-").joinToString("-") { parte -> parte.replaceFirstChar { it.uppercase() } }
        }

// ---------------------------------------------------------------------------
// Detalhes da consulta (toque num card): paciente, data, horário, Excluir / Editar
// ---------------------------------------------------------------------------

@Composable
fun DetalhesConsultaDialog(
    consulta: ConsultaProfissional,
    onFechar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    ModalDialoga(
        titulo = "Detalhes da consulta",
        onFechar = onFechar,
        rodape = {
            OutlinedButton(
                onClick = onExcluir,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CorPerigo.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CorPerigo, containerColor = CorBranco)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Excluir", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            BotaoModalPrincipal(texto = "Editar", onClick = onEditar)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CorPrimariaClara)
                .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(46.dp).clip(CircleShape).background(CorPrimaria)
            ) {
                Text(
                    text = com.aulasandroid.mobiledialoga.feature.vinculos.components.iniciaisDe(consulta.paciente),
                    maxLines = 1,
                    softWrap = false,
                    color = CorBranco,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = consulta.paciente, color = CorTexto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = consulta.subtitulo, color = CorSecundaria, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            CaixaDeInformacao("Data", Icons.Outlined.CalendarToday, dataPorExtenso(consulta.dia), Modifier.weight(1.4f))
            CaixaDeInformacao("Horário", Icons.Outlined.Schedule, consulta.hora, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CaixaDeInformacao(rotulo: String, icone: ImageVector, valor: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, contentDescription = null, tint = CorSecundaria, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = rotulo, color = CorSecundaria, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = valor, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// Confirmação de exclusão
// ---------------------------------------------------------------------------

@Composable
fun ExcluirConsultaDialog(
    consulta: ConsultaProfissional,
    removendo: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    ModalDialoga(
        titulo = "Detalhes da consulta",
        onFechar = onCancelar,
        bloqueado = removendo,
        rodape = {
            BotaoModalSecundario(texto = "Cancelar", onClick = onCancelar, habilitado = !removendo)
            BotaoModalPrincipal(
                texto = if (removendo) "Excluindo..." else "Confirmar exclusão",
                onClick = onConfirmar,
                habilitado = !removendo,
                perigo = true
            )
        }
    ) {
        val destaque = SpanStyle(color = CorTexto, fontWeight = FontWeight.Bold)
        Text(
            text = buildAnnotatedString {
                append("Tem certeza que deseja excluir a consulta de ")
                withStyle(destaque) { append(consulta.paciente) }
                append(" em ")
                withStyle(destaque) { append("${diaParaBr(consulta.dia)} às ${consulta.hora}") }
                append("?")
            },
            color = CorSecundaria,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

// ---------------------------------------------------------------------------
// Nova consulta / Editar consulta: Paciente*, Data* e Horário*
// ---------------------------------------------------------------------------

@Composable
fun FormularioConsultaDialog(
    formulario: FormularioConsulta,
    pacientes: List<PacienteDaAgenda>,
    disponibilidades: List<DisponibilidadeUi> = emptyList(),
    onPacienteTexto: (String) -> Unit,
    onPacienteEscolhido: (PacienteDaAgenda) -> Unit,
    onData: (String?) -> Unit,
    onHora: (String) -> Unit,
    onSalvar: () -> Unit,
    onFechar: () -> Unit
) {
    val diaSelecionado = isoParaDia(formulario.dataIso)
    val opcoesHorario = remember(diaSelecionado, disponibilidades) {
        if (diaSelecionado != null && disponibilidades.isNotEmpty()) {
            horariosDoDia(diaSelecionado, disponibilidades).ifEmpty { OPCOES_HORARIO }
        } else {
            OPCOES_HORARIO
        }
    }

    ModalDialoga(
        titulo = if (formulario.editando == null) "Nova consulta" else "Editar consulta",
        onFechar = onFechar,
        bloqueado = formulario.salvando,
        rodape = {
            BotaoModalSecundario(texto = "Cancelar", onClick = onFechar, habilitado = !formulario.salvando)
            BotaoModalPrincipal(
                texto = if (formulario.salvando) "Salvando..." else "Salvar",
                onClick = onSalvar,
                habilitado = !formulario.salvando
            )
        }
    ) {
        CampoPaciente(
            texto = formulario.pacienteTexto,
            escolhido = formulario.paciente != null,
            pacientes = pacientes,
            erro = formulario.erroPaciente,
            onTexto = onPacienteTexto,
            onEscolhido = onPacienteEscolhido
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.2f)) {
                RotuloObrigatorio("Data")
                CampoDataCalendario(
                    prazoIso = formulario.dataIso,
                    onPrazoChange = onData,
                    erro = formulario.erroData,
                    disponibilidades = disponibilidades
                )
            }
            Column(modifier = Modifier.weight(0.9f)) {
                RotuloObrigatorio("Horário")
                CampoHorario(
                    valor = formulario.hora,
                    opcoes = opcoesHorario,
                    erro = formulario.erroHora,
                    onEscolher = onHora
                )
            }
        }

        if (disponibilidades.isEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Você ainda não cadastrou horários de atendimento na aba Disponibilidade.",
                color = CorSecundaria,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Só é possível selecionar os dias em que você tem disponibilidade.",
                color = CorSecundaria,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (formulario.erroGeral != null) {
            Spacer(modifier = Modifier.height(12.dp))
            MensagemErroCampo(formulario.erroGeral)
        }
    }
}

// "Paciente *": campo de texto que sugere os pacientes com vínculo ativo; a consulta precisa de um deles
@Composable
private fun CampoPaciente(
    texto: String,
    escolhido: Boolean,
    pacientes: List<PacienteDaAgenda>,
    erro: String?,
    onTexto: (String) -> Unit,
    onEscolhido: (PacienteDaAgenda) -> Unit
) {
    var sugestoesAbertas by remember { mutableStateOf(false) }
    val foco = LocalFocusManager.current
    val termo = semAcento(texto.trim())
    val sugestoes = if (escolhido) emptyList() else pacientes.filter { termo.isEmpty() || semAcento(it.nome).contains(termo) }.take(6)

    Column(modifier = Modifier.fillMaxWidth()) {
        RotuloObrigatorio("Paciente")
        Box {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    onTexto(it)
                    sugestoesAbertas = true
                },
                placeholder = { Text(text = "Nome do paciente", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                isError = erro != null,
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) sugestoesAbertas = true }
            )
            // focusable = false: o teclado e o cursor continuam no campo enquanto a lista aparece
            DropdownMenu(
                expanded = sugestoesAbertas && sugestoes.isNotEmpty(),
                onDismissRequest = { sugestoesAbertas = false },
                properties = PopupProperties(focusable = false),
                containerColor = CorBranco
            ) {
                sugestoes.forEach { paciente ->
                    DropdownMenuItem(
                        text = { Text(text = paciente.nome, color = CorTexto, fontSize = 14.sp) },
                        onClick = {
                            onEscolhido(paciente)
                            sugestoesAbertas = false
                            foco.clearFocus()
                        }
                    )
                }
            }
        }
        MensagemErroCampo(erro)
        if (pacientes.isEmpty()) {
            Text(
                text = "Você ainda não tem pacientes vinculados.",
                color = CorSecundaria,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

// Campo que não digita: ao tocar abre a lista de horários
@Composable
private fun CampoHorario(
    valor: String?,
    opcoes: List<String> = OPCOES_HORARIO,
    erro: String?,
    onEscolher: (String) -> Unit
) {
    var aberta by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box {
            OutlinedTextField(
                value = valor ?: "",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = erro != null,
                placeholder = { Text(text = "hh:mm", fontSize = 14.sp) },
                trailingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = "Escolher horário", modifier = Modifier.size(20.dp)) },
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
            // Camada transparente por cima para receber o toque (um campo readOnly não abre nada sozinho)
            Box(modifier = Modifier.matchParentSize().clickable { aberta = true })
            DropdownMenu(
                expanded = aberta,
                onDismissRequest = { aberta = false },
                containerColor = CorBranco
            ) {
                opcoes.forEach { horario ->
                    DropdownMenuItem(
                        text = { Text(text = horario, color = CorTexto, fontSize = 14.sp) },
                        onClick = {
                            onEscolher(horario)
                            aberta = false
                        }
                    )
                }
            }
        }
        MensagemErroCampo(erro)
    }
}
