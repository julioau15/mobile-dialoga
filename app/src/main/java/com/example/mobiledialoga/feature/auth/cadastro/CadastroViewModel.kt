package com.example.mobiledialoga.feature.auth.cadastro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobiledialoga.core.validation.EmailValidator
import com.example.mobiledialoga.core.validation.PasswordValidator
import com.example.mobiledialoga.core.validation.PhoneValidator

class CadastroViewModel : ViewModel() {

    // TODO: buscar da API (ApiRotas.Especialidades.BASE)
    val especialidades: List<String> = listOf(
        "Psicologia",
        "Psiquiatria",
        "Neuropsicologia",
        "Terapia Ocupacional",
        "Fonoaudiologia"
    )

    // Privada (underscore notation): só a ViewModel altera
    private val _uiState = MutableLiveData(CadastroUiState())

    // Pública (somente leitura): a View observa por aqui
    val uiState: LiveData<CadastroUiState> = _uiState

    private val estado: CadastroUiState get() = _uiState.value ?: CadastroUiState()

    // ---------- Etapa 1: dados pessoais ----------

    fun onTipoContaChange(tipo: TipoConta) {
        _uiState.value = estado.copy(tipoConta = tipo, erroEspecialidade = null)
    }

    fun onNomeChange(nome: String) {
        _uiState.value = estado.copy(nome = nome, erroNome = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim(), erroEmail = null)
    }

    fun onCelularChange(celular: String) {
        _uiState.value = estado.copy(celular = PhoneValidator.limpar(celular), erroCelular = null)
    }

    fun onEspecialidadeChange(especialidade: String) {
        _uiState.value = estado.copy(especialidade = especialidade, erroEspecialidade = null)
    }

    fun continuar() {
        val atual = estado
        val validado = atual.copy(
            erroNome = if (atual.nome.isBlank()) "Informe seu nome completo" else null,
            erroEmail = EmailValidator.validar(atual.email),
            erroCelular = PhoneValidator.validar(atual.celular),
            erroEspecialidade = if (atual.tipoConta == TipoConta.PROFISSIONAL && atual.especialidade == null) {
                "Selecione sua especialidade"
            } else null
        )
        val temErro = listOf(
            validado.erroNome, validado.erroEmail, validado.erroCelular, validado.erroEspecialidade
        ).any { it != null }

        _uiState.value = if (temErro) validado else validado.copy(etapa = EtapaCadastro.SENHA)
    }

    // ---------- Etapa 2: senha ----------

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null)
    }

    fun onConfirmacaoChange(confirmacao: String) {
        _uiState.value = estado.copy(confirmacao = confirmacao, erroConfirmacao = null)
    }

    // Paciente volta direto para o login; profissional vai para a tela de análise
    fun criarConta(onPacienteCadastrado: () -> Unit) {
        val atual = estado
        val erroSenha = PasswordValidator.validar(atual.senha)
        val erroConfirmacao = PasswordValidator.validarConfirmacao(atual.senha, atual.confirmacao)
        _uiState.value = atual.copy(erroSenha = erroSenha, erroConfirmacao = erroConfirmacao)
        if (erroSenha != null || erroConfirmacao != null) return

        // TODO: enviar para a API (ApiRotas.Pacientes.BASE / ApiRotas.Profissionais.BASE)

        if (atual.tipoConta == TipoConta.PROFISSIONAL) {
            _uiState.value = estado.copy(etapa = EtapaCadastro.EM_ANALISE)
        } else {
            reiniciar()
            onPacienteCadastrado()
        }
    }

    // ---------- Navegação entre etapas ----------

    // Retorna true se voltou uma etapa; false se já está no início (quem chamou sai do cadastro)
    fun voltar(): Boolean {
        if (estado.etapa != EtapaCadastro.SENHA) return false
        _uiState.value = estado.copy(etapa = EtapaCadastro.DADOS_PESSOAIS)
        return true
    }

    fun reiniciar() {
        _uiState.value = CadastroUiState()
    }
}
