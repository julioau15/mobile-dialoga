package com.aulasandroid.mobiledialoga.feature.auth.login

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val erroGeral: String? = null,
    // true = o Credential Manager não funcionou: a tela abre o seletor de contas do aparelho
    val escolherContaDoAparelho: Boolean = false
)
