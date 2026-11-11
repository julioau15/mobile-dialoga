package com.aulasandroid.mobiledialoga.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import java.util.Locale

private const val MILISSEGUNDOS_POR_DIA = 86_400_000L
private const val ANOS_A_FRENTE = 5

// Tema claro do app para o calendário (o app usa cores claras fixas, mesmo com o celular em modo escuro).
// É o mesmo esquema usado no seletor de data da agenda do paciente.
private val EsquemaClaroSeletor = lightColorScheme(
    primary = CorPrimaria,
    onPrimary = CorBranco,
    surface = CorBranco,
    onSurface = CorTexto,
    background = CorBranco,
    onBackground = CorTexto,
    error = CorErro
)

/** "2026-11-20" -> Dia(2026, 11, 20). Devolve null se não for uma data válida. */
fun isoParaDia(iso: String?): Dia? {
    val limpo = iso?.trim()?.take(10) ?: return null
    val partes = limpo.split("-")
    if (partes.size != 3) return null
    val ano = partes[0].toIntOrNull() ?: return null
    val mes = partes[1].toIntOrNull() ?: return null
    val dia = partes[2].toIntOrNull() ?: return null
    if (mes !in 1..12 || dia !in 1..31) return null
    return Dia(ano, mes, dia)
}

/** Dia(2026, 11, 20) -> "2026-11-20" */
fun diaParaIso(dia: Dia): String =
    String.format(Locale.US, "%04d-%02d-%02d", dia.ano, dia.mes, dia.dia)

/** Dia(2026, 11, 20) -> "20/11/2026" */
fun diaParaBr(dia: Dia): String =
    String.format(Locale.US, "%02d/%02d/%04d", dia.dia, dia.mes, dia.ano)

/** Prazo válido: de hoje até 5 anos à frente (mesma regra que existia na digitação manual). */
fun prazoDiaValido(dia: Dia): Boolean {
    val hoje = Dia.hoje()
    return dia >= hoje && dia.ano <= hoje.ano + ANOS_A_FRENTE
}

/**
 * Campo de data que NÃO é digitado: ao tocar (ou no ícone de calendário) abre o mesmo calendário
 * Material 3 do campo de data da agenda. O valor é guardado como ISO "yyyy-MM-dd".
 *
 * - [prazoIso]: valor atual (null = vazio)
 * - [onPrazoChange]: devolve a data escolhida em ISO, ou null se a escolhida for inválida (ex.: no passado)
 */
@Composable
fun CampoDataCalendario(
    prazoIso: String?,
    onPrazoChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String? = null,
    placeholder: String = "dd/mm/aaaa",
    erro: String? = null,
    enabled: Boolean = true
) {
    var mostrarCalendario by remember { mutableStateOf(false) }
    var erroLocal by remember { mutableStateOf<String?>(null) }

    val dia = isoParaDia(prazoIso)
    val erroExibido = erro ?: erroLocal

    Column(modifier = modifier) {
        if (rotulo != null) {
            Text(
                text = rotulo,
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Box {
            OutlinedTextField(
                value = dia?.let { diaParaBr(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = erroExibido != null,
                placeholder = { Text(text = placeholder, fontSize = 14.sp) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Escolher data",
                        tint = CorSecundaria,
                        modifier = Modifier.size(22.dp)
                    )
                },
                shape = FormatoCampo,
                colors = coresCampo(),
                modifier = Modifier.fillMaxWidth()
            )
            // Camada transparente por cima para receber o toque (um campo readOnly não abre nada sozinho)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled) { mostrarCalendario = true }
            )
        }
        MensagemErroCampo(erroExibido)
    }

    if (mostrarCalendario) {
        SeletorDataCalendario(
            inicial = dia,
            onConfirmar = { escolhido ->
                if (prazoDiaValido(escolhido)) {
                    erroLocal = null
                    onPrazoChange(diaParaIso(escolhido))
                } else {
                    erroLocal = "Escolha uma data a partir de hoje."
                    onPrazoChange(null)
                }
            },
            onFechar = { mostrarCalendario = false }
        )
    }
}

// Calendário do Material 3. O valor vem em milissegundos à meia-noite UTC, ou seja, dias inteiros desde 1970.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDataCalendario(
    inicial: Dia?,
    onConfirmar: (Dia) -> Unit,
    onFechar: () -> Unit
) {
    val hoje = Dia.hoje()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = (inicial ?: hoje).let { it.diasDesdeEpoca() * MILISSEGUNDOS_POR_DIA },
        yearRange = hoje.ano..(hoje.ano + ANOS_A_FRENTE)
    )
    MaterialTheme(colorScheme = EsquemaClaroSeletor) {
        DatePickerDialog(
            onDismissRequest = onFechar,
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let {
                            onConfirmar(Dia.deEpoca(Math.floorDiv(it, MILISSEGUNDOS_POR_DIA)))
                        }
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
