package com.aulasandroid.mobiledialoga.feature.auth.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.DialogaLogo
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

@Composable
fun AdminBlockedScreen(
    onVoltarParaLogin: () -> Unit
) {
    AuthLayout {
        DialogaLogo(altura = 56.dp)

        Spacer(modifier = Modifier.height(24.dp))

        DialogaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(CorPrimaria.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Acesso Desktop",
                        tint = CorPrimaria,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Acesso Restrito ao Desktop",
                    color = CorTexto,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A área administrativa do DiaLoga não possui versão para dispositivos móveis.",
                    color = CorSecundaria,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Para acessar o painel de administração, utilize um navegador no seu computador.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                DialogaButton(
                    texto = "Voltar para o Login",
                    onClick = onVoltarParaLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
