package com.aulasandroid.mobiledialoga.feature.paciente.profissionais

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
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
data class ProfissionalVinculadoUi(
    val idProfissional: Int,
    val nome: String,
    val especialidades: String?,
    val statusVinculo: Int,
    val desdeIso: String?,
    val proximaIso: String?
) {
    val atual: Boolean get() = statusVinculo != 2
}

data class ProfissionalBuscaUi(
    val id: Int,
    val nome: String,
    val especialidades: String?,
    val crp: String?
)

data class ProfissionaisPacienteState(
    val isLoading: Boolean = false,
    val carregou: Boolean = false,
    val erro: String? = null,
    val profissionais: List<ProfissionalVinculadoUi> = emptyList(),
    // busca do modal "Novo profissional"
    val buscando: Boolean = false,
    val buscaFeita: Boolean = false,
    val resultadosBusca: List<ProfissionalBuscaUi> = emptyList(),
    // ações
    val salvando: Boolean = false,
    val toast: String? = null,
    val toastSucesso: Boolean = true
)

class ProfissionaisPacienteViewModel : ViewModel() {

    var state by mutableStateOf(ProfissionaisPacienteState())
        private set

    private class Resp(val conectou: Boolean, val codigo: Int, val corpo: JsonObject?) {
        // O back responde 404 quando a lista está vazia: não é erro
        val falhou: Boolean get() = !conectou || (codigo !in 200..299 && codigo != 404)
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
            val pacienteService = RetrofitFactory().getPacienteService()

            val (respProfissionais, respConsultas) = coroutineScope {
                val p = async { chamar { pacienteService.getMeusProfissionais(token) } }
                val c = async { chamar { pacienteService.getMinhasConsultas(token) } }
                p.await() to c.await()
            }

            if (respProfissionais.falhou) {
                state = state.copy(
                    isLoading = false,
                    erro = mensagemErroHttp(respProfissionais.codigo, !respProfissionais.conectou, "carregar os profissionais")
                )
                return@launch
            }

            val proximaPorProfissional = lerProximasConsultas(respConsultas.corpo)

            val lista = respProfissionais.corpo.listaEm("profissional").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    val id = obj.num("id")
                    if (id <= 0) return@mapNotNull null
                    ProfissionalVinculadoUi(
                        idProfissional = id,
                        nome = obj.txt("nome_completo") ?: "Profissional $id",
                        especialidades = obj.txt("especialidades"),
                        statusVinculo = obj.num("status"),
                        desdeIso = obj.txt("desde").paraIso(),
                        proximaIso = proximaPorProfissional[id]
                    )
                } catch (_: Exception) {
                    null
                }
            }

            state = state.copy(isLoading = false, carregou = true, erro = null, profissionais = lista)
        }
    }

    // Próxima consulta com cada profissional = a data mais próxima de hoje em diante (consultas canceladas não contam)
    private fun lerProximasConsultas(corpo: JsonObject?): Map<Int, String> {
        val hoje = hojeIso()
        val mapa = mutableMapOf<Int, String>()
        for (elem in corpo.listaEm("consulta")) {
            try {
                val obj = elem.asJsonObject
                if ((obj.txt("status") ?: "").lowercase().contains("cancel")) continue
                val idProf = obj.num("id_profissional")
                val data = obj.txt("data").paraIso() ?: continue
                if (idProf <= 0 || data < hoje) continue
                val atual = mapa[idProf]
                if (atual == null || data < atual) mapa[idProf] = data
            } catch (_: Exception) { }
        }
        return mapa
    }

    // ---------------------------------------------------------------------
    // Busca (modal "Novo profissional")
    // ---------------------------------------------------------------------

    fun buscar(token: String, termo: String) {
        val t = termo.trim()
        if (t.length < 2) {
            limparBusca()
            return
        }
        state = state.copy(buscando = true)

        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getProfissionalService().buscarProfissionais(token, t) }
            val jaVinculados = state.profissionais.filter { it.atual }.map { it.idProfissional }.toSet()

            val resultados = resp.corpo.listaEm("profissional").mapNotNull { elem ->
                try {
                    val obj = elem.asJsonObject
                    val id = obj.num("id")
                    if (id <= 0 || id in jaVinculados) return@mapNotNull null
                    ProfissionalBuscaUi(
                        id = id,
                        nome = obj.txt("nome_completo") ?: "Profissional $id",
                        especialidades = obj.txt("especialidades"),
                        crp = obj.txt("crp")
                    )
                } catch (_: Exception) {
                    null
                }
            }
            state = state.copy(buscando = false, buscaFeita = true, resultadosBusca = resultados)
        }
    }

    // ---------------------------------------------------------------------
    // Adicionar / remover
    // ---------------------------------------------------------------------

    /** Cria o vínculo (fica "aguardando aceite" até o profissional aceitar). [aoConcluir] fecha o modal. */
    fun adicionar(token: String, idProfissional: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar {
                RetrofitFactory().getPacienteService().vincularProfissional(token, idProfissional.toString())
            }
            if (resp.conectou && resp.codigo in 200..299) {
                state = state.copy(
                    salvando = false,
                    toast = "Solicitação enviada ao profissional.",
                    toastSucesso = true
                )
                limparBusca()
                aoConcluir()
                state = state.copy(isLoading = false)
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "adicionar o profissional"),
                    toastSucesso = false
                )
            }
        }
    }

    fun remover(token: String, idProfissional: Int, aoConcluir: () -> Unit) {
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            val resp = chamar {
                RetrofitFactory().getPacienteService().desvincularProfissional(token, idProfissional.toString())
            }
            if (resp.conectou && resp.codigo in 200..299) {
                state = state.copy(
                    salvando = false,
                    toast = "Profissional removido do acompanhamento.",
                    toastSucesso = true
                )
                aoConcluir()
                carregar(token)
            } else {
                state = state.copy(
                    salvando = false,
                    toast = mensagemErroHttp(resp.codigo, !resp.conectou, "remover o profissional"),
                    toastSucesso = false
                )
            }
        }
    }

    // ---------------------------------------------------------------------

    private suspend fun chamar(bloco: () -> Call<JsonObject>): Resp = try {
        val r = bloco().awaitResponse()
        Resp(true, r.code(), if (r.isSuccessful) r.body() else null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resp(false, 0, null)
    }
}
