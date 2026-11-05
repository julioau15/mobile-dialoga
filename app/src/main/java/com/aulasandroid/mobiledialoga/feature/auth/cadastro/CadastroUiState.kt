package com.aulasandroid.mobiledialoga.feature.auth.cadastro

enum class TipoConta(val rotulo: String) {
    PACIENTE("Paciente"),
    PROFISSIONAL("Profissional")
}

// As etapas do cadastro. A ViewModel decide qual está ativa.
enum class EtapaCadastro {
    DADOS_PESSOAIS,
    SENHA,
    EM_ANALISE
}

data class CadastroUiState(
    val etapa: EtapaCadastro = EtapaCadastro.DADOS_PESSOAIS,
    val tipoConta: TipoConta = TipoConta.PACIENTE,
    val nome: String = "",
    val email: String = "",
    val celular: String = "",
    val especialidade: String? = null,
    val senha: String = "",
    val confirmacao: String = "",
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroCelular: String? = null,
    val erroEspecialidade: String? = null,
    val erroSenha: String? = null,
    val erroConfirmacao: String? = null
)
