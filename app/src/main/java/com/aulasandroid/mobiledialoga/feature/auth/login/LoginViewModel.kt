package com.aulasandroid.mobiledialoga.feature.auth.login

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
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
                val lowerMsg = (e.message ?: "").lowercase()
                if (e.codigo == "PROFISSIONAL_EM_ANALISE" || e.codigo == "PROFISSIONAL_RECUSADO" ||
                    lowerMsg.contains("análise") || lowerMsg.contains("analise") ||
                    lowerMsg.contains("aprovação") || lowerMsg.contains("aprovacao") ||
                    lowerMsg.contains("pendente") || lowerMsg.contains("aguardando")) {
                    _uiState.value = estado.copy(isLoading = false)
                    onProfissionalEmAnalise()
                } else {
                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroGeral = ErrorTranslator.traduzirLogin(e.message)
                    )
                }
            } catch (e: Exception) {
                val lowerMsg = (e.message ?: "").lowercase()
                if (lowerMsg.contains("análise") || lowerMsg.contains("analise") ||
                    lowerMsg.contains("aprovação") || lowerMsg.contains("aprovacao") ||
                    lowerMsg.contains("pendente") || lowerMsg.contains("aguardando")) {
                    _uiState.value = estado.copy(isLoading = false)
                    onProfissionalEmAnalise()
                } else {
                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroGeral = ErrorTranslator.traduzirLogin(e.message)
                    )
                }
            }
        }
    }

    fun entrarComGoogle(
        context: Context,
        authState: AuthState,
        onLoginSuccess: (String) -> Unit,
        onProfissionalEmAnalise: () -> Unit,
        onCadastrarComGoogle: (String, String, String?) -> Unit
    ) {
        if (estado.isLoading || estado.isGoogleLoading) return
        viewModelScope.launch {
            _uiState.value = estado.copy(isGoogleLoading = true, erroGeral = null)
            try {
                val googleData = GoogleSignIn.obterDadosGoogle(context)
                val auth = try {
                    authState.loginGoogle(googleData.idGoogle, googleData.email)
                } catch (e: Exception) {
                    // Se a conta não existe (ou falhou o login com Google), direciona para o cadastro
                    _uiState.value = estado.copy(isGoogleLoading = false)
                    onCadastrarComGoogle(googleData.idGoogle, googleData.email, googleData.nome)
                    return@launch
                }
                _uiState.value = estado.copy(isGoogleLoading = false)
                onLoginSuccess(auth.nivel)
            } catch (e: GetCredentialCancellationException) {
                // a pessoa fechou o seletor de contas: não é erro
                _uiState.value = estado.copy(isGoogleLoading = false)
            } catch (e: NoCredentialException) {
                _uiState.value = estado.copy(
                    isGoogleLoading = false,
                    erroGeral = "Nenhuma conta Google encontrada neste aparelho. Adicione uma conta e tente novamente."
                )
            } catch (e: GetCredentialException) {
                _uiState.value = estado.copy(isGoogleLoading = false, erroGeral = "Não foi possível entrar com o Google.")
            } catch (e: Exception) {
                _uiState.value = estado.copy(
                    isGoogleLoading = false,
                    erroGeral = ErrorTranslator.traduzirLogin(e.message)
                )
            }
        }
    }
}
