package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.home.DashboardMapper
import com.aulasandroid.mobiledialoga.feature.paciente.home.RegistroHumorApi
import kotlinx.coroutines.Job
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.hojeIso
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.paraIso
import com.aulasandroid.mobiledialoga.feature.vinculos.components.listaEm
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mensagemErroHttp
import com.aulasandroid.mobiledialoga.feature.vinculos.components.num
import com.aulasandroid.mobiledialoga.feature.vinculos.components.txt
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

/** Status do vínculo no back: 0 = aguardando aceite, 1 = ativo, 2 = recusado. */
data class PacienteVinculadoUi(
    val id: Int,
    val idVinculo: Int,
    val nome: String,
    val ativo: Boolean, // status_atividade do paciente
    val statusVinculo: Int,
    val origemCadastro: Int, // 1 = profissional convidou, 2 = paciente convidou
    val desdeIso: String?,
    val email: String?, // o back só devolve contato depois que o vínculo é aceito
    val celular: String?,
    val foto: String? = null
) {
    val aguardandoAceite: Boolean get() = statusVinculo == 0
    val podeAprovar: Boolean get() = aguardandoAceite && origemCadastro == 2
}

data class ConsultaDoPacienteUi(val idPaciente: Int, val dataIso: String)

data class AnotacaoUi(
    val id: Int,
    val titulo: String,
    val conteudo: String,
    val criadaIso: String?
)

data class PacienteBuscaUi(val id: Int, val nome: String, val email: String?)

data class PacientesProfissionalState(
    val isLoading: Boolean = false,
    val carregou: Boolean = false,
    val erro: String? = null,
    val pacientes: List<PacienteVinculadoUi> = emptyList(),
    val consultas: List<ConsultaDoPacienteUi> = emptyList(),
    // anotações do paciente aberto
    val anotacoes: List<AnotacaoUi> = emptyList(),
    val carregandoAnotacoes: Boolean = false,
    val erroAnotacoes: String? = null,
    // busca do modal "Novo paciente"
    val buscando: Boolean = false,
    val buscaFeita: Boolean = false,
    val resultadosBusca: List<PacienteBuscaUi> = emptyList(),
    // ações
    val salvando: Boolean = false,
    val acessoSolicitado: Set<Int> = emptySet(),
    val acessos: Map<Int, String> = emptyMap(),
    val idIndicadores: Int? = null,
    val carregandoIndicadores: Boolean = false,
    val erroIndicadores: String? = null,
    val categorias: Set<String> = emptySet(),
    val registrosCompartilhados: List<RegistroHumorApi> = emptyList(),
    val toast: String? = null,
    val toastSucesso: Boolean = true
) {
    val pacientesAtivos: Int get() = pacientes.count { it.ativo }

    fun proximaConsulta(idPaciente: Int): String? {
        val hoje = hojeIso()
        return consultas.filter { it.idPaciente == idPaciente && it.dataIso >= hoje }.minOfOrNull { it.dataIso }
    }

    /** Consultas já realizadas (até hoje, sem as canceladas) */
    fun sessoes(idPaciente: Int): Int {
        val hoje = hojeIso()
        return consultas.count { it.idPaciente == idPaciente && it.dataIso <= hoje }
    }
}

class PacientesProfissionalViewModel : ViewModel() {

    var state by mutableStateOf(PacientesProfissionalState())
        private set

    private var indicadoresJob: Job? = null

    private class Resp(val conectou: Boolean, val codigo: Int, val corpo: JsonObject?) {
        // O back responde 404 quando a lista está vazia: não é erro
        val falhou: Boolean get() = !conectou || (codigo !in 200..299 && codigo != 404)
        val ok: Boolean get() = conectou && codigo in 200..299
    }

    fun limparToast() {
        state = state.copy(toast = null)
    }

    fun limparBusca() {
        state = state.copy(buscando = false, buscaFeita = false, resultadosBusca = emptyList())
    }

    // ---------------------------------------------------------------------
    // Lista
    // ---------------------------------------------------------------------

