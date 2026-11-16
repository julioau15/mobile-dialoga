package com.aulasandroid.mobiledialoga.feature.profissional.consultas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.MensagemToast
import com.aulasandroid.mobiledialoga.feature.vinculos.components.listaEm
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mensagemErroHttp
import com.aulasandroid.mobiledialoga.feature.vinculos.components.num
import com.aulasandroid.mobiledialoga.feature.vinculos.components.txt
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse
import java.text.Normalizer

// ---------------------------------------------------------------------------
// Modelos da tela "Agenda" do profissional
// ---------------------------------------------------------------------------

enum class AbaAgenda { EVENTOS, DISPONIBILIDADE }

/** Uma consulta marcada com um paciente vinculado ([hora] no formato "HH:mm"). */
data class ConsultaProfissional(
    val id: Int,
    val idVinculo: Int,
    val paciente: String,
    val especialidade: String?,
    val dia: Dia,
    val hora: String
) {
    val subtitulo: String get() = especialidade ?: "Consulta"
}

/** Paciente com vínculo ativo: a consulta é gravada no [idVinculo]. */
data class PacienteDaAgenda(val idVinculo: Int, val nome: String)

/** Estado do pop-up "Nova consulta" / "Editar consulta". */
data class FormularioConsulta(
    val editando: ConsultaProfissional? = null,
    val paciente: PacienteDaAgenda? = null,
    val pacienteTexto: String = "",
    val dataIso: String? = null,
    val hora: String? = null,
    val erroPaciente: String? = null,
    val erroData: String? = null,
    val erroHora: String? = null,
    val erroGeral: String? = null,
    val salvando: Boolean = false
)

data class AgendaProfissionalState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val consultas: List<ConsultaProfissional> = emptyList(),
    val pacientes: List<PacienteDaAgenda> = emptyList(),
    val aba: AbaAgenda = AbaAgenda.EVENTOS,
    val diaSelecionado: Dia = Dia.hoje(),
    val mesVisivel: Dia = Dia.hoje(),
    val busca: String = "",
    val formulario: FormularioConsulta? = null,
    val detalhe: ConsultaProfissional? = null,
    val excluindo: ConsultaProfissional? = null,
    val removendo: Boolean = false,
    val toast: MensagemToast? = null
) {
    // Consultas que passam pela busca "Buscar paciente..." (sem diferenciar maiúsculas e acentos)
    val consultasFiltradas: List<ConsultaProfissional>
        get() {
            val termo = semAcento(busca.trim())
            return if (termo.isEmpty()) consultas else consultas.filter { semAcento(it.paciente).contains(termo) }
        }

    val diasComConsulta: Set<Dia> get() = consultas.map { it.dia }.toSet()

    fun doDia(dia: Dia): List<ConsultaProfissional> =
        consultasFiltradas.filter { it.dia == dia }.sortedBy { it.hora }

    // "Próximos atendimentos": de agora em diante
    fun proximas(limite: Int = 30): List<ConsultaProfissional> {
        val hoje = Dia.hoje()
        val agora = Dia.horaAgora()
        return consultasFiltradas
            .filter { it.dia > hoje || (it.dia == hoje && it.hora >= agora) }
            .take(limite)
    }
}

