package com.example.mobiledialoga.feature.auth.recuperarsenha.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobiledialoga.core.components.CabecalhoCard
import com.example.mobiledialoga.core.components.DialogaButton
import com.example.mobiledialoga.core.components.DialogaCard
import com.example.mobiledialoga.feature.auth.components.CamposSenha
import com.example.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaUiState

// Etapa 3: "Defina sua senha" + Salvar
@Composable
fun NovaSenhaForm(
    estado: RecuperarSenhaUiState,
    onSenhaChange: (String) -> Unit,
    onConfirmacaoChange: (String) -> Unit,
    onSalvar: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogaCard(modifier = modifier) {
        CabecalhoCard(titulo = "Defina sua senha", subtitulo = "Crie uma senha segura para sua conta")

        CamposSenha(
            senha = estado.senha,
            onSenhaChange = onSenhaChange,
            erroSenha = estado.erroSenha,
            confirmacao = estado.confirmacao,
            onConfirmacaoChange = onConfirmacaoChange,
            erroConfirmacao = estado.erroConfirmacao
        )

        Spacer(modifier = Modifier.height(24.dp))

        DialogaButton(texto = "Salvar", onClick = onSalvar, modifier = Modifier.fillMaxWidth())
    }
}
