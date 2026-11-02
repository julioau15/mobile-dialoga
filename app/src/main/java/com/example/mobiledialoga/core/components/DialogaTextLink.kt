package com.example.mobiledialoga.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mobiledialoga.ui.theme.CorPrimaria
import com.example.mobiledialoga.ui.theme.CorSecundaria

// "Já tem conta? Entrar", "Não tem conta? Cadastre-se", "Voltar para o login"
@Composable
fun DialogaTextLink(
    texto: String,
    link: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$texto ", color = CorSecundaria, fontSize = 14.sp)
        Text(
            text = link,
            color = CorPrimaria,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
