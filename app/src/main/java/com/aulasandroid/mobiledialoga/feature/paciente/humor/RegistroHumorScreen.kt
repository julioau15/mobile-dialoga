package com.aulasandroid.mobiledialoga.feature.paciente.humor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.CampoDeTexto
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RotuloCampo
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.google.gson.JsonObject

private val BordaChip = Color(0xFFE0D8EE)
private val DivisorModal = Color(0xFFEFEAF6)

/**
 * Modal "Como você está hoje?" (substitui a antiga RegistroHumorScreen em tela cheia).
 * Quem chama decide o que fazer com o JSON (ver PacienteDashboardViewModel.registrarHumor).
 */
@Composable
fun RegistroHumorDialog(
    salvando: Boolean,
    erro: String?,
    onCancelar: () -> Unit,
    onSalvar: (JsonObject) -> Unit
) {
    val form = rememberHumorFormState()

    Dialog(
        onDismissRequest = { if (!salvando) onCancelar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                // cabeçalho
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Como você está hoje?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D1B45)
                    )
                    IconButton(onClick = onCancelar, enabled = !salvando) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CorSecundaria)
                    }
                }
                HorizontalDivider(color = DivisorModal)

                // conteúdo rolável
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    FormularioHumor(state = form)
                }

                HorizontalDivider(color = DivisorModal)

                if (erro != null) {
                    Text(
                        text = erro,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                    )
                }

                // rodapé (botões centralizados)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !salvando,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BordaChip)
                    ) {
                        Text("Cancelar", color = Color(0xFF2D1B45), fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onSalvar(form.toJsonObject()) },
                        enabled = form.isValido && !salvando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria)
                    ) {
                        if (salvando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Salvar registro", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ordem igual ao Figma: Sentimento, Energia, Humor, Pensamentos, Horas de sono, Relato.
 * ⚠️ Apague a função FormularioHumor antiga do arquivo onde está HumorFormState.
 */
@Composable
fun FormularioHumor(
    state: HumorFormState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RotuloCampo(texto = "Sentimento do dia")
        ChipsSelecao(HumorOptions.SENTIMENTOS, state.sentimento) { state.sentimento = it }

        Spacer(modifier = Modifier.height(20.dp))
        RotuloCampo(texto = "Energia do dia")
        ChipsSelecao(HumorOptions.ENERGIAS, state.energia, larguraIgual = true) { state.energia = it }

        Spacer(modifier = Modifier.height(20.dp))
        RotuloCampo(texto = "Humor do dia")
        ChipsSelecao(HumorOptions.HUMORES, state.humor) { state.humor = it }

        Spacer(modifier = Modifier.height(20.dp))
        RotuloCampo(texto = "Pensamentos")
        ChipsSelecao(HumorOptions.PENSAMENTOS, state.pensamentoSelecionado) { state.pensamentoSelecionado = it }

        Spacer(modifier = Modifier.height(20.dp))
        RotuloCampo(texto = "Quantas horas você dormiu?")
        CampoDeTexto(
            valor = state.horasSono,
            onValorChange = { text ->
                val filtrado = text.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                if (filtrado.count { it == '.' || it == ',' } <= 1) {
                    state.horasSono = filtrado
                }
            },
            placeholder = "Ex: 7.5",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(20.dp))
        RotuloCampo(texto = "Algo importante que aconteceu hoje (opcional)")
        CampoDeTexto(
            valor = state.relato,
            onValorChange = { state.relato = it },
            placeholder = "Descreva um acontecimento relevante do seu dia...",
            singleLine = false,
            minLinhas = 3
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsSelecao(
    opcoes: List<String>,
    selecionado: String?,
    larguraIgual: Boolean = false,
    onSelecionar: (String) -> Unit
) {
    if (larguraIgual) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            opcoes.forEach {
                ChipOpcao(it, it == selecionado, { onSelecionar(it) }, Modifier.weight(1f))
            }
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opcoes.forEach { ChipOpcao(it, it == selecionado, { onSelecionar(it) }) }
        }
    }
}

@Composable
private fun ChipOpcao(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(forma)
            .background(if (selecionado) CorPrimaria else Color.White)
            .border(1.dp, if (selecionado) CorPrimaria else BordaChip, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            color = if (selecionado) Color.White else CorSecundaria,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}