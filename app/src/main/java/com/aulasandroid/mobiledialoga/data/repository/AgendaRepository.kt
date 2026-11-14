package com.aulasandroid.mobiledialoga.data.repository

import com.aulasandroid.mobiledialoga.data.remote.AgendaService
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaJson
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaRegras
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Consulta
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.DadosAgenda
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.HorariosDoProfissional
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.ProfissionalVinculado
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.io.IOException

// Erro com mensagem pronta para mostrar ao usuário
class AgendaException(message: String) : Exception(message)

private const val ERRO_CONEXAO = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."
private const val ERRO_SESSAO = "Sua sessão expirou. Faça login novamente."
private const val ERRO_SERVIDOR = "Tivemos um problema no servidor. Tente novamente em instantes."
private const val ERRO_ROTA = "Este recurso não existe no servidor. Atualize o back-end e reinicie-o."
private const val ERRO_PADRAO = "Não foi possível concluir a ação."

class AgendaRepository(
    private val servico: AgendaService = RetrofitFactory().getAgendaService()
) {

    // Carrega tudo da Agenda: consultas do paciente e os profissionais com quem ele tem vínculo ativo
    suspend fun carregar(token: String): DadosAgenda = withContext(Dispatchers.IO) {
        val t = limpar(token)
        coroutineScope {
            val paciente = async { executar(servico.getPaciente(t)) }
            val vinculos = async { executar(servico.getVinculos(t), vazioEm404 = true) }
            val profissionais = async { executar(servico.getProfissionais(t), vazioEm404 = true) }
            val consultas = async { executar(servico.getConsultas(t), vazioEm404 = true) }

            val profissionaisJson = profissionais.await()
            val idPaciente = AgendaJson.idDoPaciente(paciente.await())
                ?: throw AgendaException("Não foi possível identificar o seu cadastro de paciente.")

            val meusVinculos = AgendaJson.vinculosDoPaciente(vinculos.await(), idPaciente)
            val idsVinculos = meusVinculos.map { it.id }.toSet()

            DadosAgenda(
                consultas = AgendaJson.consultasDosVinculos(AgendaJson.consultas(consultas.await()), idsVinculos),
                profissionais = profissionaisJson.let { json ->
                    AgendaRegras.profissionaisVinculados(
                        meusVinculos,
                        AgendaJson.nomesDosProfissionais(json),
                        AgendaJson.especialidadesDosProfissionais(json)
                    )
                },
                idsVinculos = idsVinculos
            )
        }
    }

    // Recarrega só as consultas (depois de agendar)
    suspend fun consultasDosVinculos(token: String, idsVinculos: Set<Int>): List<Consulta> =
        withContext(Dispatchers.IO) {
            val resposta = executar(servico.getConsultas(limpar(token)), vazioEm404 = true)
            AgendaJson.consultasDosVinculos(AgendaJson.consultas(resposta), idsVinculos)
        }

    // Dias e horários em que o profissional deste vínculo atende e ainda está livre
    suspend fun horarios(token: String, idVinculo: Int): HorariosDoProfissional =
        withContext(Dispatchers.IO) {
            val resposta = executar(servico.getHorarios(limpar(token), idVinculo))
            AgendaJson.horarios(resposta) ?: throw AgendaException(ERRO_PADRAO)
        }

    // Agenda a consulta no vínculo do profissional escolhido
    suspend fun agendar(
        token: String,
        profissional: ProfissionalVinculado,
        dia: Dia,
        hora: String,
        especialidade: String
    ) {
        withContext(Dispatchers.IO) {
            val corpo = AgendaJson.corpoDaNovaConsulta(profissional, dia, hora, especialidade)
            executar(servico.criarConsulta(limpar(token), corpo))
        }
    }

    // Cancela (apaga) a consulta
    suspend fun cancelar(token: String, idConsulta: Int) {
        withContext(Dispatchers.IO) {
            executar(servico.cancelarConsulta(limpar(token), idConsulta))
        }
    }

    // Token como o resto do app usa (sem o prefixo "Bearer ")
    private fun limpar(token: String): String = token.removePrefix("Bearer ").trim()

    /**
     * Executa a chamada e devolve o JSON, ou lança [AgendaException] com uma mensagem amigável.
     * [vazioEm404]: o back responde 404 (em JSON, com status_code 404) quando uma lista está vazia.
     * Já um 404 SEM esse JSON (HTML do Express) é rota inexistente: back-end desatualizado, e isso é erro.
     */
    private fun executar(chamada: Call<JsonObject>, vazioEm404: Boolean = false): JsonObject? {
        val resposta = try {
            chamada.execute()
        } catch (e: IOException) {
            throw AgendaException(ERRO_CONEXAO)
        }

        if (resposta.isSuccessful) return resposta.body()

        val corpo = resposta.errorBody()?.string()

        if (resposta.code() == 404) {
            if (AgendaJson.statusCodeDoCorpo(corpo) != 404) throw AgendaException(ERRO_ROTA)
            if (vazioEm404) return null
            throw AgendaException(AgendaJson.mensagemDeErro(corpo) ?: ERRO_PADRAO)
        }
        if (resposta.code() == 401) throw AgendaException(ERRO_SESSAO)
        if (resposta.code() >= 500) throw AgendaException(ERRO_SERVIDOR)

        throw AgendaException(AgendaJson.mensagemDeErro(corpo) ?: ERRO_PADRAO)
    }
}
