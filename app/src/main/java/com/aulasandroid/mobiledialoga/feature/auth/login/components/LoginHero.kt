package com.aulasandroid.mobiledialoga.feature.auth.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.DialogaLogo
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Logo + "Bem-vindo de volta"
@Composable
fun LoginHero(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        DialogaLogo(altura = 76.dp)
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Bem-vindo de volta",
            color = CorTexto,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Entre na sua conta para continuar",
            color = CorSecundaria,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
