package com.example.mobiledialoga.feature.auth.recuperarsenha.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledialoga.R
import com.example.mobiledialoga.core.components.CabecalhoCard
import com.example.mobiledialoga.core.components.DialogaButton
import com.example.mobiledialoga.core.components.DialogaCard
import com.example.mobiledialoga.core.components.DialogaTextField
import com.example.mobiledialoga.ui.theme.CorSecundaria

// Etapa 1: "Redefinir senha"
@Composable
fun SolicitarCodigoForm(
    email: String,
    erroEmail: String?,
    onEmailChange: (String) -> Unit,
    onVoltar: () -> Unit,
    onEnviarCodigo: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogaCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onVoltar)
                .padding(vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = CorSecundaria,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Voltar",
                color = CorSecundaria,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CabecalhoCard(
            titulo = "Redefinir senha",
            subtitulo = "Informe seu e-mail e enviaremos um link para redefinir sua senha."
        )

        DialogaTextField(
            rotulo = "E-mail",
            placeholder = "seu@email.com",
            value = email,
            onValueChange = onEmailChange,
            icone = R.drawable.ic_email,
            erro = erroEmail,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        DialogaButton(texto = "Enviar código", onClick = onEnviarCodigo, modifier = Modifier.fillMaxWidth())
    }
}
