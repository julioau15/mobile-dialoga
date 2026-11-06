package com.aulasandroid.mobiledialoga.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.DialogaTextLink
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroScreen
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroViewModel
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.components.PerfilEmAnalise
import com.aulasandroid.mobiledialoga.feature.auth.components.AuthLayout
import com.aulasandroid.mobiledialoga.feature.auth.login.LoginScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaViewModel
import com.aulasandroid.mobiledialoga.feature.paciente.PacienteScreen
import com.aulasandroid.mobiledialoga.feature.profissional.ProfissionalScreen


// Escolhe qual tela mostrar de acordo com a rota atual
@Composable
fun AppNavigation() {
    val authState = useAuth()
    val auth = authState.auth

    var rota by rememberSaveable { mutableStateOf(Rota.LOGIN) }

    val cadastroViewModel: CadastroViewModel = viewModel()
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = viewModel()

    // Ao voltar para o login, os fluxos começam do zero na próxima vez
    val irParaLogin = {
        cadastroViewModel.reiniciar()
        recuperarSenhaViewModel.reiniciar()
        rota = Rota.LOGIN
    }

    if (auth != null) {
        // Se estiver autenticado, exibe a tela correspondente ao nível do usuário
        // Nivel 1 = Profissional, Nivel 2 = Paciente
        if (auth.nivel == "1" || auth.nivel == "1.0") {
            ProfissionalScreen(onLogout = irParaLogin)
        } else {
            PacienteScreen(onLogout = irParaLogin)
        }
        return
    }

    // Botão "voltar" do Android: volta uma etapa do fluxo ou, no início dele, volta ao login
    BackHandler(enabled = rota != Rota.LOGIN) {
        val voltouEtapa = when (rota) {
            Rota.CADASTRO -> cadastroViewModel.voltar()
            Rota.RECUPERAR_SENHA -> recuperarSenhaViewModel.voltar()
            Rota.LOGIN -> false
            else -> false
        }
        if (!voltouEtapa) irParaLogin()
    }

    when (rota) {
        Rota.LOGIN -> LoginScreen(
            onCadastrar = { rota = Rota.CADASTRO },
            onEsqueciSenha = { rota = Rota.RECUPERAR_SENHA },
            onLoginSuccess = {},
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

        else -> LoginScreen(
            onCadastrar = { rota = Rota.CADASTRO },
            onEsqueciSenha = { rota = Rota.RECUPERAR_SENHA },
            onLoginSuccess = {},
            onProfissionalEmAnalise = { rota = Rota.EM_ANALISE }
        )
    }
}
