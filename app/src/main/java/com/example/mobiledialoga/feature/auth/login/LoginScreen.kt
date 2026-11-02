package com.example.mobiledialoga.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiledialoga.core.components.DialogaTextLink
import com.example.mobiledialoga.feature.auth.login.components.LoginForm
import com.example.mobiledialoga.feature.auth.login.components.LoginHero
import com.example.mobiledialoga.ui.theme.CorFundoLogin
import com.example.mobiledialoga.ui.theme.CorSecundaria
import com.example.mobiledialoga.ui.theme.MobileDialogaTheme

@Composable
fun LoginScreen(
    onCadastrar: () -> Unit,
    onEsqueciSenha: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.observeAsState(LoginUiState())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoLogin)
            .systemBarsPadding()
            .imePadding()
    ) {
        // ---------- Conteúdo principal (rolável) ----------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            LoginHero(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            LoginForm(
                estado = estado,
                onEmailChange = viewModel::onEmailChange,
                onSenhaChange = viewModel::onSenhaChange,
                onEsqueciSenha = onEsqueciSenha,
                onEntrar = viewModel::entrar,
                onEntrarComGoogle = viewModel::entrarComGoogle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            DialogaTextLink(texto = "Não tem conta?", link = "Cadastre-se", onClick = onCadastrar)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ---------- Rodapé ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // TODO: abrir os documentos quando existirem
            Text(text = "Termos de Uso", color = CorSecundaria, fontSize = 13.sp)
            Text(text = "  |  ", color = CorSecundaria, fontSize = 13.sp)
            Text(text = "Política de Privacidade", color = CorSecundaria, fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MobileDialogaTheme {
        LoginScreen(onCadastrar = {}, onEsqueciSenha = {})
    }
}
