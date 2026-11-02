package com.example.mobiledialoga.feature.auth.cadastro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobiledialoga.core.components.CabecalhoCard
import com.example.mobiledialoga.core.components.DialogaButton
import com.example.mobiledialoga.core.components.DialogaCard
import com.example.mobiledialoga.core.components.DialogaOutlinedButton
import com.example.mobiledialoga.feature.auth.cadastro.CadastroUiState
import com.example.mobiledialoga.feature.auth.components.CamposSenha

// Etapa 2 do cadastro: "Defina sua senha"
@Composable
fun SenhaForm(
    estado: CadastroUiState,
    onSenhaChange: (String) -> Unit,
    onConfirmacaoChange: (String) -> Unit,
    onVoltar: () -> Unit,
    onCriarConta: () -> Unit,
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DialogaOutlinedButton(texto = "Voltar", onClick = onVoltar, modifier = Modifier.weight(1f))
            DialogaButton(texto = "Criar conta", onClick = onCriarConta, modifier = Modifier.weight(1f))
        }
    }
}
