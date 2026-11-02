package com.example.mobiledialoga.feature.auth.login.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledialoga.R
import com.example.mobiledialoga.core.components.DialogaButton
import com.example.mobiledialoga.core.components.DialogaOutlinedButton
import com.example.mobiledialoga.core.components.DialogaPasswordField
import com.example.mobiledialoga.core.components.DialogaTextField
import com.example.mobiledialoga.feature.auth.login.LoginUiState
import com.example.mobiledialoga.ui.theme.CorPrimaria
import com.example.mobiledialoga.ui.theme.CorSecundaria

@Composable
fun LoginForm(
    estado: LoginUiState,
    onEmailChange: (String) -> Unit,
    onSenhaChange: (String) -> Unit,
    onEsqueciSenha: () -> Unit,
    onEntrar: () -> Unit,
    onEntrarComGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DialogaTextField(
            rotulo = "E-mail",
            placeholder = "seu@email.com",
            value = estado.email,
            onValueChange = onEmailChange,
            icone = R.drawable.ic_email,
            erro = estado.erroEmail,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DialogaPasswordField(
            rotulo = "Senha",
            placeholder = "••••••••",
            value = estado.senha,
            onValueChange = onSenhaChange,
            erro = estado.erroSenha,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Esqueceu sua senha?",
            color = CorPrimaria,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onEsqueciSenha)
        )

        Spacer(modifier = Modifier.height(16.dp))

        DialogaButton(texto = "Entrar", onClick = onEntrar, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CorSecundaria.copy(alpha = 0.6f))
            Text(
                text = "ou continue com",
                color = CorSecundaria,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = CorSecundaria.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        DialogaOutlinedButton(
            texto = "Continuar com Google",
            onClick = onEntrarComGoogle,
            corConteudo = CorPrimaria,
            corBorda = CorPrimaria,
            icone = R.drawable.ic_google,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
