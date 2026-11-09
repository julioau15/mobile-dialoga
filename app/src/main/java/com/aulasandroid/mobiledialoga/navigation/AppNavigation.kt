package com.aulasandroid.mobiledialoga.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaTextLink
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.auth.admin.AdminBlockedScreen
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroScreen
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroViewModel
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.PerfilEmAnalise
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.feature.auth.login.LoginScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaViewModel
import com.aulasandroid.mobiledialoga.feature.paciente.PacienteScreen
import com.aulasandroid.mobiledialoga.feature.paciente.primeiroacesso.PrimeiroAcessoPacienteScreen
import com.aulasandroid.mobiledialoga.feature.profissional.ProfissionalScreen
import com.aulasandroid.mobiledialoga.feature.profissional.primeiroacesso.PrimeiroAcessoProfissionalScreen
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val authState = useAuth()
    val auth = authState.auth
    val scope = rememberCoroutineScope()

    var rota by rememberSaveable { mutableStateOf(Rota.LOGIN) }

    val cadastroViewModel: CadastroViewModel = viewModel()
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = viewModel()

    val irParaLogin = {
        cadastroViewModel.reiniciar()
        recuperarSenhaViewModel.reiniciar()
        rota = Rota.LOGIN
    }

    if (auth != null) {
        val nivel = auth.nivel.trim().lowercase()
        val isAdmin = nivel == "0" || nivel == "0.0" || nivel == "3" || nivel == "3.0" || nivel.contains("admin")
        val isProfissional = nivel == "1" || nivel == "1.0" || nivel == "profissional"

        // Verifica se o usuário ainda precisa realizar o primeiro acesso
        val precisaPrimeiroAcesso = authState.userProfile?.primeiroAcesso == true

        if (isAdmin) {
            AdminBlockedScreen(onVoltarParaLogin = { authState.logout(); irParaLogin() })
        } else if (precisaPrimeiroAcesso) {
            if (isProfissional) {
                PrimeiroAcessoProfissionalScreen(
                    nome = authState.userProfile?.nome,
                    onFinalizar = { scope.launch { authState.concluirPrimeiroAcesso() } }
                )
            } else {
                PrimeiroAcessoPacienteScreen(
                    nomeCompleto = authState.userProfile?.nome,
                    onFinalizar = { dados -> 
                        scope.launch { authState.concluirPrimeiroAcesso(dados) }
                    },
                    buscarProfissionais = { query -> authState.buscarProfissionais(query) }
                )
            }
        } else if (isProfissional) {
            ProfissionalScreen(onLogout = irParaLogin)
        } else {
            PacienteScreen(onLogout = irParaLogin)
        }
        return
    }

    BackHandler(enabled = rota != Rota.LOGIN) {
        val voltouEtapa = when (rota) {
            Rota.CADASTRO -> cadastroViewModel.voltar()
            Rota.RECUPERAR_SENHA -> recuperarSenhaViewModel.voltar()
            else -> false
        }
        if (!voltouEtapa) irParaLogin()
    }

    when (rota) {
        Rota.LOGIN -> LoginScreen(
            onCadastrar = { rota = Rota.CADASTRO },
            onEsqueciSenha = { rota = Rota.RECUPERAR_SENHA },
            onLoginSuccess = { /* O redirecionamento é automático pelo estado do authState */ },
            onProfissionalEmAnalise = { rota = Rota.EM_ANALISE }
        )

        Rota.CADASTRO -> CadastroScreen(
            onIrParaLogin = irParaLogin,
            viewModel = cadastroViewModel
        )

        Rota.RECUPERAR_SENHA -> RecuperarSenhaScreen(
            onIrParaLogin = irParaLogin,
            viewModel = recuperarSenhaViewModel
        )

        Rota.EM_ANALISE -> {
            AuthLayout {
                PerfilEmAnalise(onCiente = irParaLogin)
                Spacer(modifier = Modifier.height(24.dp))
                DialogaTextLink(texto = "Voltar para o", link = "login", onClick = irParaLogin)
            }
        }

        else -> irParaLogin()
    }
}
