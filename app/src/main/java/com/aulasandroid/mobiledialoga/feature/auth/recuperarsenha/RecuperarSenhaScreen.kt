package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaLogo
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components.NovaSenhaForm
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components.SolicitarCodigoForm
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components.ValidarCodigoForm
import com.aulasandroid.mobiledialoga.ui.theme.MobileDialogaTheme


// Escolhe qual etapa mostrar de acordo com o estado da ViewModel
@Composable
fun RecuperarSenhaScreen(
    onIrParaLogin: () -> Unit,
    viewModel: RecuperarSenhaViewModel = viewModel()
) {
    val estado by viewModel.uiState.observeAsState(RecuperarSenhaUiState())

    AuthLayout {
        DialogaLogo()

        Spacer(modifier = Modifier.height(20.dp))

        when (estado.etapa) {
            EtapaRecuperacao.EMAIL -> SolicitarCodigoForm(
                email = estado.email,
                erroEmail = estado.erroEmail,
                onEmailChange = viewModel::onEmailChange,
                onVoltar = onIrParaLogin,
                onEnviarCodigo = viewModel::enviarCodigo
            )

            EtapaRecuperacao.CODIGO -> ValidarCodigoForm(
                codigo = estado.codigo,
                erroCodigo = estado.erroCodigo,
                onCodigoChange = viewModel::onCodigoChange,
                onRedefinirSenha = viewModel::validarCodigo,
                onReenviar = viewModel::reenviarCodigo
            )

            EtapaRecuperacao.NOVA_SENHA -> NovaSenhaForm(
                estado = estado,
                onSenhaChange = viewModel::onSenhaChange,
                onConfirmacaoChange = viewModel::onConfirmacaoChange,
                onSalvar = { viewModel.salvar(onSenhaRedefinida = onIrParaLogin) }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecuperarSenhaScreenPreview() {
    MobileDialogaTheme {
        RecuperarSenhaScreen(onIrParaLogin = {})
    }
}
