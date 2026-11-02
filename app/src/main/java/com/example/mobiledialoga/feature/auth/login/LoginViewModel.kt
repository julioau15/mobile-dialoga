package com.example.mobiledialoga.feature.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobiledialoga.core.validation.EmailValidator

class LoginViewModel : ViewModel() {

    // Privada (underscore notation): só a ViewModel altera
    private val _uiState = MutableLiveData(LoginUiState())

    // Pública (somente leitura): a View observa por aqui
    val uiState: LiveData<LoginUiState> = _uiState

    private val estado: LoginUiState get() = _uiState.value ?: LoginUiState()

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim(), erroEmail = null)
    }

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null)
    }

    fun entrar() {
        val erroEmail = EmailValidator.validar(estado.email)
        val erroSenha = if (estado.senha.isEmpty()) "Informe a senha" else null
        _uiState.value = estado.copy(erroEmail = erroEmail, erroSenha = erroSenha)
        if (erroEmail != null || erroSenha != null) return

        // TODO: chamar a API (ApiRotas.Auth.LOGIN) e navegar conforme o nível do usuário
    }

    fun entrarComGoogle() {
        // TODO: integrar Google Sign-In (ApiRotas.Auth.loginGoogle)
    }
}