    fun carregar(token: String) {
        if (state.isLoading) return
        state = state.copy(isLoading = true, erro = null)

        viewModelScope.launch {
            val profissionalService = RetrofitFactory().getProfissionalService()

            val (respPacientes, respConsultas, respAcessos) = coroutineScope {
                val p = async { chamar { profissionalService.listarPacientesVinculados(token) } }
                val c = async { chamar { profissionalService.getMinhasConsultas(token) } }
                val a = async { chamar { RetrofitFactory().getSolicitacaoAcessoService().listarSolicitacoesAcesso(token) } }
                Triple(p.await(), c.await(), a.await())
            }

            if (respPacientes.falhou) {
                state = state.copy(
                    isLoading = false,
                    erro = mensagemErroHttp(respPacientes.codigo, !respPacientes.conectou, "carregar os pacientes")
                )
                return@launch
            }

            val pacientes = respPacientes.corpo.listaEm("paciente").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    val id = obj.num("id")
                    if (id <= 0) return@mapNotNull null
                    val atividade = (obj.txt("status_atividade") ?: "").lowercase()
                    PacienteVinculadoUi(
                        id = id,
                        idVinculo = obj.num("id_vinculo"),
                        nome = obj.txt("nome_completo") ?: obj.txt("apelido") ?: "Paciente $id",
                        ativo = atividade.isEmpty() || atividade.startsWith("ativ"),
                        statusVinculo = obj.num("status_vinculo"),
                        origemCadastro = obj.num("origem_cadastro"),
                        desdeIso = obj.txt("desde").paraIso(),
                        email = obj.txt("email"),
                        celular = obj.txt("celular"),
                        foto = obj.txt("foto_avatar")
                    )
                } catch (_: Exception) {
                    null
                }
            }

