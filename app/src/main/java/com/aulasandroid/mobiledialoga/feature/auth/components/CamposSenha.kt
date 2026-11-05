package com.aulasandroid.mobiledialoga.feature.auth.components
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.components.DialogaPasswordField
import com.aulasandroid.mobiledialoga.core.validation.PasswordValidator

// "Senha" + "Confirmar senha", usados no cadastro e na redefinição de senha
@Composable
fun CamposSenha(
    senha: String,
    onSenhaChange: (String) -> Unit,
    erroSenha: String?,
    confirmacao: String,
    onConfirmacaoChange: (String) -> Unit,
    erroConfirmacao: String?
) {
    DialogaPasswordField(
        rotulo = "Senha",
        placeholder = "Mínimo ${PasswordValidator.TAMANHO_MINIMO} caracteres",
        value = senha,
        onValueChange = onSenhaChange,
        erro = erroSenha,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    DialogaPasswordField(
        rotulo = "Confirmar senha",
        placeholder = "Repita a senha",
        value = confirmacao,
        onValueChange = onConfirmacaoChange,
        erro = erroConfirmacao,
        permitirMostrar = false,
        modifier = Modifier.fillMaxWidth()
    )
}