internal fun semAcento(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class AgendaProfissionalViewModel : ViewModel() {

    var state by mutableStateOf(AgendaProfissionalState())
        private set

    private class Resp(val conectou: Boolean, val codigo: Int, val corpo: JsonObject?, val mensagem: String?) {
        val ok: Boolean get() = conectou && codigo in 200..299
    }

    // Quando a API recusa (ex.: "Esse horário já está ocupado."), a frase vem em "message": mostramos ela ao profissional
    private suspend fun chamar(bloco: () -> Call<JsonObject>): Resp = try {
        val r = bloco().awaitResponse()
        val mensagem = if (r.isSuccessful) null else try {
            val texto = r.errorBody()?.string()
            val json = Gson().fromJson(texto, JsonObject::class.java)
            json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
        Resp(true, r.code(), if (r.isSuccessful) r.body() else null, mensagem)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Resp(false, 0, null, null)
    }

    // ---------- Carregamento ----------

    fun carregar(token: String) {
        state = state.copy(carregando = true, erro = null)
        viewModelScope.launch {
            val (respConsultas, respPacientes) = coroutineScope {
                val c = async { chamar { RetrofitFactory().getProfissionalService().getMinhasConsultas(token) } }
                val p = async { chamar { RetrofitFactory().getProfissionalService().listarPacientesVinculados(token) } }
                c.await() to p.await()
            }
            // 404 = ainda não tem nenhuma: não é erro
            val consultasOk = respConsultas.conectou && (respConsultas.ok || respConsultas.codigo == 404)
            if (!consultasOk) {
                state = state.copy(
                    carregando = false,
                    erro = mensagemErroHttp(respConsultas.codigo, !respConsultas.conectou, "carregar a agenda")
                )
                return@launch
            }
            state = state.copy(
                carregando = false,
                consultas = lerConsultas(respConsultas.corpo),
                // sem a lista de pacientes só o "Nova consulta" fica sem sugestões; a agenda continua funcionando
                pacientes = lerPacientes(respPacientes.corpo)
            )
        }
    }

    private suspend fun recarregarConsultas(token: String): List<ConsultaProfissional>? {
        val resp = chamar { RetrofitFactory().getProfissionalService().getMinhasConsultas(token) }
        return when {
            resp.ok -> lerConsultas(resp.corpo)
            resp.conectou && resp.codigo == 404 -> emptyList()
            else -> null
        }
    }

    private fun lerConsultas(corpo: JsonObject?): List<ConsultaProfissional> =
        corpo.listaEm("consulta").mapNotNull { el ->
            try {
                val o = el.asJsonObject
                if ((o.txt("status") ?: "").lowercase().contains("cancel")) return@mapNotNull null
                val id = o.num("id")
                if (id <= 0) return@mapNotNull null
                val dia = Dia.deIso(o.txt("data")) ?: return@mapNotNull null
                val hora = (o.txt("hora") ?: return@mapNotNull null).take(5)
                ConsultaProfissional(
                    id = id,
                    idVinculo = o.num("id_vinculo"),
                    paciente = o.txt("paciente_nome") ?: "Paciente",
                    especialidade = o.txt("especialidade"),
                    dia = dia,
                    hora = hora
                )
            } catch (_: Exception) {
                null
            }
        }.sortedWith(compareBy({ it.dia }, { it.hora }))

    // Só pacientes com vínculo ativo (status 1) e conta ativa podem receber consulta
    private fun lerPacientes(corpo: JsonObject?): List<PacienteDaAgenda> =
        corpo.listaEm("paciente").mapNotNull { el ->
            try {
                val o = el.asJsonObject
                val idVinculo = o.num("id_vinculo")
                if (idVinculo <= 0 || o.num("status_vinculo") != 1) return@mapNotNull null
                val atividade = (o.txt("status_atividade") ?: "").lowercase()
                if (atividade.isNotEmpty() && !atividade.startsWith("ativ")) return@mapNotNull null
                PacienteDaAgenda(idVinculo, o.txt("nome_completo") ?: o.txt("apelido") ?: "Paciente")
            } catch (_: Exception) {
                null
            }
        }.sortedBy { semAcento(it.nome) }

    // ---------- Navegação da tela ----------

    fun selecionarAba(aba: AbaAgenda) {
        state = state.copy(aba = aba)
    }

    fun selecionarDia(dia: Dia) {
        state = state.copy(diaSelecionado = dia, mesVisivel = dia)
    }

    // direcao: -1 mês anterior, +1 próximo
    fun mudarMes(direcao: Int) {
        state = state.copy(mesVisivel = state.mesVisivel.maisMeses(direcao))
    }

    fun alterarBusca(texto: String) {
        state = state.copy(busca = texto)
    }

    fun fecharToast() {
        state = state.copy(toast = null)
    }

    // ---------- Detalhes e exclusão ----------

    fun abrirDetalhe(consulta: ConsultaProfissional) {
        state = state.copy(detalhe = consulta)
    }

    fun fecharDetalhe() {
        state = state.copy(detalhe = null)
    }

    fun pedirExclusao(consulta: ConsultaProfissional) {
        state = state.copy(detalhe = null, excluindo = consulta)
    }

    fun cancelarExclusao() {
        if (!state.removendo) state = state.copy(excluindo = null)
    }

    fun confirmarExclusao(token: String) {
        val consulta = state.excluindo ?: return
        if (state.removendo) return
        state = state.copy(removendo = true)
        viewModelScope.launch {
            val resp = chamar { RetrofitFactory().getAgendaService().cancelarConsulta(token, consulta.id) }
            state = if (resp.ok) {
                val atualizadas = recarregarConsultas(token) ?: state.consultas.filter { it.id != consulta.id }
                // vermelho, como no protótipo ("Consulta removida.")
                state.copy(removendo = false, excluindo = null, consultas = atualizadas, toast = MensagemToast("Consulta removida.", erro = true))
            } else {
                state.copy(
                    removendo = false,
                    excluindo = null,
                    toast = MensagemToast(resp.mensagem ?: mensagemErroHttp(resp.codigo, !resp.conectou, "remover a consulta"), erro = true)
                )
            }
        }
    }

    // ---------- Formulário (nova consulta / editar) ----------

    fun abrirNovaConsulta() {
        state = state.copy(formulario = FormularioConsulta())
    }

    fun abrirEdicao(consulta: ConsultaProfissional) {
        val paciente = state.pacientes.firstOrNull { it.idVinculo == consulta.idVinculo }
            ?: PacienteDaAgenda(consulta.idVinculo, consulta.paciente)
        state = state.copy(
            detalhe = null,
            formulario = FormularioConsulta(
                editando = consulta,
                paciente = paciente,
                pacienteTexto = paciente.nome,
                dataIso = consulta.dia.paraIso(),
                hora = consulta.hora
            )
        )
    }

    fun fecharFormulario() {
        if (state.formulario?.salvando == true) return
        state = state.copy(formulario = null)
    }

    private fun atualizarFormulario(alterar: (FormularioConsulta) -> FormularioConsulta) {
        val atual = state.formulario ?: return
        state = state.copy(formulario = alterar(atual))
    }

    fun alterarPacienteTexto(texto: String) = atualizarFormulario { f ->
        // digitar de novo invalida o paciente escolhido: ele precisa ser escolhido na lista
        f.copy(
            pacienteTexto = texto,
            paciente = f.paciente?.takeIf { it.nome == texto },
            erroPaciente = null,
            erroGeral = null
        )
    }

    fun escolherPaciente(paciente: PacienteDaAgenda) = atualizarFormulario {
        it.copy(paciente = paciente, pacienteTexto = paciente.nome, erroPaciente = null, erroGeral = null)
    }

    fun alterarData(iso: String?) = atualizarFormulario { it.copy(dataIso = iso, erroData = null, erroGeral = null) }

    fun alterarHora(hora: String) = atualizarFormulario { it.copy(hora = hora, erroHora = null, erroGeral = null) }

    fun salvar(token: String) {
        val f = state.formulario ?: return
        if (f.salvando) return

        val paciente = f.paciente
        val dia = Dia.deIso(f.dataIso)
        val hora = f.hora
        if (paciente == null || dia == null || hora == null) {
            state = state.copy(
                formulario = f.copy(
                    erroPaciente = if (paciente == null) "Escolha um paciente da lista." else null,
                    erroData = if (dia == null) "Escolha a data." else null,
                    erroHora = if (hora == null) "Escolha o horário." else null
                )
            )
            return
        }

        val editando = f.editando
        state = state.copy(formulario = f.copy(salvando = true, erroGeral = null))

        viewModelScope.launch {
            val corpo = corpoDaConsulta(paciente, dia, hora, editando?.especialidade)
            val resp = chamar {
                val agenda = RetrofitFactory().getAgendaService()
                if (editando == null) agenda.criarConsulta(token, corpo) else agenda.atualizarConsulta(token, editando.id, corpo)
            }
            if (resp.ok) {
                val atualizadas = recarregarConsultas(token)
                state = state.copy(
                    formulario = null,
                    consultas = atualizadas ?: state.consultas,
                    // mostra o dia da consulta salva
                    diaSelecionado = dia,
                    mesVisivel = dia,
                    toast = MensagemToast(if (editando == null) "Consulta agendada" else "Consulta atualizada")
                )
            } else {
                val atual = state.formulario ?: return@launch
                state = state.copy(
                    formulario = atual.copy(
                        salvando = false,
                        erroGeral = resp.mensagem ?: mensagemErroHttp(resp.codigo, !resp.conectou, "salvar a consulta")
                    )
                )
            }
        }
    }

    /**
     * Corpo do POST/PUT /consultas. O back valida "nome_da_consulta" (e já gravou "nome_da_conculta", com erro de
     * digitação: mandamos os dois, como o app do paciente) e exige "especialidade" presente: vazio é aceito e o back
     * usa a do profissional quando ele tem só uma. Quem criou (profissional) e o status o back define pelo token.
     */
    private fun corpoDaConsulta(paciente: PacienteDaAgenda, dia: Dia, hora: String, especialidade: String?): JsonObject =
        JsonObject().apply {
            addProperty("nome_da_consulta", paciente.nome)
            addProperty("nome_da_conculta", paciente.nome)
            addProperty("especialidade", especialidade.orEmpty())
            addProperty("data", dia.paraIso())
            addProperty("hora", hora)
            addProperty("id_vinculo", paciente.idVinculo)
        }
}
