package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.DialogaDialogo
import com.aulasandroid.mobiledialoga.components.DialogaOutlinedButton
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

// Pop-up "Cancelar consulta": "Voltar" ou "Sim" (vermelho)
@Composable
fun CancelarConsultaDialog(
    cancelando: Boolean,
    onVoltar: () -> Unit,
    onConfirmar: () -> Unit
) {
    DialogaDialogo(
        titulo = "Cancelar consulta",
        onFechar = onVoltar,
        rodape = {
            DialogaOutlinedButton(
                texto = "Voltar",
                onClick = { if (!cancelando) onVoltar() },
                corConteudo = CorTexto
            )
            Button(
                onClick = onConfirmar,
                enabled = !cancelando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CorErro,
                    contentColor = CorBranco,
                    disabledContainerColor = CorErro.copy(alpha = 0.5f),
                    disabledContentColor = CorBranco
                ),
                modifier = Modifier.height(46.dp)
            ) {
                Text(text = if (cancelando) "Cancelando..." else "Sim", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Text(text = "Deseja cancelar a sua consulta ?", color = CorTexto, fontSize = 14.sp)
    }
}
