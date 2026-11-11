package com.aulasandroid.mobiledialoga.feature.profissional.perfil

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.components.MascaraCrp
import com.aulasandroid.mobiledialoga.core.validation.PhoneValidator
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.EspecialidadeDto
import com.aulasandroid.mobiledialoga.feature.perfil.components.comoCorpoTexto
import com.aulasandroid.mobiledialoga.feature.perfil.components.criarPartFoto
import com.aulasandroid.mobiledialoga.feature.perfil.components.formatarCpf
import com.aulasandroid.mobiledialoga.feature.perfil.components.primeiroItem
import com.aulasandroid.mobiledialoga.feature.perfil.components.texto
import com.aulasandroid.mobiledialoga.feature.perfil.components.traduzirErroPerfil
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Cópia dos dados salvos no servidor — é para onde o "Cancelar" volta. */
data class DadosPerfilProfissional(
    val nome: String = "",
    val email: String = "",
    val celular: String = "",
    val crp: String = "",
    val cpf: String = "",
    val especialidades: List<EspecialidadeDto> = emptyList(),
    val fotoUrl: String? = null
)

data class PerfilProfissionalUiState(
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val editando: Boolean = false,
    // Valores dos campos (o que está na tela)
    val nome: String = "",
    val celular: String = "",
    val especialidadesSelecionadas: List<String> = emptyList(), // nomes
    val fotoUrl: String? = null,
    val fotoLocal: Uri? = null, // foto escolhida na galeria, ainda não enviada
    // Somente leitura
    val email: String = "",
    val crp: String = "",
    val cpf: String = "",
    val especialidadesDisponiveis: List<EspecialidadeDto> = emptyList(),
    val original: DadosPerfilProfissional? = null,
    // Erros
    val erroNome: String? = null,
    val erroCelular: String? = null,
    val erroEspecialidade: String? = null,
    val erroGeral: String? = null,
    // Aviso temporário
    val toastMensagem: String? = null,
    val toastSucesso: Boolean = true,
    val versaoSalva: Int = 0 // sobe a cada salvamento; a tela usa para atualizar nome/foto do menu lateral
)

class PerfilProfissionalViewModel : ViewModel() {

    private val _uiState = MutableLiveData(PerfilProfissionalUiState())
    val uiState: LiveData<PerfilProfissionalUiState> = _uiState

    private val estado: PerfilProfissionalUiState get() = _uiState.value ?: PerfilProfissionalUiState()

    fun carregar(token: String, forcar: Boolean = false) {
        if (estado.carregando) return
        if (!forcar && estado.original != null) return

        _uiState.value = estado.copy(carregando = true, erroGeral = null)
        carregarEspecialidadesDisponiveis()

        RetrofitFactory().getProfissionalService().getPerfilProfissional(token)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val item = if (response.isSuccessful) response.body()?.primeiroItem("profissional") else null
                    if (item == null) {
                        _uiState.value = estado.copy(
                            carregando = false,
                            erroGeral = traduzirErroPerfil(response.code(), acao = "carregar o perfil")
                        )
                        return
                    }

                    val dados = DadosPerfilProfissional(
                        nome = item.texto("nome_completo").orEmpty(),
                        email = item.texto("email").orEmpty(),
                        celular = PhoneValidator.limpar(item.texto("celular").orEmpty()),
                        crp = MascaraCrp.formatarCrp(item.texto("crp").orEmpty()),
                        cpf = formatarCpf(item.texto("cpf").orEmpty()),
                        especialidades = lerEspecialidades(item),
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

    // Lista de especialidades para o seletor (mesma rota do cadastro). Se falhar, o perfil ainda abre.
    private fun carregarEspecialidadesDisponiveis() {
        if (estado.especialidadesDisponiveis.isNotEmpty()) return

        RetrofitFactory().getEspecialidadeService().listarEspecialidades()
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (!response.isSuccessful) return
                    val resp = try { response.body()?.getAsJsonObject("response") } catch (e: Exception) { null }
                    val array = try {
                        resp?.getAsJsonArray("especialidade") ?: resp?.getAsJsonArray("especialidades")
                    } catch (e: Exception) { null }
                    _uiState.value = estado.copy(especialidadesDisponiveis = array?.let { converterLista(it) }.orEmpty())
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {}
            })
    }

