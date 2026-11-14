package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaOutlinedButton
import com.aulasandroid.mobiledialoga.components.DialogaTextField
import com.aulasandroid.mobiledialoga.components.DialogaDialogo
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.MensagemErroCampo
import com.aulasandroid.mobiledialoga.components.RotuloCampo
import com.aulasandroid.mobiledialoga.components.coresCampo
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaRegras
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.NovaConsultaForm
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.ProfissionalVinculado
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import java.util.Locale

private const val MILISSEGUNDOS_POR_DIA = 86_400_000L

// Tema claro do app para os seletores de data/hora (o app usa cores claras fixas, mesmo com o celular em modo escuro)
private val EsquemaClaro = lightColorScheme(
    primary = CorPrimaria,
    onPrimary = CorBranco,
    surface = CorBranco,
    onSurface = CorTexto,
    background = CorBranco,
    onBackground = CorTexto,
    error = CorErro
)

/**
 * Pop-up "Nova consulta": Profissional*, Especialidade, Data*, Horário*, Observações e Lembrete.
 * "Adicionar outra" salva e deixa o pop-up aberto; "Salvar" salva e fecha.
 */
@Composable
fun NovaConsultaDialog(
    formulario: NovaConsultaForm,
    profissionais: List<ProfissionalVinculado>,
    salvando: Boolean,
    onProfissionalTexto: (String) -> Unit,
    onProfissionalEscolhido: (ProfissionalVinculado) -> Unit,
    onEspecialidade: (String) -> Unit,
    onData: (Dia) -> Unit,
    onHora: (String) -> Unit,
    onObservacoes: (String) -> Unit,
    onLembrete: (Boolean) -> Unit,
    onAdicionarOutra: () -> Unit,
    onSalvar: () -> Unit,
    onFechar: () -> Unit
) {
    var mostrarSeletorData by remember { mutableStateOf(false) }

    DialogaDialogo(
        titulo = "Nova consulta",
        onFechar = onFechar,
        rodape = {
            DialogaOutlinedButton(
                texto = "Adicionar outra",
                onClick = { if (!salvando) onAdicionarOutra() },
                corConteudo = CorTexto
            )
            DialogaButton(
                texto = if (salvando) "Salvando..." else "Salvar",
                onClick = onSalvar,
                enabled = !salvando
            )
        }
    ) {
        CampoProfissional(
            texto = formulario.profissionalTexto,
            profissionais = profissionais,
            erro = formulario.erroProfissional,
            onTexto = onProfissionalTexto,
            onEscolhido = onProfissionalEscolhido
        )
        Spacer(modifier = Modifier.height(16.dp))

        val profissionalEscolhido = formulario.profissional
        val horarios = formulario.horarios

        CampoLista(
            rotulo = "Especialidade",
            valor = formulario.especialidade,
            placeholder = when {
                profissionalEscolhido == null -> "Escolha o profissional primeiro"
                profissionalEscolhido.especialidades.isEmpty() -> "Sem especialidade cadastrada"
                else -> "Selecione a especialidade"
            },
            opcoes = profissionalEscolhido?.especialidades.orEmpty(),
            habilitado = profissionalEscolhido != null && profissionalEscolhido.especialidades.size > 1,
            erro = null,
            onEscolher = onEspecialidade
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            CampoQueAbreSeletor(
                rotulo = "Data",
                valor = formulario.data?.let { AgendaCalendario.dataCurta(it) } ?: "",
                placeholder = when {
                    profissionalEscolhido == null -> "Escolha o profissional"
                    formulario.carregandoHorarios -> "Carregando..."
                    else -> "dd/mm/aaaa"
                },
                erro = formulario.erroData,
                habilitado = horarios != null && horarios.dias.isNotEmpty(),
                onClick = { mostrarSeletorData = true },
                modifier = Modifier.weight(1f)
            )
            CampoLista(
                rotulo = "Horário",
                valor = formulario.hora ?: "",
                placeholder = if (formulario.data == null) "Escolha a data" else "hh:mm",
                opcoes = horarios?.horariosDo(formulario.data).orEmpty(),
                habilitado = formulario.data != null,
                erro = formulario.erroHora,
                obrigatorio = true,
                onEscolher = onHora,
                modifier = Modifier.weight(1f)
            )
        }

        // Aviso do que está acontecendo com os horários do profissional
        val avisoHorarios = when {
            formulario.erroHorarios != null -> formulario.erroHorarios
            horarios != null && !horarios.cadastrouDisponibilidade -> "Este profissional ainda não cadastrou os horários de atendimento."
            horarios != null && horarios.dias.isEmpty() -> "Este profissional não tem horários livres nos próximos dias."
            horarios != null -> "Só aparecem os dias e horários em que o profissional atende."
            else -> null
        }
        if (avisoHorarios != null) {
            Text(
                text = avisoHorarios,
                color = if (formulario.erroHorarios != null) CorErro else CorSecundaria,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        RotuloCampo("Observações (opcional)")
        OutlinedTextField(
            value = formulario.observacoes,
            onValueChange = onObservacoes,
            placeholder = { Text(text = "Anotações sobre a consulta...", fontSize = 14.sp) },
            minLines = 3,
            maxLines = 4,
            shape = FormatoCampo,
            colors = coresCampo(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = formulario.lembrete,
                onCheckedChange = onLembrete,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CorBranco,
                    checkedTrackColor = CorPrimaria,
                    uncheckedThumbColor = CorBranco,
                    uncheckedTrackColor = CorBorda,
                    uncheckedBorderColor = CorBorda
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = CorSecundaria,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Lembrete", color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        if (formulario.erroGeral != null) {
            Spacer(modifier = Modifier.height(12.dp))
            MensagemErroCampo(formulario.erroGeral)
        }
    }

    if (mostrarSeletorData && formulario.horarios != null) {
        SeletorDeData(
            inicial = formulario.data,
            diasLivres = formulario.horarios.diasLivres,
            onConfirmar = onData,
            onFechar = { mostrarSeletorData = false }
        )
    }
}

// "Profissional *": campo de texto que sugere os profissionais vinculados. A consulta precisa de um deles (do vínculo).
@Composable
private fun CampoProfissional(
    texto: String,
    profissionais: List<ProfissionalVinculado>,
    erro: String?,
    onTexto: (String) -> Unit,
    onEscolhido: (ProfissionalVinculado) -> Unit
) {
    var sugestoesAbertas by remember { mutableStateOf(false) }
    val foco = LocalFocusManager.current
    val sugestoes = AgendaRegras.sugestoes(profissionais, texto)

    Column(modifier = Modifier.fillMaxWidth()) {
        RotuloObrigatorio("Profissional")
        Box {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    onTexto(it)
                    sugestoesAbertas = true
                },
                placeholder = { Text(text = "Nome do profissional", fontSize = 14.sp) },
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
                sugestoes.forEach { profissional ->
                    DropdownMenuItem(
                        text = { Text(text = profissional.nome, color = CorTexto, fontSize = 14.sp) },
                        onClick = {
                            onEscolhido(profissional)
                            sugestoesAbertas = false
                            foco.clearFocus()
                        }
                    )
                }
            }
        }
        MensagemErroCampo(erro)
        if (profissionais.isEmpty() && erro == null) {
            Text(
                text = "Você ainda não tem um profissional vinculado.",
                color = CorSecundaria,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Campo que não digita: ao tocar abre o seletor (Data e Horário)
@Composable
private fun CampoQueAbreSeletor(
    rotulo: String,
    valor: String,
    placeholder: String,
    erro: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Column(modifier = modifier) {
        RotuloObrigatorio(rotulo)
        Box {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = erro != null,
                placeholder = { Text(text = placeholder, fontSize = 14.sp) },
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
            // Camada transparente por cima para receber o toque (um campo readOnly não abre nada sozinho)
            if (habilitado) Box(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
        }
        MensagemErroCampo(erro)
    }
}

// Campo que não digita: ao tocar abre uma lista de opções (Especialidade e Horário)
@Composable
private fun CampoLista(
    rotulo: String,
    valor: String,
    placeholder: String,
    opcoes: List<String>,
    habilitado: Boolean,
    erro: String?,
    onEscolher: (String) -> Unit,
    modifier: Modifier = Modifier,
    obrigatorio: Boolean = false
) {
    var aberta by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (obrigatorio) RotuloObrigatorio(rotulo) else RotuloCampo(rotulo)
        Box {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                enabled = habilitado || valor.isNotEmpty(),
                isError = erro != null,
                placeholder = { Text(text = placeholder, fontSize = 14.sp) },
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
            if (habilitado && opcoes.isNotEmpty()) {
                Box(modifier = Modifier.matchParentSize().clickable { aberta = true })
            }
            DropdownMenu(
                expanded = aberta,
                onDismissRequest = { aberta = false },
                containerColor = CorBranco
            ) {
                opcoes.forEach { opcao ->
                    DropdownMenuItem(
                        text = { Text(text = opcao, color = CorTexto, fontSize = 14.sp) },
                        onClick = {
                            onEscolher(opcao)
                            aberta = false
                        }
                    )
                }
            }
        }
        MensagemErroCampo(erro)
    }
}

// Calendário do Material 3. O valor vem em milissegundos à meia-noite UTC, ou seja, dias inteiros desde 1970.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDeData(
    inicial: Dia?,
    diasLivres: Set<Dia>,
    onConfirmar: (Dia) -> Unit,
    onFechar: () -> Unit
) {
    // Só os dias em que o profissional atende (e nunca antes de hoje) podem ser tocados; os outros ficam apagados
    val hoje = remember { Dia.hoje() }
    val anos = remember(diasLivres) { diasLivres.map { it.ano } }
    val anoMinimo = anos.minOrNull() ?: hoje.ano
    val anoMaximo = anos.maxOrNull() ?: hoje.ano
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = inicial?.let { it.diasDesdeEpoca() * MILISSEGUNDOS_POR_DIA },
        initialDisplayedMonthMillis = (diasLivres.minOrNull() ?: hoje).let { it.diasDesdeEpoca() * MILISSEGUNDOS_POR_DIA },
        yearRange = anoMinimo..anoMaximo,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val dia = Dia.deEpoca(Math.floorDiv(utcTimeMillis, MILISSEGUNDOS_POR_DIA))
                return dia >= hoje && dia in diasLivres
            }

            override fun isSelectableYear(year: Int): Boolean = year in anoMinimo..anoMaximo
        }
    )
    MaterialTheme(colorScheme = EsquemaClaro) {
        DatePickerDialog(
            onDismissRequest = onFechar,
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { onConfirmar(Dia.deEpoca(Math.floorDiv(it, MILISSEGUNDOS_POR_DIA))) }
                        onFechar()
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
        ) {
            DatePicker(state = estado)
        }
    }
}
