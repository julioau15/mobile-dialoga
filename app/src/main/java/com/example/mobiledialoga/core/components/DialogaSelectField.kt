package com.example.mobiledialoga.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledialoga.R
import com.example.mobiledialoga.ui.theme.CorBorda
import com.example.mobiledialoga.ui.theme.CorBranco
import com.example.mobiledialoga.ui.theme.CorErro
import com.example.mobiledialoga.ui.theme.CorTexto

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
                    color = CorTexto,
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
