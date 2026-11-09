package com.aulasandroid.mobiledialoga.feature.auth.cadastro.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RotuloCampo
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.TipoConta
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria


// "Tipo de conta": Paciente | Profissional
@Composable
fun SeletorTipoConta(
    selecionado: TipoConta,
    onSelecionar: (TipoConta) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RotuloCampo("Tipo de conta")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TipoConta.entries.forEach { tipo ->
                OpcaoTipoConta(
                    texto = tipo.rotulo,
                    selecionada = tipo == selecionado,
                    onClick = { onSelecionar(tipo) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Componente STATELESS: a aparência depende só de "selecionada", que vem de fora
@Composable
private fun OpcaoTipoConta(
    texto: String,
    selecionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corFundo by animateColorAsState(if (selecionada) CorPrimariaClara else CorBranco, label = "fundoTipoConta")
    val corBorda by animateColorAsState(if (selecionada) CorPrimaria else CorBorda, label = "bordaTipoConta")
    val formato = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(formato)
            .background(corFundo)
            .border(1.dp, corBorda, formato)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = if (selecionada) CorPrimaria else CorSecundaria,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
