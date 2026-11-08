package com.aulasandroid.mobiledialoga.feature.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.core.error.ErrorTranslator
import com.aulasandroid.mobiledialoga.core.validation.EmailValidator
import com.aulasandroid.mobiledialoga.data.AuthException
import com.aulasandroid.mobiledialoga.data.AuthState
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState

    private val estado: LoginUiState get() = _uiState.value ?: LoginUiState()

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim(), erroEmail = null, erroGeral = null)
    }

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null, erroGeral = null)
    }

    fun entrar(authState: AuthState, onLoginSuccess: (String) -> Unit, onProfissionalEmAnalise: () -> Unit) {
        val erroEmail = EmailValidator.validar(estado.email)
        val erroSenha = if (estado.senha.isEmpty()) "Informe a senha" else null
        _uiState.value = estado.copy(erroEmail = erroEmail, erroSenha = erroSenha, erroGeral = null)
        if (erroEmail != null || erroSenha != null) return

        viewModelScope.launch {
            _uiState.value = estado.copy(isLoading = true, erroGeral = null)
            try {
                val auth = authState.login(estado.email, estado.senha)
                _uiState.value = estado.copy(isLoading = false)
                onLoginSuccess(auth.nivel)
            } catch (e: AuthException) {
                if (e.codigo == "PROFISSIONAL_EM_ANALISE" || e.codigo == "PROFISSIONAL_RECUSADO") {
                    _uiState.value = estado.copy(isLoading = false)
                    onProfissionalEmAnalise()
                } else {
                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroGeral = ErrorTranslator.traduzirLogin(e.message)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = estado.copy(
                    isLoading = false,
                    erroGeral = ErrorTranslator.traduzirLogin(e.message)
                )
            }
        }
    }

    fun entrarComGoogle() {
        // TODO: integrar Google Sign-In
    }
}
