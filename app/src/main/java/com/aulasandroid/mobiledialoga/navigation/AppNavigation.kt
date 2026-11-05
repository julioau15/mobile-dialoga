package com.aulasandroid.mobiledialoga.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroScreen
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroViewModel
import com.aulasandroid.mobiledialoga.feature.auth.login.LoginScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaScreen
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaViewModel

// Escolhe qual tela mostrar de acordo com a rota atual
@Composable
fun AppNavigation() {
    var rota by rememberSaveable { mutableStateOf(Rota.LOGIN) }

    val cadastroViewModel: CadastroViewModel = viewModel()
    val recuperarSenhaViewModel: RecuperarSenhaViewModel = viewModel()

    // Ao voltar para o login, os fluxos começam do zero na próxima vez
    val irParaLogin = {
        cadastroViewModel.reiniciar()
        recuperarSenhaViewModel.reiniciar()
        rota = Rota.LOGIN
    }

    // Botão "voltar" do Android: volta uma etapa do fluxo ou, no início dele, volta ao login
    BackHandler(enabled = rota != Rota.LOGIN) {
        val voltouEtapa = when (rota) {
            Rota.CADASTRO -> cadastroViewModel.voltar()
            Rota.RECUPERAR_SENHA -> recuperarSenhaViewModel.voltar()
            Rota.LOGIN -> false
        }
        if (!voltouEtapa) irParaLogin()
    }

    when (rota) {
        Rota.LOGIN -> LoginScreen(
            onCadastrar = { rota = Rota.CADASTRO },
            onEsqueciSenha = { rota = Rota.RECUPERAR_SENHA }
        )

        Rota.CADASTRO -> CadastroScreen(
            onIrParaLogin = irParaLogin,
            viewModel = cadastroViewModel
        )

        Rota.RECUPERAR_SENHA -> RecuperarSenhaScreen(
            onIrParaLogin = irParaLogin,
            viewModel = recuperarSenhaViewModel
        )
    }
}
