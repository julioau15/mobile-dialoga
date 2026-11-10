package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.components.CabecalhoCard
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.feature.auth.components.CamposSenha
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaUiState

// Etapa 3: "Defina sua senha" + Salvar
@Composable
fun NovaSenhaForm(
    estado: RecuperarSenhaUiState,
    onSenhaChange: (String) -> Unit,
    onConfirmacaoChange: (String) -> Unit,
    onSalvar: () -> Unit,
    isLoading: Boolean = false,
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

        DialogaButton(texto = "Salvar", onClick = onSalvar, isLoading = isLoading, modifier = Modifier.fillMaxWidth())
    }
}