            val consultas = respConsultas.corpo.listaEm("consulta").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    if ((obj.txt("status") ?: "").lowercase().contains("cancel")) return@mapNotNull null
                    val data = obj.txt("data").paraIso() ?: return@mapNotNull null
                    val idPaciente = obj.num("id_paciente")
                    if (idPaciente <= 0) null else ConsultaDoPacienteUi(idPaciente, data)
                } catch (_: Exception) {
                    null
                }
            }

            state = state.copy(
                isLoading = false,
                carregou = true,
                erro = null,
                pacientes = pacientes,
                consultas = consultas,
                acessos = if (respAcessos.ok) respAcessos.corpo.listaEm("solicitacaoAcesso")
                    .map { it.asJsonObject }
                    .sortedBy { it.num("id") }
                    .associate { it.num("id_paciente") to it.txt("status").orEmpty() }
                    else emptyMap(),
                acessoSolicitado = if (respAcessos.ok) respAcessos.corpo.listaEm("solicitacaoAcesso")
                    .map { it.asJsonObject }
                    .sortedBy { it.num("id") }
                    .associate { it.num("id_paciente") to it.txt("status") }
                    .filterValues { it == "pendente" }.keys else emptySet()
            )
        }
    }

    // ---------------------------------------------------------------------
    // Novo paciente (busca na plataforma + vínculo)
    // ---------------------------------------------------------------------

    fun buscar(token: String, termo: String) {
        val t = termo.trim()
        if (t.length < 2) {
            limparBusca()
            return
        }
        state = state.copy(buscando = true)

        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getPacienteService().buscarPacientesPorTermo(token, t) }
            val jaVinculados = state.pacientes.map { it.id }.toSet()

            val resultados = resp.corpo.listaEm("paciente").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    val id = obj.num("id")
                    if (id <= 0 || id in jaVinculados) return@mapNotNull null
                    PacienteBuscaUi(
                        id = id,
                        nome = obj.txt("nome_completo") ?: obj.txt("apelido") ?: "Paciente $id",
                        email = obj.txt("email")
                    )
                } catch (_: Exception) {
                    null
                }
            }
            state = state.copy(buscando = false, buscaFeita = true, resultadosBusca = resultados)
        }
    }

    /** Cria o vínculo (fica "aguardando aceite" até o paciente aceitar). [aoConcluir] fecha o modal. */
    fun adicionarPaciente(token: String, idPaciente: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar {
                RetrofitFactory().getProfissionalService().vincularPaciente(token, idPaciente.toString())
            }
            if (resp.ok) {
                state = state.copy(salvando = false, toast = "Solicitação enviada ao paciente.", toastSucesso = true)
                limparBusca()
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "adicionar o paciente"),
                    toastSucesso = false
                )
            }
        }
    }

    /** Remove o vínculo com o paciente (o back apaga a linha do vínculo). */
    fun remover(token: String, idPaciente: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar {
                RetrofitFactory().getProfissionalService().desvincularPaciente(token, idPaciente.toString())
            }
            if (resp.conectou && resp.codigo in 200..299) {
                state = state.copy(salvando = false, toast = "Paciente removido do acompanhamento.", toastSucesso = true)
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "remover o paciente"),
                    toastSucesso = false
                )
            }
        }
    }

    fun aceitarVinculo(token: String, idVinculo: Int, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getAgendaService().aceitarVinculo(token, idVinculo) }
            if (resp.ok) {
                state = state.copy(toast = "Vínculo aceito com sucesso.", toastSucesso = true)
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(toast = mensagemErroHttp(resp.codigo, !resp.conectou, "aceitar o vínculo"), toastSucesso = false)
            }
        }
    }

    fun recusarVinculo(token: String, idVinculo: Int, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getAgendaService().recusarVinculo(token, idVinculo) }
            if (resp.ok) {
                state = state.copy(toast = "Vínculo recusado.", toastSucesso = true)
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(toast = mensagemErroHttp(resp.codigo, !resp.conectou, "recusar o vínculo"), toastSucesso = false)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Anotações privadas
    // ---------------------------------------------------------------------

    fun carregarAnotacoes(token: String, idPaciente: Int) {
        state = state.copy(carregandoAnotacoes = true, erroAnotacoes = null, anotacoes = emptyList())

        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getAnotacaoService().listarAnotacoesDoPaciente(token, idPaciente) }
            if (resp.falhou) {
                state = state.copy(
                    carregandoAnotacoes = false,
                    erroAnotacoes = mensagemErroHttp(resp.codigo, !resp.conectou, "carregar as anotações")
                )
                return@launch
            }
            val lista = resp.corpo.listaEm("anotacao").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    val id = obj.num("id")
                    if (id <= 0) return@mapNotNull null
                    AnotacaoUi(
                        id = id,
                        titulo = obj.txt("titulo") ?: "Sem título",
                        conteudo = obj.txt("conteudo") ?: "",
                        criadaIso = obj.txt("data_criacao").paraIso()
                    )
                } catch (_: Exception) {
                    null
                }
            }.sortedWith(compareByDescending<AnotacaoUi> { it.criadaIso ?: "" }.thenByDescending { it.id })

            state = state.copy(carregandoAnotacoes = false, anotacoes = lista)
        }
    }

    /** [idNota] nulo cria uma nota nova; preenchido edita a existente. */
    fun salvarAnotacao(
        token: String,
        idPaciente: Int,
        idNota: Int?,
        titulo: String,
        conteudo: String,
        aoConcluir: () -> Unit
    ) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        val corpo = JsonObject().apply {
            addProperty("titulo", titulo.trim())
            addProperty("conteudo", conteudo.trim())
        }

        viewModelScope.launch {
            val service = RetrofitFactory().getAnotacaoService()
            val resp = chamar {
                if (idNota == null) service.criarAnotacaoDoPaciente(token, idPaciente, corpo)
                else service.atualizarAnotacao(token, idNota, corpo)
            }
            if (resp.ok) {
                state = state.copy(salvando = false, toast = "Anotação salva com sucesso.", toastSucesso = true)
                aoConcluir()
                carregarAnotacoes(token, idPaciente)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "salvar a anotação"),
                    toastSucesso = false
                )
            }
        }
    }

    fun excluirAnotacao(token: String, idPaciente: Int, idNota: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getAnotacaoService().excluirAnotacao(token, idNota) }
            if (resp.ok) {
                state = state.copy(salvando = false, toast = "Anotação excluída.", toastSucesso = true)
                aoConcluir()
                carregarAnotacoes(token, idPaciente)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "excluir a anotação"),
                    toastSucesso = false
                )
            }
        }
    }

    fun solicitarAcesso(token: String, idPaciente: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getSolicitacaoAcessoService().solicitarAcessoAoPaciente(token, idPaciente) }
            if (resp.ok) {
                state = state.copy(
                    salvando = false,
                    acessoSolicitado = state.acessoSolicitado + idPaciente,
                    toast = "Solicitação de acesso enviada ao paciente.",
                    toastSucesso = true
                )
                aoConcluir()
                carregar(token)
                carregarIndicadores(token, idPaciente)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "enviar a solicitação"),
                    toastSucesso = false
                )
            }
        }
    }

    fun carregarIndicadores(token: String, idPaciente: Int) {
        indicadoresJob?.cancel()
        state = state.copy(
            idIndicadores = idPaciente, carregandoIndicadores = true,
            erroIndicadores = null, categorias = emptySet(), registrosCompartilhados = emptyList()
        )
        indicadoresJob = viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getSolicitacaoAcessoService().indicadoresCompartilhados(token, idPaciente) }
            if (!resp.ok) {
                state = state.copy(
                    carregandoIndicadores = false,
                    acessos = state.acessos - idPaciente,
                    erroIndicadores = mensagemErroHttp(resp.codigo, !resp.conectou, "carregar os indicadores")
                )
                return@launch
            }
            val dados = resp.corpo?.getAsJsonObject("response")
            val status = dados?.txt("status").orEmpty()
            val categorias = dados?.getAsJsonArray("categorias")
                ?.mapNotNull { it.takeIf { v -> v.isJsonPrimitive }?.asString }?.toSet().orEmpty()
            state = state.copy(
                carregandoIndicadores = false,
                acessos = state.acessos + (idPaciente to status),
                acessoSolicitado = if (status == "pendente") state.acessoSolicitado + idPaciente else state.acessoSolicitado - idPaciente,
                categorias = categorias,
                registrosCompartilhados = DashboardMapper.registrosHumor(resp.corpo)
            )
        }
    }

    private suspend fun chamar(bloco: () -> Call<JsonObject>): Resp = try {
        val r = bloco().awaitResponse()
        Resp(true, r.code(), if (r.isSuccessful) r.body() else null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resp(false, 0, null)
    }
}