package com.aulasandroid.mobiledialoga.feature.auth.cadastro

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaTextLink
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.DadosPessoaisForm
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.IndicadorEtapas
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.PerfilEmAnalise
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.SenhaForm
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.ui.theme.MobileDialogaTheme


// Escolhe qual etapa mostrar de acordo com o estado da ViewModel
@Composable
fun CadastroScreen(
    onIrParaLogin: () -> Unit,
    viewModel: CadastroViewModel = viewModel()
) {
    val estado by viewModel.uiState.observeAsState(CadastroUiState())

    AuthLayout {
        when (estado.etapa) {
            EtapaCadastro.DADOS_PESSOAIS -> {
                IndicadorEtapas(etapaAtual = 1, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                DadosPessoaisForm(
                    estado = estado,
                    especialidades = viewModel.especialidades,
                    onTipoContaChange = viewModel::onTipoContaChange,
                    onNomeChange = viewModel::onNomeChange,
                    onEmailChange = viewModel::onEmailChange,
                    onCelularChange = viewModel::onCelularChange,
                    onEspecialidadeChange = viewModel::onEspecialidadeChange,
                    onContinuar = viewModel::continuar
                )
                Spacer(modifier = Modifier.height(24.dp))
                DialogaTextLink(texto = "Já tem conta?", link = "Entrar", onClick = onIrParaLogin)
            }

            EtapaCadastro.SENHA -> {
                IndicadorEtapas(etapaAtual = 2, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                SenhaForm(
                    estado = estado,
                    onSenhaChange = viewModel::onSenhaChange,
                    onConfirmacaoChange = viewModel::onConfirmacaoChange,
                    onVoltar = { viewModel.voltar() },
                    onCriarConta = { viewModel.criarConta(onPacienteCadastrado = onIrParaLogin) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                DialogaTextLink(texto = "Já tem conta?", link = "Entrar", onClick = onIrParaLogin)
            }

            EtapaCadastro.EM_ANALISE -> {
                PerfilEmAnalise(onCiente = onIrParaLogin)
                Spacer(modifier = Modifier.height(24.dp))
                DialogaTextLink(texto = "Voltar para o", link = "login", onClick = onIrParaLogin)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CadastroScreenPreview() {
    MobileDialogaTheme {
        CadastroScreen(onIrParaLogin = {})
    }
}
