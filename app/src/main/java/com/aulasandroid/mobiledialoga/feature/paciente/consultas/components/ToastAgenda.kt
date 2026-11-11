package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.MensagemToast
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import kotlinx.coroutines.delay

private const val DURACAO_TOAST_MS = 3500L

// Aviso verde "✓ Consulta agendada!" (vermelho quando [mensagem.erro]); some sozinho
@Composable
fun ToastAgenda(
    mensagem: MensagemToast,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(mensagem) {
        delay(DURACAO_TOAST_MS)
        onFechar()
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (mensagem.erro) CorErro else CorSucesso,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = if (mensagem.erro) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                tint = CorBranco,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = mensagem.texto, color = CorBranco, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