    private fun lerEspecialidades(item: JsonObject): List<EspecialidadeDto> {
        val array = try { item.getAsJsonArray("especialidade") } catch (e: Exception) { null } ?: return emptyList()
        return converterLista(array)
    }

    private fun converterLista(array: JsonArray): List<EspecialidadeDto> = array.mapNotNull { elemento ->
        try {
            val obj = elemento.asJsonObject
            val nome = obj.texto("especialidade") ?: obj.texto("nome") ?: return@mapNotNull null
            EspecialidadeDto(obj.get("id").asInt, nome)
        } catch (e: Exception) {
            null
        }
    }

    // Se a pessoa já estiver editando, não atropela o que ela digitou
    private fun aplicarDados(dados: DadosPerfilProfissional) {
        val atual = estado
        _uiState.value = if (atual.editando) {
            atual.copy(
                carregando = false,
                original = dados,
                email = dados.email,
                crp = dados.crp,
                cpf = dados.cpf
            )
        } else {
            atual.copy(
                carregando = false,
                original = dados,
                nome = dados.nome,
                celular = dados.celular,
                especialidadesSelecionadas = dados.especialidades.map { it.nome },
                fotoUrl = dados.fotoUrl,
                fotoLocal = null,
                email = dados.email,
                crp = dados.crp,
                cpf = dados.cpf
            )
        }
    }

    fun iniciarEdicao() {
        _uiState.value = estado.copy(
            editando = true,
            erroNome = null,
            erroCelular = null,
            erroEspecialidade = null,
            erroGeral = null
        )
    }

    fun cancelarEdicao() {
        val o = estado.original ?: DadosPerfilProfissional()
        _uiState.value = estado.copy(
            editando = false,
            nome = o.nome,
            celular = o.celular,
            especialidadesSelecionadas = o.especialidades.map { it.nome },
            fotoUrl = o.fotoUrl,
            fotoLocal = null,
            erroNome = null,
            erroCelular = null,
            erroEspecialidade = null,
            erroGeral = null
        )
    }

    fun onNomeChange(valor: String) {
        _uiState.value = estado.copy(nome = valor.take(150), erroNome = null)
    }

    fun onCelularChange(valor: String) {
        _uiState.value = estado.copy(celular = PhoneValidator.limpar(valor), erroCelular = null)
    }

    fun onEspecialidadeToggle(nome: String) {
        val atuais = estado.especialidadesSelecionadas.toMutableList()
        if (!atuais.remove(nome)) atuais.add(nome)
        _uiState.value = estado.copy(especialidadesSelecionadas = atuais, erroEspecialidade = null)
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

        // Nome -> id: junta a lista do servidor com as especialidades que o perfil já tinha
        val idsPorNome = (atual.especialidadesDisponiveis + (atual.original?.especialidades.orEmpty()))
            .associate { it.nome to it.id }
        val idsSelecionados = atual.especialidadesSelecionadas.mapNotNull { idsPorNome[it] }.distinct()

        val nome = atual.nome.trim()
        val erroNome = if (nome.isEmpty()) "Informe seu nome completo" else null
        val erroCelular = PhoneValidator.validar(atual.celular)
        // O back apaga as especialidades e regrava; mandar lista vazia deixaria o profissional sem nenhuma
        val erroEspecialidade = if (idsSelecionados.isEmpty()) "Selecione ao menos uma especialidade" else null
        if (erroNome != null || erroCelular != null || erroEspecialidade != null) {
            _uiState.value = atual.copy(
                erroNome = erroNome,
                erroCelular = erroCelular,
                erroEspecialidade = erroEspecialidade
            )
            return
        }

        val foto = atual.fotoLocal?.let { uri ->
            criarPartFoto(context, uri) ?: run {
                mostrarErro("Não foi possível ler a foto. Use uma imagem de até 5 MB.")
                return
            }
        }

        _uiState.value = atual.copy(
            salvando = true,
            erroNome = null,
            erroCelular = null,
            erroEspecialidade = null,
            erroGeral = null
        )

        // Mesmo formato do cadastro: lista de ids como texto, ex.: "[1, 3]"
        RetrofitFactory().getProfissionalService().atualizarPerfilProfissionalMe(
            token = token,
            nome = nome.comoCorpoTexto(),
            celular = atual.celular.comoCorpoTexto(),
            idEspecialidade = idsSelecionados.toString().comoCorpoTexto(),
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
