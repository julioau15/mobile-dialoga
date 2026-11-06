package com.aulasandroid.mobiledialoga.feature.auth.cadastro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.core.error.ErrorTranslator
import com.aulasandroid.mobiledialoga.core.validation.EmailValidator
import com.aulasandroid.mobiledialoga.core.validation.PasswordValidator
import com.aulasandroid.mobiledialoga.core.validation.PhoneValidator
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel que consome a API utilizando Retrofit com o padrão Call e enqueue,
 * idêntico ao modelo de projeto (android-consumo-api).
 */
class CadastroViewModel : ViewModel() {

    private val _uiState = MutableLiveData(CadastroUiState())
    val uiState: LiveData<CadastroUiState> = _uiState

    private val estado: CadastroUiState get() = _uiState.value ?: CadastroUiState()

    init {
        carregarEspecialidades()
    }

    /**
     * Busca a lista de especialidades usando Retrofit e callback enqueue.
     */
    fun carregarEspecialidades() {
        RetrofitFactory().getEspecialidadeService().listarEspecialidades().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val json = response.body()
                    val respObj = json?.getAsJsonObject("response")
                    val array = respObj?.getAsJsonArray("especialidade")
                        ?: json?.getAsJsonArray("response")
                        ?: respObj?.getAsJsonArray("especialidades")

                    val list = mutableListOf<EspecialidadeDto>()
                    if (array != null) {
                        for (element in array) {
                            val item = element.asJsonObject
                            list.add(
                                EspecialidadeDto(
                                    item.get("id").asInt,
                                    if (item.has("especialidade")) item.get("especialidade").asString else item.get("nome").asString
                                )
                            )
                        }
                    }
                    _uiState.value = estado.copy(especialidadesList = list)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // Falha de rede ao carregar especialidades
            }
        })
    }

    // ---------- Etapa 1: Dados Pessoais ----------

    fun onTipoContaChange(tipo: TipoConta) {
        _uiState.value = estado.copy(tipoConta = tipo, erroEspecialidade = null, erroCpf = null, erroCrp = null, erroGeral = null)
    }

    fun onNomeChange(nome: String) {
        _uiState.value = estado.copy(nome = nome, erroNome = null, erroGeral = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = estado.copy(email = email.trim().lowercase(), erroEmail = null, erroGeral = null)
    }

    fun onCelularChange(celular: String) {
        _uiState.value = estado.copy(celular = PhoneValidator.limpar(celular), erroCelular = null, erroGeral = null)
    }

    fun onCpfChange(cpf: String) {
        val limpo = cpf.filter { it.isDigit() }.take(11)
        _uiState.value = estado.copy(cpf = limpo, erroCpf = null, erroGeral = null)
    }

    fun onCrpChange(crp: String) {
        _uiState.value = estado.copy(crp = crp.uppercase(), erroCrp = null, erroGeral = null)
    }

    fun onInstituicaoChange(instituicao: String) {
        _uiState.value = estado.copy(instituicaoClinica = instituicao, erroGeral = null)
    }

    fun onEspecialidadeChange(especialidade: String) {
        _uiState.value = estado.copy(especialidade = especialidade, erroEspecialidade = null, erroGeral = null)
    }

    fun continuar() {
        val atual = estado
        val erroCpf = if (atual.tipoConta == TipoConta.PROFISSIONAL && atual.cpf.length < 11) "CPF inválido" else null
        val erroCrp = if (atual.tipoConta == TipoConta.PROFISSIONAL && atual.crp.isBlank()) "Informe seu CRP" else null
        val erroEspecialidade = if (atual.tipoConta == TipoConta.PROFISSIONAL && atual.especialidade == null) {
            "Selecione sua especialidade"
        } else null

        val validado = atual.copy(
            erroNome = if (atual.nome.isBlank()) "Informe seu nome completo" else null,
            erroEmail = EmailValidator.validar(atual.email),
            erroCelular = PhoneValidator.validar(atual.celular),
            erroCpf = erroCpf,
            erroCrp = erroCrp,
            erroEspecialidade = erroEspecialidade,
            erroGeral = null
        )
        val temErro = listOf(
            validado.erroNome, validado.erroEmail, validado.erroCelular,
            validado.erroCpf, validado.erroCrp, validado.erroEspecialidade
        ).any { it != null }

        if (temErro) {
            _uiState.value = validado
            return
        }

        // Verifica no backend se e-mail, celular, CPF ou CRP já estão cadastrados
        _uiState.value = validado.copy(isLoading = true, erroGeral = null)

        val cpfArg = if (atual.tipoConta == TipoConta.PROFISSIONAL) atual.cpf else null
        val crpArg = if (atual.tipoConta == TipoConta.PROFISSIONAL) atual.crp else null

        RetrofitFactory().getAuthService().verificarCadastro(
            email = atual.email,
            celular = atual.celular,
            cpf = cpfArg,
            crp = crpArg
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val json = response.body()
                if (response.isSuccessful && (json?.get("status")?.asBoolean == true)) {
                    // Disponível! Avança para a etapa de senha
                    _uiState.value = estado.copy(isLoading = false, etapa = EtapaCadastro.SENHA, erroGeral = null)
                } else {
                    val rawMsg = json?.get("message")?.asString
                    val msg = ErrorTranslator.traduzir(rawMsg)
                    val lowerMsg = (rawMsg ?: "").lowercase()
                    val erroEmail = if (lowerMsg.contains("e-mail") || lowerMsg.contains("email")) msg else null
                    val erroCelular = if (lowerMsg.contains("celular")) msg else null
                    val erroCpf = if (lowerMsg.contains("cpf")) msg else null
                    val erroCrp = if (lowerMsg.contains("crp")) msg else null

                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroEmail = erroEmail ?: estado.erroEmail,
                        erroCelular = erroCelular ?: estado.erroCelular,
                        erroCpf = erroCpf ?: estado.erroCpf,
                        erroCrp = erroCrp ?: estado.erroCrp,
                        erroGeral = if (erroEmail == null && erroCelular == null && erroCpf == null && erroCrp == null) msg else null
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(
                    isLoading = false,
                    erroGeral = ErrorTranslator.traduzir(t.message)
                )
            }
        })
    }

    // ---------- Etapa 2: Senha ----------

    fun onSenhaChange(senha: String) {
        _uiState.value = estado.copy(senha = senha, erroSenha = null, erroGeral = null)
    }

    fun onConfirmacaoChange(confirmacao: String) {
        _uiState.value = estado.copy(confirmacao = confirmacao, erroConfirmacao = null, erroGeral = null)
    }

    /**
     * Realiza o cadastro de Paciente ou Profissional utilizando Retrofit e enqueue,
     * exatamente como no modelo android-consumo-api.
     */
    fun criarConta(onPacienteCadastrado: () -> Unit) {
        val atual = estado
        val erroSenha = PasswordValidator.validar(atual.senha)
        val erroConfirmacao = PasswordValidator.validarConfirmacao(atual.senha, atual.confirmacao)
        _uiState.value = atual.copy(erroSenha = erroSenha, erroConfirmacao = erroConfirmacao, erroGeral = null)
        if (erroSenha != null || erroConfirmacao != null) return

        _uiState.value = estado.copy(isLoading = true, erroGeral = null)

        val mediaType = MediaType.parse("text/plain")

        if (atual.tipoConta == TipoConta.PACIENTE) {
            val nomeBody = RequestBody.create(mediaType, atual.nome)
            val emailBody = RequestBody.create(mediaType, atual.email)
            val celularBody = RequestBody.create(mediaType, atual.celular)
            val senhaBody = RequestBody.create(mediaType, atual.senha)

            RetrofitFactory().getPacienteService().cadastrarPaciente(nomeBody, emailBody, celularBody, senhaBody)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val json = response.body()
                        if (response.isSuccessful && (json?.get("status")?.asBoolean == true)) {
                            reiniciar()
                            onPacienteCadastrado()
                        } else {
                            val rawMsg = json?.get("message")?.asString
                            val msg = ErrorTranslator.traduzir(rawMsg)
                            _uiState.value = estado.copy(isLoading = false, erroGeral = msg)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        _uiState.value = estado.copy(
                            isLoading = false,
                            erroGeral = ErrorTranslator.traduzir(t.message)
                        )
                    }
                })
        } else {
            val selectedEspId = estado.especialidadesList.find { it.nome == atual.especialidade }?.id ?: 1
            val nomeBody = RequestBody.create(mediaType, atual.nome)
            val emailBody = RequestBody.create(mediaType, atual.email)
            val celularBody = RequestBody.create(mediaType, atual.celular)
            val senhaBody = RequestBody.create(mediaType, atual.senha)
            val cpfBody = RequestBody.create(mediaType, atual.cpf)
            val crpBody = RequestBody.create(mediaType, atual.crp)
            val instBody = if (atual.instituicaoClinica.isNotBlank()) RequestBody.create(mediaType, atual.instituicaoClinica) else null
            val espBody = RequestBody.create(mediaType, "[$selectedEspId]")

            RetrofitFactory().getProfissionalService().cadastrarProfissional(
                nomeBody, emailBody, celularBody, senhaBody, cpfBody, crpBody, instBody, espBody
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val json = response.body()
                    if (response.isSuccessful && (json?.get("status")?.asBoolean == true)) {
                        _uiState.value = estado.copy(isLoading = false, etapa = EtapaCadastro.EM_ANALISE)
                    } else {
                        val rawMsg = json?.get("message")?.asString
                        val msg = ErrorTranslator.traduzir(rawMsg)
                        _uiState.value = estado.copy(isLoading = false, erroGeral = msg)
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    _uiState.value = estado.copy(
                        isLoading = false,
                        erroGeral = ErrorTranslator.traduzir(t.message)
                    )
                }
            })
        }
    }

    // ---------- Navegação entre Etapas ----------

    fun voltar(): Boolean {
        if (estado.etapa != EtapaCadastro.SENHA) return false
        _uiState.value = estado.copy(etapa = EtapaCadastro.DADOS_PESSOAIS, erroGeral = null)
        return true
    }

    fun reiniciar() {
        _uiState.value = CadastroUiState(especialidadesList = estado.especialidadesList)
    }
}
