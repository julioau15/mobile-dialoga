package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.AuthState
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.home.DashboardMapper
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

class RelatoriosPacienteViewModel : ViewModel() {

    var state by mutableStateOf(estadoInicial())
        private set

    private var jaCarregou = false

    private fun estadoInicial(): RelatoriosState {
        val hoje = DatasRelatorio.hoje()
        return RelatoriosState(
            inicioIso = DatasRelatorio.somarDias(hoje, -6), // padrão: últimos 7 dias, como no protótipo
            fimIso = hoje,
            mesVisivel = DatasRelatorio.mesDe(hoje)
        )
    }

    /** Carrega humor, tarefas e solicitações. Só refaz se [forcar] ou se ainda não carregou. */
    fun carregar(authState: AuthState, forcar: Boolean = false) {
        if (jaCarregou && !forcar) return
        val token = authState.auth?.token ?: return
        jaCarregou = true
        state = state.copy(carregando = true, erro = null)

        viewModelScope.launch {
            var semConexao = false

            // Resposta não-2xx (ex.: 404 de lista vazia) vira null; só exceção de rede conta como falha.
            suspend fun Call<JsonObject>.corpo(): JsonObject? = try {
                val r = awaitResponse()
                if (r.isSuccessful) r.body() else null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                semConexao = true
                null
            }

            val pacienteService = RetrofitFactory().getPacienteService()
            val solicitacaoService = RetrofitFactory().getSolicitacaoAcessoService()

            val (humor, tarefas, solicitacoes) = coroutineScope {
                listOf(
                    async { pacienteService.getMeusRegistrosHumor(token).corpo() },
                    async { pacienteService.getMinhasTarefas(token).corpo() },
                    async { solicitacaoService.listarSolicitacoesAcesso(token).corpo() }
                ).awaitAll()
            }

            if (semConexao && humor == null && tarefas == null && solicitacoes == null) {
                jaCarregou = false
                state = state.copy(
                    carregando = false,
                    erro = "Não foi possível carregar os relatórios. Verifique sua conexão."
                )
                return@launch
            }

            state = state.copy(
                carregando = false,
                registros = DashboardMapper.registrosHumor(humor),
                tarefas = RelatoriosMapper.tarefas(tarefas),
                solicitacoes = RelatoriosMapper.solicitacoes(solicitacoes)
            )

            completarNomesProfissionais(token)
        }
    }

    /** A listagem de solicitações só traz o id do profissional: busca nome, especialidade e foto de cada um. */
    private suspend fun completarNomesProfissionais(token: String) {
        val faltando = state.solicitacoes.filter { it.nome.isBlank() || it.foto == null }.map { it.idProfissional }.distinct()
        if (faltando.isEmpty()) return
        val service = RetrofitFactory().getProfissionalService()

        val dados = coroutineScope {
            faltando.map { id ->
                async {
                    val tri = try {
                        val r = service.getProfissionalPorId(token, id).awaitResponse()
                        if (r.isSuccessful) RelatoriosMapper.dadosProfissional(r.body()) else null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                    id to tri
                }
            }.awaitAll().toMap()
        }

        state = state.copy(
            solicitacoes = state.solicitacoes.map { s ->
                if (s.nome.isNotBlank() && s.foto != null) return@map s
                val tri = dados[s.idProfissional]
                s.copy(
                    nome = s.nome.ifBlank { tri?.first ?: "Profissional" },
                    especialidade = s.especialidade.ifBlank { tri?.second.orEmpty() },
                    foto = tri?.third ?: s.foto
                )
            }
        )
    }

    // ---------------------------------------------------------------- período

    /** Primeiro toque define o início; o segundo define o fim (se for antes do início, inverte). */
    fun selecionarDia(iso: String) {
        if (iso > DatasRelatorio.hoje()) return
        val aguardandoFim = state.fimIso == null
        state = if (!aguardandoFim) {
            state.copy(inicioIso = iso, fimIso = null)
        } else if (iso < state.inicioIso) {
            state.copy(inicioIso = iso, fimIso = state.inicioIso)
        } else {
            state.copy(fimIso = iso)
        }
    }

    fun mesAnterior() {
        state = state.copy(mesVisivel = DatasRelatorio.mesAnterior(state.mesVisivel))
    }

    fun proximoMes() {
        state = state.copy(mesVisivel = DatasRelatorio.proximoMes(state.mesVisivel))
    }

    // ---------------------------------------------------------------- solicitações

    /** PUT /solicitacoes-acesso/{id}/aprovar ou /recusar. */
    fun responderSolicitacao(authState: AuthState, id: Int, aprovar: Boolean, categorias: List<String> = emptyList(), editar: Boolean = false, aoConcluir: () -> Unit = {}) {
        if (state.respondendoSolicitacao != null) return
        val token = authState.auth?.token ?: return
        state = state.copy(respondendoSolicitacao = id)

        viewModelScope.launch {
            val service = RetrofitFactory().getSolicitacaoAcessoService()
            try {
                val chamada = if (aprovar) {
                    val body = JsonObject().apply {
                        val arr = com.google.gson.JsonArray()
                        categorias.forEach { arr.add(it) }
                        add("categorias", arr)
                    }
                    if (editar) service.editarCategorias(token, id, body)
                    else service.aprovarSolicitacaoAcessoComCategorias(token, id, body)
                } else {
                    service.recusarSolicitacaoAcesso(token, id)
                }
                val resp = chamada.awaitResponse()
                if (resp.isSuccessful) {
                    val novoStatus = if (aprovar) "Aprovada" else "Recusada"
                    state = state.copy(
                        solicitacoes = state.solicitacoes.map { if (it.id == id) it.copy(status = novoStatus, categorias = if (aprovar) categorias.toSet() else emptySet()) else it }
                            .sortedWith(compareByDescending<SolicitacaoAcessoUi> { it.pendente }.thenByDescending { it.id }),
                        aviso = if (editar) "Compartilhamento atualizado." else if (aprovar) "Acesso aprovado." else "Solicitação recusada."
                    )
                    aoConcluir()
                    authState.verificarNovasNotificacoes()
                } else {
                    state = state.copy(aviso = "Não foi possível responder a solicitação. Tente novamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = state.copy(aviso = "Sem conexão com o servidor. Tente novamente.")
            } finally {
                state = state.copy(respondendoSolicitacao = null)
            }
        }
    }

    fun removerAcesso(authState: AuthState, id: Int, aoConcluir: () -> Unit) {
        if (state.respondendoSolicitacao != null) return
        val token = authState.auth?.token ?: return
        state = state.copy(respondendoSolicitacao = id)
        viewModelScope.launch {
            try {
                val resposta = RetrofitFactory().getSolicitacaoAcessoService().removerAcesso(token, id).awaitResponse()
                if (resposta.isSuccessful) {
                    val profissional = state.solicitacoes.firstOrNull { it.id == id }?.idProfissional
                    state = state.copy(
                        solicitacoes = state.solicitacoes.filterNot { it.idProfissional == profissional },
                        aviso = "Solicitação removida. O profissional não tem mais acesso."
                    )
                    aoConcluir()
                } else {
                    state = state.copy(aviso = "Não foi possível remover o acesso. Tente novamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                state = state.copy(aviso = "Sem conexão com o servidor. Tente novamente.")
            } finally {
                state = state.copy(respondendoSolicitacao = null)
            }
        }
    }

    fun limparAviso() {
        state = state.copy(aviso = null)
    }
}
