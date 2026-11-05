package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.core.validation.EmailValidator
import com.aulasandroid.mobiledialoga.core.validation.PasswordValidator


class RecuperarSenhaViewModel : ViewModel() {

    // Privada (underscore notation): só a ViewModel altera
    private val _uiState = MutableLiveData(RecuperarSenhaUiState())

    // Pública (somente leitura): a View observa por aqui
    val uiState: LiveData<RecuperarSenhaUiState> = _uiState

    private val estado: RecuperarSenhaUiState get() = _uiState.value ?: RecuperarSenhaUiState()

    // ---------- Etapa 1: e-mail ----------

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim(), erroEmail = null)
    }

    fun enviarCodigo() {
        val erroEmail = EmailValidator.validar(estado.email)
        if (erroEmail != null) {
            _uiState.value = estado.copy(erroEmail = erroEmail)
            return
        }

        // TODO: chamar a API (ApiRotas.Auth.RECUPERAR_SENHA_SOLICITAR)
        _uiState.value = estado.copy(etapa = EtapaRecuperacao.CODIGO)
    }

    // ---------- Etapa 2: código ----------

    fun onCodigoChange(codigo: String) {
        _uiState.value = estado.copy(
            codigo = codigo.filter { it.isDigit() }.take(TAMANHO_CODIGO),
            erroCodigo = null
        )
    }

    fun validarCodigo() {
        if (estado.codigo.length < TAMANHO_CODIGO) {
            _uiState.value = estado.copy(erroCodigo = "Digite o código de $TAMANHO_CODIGO dígitos")
            return
        }

        // TODO: chamar a API (ApiRotas.Auth.RECUPERAR_SENHA_VALIDAR_CODIGO)
        _uiState.value = estado.copy(etapa = EtapaRecuperacao.NOVA_SENHA)
    }

    fun reenviarCodigo() {
        // TODO: chamar a API (ApiRotas.Auth.RECUPERAR_SENHA_SOLICITAR) novamente
        _uiState.value = estado.copy(codigo = "", erroCodigo = null)
    }

    // ---------- Etapa 3: nova senha ----------

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null)
    }

    fun onConfirmacaoChange(confirmacao: String) {
        _uiState.value = estado.copy(confirmacao = confirmacao, erroConfirmacao = null)
    }

    fun salvar(onSenhaRedefinida: () -> Unit) {
        val atual = estado
        val erroSenha = PasswordValidator.validar(atual.senha)
        val erroConfirmacao = PasswordValidator.validarConfirmacao(atual.senha, atual.confirmacao)
        _uiState.value = atual.copy(erroSenha = erroSenha, erroConfirmacao = erroConfirmacao)
        if (erroSenha != null || erroConfirmacao != null) return

        // TODO: chamar a API (ApiRotas.Auth.RECUPERAR_SENHA_REDEFINIR)
        reiniciar()
        onSenhaRedefinida()
    }

    // ---------- Navegação entre etapas ----------

    // Retorna true se voltou uma etapa; false se já está no início (quem chamou sai da tela)
    fun voltar(): Boolean {
        val anterior = when (estado.etapa) {
            EtapaRecuperacao.EMAIL -> return false
            EtapaRecuperacao.CODIGO -> EtapaRecuperacao.EMAIL
            EtapaRecuperacao.NOVA_SENHA -> EtapaRecuperacao.CODIGO
        }
        _uiState.value = estado.copy(etapa = anterior)
        return true
    }

    fun reiniciar() {
        _uiState.value = RecuperarSenhaUiState()
    }

    companion object {
        const val TAMANHO_CODIGO = 4
    }
}
