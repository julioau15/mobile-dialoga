package com.aulasandroid.mobiledialoga.feature.paciente.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.AuthState
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

class PacienteDashboardViewModel : ViewModel() {
    var state by mutableStateOf(DashboardState())
        private set

    var salvandoHumor by mutableStateOf(false)
        private set

    var erroHumor by mutableStateOf<String?>(null)
        private set

    fun limparErroHumor() {
        erroHumor = null
    }

    fun carregarDados(authState: AuthState) {
        val token = authState.auth?.token ?: return
        val nome = authState.userProfile?.getNomeExibicao()
            ?.trim()?.substringBefore(" ").orEmpty().ifBlank { "Paciente" }

        state = state.copy(isLoading = true, erro = null, nomeUsuario = nome)

        viewModelScope.launch {
            val service = RetrofitFactory().getPacienteService()

            // As 5 chamadas rodam em paralelo; falha individual vira null (seção vazia).
            val respostas = coroutineScope {
                listOf(
                    async { service.getMeusTratamentos(token).corpo() },
                    async { service.getMeusProfissionais(token).corpo() },
                    async { service.getMeusRegistrosHumor(token).corpo() },
                    async { service.getMinhasTarefas(token).corpo() },
                    async { service.getMinhasConsultas(token).corpo() }
                ).awaitAll()
            }
            val (tratamentos, profissionais, humor, tarefas, consultas) = respostas

            if (respostas.all { it == null }) {
                state = state.copy(
                    isLoading = false,
                    erro = "Não foi possível carregar os dados. Verifique sua conexão."
                )
                return@launch
            }

            val medicamentos = DashboardMapper.medicamentos(tratamentos)
            val consultasFuturas = DashboardMapper.consultas(consultas)
            val listaTarefas = DashboardMapper.tarefas(tarefas)
            val registros = DashboardMapper.registrosHumor(humor)

            state = state.copy(
                isLoading = false,
                indicadoresHoje = DashboardMapper.indicadoresHoje(registros),
                sonoSemana = DashboardMapper.sonoSemana(registros),
                estatisticas = EstatisticasGerais(
                    tratamentosAtivos = medicamentos.size,
                    proximasConsultas = consultasFuturas.size,
                    tarefasPendentes = listaTarefas.count { it.status != "Concluída" },
                    profissionais = DashboardMapper.quantidade(profissionais, "profissionais", "response", "dados", "data")
                ),
                medicamentosHoje = medicamentos,
                proximasConsultas = consultasFuturas.take(3),
                tarefasRecentes = listaTarefas.take(3)
            )
        }
    }

    /** POST /pacientes/me/registros-humor. Em sucesso chama [onSucesso] e recarrega o dashboard. */
    fun registrarHumor(authState: AuthState, dados: JsonObject, onSucesso: () -> Unit) {
        if (salvandoHumor) return
        val token = authState.auth?.token
        if (token == null) {
            erroHumor = "Sessão expirada. Faça login novamente."
            return
        }

        salvandoHumor = true
        erroHumor = null

        viewModelScope.launch {
            try {
                val resp = RetrofitFactory().getPacienteService().registrarHumor(token, dados).awaitResponse()
                if (resp.isSuccessful) {
                    onSucesso()
                    carregarDados(authState)
                } else {
                    erroHumor = mensagemDeErro(resp.errorBody()?.string())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                erroHumor = "Sem conexão com o servidor. Tente novamente."
            } finally {
                salvandoHumor = false
            }
        }
    }

    private fun mensagemDeErro(corpo: String?): String {
        val padrao = "Não foi possível salvar o registro. Tente novamente."
        if (corpo.isNullOrBlank()) return padrao
        return try {
            val json = JsonParser().parse(corpo).asJsonObject
            listOf("message", "erro", "error").firstNotNullOfOrNull { k ->
                json.get(k)?.takeIf { it.isJsonPrimitive }?.asString
            } ?: padrao
        } catch (e: Exception) {
            padrao
        }
    }

    private suspend fun Call<JsonObject>.corpo(): JsonObject? = try {
        val r = awaitResponse()
        if (r.isSuccessful) r.body() else null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}