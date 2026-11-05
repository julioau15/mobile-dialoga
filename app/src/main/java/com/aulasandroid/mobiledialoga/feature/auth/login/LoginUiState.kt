package com.aulasandroid.mobiledialoga.feature.auth.login

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val erroEmail: String? = null,
    val erroSenha: String? = null
)
