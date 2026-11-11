package com.aulasandroid.mobiledialoga.feature.profissional.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.hojeIso
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.paraIso
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

data class AtendimentoHojeUi(
    val id: Int,
    val paciente: String,
    val titulo: String,
    val hora: String // "09:00"
)

data class PacienteRecenteUi(
    val id: Int,
    val nome: String,
    val proximaIso: String?, // próxima consulta (yyyy-MM-dd) ou null
    val ativo: Boolean
)

data class ProfissionalDashboardState(
    val isLoading: Boolean = false,
    val carregou: Boolean = false,
    val erro: String? = null,
    val pacientes: List<PacienteRecenteUi> = emptyList(),
    val atendimentosHoje: List<AtendimentoHojeUi> = emptyList()
) {
    val pacientesAtivos: Int get() = pacientes.count { it.ativo }
}

/**
 * Pacientes e consultas vêm do back (profissionais/me/pacientes e profissionais/me/consultas).
 * As tarefas já são carregadas pelo TarefasProfissionalViewModel, que o dashboard reaproveita.
 */
class ProfissionalDashboardViewModel : ViewModel() {

    var state by mutableStateOf(ProfissionalDashboardState())
        private set

    private class Resp(val conectou: Boolean, val codigo: Int, val corpo: JsonObject?) {
        // O back responde 404 quando a lista está vazia: isso não é erro, é "nada por aqui"
        val falhou: Boolean get() = !conectou || (codigo !in 200..299 && codigo != 404)
    }

    fun carregar(token: String) {
        if (state.isLoading) return
        state = state.copy(isLoading = true, erro = null)

        viewModelScope.launch {
            val service = RetrofitFactory().getProfissionalService()

            val (respPacientes, respConsultas) = coroutineScope {
                val pacientes = async { chamar { service.listarPacientesVinculados(token) } }
                val consultas = async { chamar { service.getMinhasConsultas(token) } }
                pacientes.await() to consultas.await()
            }

            if (respPacientes.falhou && respConsultas.falhou) {
                val sessaoExpirada = respPacientes.codigo == 401 || respConsultas.codigo == 401
                state = state.copy(
                    isLoading = false,
                    erro = if (sessaoExpirada) "Sua sessão expirou. Entre novamente."
                    else "Não foi possível carregar o painel. Verifique sua conexão."
                )
                return@launch
            }

            val consultas = lerConsultas(respConsultas.corpo)
            val hoje = hojeIso()

            val atendimentosHoje = consultas
                .filter { it.dataIso == hoje }
                .sortedBy { it.hora }
                .map { AtendimentoHojeUi(it.id, it.paciente, it.titulo, it.hora) }

            // Próxima consulta de cada paciente = a data mais próxima de hoje em diante
            val proximaPorPaciente: Map<Int, String> = consultas
                .filter { it.dataIso != null && it.dataIso >= hoje }
                .groupBy { it.idPaciente }
                .mapValues { (_, lista) -> lista.minOf { it.dataIso!! } }

            val pacientes = lerPacientes(respPacientes.corpo, proximaPorPaciente)

            state = ProfissionalDashboardState(
                isLoading = false,
                carregou = true,
                erro = null,
                pacientes = pacientes,
                atendimentosHoje = atendimentosHoje
            )
        }
    }

    // ---------------------------------------------------------------------
    // Rede + parse
    // ---------------------------------------------------------------------

    private suspend fun chamar(bloco: () -> Call<JsonObject>): Resp = try {
        val r = bloco().awaitResponse()
        Resp(true, r.code(), if (r.isSuccessful) r.body() else null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resp(false, 0, null)
    }

    private class ConsultaApi(
        val id: Int,
        val idPaciente: Int,
        val paciente: String,
        val titulo: String,
        val dataIso: String?,
        val hora: String
    )

    private fun lerConsultas(corpo: JsonObject?): List<ConsultaApi> {
        val lista = array(corpo, "consulta")
        val resultado = mutableListOf<ConsultaApi>()
        for (elem in lista) {
            try {
                val obj = elem.asJsonObject
                val status = texto(obj, "status")?.lowercase().orEmpty()
                if (status.contains("cancel")) continue // consulta cancelada não conta como atendimento
                resultado.add(
                    ConsultaApi(
                        id = inteiro(obj, "id"),
                        idPaciente = inteiro(obj, "id_paciente"),
                        paciente = texto(obj, "paciente_nome") ?: "Paciente",
                        titulo = texto(obj, "nome_da_consulta") ?: texto(obj, "especialidade") ?: "Consulta",
                        dataIso = texto(obj, "data").paraIso(),
                        hora = (texto(obj, "hora") ?: "").take(5)
                    )
                )
            } catch (_: Exception) { }
        }
        return resultado
    }

    private fun lerPacientes(corpo: JsonObject?, proximaPorPaciente: Map<Int, String>): List<PacienteRecenteUi> {
        val resultado = mutableListOf<PacienteRecenteUi>()
        for (elem in array(corpo, "paciente")) {
            try {
                val obj = elem.asJsonObject
                val id = inteiro(obj, "id")
                if (id <= 0) continue
                val status = texto(obj, "status_atividade")?.lowercase().orEmpty()
                resultado.add(
                    PacienteRecenteUi(
                        id = id,
                        nome = texto(obj, "nome_completo") ?: "Paciente $id",
                        proximaIso = proximaPorPaciente[id],
                        ativo = status.isEmpty() || status.startsWith("ativ")
                    )
                )
            } catch (_: Exception) { }
        }
        // Quem tem consulta mais próxima vem primeiro; quem não tem fica no fim, em ordem alfabética
        return resultado.sortedWith(
            compareBy<PacienteRecenteUi> { it.proximaIso == null }
                .thenBy { it.proximaIso ?: "" }
                .thenBy { it.nome.lowercase() }
        )
    }

    private fun array(corpo: JsonObject?, chave: String): JsonArray = try {
        corpo?.getAsJsonObject("response")?.getAsJsonArray(chave) ?: JsonArray()
    } catch (_: Exception) {
        JsonArray()
    }

    private fun texto(obj: JsonObject, chave: String): String? {
        val e = obj.get(chave) ?: return null
        if (e.isJsonNull) return null
        val s = try { e.asString.trim() } catch (_: Exception) { "" }
        return if (s.isEmpty() || s.equals("null", true)) null else s
    }

    private fun inteiro(obj: JsonObject, chave: String): Int {
        val e = obj.get(chave) ?: return 0
        if (e.isJsonNull) return 0
        return try { e.asInt } catch (_: Exception) { 0 }
    }
}
