package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha

// As etapas da redefinição de senha. A ViewModel decide qual está ativa.
enum class EtapaRecuperacao {
    EMAIL,
    CODIGO,
    NOVA_SENHA
}

data class RecuperarSenhaUiState(
    val etapa: EtapaRecuperacao = EtapaRecuperacao.EMAIL,
    val email: String = "",
    val codigo: String = "",
    val senha: String = "",
    val confirmacao: String = "",
    val erroEmail: String? = null,
    val erroCodigo: String? = null,
    val erroSenha: String? = null,
    val erroConfirmacao: String? = null
)
