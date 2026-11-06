package com.aulasandroid.mobiledialoga.feature.auth.cadastro

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaTextLink
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.DadosPessoaisForm
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.IndicadorEtapas
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.PerfilEmAnalise
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.SenhaForm
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.ui.theme.MobileDialogaTheme


@Composable
fun CadastroScreen(
    onIrParaLogin: () -> Unit,
    viewModel: CadastroViewModel = viewModel()
) {
    val estado by viewModel.uiState.observeAsState(CadastroUiState())

    AuthLayout {
        if (estado.erroGeral != null && estado.etapa != EtapaCadastro.EM_ANALISE) {
            Text(
                text = estado.erroGeral!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        when (estado.etapa) {
            EtapaCadastro.DADOS_PESSOAIS -> {
                IndicadorEtapas(etapaAtual = 1, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                DadosPessoaisForm(
                    estado = estado,
                    especialidades = estado.especialidadesList.map { it.nome },
                    onTipoContaChange = viewModel::onTipoContaChange,
                    onNomeChange = viewModel::onNomeChange,
                    onEmailChange = viewModel::onEmailChange,
                    onCelularChange = viewModel::onCelularChange,
                    onCpfChange = viewModel::onCpfChange,
                    onCrpChange = viewModel::onCrpChange,
                    onInstituicaoChange = viewModel::onInstituicaoChange,
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
