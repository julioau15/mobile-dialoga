package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.core.error.ErrorTranslator
import com.aulasandroid.mobiledialoga.core.validation.EmailValidator
import com.aulasandroid.mobiledialoga.core.validation.PasswordValidator
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarSenhaViewModel : ViewModel() {

    private val _uiState = MutableLiveData(RecuperarSenhaUiState())
    val uiState: LiveData<RecuperarSenhaUiState> = _uiState

    private val estado: RecuperarSenhaUiState get() = _uiState.value ?: RecuperarSenhaUiState()

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim().lowercase(), erroEmail = null, erroGeral = null)
    }

    fun enviarCodigo() {
        val erroEmail = EmailValidator.validar(estado.email)
        if (erroEmail != null) {
            _uiState.value = estado.copy(erroEmail = erroEmail, erroGeral = null)
            return
        }

        _uiState.value = estado.copy(isLoading = true, erroGeral = null)

        // Valida primeiro se o e-mail existe no sistema
        RetrofitFactory().getAuthService().verificarCadastro(email = estado.email, celular = "00000000000").enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val isEmailCadastrado = !response.isSuccessful // 409 Conflict significa que o e-mail já existe
                if (!isEmailCadastrado) {
                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroEmail = "O e-mail informado está incorreto."
                    )
                } else {
                    executarSolicitarCodigo()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                executarSolicitarCodigo()
            }
        })
    }

    private fun executarSolicitarCodigo() {
        val body = JsonObject().apply { addProperty("email", estado.email) }

        RetrofitFactory().getAuthService().solicitarRecuperarSenha(body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val json = if (response.isSuccessful) {
                    response.body()
                } else {
                    val errorStr = response.errorBody()?.string()
                    if (!errorStr.isNullOrBlank()) {
                        try { JsonParser().parse(errorStr).asJsonObject } catch (e: Exception) { null }
                    } else null
                }
                val isSuccess = response.isSuccessful || (json?.has("status") == true && json.get("status").asBoolean)
                if (isSuccess) {
                    _uiState.value = estado.copy(isLoading = false, etapa = EtapaRecuperacao.CODIGO, erroGeral = null)
                } else {
                    val rawMsg = json?.get("message")?.asString ?: json?.get("erro")?.asString
                    val msg = ErrorTranslator.traduzirRecuperacaoSenha(rawMsg)
                    _uiState.value = estado.copy(isLoading = false, erroGeral = msg)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(
                    isLoading = false,
                    erroGeral = ErrorTranslator.traduzirRecuperacaoSenha(t.message)
                )
            }
        })
    }

    fun onCodigoChange(codigo: String) {
        _uiState.value = estado.copy(
            codigo = codigo.filter { it.isDigit() }.take(TAMANHO_CODIGO),
            erroCodigo = null,
            erroGeral = null
        )
    }

    fun validarCodigo() {
        if (estado.codigo.length < TAMANHO_CODIGO) {
            _uiState.value = estado.copy(erroCodigo = "Digite o código de $TAMANHO_CODIGO dígitos", erroGeral = null)
            return
        }

        _uiState.value = estado.copy(isLoading = true, erroGeral = null)
        val body = JsonObject().apply {
            addProperty("email", estado.email)
            addProperty("codigo", estado.codigo)
        }

        RetrofitFactory().getAuthService().validarCodigoRecuperacao(body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val json = if (response.isSuccessful) {
                    response.body()
                } else {
                    val errorStr = response.errorBody()?.string()
                    if (!errorStr.isNullOrBlank()) {
                        try { JsonParser().parse(errorStr).asJsonObject } catch (e: Exception) { null }
                    } else null
                }
                val isSuccess = response.isSuccessful || (json?.has("status") == true && json.get("status").asBoolean)
                if (isSuccess) {
                    _uiState.value = estado.copy(isLoading = false, etapa = EtapaRecuperacao.NOVA_SENHA, erroGeral = null)
                } else {
                    val rawMsg = json?.get("message")?.asString ?: json?.get("erro")?.asString
                    val msg = ErrorTranslator.traduzirRecuperacaoSenha(rawMsg)
                    _uiState.value = estado.copy(isLoading = false, erroCodigo = msg, erroGeral = msg)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(
                    isLoading = false,
                    erroGeral = ErrorTranslator.traduzirRecuperacaoSenha(t.message)
                )
            }
        })
    }

    fun reenviarCodigo() {
        _uiState.value = estado.copy(isLoading = true, codigo = "", erroCodigo = null, erroGeral = null)
        val body = JsonObject().apply { addProperty("email", estado.email) }

        RetrofitFactory().getAuthService().solicitarRecuperarSenha(body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                _uiState.value = estado.copy(isLoading = false)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(isLoading = false)
            }
        })
    }

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null, erroGeral = null)
    }

    fun onConfirmacaoChange(confirmacao: String) {
        _uiState.value = estado.copy(confirmacao = confirmacao, erroConfirmacao = null, erroGeral = null)
    }

    fun salvar(onSenhaRedefinida: () -> Unit) {
        val atual = estado
        val erroSenha = PasswordValidator.validar(atual.senha)
        val erroConfirmacao = PasswordValidator.validarConfirmacao(atual.senha, atual.confirmacao)
        _uiState.value = atual.copy(erroSenha = erroSenha, erroConfirmacao = erroConfirmacao, erroGeral = null)
        if (erroSenha != null || erroConfirmacao != null) return

        _uiState.value = estado.copy(isLoading = true, erroGeral = null)
        val body = JsonObject().apply {
            addProperty("email", estado.email)
            addProperty("codigo", estado.codigo)
            addProperty("senha", estado.senha)
        }

        RetrofitFactory().getAuthService().redefinirSenha(body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val json = if (response.isSuccessful) {
                    response.body()
                } else {
                    val errorStr = response.errorBody()?.string()
                    if (!errorStr.isNullOrBlank()) {
                        try { JsonParser().parse(errorStr).asJsonObject } catch (e: Exception) { null }
                    } else null
                }
                val isSuccess = response.isSuccessful || (json?.has("status") == true && json.get("status").asBoolean)
                if (isSuccess) {
                    reiniciar()
                    onSenhaRedefinida()
                } else {
                    val rawMsg = json?.get("message")?.asString ?: json?.get("erro")?.asString
                    val msg = ErrorTranslator.traduzirRecuperacaoSenha(rawMsg)
                    _uiState.value = estado.copy(isLoading = false, erroGeral = msg)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(
                    isLoading = false,
                    erroGeral = ErrorTranslator.traduzirRecuperacaoSenha(t.message)
                )
            }
        })
    }

    fun voltar(): Boolean {
        val anterior = when (estado.etapa) {
            EtapaRecuperacao.EMAIL -> return false
            EtapaRecuperacao.CODIGO -> EtapaRecuperacao.EMAIL
            EtapaRecuperacao.NOVA_SENHA -> EtapaRecuperacao.CODIGO
        }
        _uiState.value = estado.copy(etapa = anterior, erroGeral = null)
        return true
    }

    fun reiniciar() {
        _uiState.value = RecuperarSenhaUiState()
    }

    companion object {
        const val TAMANHO_CODIGO = 4
    }
}
