package com.aulasandroid.mobiledialoga.components
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RotuloCampo
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Campo de seleção ("Especialidade"). Stateless em relação ao valor escolhido.
@Composable
fun DialogaSelectField(
    rotulo: String,
    opcoes: List<String>,
    selecionada: String?,
    onSelecionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione...",
    erro: String? = null
) {
    var aberto by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        RotuloCampo(rotulo)
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(FormatoCampo)
                    .background(CorBranco)
                    .border(1.dp, if (erro != null) CorErro else CorBorda, FormatoCampo)
                    .clickable { aberto = true }
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = selecionada ?: placeholder,
                    color = if (selecionada == null) CorSecundaria else CorTexto,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = CorTexto,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = aberto,
                onDismissRequest = { aberto = false },
                containerColor = CorBranco
            ) {
                opcoes.forEach { opcao ->
                    DropdownMenuItem(
                        text = { Text(text = opcao, color = CorTexto, fontSize = 14.sp) },
                        onClick = {
                            onSelecionar(opcao)
                            aberto = false
                        }
                    )
                }
            }
        }
        MensagemErroCampo(erro)
    }
}

// Campo de seleção múltipla ("Especialidades")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogaMultiSelectField(
    rotulo: String,
    opcoes: List<String>,
    selecionadas: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione uma ou mais...",
    erro: String? = null
) {
    var aberto by remember { mutableStateOf(false) }
    val textoExibicao = when {
        selecionadas.isEmpty() -> placeholder
        selecionadas.size == 1 -> selecionadas.first()
        else -> "${selecionadas.size} especialidades selecionadas"
    }

    Column(modifier = modifier) {
        RotuloCampo(rotulo)
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(FormatoCampo)
                    .background(CorBranco)
                    .border(1.dp, if (erro != null) CorErro else CorBorda, FormatoCampo)
                    .clickable { aberto = true }
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = textoExibicao,
                    color = if (selecionadas.isEmpty()) CorSecundaria else CorTexto,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = CorTexto,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = aberto,
                onDismissRequest = { aberto = false },
                containerColor = CorBranco
            ) {
                opcoes.forEach { opcao ->
                    val estaMarcada = selecionadas.contains(opcao)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = estaMarcada,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = CorPrimaria)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = opcao,
                                    color = CorTexto,
                                    fontSize = 14.sp,
                                    fontWeight = if (estaMarcada) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = { onToggle(opcao) }
                    )
                }
            }
        }
        if (selecionadas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selecionadas.forEach { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CorPrimaria.copy(alpha = 0.12f))
                            .clickable { onToggle(item) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$item  ✕",
                            fontSize = 12.sp,
                            color = CorPrimaria,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        MensagemErroCampo(erro)
    }
}
