package com.aulasandroid.mobiledialoga.feature.auth.cadastro.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorEtapaConcluida
import com.aulasandroid.mobiledialoga.ui.theme.CorEtapaInativa
import com.aulasandroid.mobiledialoga.ui.theme.CorEtapaInativaTexto
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria


// Bolinhas "1 —— 2" acima do card. Etapas anteriores aparecem com ✓.
@Composable
fun IndicadorEtapas(
    etapaAtual: Int,
    modifier: Modifier = Modifier,
    totalEtapas: Int = 2
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (numero in 1..totalEtapas) {
            if (numero > 1) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(32.dp)
                        .height(2.dp)
                        .background(CorBorda)
                )
            }
            BolinhaEtapa(numero = numero, etapaAtual = etapaAtual)
        }
    }
}

@Composable
private fun BolinhaEtapa(numero: Int, etapaAtual: Int) {
    val concluida = numero < etapaAtual
    val atual = numero == etapaAtual
    val corFundo = when {
        concluida -> CorEtapaConcluida
        atual -> CorPrimaria
        else -> CorEtapaInativa
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(corFundo, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (concluida) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Etapa $numero concluída",
                tint = CorBranco,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = numero.toString(),
                color = if (atual) CorBranco else CorEtapaInativaTexto,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
