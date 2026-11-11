package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.VisaoAgenda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorEtapaInativa
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// Seletor "Mês | Semana | Dia". [compacto] = versão menor, usada na visão Semana (fica ao lado do título).
@Composable
fun SeletorVisao(
    visao: VisaoAgenda,
    onMudar: (VisaoAgenda) -> Unit,
    modifier: Modifier = Modifier,
    compacto: Boolean = false
) {
    val opcoes = listOf(
        VisaoAgenda.MES to "Mês",
        VisaoAgenda.SEMANA to "Semana",
        VisaoAgenda.DIA to "Dia"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CorEtapaInativa)
            .padding(3.dp)
    ) {
        opcoes.forEach { (opcao, texto) ->
            val selecionada = opcao == visao
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selecionada) CorBranco else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (selecionada) CorPrimaria.copy(alpha = 0.35f) else Color.Transparent,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onMudar(opcao) }
                    .padding(horizontal = if (compacto) 8.dp else 14.dp, vertical = if (compacto) 4.dp else 6.dp)
            ) {
                Text(
                    text = texto,
                    color = if (selecionada) CorTexto else CorSecundaria,
                    fontSize = if (compacto) 11.sp else 13.sp,
                    fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ‹ título › : setas para o período anterior e o próximo
@Composable
fun NavegacaoCalendario(
    titulo: String,
    onAnterior: () -> Unit,
    onProximo: () -> Unit,
    modifier: Modifier = Modifier,
    compacto: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        IconButton(onClick = onAnterior, modifier = Modifier.size(if (compacto) 28.dp else 36.dp)) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = CorSecundaria)
        }
        Text(
            text = titulo,
            color = CorTexto,
            fontSize = if (compacto) 11.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = if (compacto) 2.dp else 8.dp)
        )
        IconButton(onClick = onProximo, modifier = Modifier.size(if (compacto) 28.dp else 36.dp)) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Próximo", tint = CorSecundaria)
        }
    }
}
