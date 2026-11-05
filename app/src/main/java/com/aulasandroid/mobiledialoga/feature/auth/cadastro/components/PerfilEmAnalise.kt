package com.aulasandroid.mobiledialoga.feature.auth.cadastro.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Mostrado ao profissional depois de criar a conta
@Composable
fun PerfilEmAnalise(
    onCiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogaCard(modifier = modifier) {
        // TODO: ilustração recortada do protótipo; trocar pelo arquivo original
        Image(
            painter = painterResource(R.drawable.ilustracao_perfil_analise),
            contentDescription = null,
            modifier = Modifier
                .height(104.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Seu perfil foi enviado\npara análise",
            color = CorTexto,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nossa equipe está revisando suas informações para validar seu cadastro. " +
                "Você receberá um e-mail com o resultado em breve.",
            color = CorSecundaria,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DialogaButton(texto = "Ciente", onClick = onCiente, modifier = Modifier.fillMaxWidth())
    }
}
