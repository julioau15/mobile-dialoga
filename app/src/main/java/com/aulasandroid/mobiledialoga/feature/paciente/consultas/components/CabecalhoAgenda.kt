package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

private val FormatoBotaoNovaConsulta = RoundedCornerShape(12.dp)

// "Agenda / Suas consultas e atendimentos" + botão roxo "+ Nova consulta"
@Composable
fun CabecalhoAgenda(
    onNovaConsulta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Agenda", color = CorTexto, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "Suas consultas e atendimentos", color = CorSecundaria, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onNovaConsulta,
            shape = FormatoBotaoNovaConsulta,
            colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .height(44.dp)
                .shadow(4.dp, FormatoBotaoNovaConsulta, ambientColor = CorPrimaria, spotColor = CorPrimaria)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Nova consulta", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
