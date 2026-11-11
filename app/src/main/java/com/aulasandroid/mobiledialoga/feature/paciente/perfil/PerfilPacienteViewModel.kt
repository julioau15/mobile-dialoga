package com.aulasandroid.mobiledialoga.feature.paciente.perfil

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.core.validation.PhoneValidator
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.perfil.components.comoCorpoTexto
import com.aulasandroid.mobiledialoga.feature.perfil.components.criarPartFoto
import com.aulasandroid.mobiledialoga.feature.perfil.components.primeiroItem
import com.aulasandroid.mobiledialoga.feature.perfil.components.texto
import com.aulasandroid.mobiledialoga.feature.perfil.components.traduzirErroPerfil
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Cópia dos dados salvos no servidor — é para onde o "Cancelar" volta. */
data class DadosPerfilPaciente(
    val nome: String = "",
    val apelido: String = "",
    val email: String = "",
    val celular: String = "",
    val fotoUrl: String? = null
)

data class PerfilPacienteUiState(
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val editando: Boolean = false,
    // Valores dos campos (o que está na tela)
    val nome: String = "",
    val apelido: String = "",
    val email: String = "",
    val celular: String = "",
    val fotoUrl: String? = null,
    val fotoLocal: Uri? = null, // foto escolhida na galeria, ainda não enviada
    val original: DadosPerfilPaciente? = null,
    // Erros
    val erroNome: String? = null,
    val erroCelular: String? = null,
    val erroGeral: String? = null,
    // Aviso temporário
    val toastMensagem: String? = null,
    val toastSucesso: Boolean = true,
    val versaoSalva: Int = 0 // sobe a cada salvamento; a tela usa para atualizar nome/foto do menu lateral
)

class PerfilPacienteViewModel : ViewModel() {

    private val _uiState = MutableLiveData(PerfilPacienteUiState())
    val uiState: LiveData<PerfilPacienteUiState> = _uiState

    private val estado: PerfilPacienteUiState get() = _uiState.value ?: PerfilPacienteUiState()

    fun carregar(token: String, forcar: Boolean = false) {
        if (estado.carregando) return
        if (!forcar && estado.original != null) return

        _uiState.value = estado.copy(carregando = true, erroGeral = null)

        RetrofitFactory().getPacienteService().getPerfilPaciente(token)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val item = if (response.isSuccessful) response.body()?.primeiroItem("paciente") else null
                    if (item == null) {
                        _uiState.value = estado.copy(
                            carregando = false,
                            erroGeral = traduzirErroPerfil(response.code(), acao = "carregar o perfil")
                        )
                        return
                    }

                    val dados = DadosPerfilPaciente(
                        nome = item.texto("nome_completo").orEmpty(),
                        apelido = item.texto("apelido").orEmpty(),
                        email = item.texto("email").orEmpty(),
                        celular = PhoneValidator.limpar(item.texto("celular").orEmpty()),
                        fotoUrl = UserProfile(foto = item.texto("foto_avatar")).getFotoUrlCompleta()
                    )
                    aplicarDados(dados)
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    _uiState.value = estado.copy(
                        carregando = false,
                        erroGeral = traduzirErroPerfil(null, t, acao = "carregar o perfil")
                    )
                }
            })
    }

    // Se a pessoa já estiver editando, não atropela o que ela digitou
    private fun aplicarDados(dados: DadosPerfilPaciente) {
        val atual = estado
        _uiState.value = if (atual.editando) {
            atual.copy(carregando = false, original = dados, email = dados.email)
        } else {
            atual.copy(
                carregando = false,
                original = dados,
                nome = dados.nome,
                apelido = dados.apelido,
                email = dados.email,
                celular = dados.celular,
                fotoUrl = dados.fotoUrl,
                fotoLocal = null
            )
        }
    }

    fun iniciarEdicao() {
        _uiState.value = estado.copy(editando = true, erroNome = null, erroCelular = null, erroGeral = null)
    }

    fun cancelarEdicao() {
        val o = estado.original ?: DadosPerfilPaciente()
        _uiState.value = estado.copy(
            editando = false,
            nome = o.nome,
            apelido = o.apelido,
            celular = o.celular,
            fotoUrl = o.fotoUrl,
            fotoLocal = null,
            erroNome = null,
            erroCelular = null,
            erroGeral = null
        )
    }

    fun onNomeChange(valor: String) {
        _uiState.value = estado.copy(nome = valor.take(150), erroNome = null)
    }

    fun onApelidoChange(valor: String) {
        _uiState.value = estado.copy(apelido = valor.take(50))
    }

    fun onCelularChange(valor: String) {
        _uiState.value = estado.copy(celular = PhoneValidator.limpar(valor), erroCelular = null)
    }

    // Escolher uma foto já coloca a tela em modo de edição; ela só vai para o servidor ao salvar
    fun onFotoEscolhida(uri: Uri) {
        _uiState.value = estado.copy(fotoLocal = uri, editando = true, erroGeral = null)
    }

    fun limparToast() {
        _uiState.value = estado.copy(toastMensagem = null)
    }

    fun salvar(context: Context, token: String) {
        val atual = estado
        if (atual.salvando) return

        val nome = atual.nome.trim()
        val erroNome = if (nome.isEmpty()) "Informe seu nome completo" else null
        val erroCelular = PhoneValidator.validar(atual.celular)
        if (erroNome != null || erroCelular != null) {
            _uiState.value = atual.copy(erroNome = erroNome, erroCelular = erroCelular)
            return
        }

        val foto = atual.fotoLocal?.let { uri ->
            criarPartFoto(context, uri) ?: run {
                mostrarErro("Não foi possível ler a foto. Use uma imagem de até 5 MB.")
                return
            }
        }

        _uiState.value = atual.copy(salvando = true, erroNome = null, erroCelular = null, erroGeral = null)

        // O back converte campo ausente em "undefined", então os três textos vão sempre (apelido pode ir vazio)
        RetrofitFactory().getPacienteService().atualizarPerfilPacienteMe(
            token = token,
            nome = nome.comoCorpoTexto(),
            apelido = atual.apelido.trim().comoCorpoTexto(),
            celular = atual.celular.comoCorpoTexto(),
            fotoAvatar = foto
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    _uiState.value = estado.copy(
                        salvando = false,
                        editando = false,
                        toastMensagem = "Perfil atualizado!",
                        toastSucesso = true,
                        versaoSalva = estado.versaoSalva + 1
                    )
                    // Recarrega do servidor para a tela mostrar exatamente o que ficou salvo
                    carregar(token, forcar = true)
                } else {
                    _uiState.value = estado.copy(salvando = false)
                    mostrarErro(traduzirErroPerfil(response.code()))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(salvando = false)
                mostrarErro(traduzirErroPerfil(null, t))
            }
        })
    }

    private fun mostrarErro(mensagem: String) {
        _uiState.value = estado.copy(toastMensagem = mensagem, toastSucesso = false)
    }
}
