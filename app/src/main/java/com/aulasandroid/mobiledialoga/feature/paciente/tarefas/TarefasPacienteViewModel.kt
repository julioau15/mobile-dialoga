package com.aulasandroid.mobiledialoga.feature.paciente.tarefas

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.data.remote.TarefaService
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.OpcaoPerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.PerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.TarefaItemDto
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAMANHO_MAX_ANEXO_BYTES = 10 * 1024 * 1024 // 10 MB

data class TarefasPacienteUiState(
    val tarefas: List<TarefaItemDto> = emptyList(),
    val filtroAtivo: String = "Todas", // "Todas", "Em andamento", "Em atraso", "Concluídas", "Pendentes"
    val isLoading: Boolean = false,
    val erroGeral: String? = null,
    val toastMensagem: String? = null,
    val toastErro: Boolean = false,
    // Tela de resposta em tela cheia
    val tarefaEmEdicao: TarefaItemDto? = null,
    val perguntas: List<PerguntaDto> = emptyList(),
    val isLoadingDetalhes: Boolean = false,
    val isEnviandoResposta: Boolean = false,
    val erroDetalhes: String? = null,
    // Nova tarefa
    val isCriandoNovaTarefa: Boolean = false,
    val erroNovaTarefa: String? = null,
    // Respostas temporárias do questionário
    val respostasOpcaoUnica: Map<Int, Int> = emptyMap(), // idPergunta -> idOpcao (humor / unica_escolha)
    val respostasMultiplasOpcoes: Map<Int, List<Int>> = emptyMap(), // idPergunta -> idOpcoes (multipla_escolha)
    val respostasTexto: Map<Int, String> = emptyMap(), // idPergunta -> texto
    val respostasEscala: Map<Int, Int> = emptyMap(), // idPergunta -> número (0..10)
    // Modais
    val mostrarModalConfirmarEnvio: Boolean = false,
    val mostrarModalNovaTarefa: Boolean = false
) {
    val todasCount: Int get() = tarefas.size
    val emAndamentoCount: Int get() = tarefas.count { it.getStatusFormatado() == "Em andamento" }
    val emAtrasoCount: Int get() = tarefas.count { it.getStatusFormatado() == "Em atraso" }
    val concluidasCount: Int get() = tarefas.count { it.getStatusFormatado() == "Concluída" }
    val pendentesCount: Int get() = tarefas.count { it.getStatusFormatado() == "Pendente" }

    fun perguntaRespondida(p: PerguntaDto): Boolean = when (p.tipoNormalizado()) {
        "humor", "unica_escolha" -> respostasOpcaoUnica.containsKey(p.id)
        "multipla_escolha" -> respostasMultiplasOpcoes[p.id].orEmpty().isNotEmpty()
        "escala" -> respostasEscala.containsKey(p.id)
        else -> respostasTexto[p.id].orEmpty().isNotBlank()
    }

    val perguntasObrigatoriasPendentes: List<PerguntaDto>
        get() = perguntas.filter { it.obrigatoria && !perguntaRespondida(it) }

    val algumaRespostaPreenchida: Boolean get() = perguntas.any { perguntaRespondida(it) }

    val progressoRespostas: Int
        get() = if (perguntas.isEmpty()) 0
            else perguntas.count { perguntaRespondida(it) } * 100 / perguntas.size

    /** Só libera o envio com todas as perguntas obrigatórias (*) respondidas. */
    val podeEnviar: Boolean
        get() = perguntas.isNotEmpty() && algumaRespostaPreenchida && perguntasObrigatoriasPendentes.isEmpty()
}

class TarefasPacienteViewModel : ViewModel() {

    private val _uiState = MutableLiveData(TarefasPacienteUiState())
    val uiState: LiveData<TarefasPacienteUiState> = _uiState

    private val estado: TarefasPacienteUiState get() = _uiState.value ?: TarefasPacienteUiState()

    private val service: TarefaService get() = RetrofitFactory().getTarefaService()
    private var respostasCarregadas = false
    private var versaoLista = 0
    private val progressosSalvos = mutableMapOf<Int, Int>()

    // ---------- Lista de tarefas ----------

    fun carregarTarefas(token: String?) {
        if (token.isNullOrBlank()) return
        val versao = ++versaoLista
        _uiState.value = estado.copy(isLoading = true, erroGeral = null)

        service.listarTarefas(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when {
                    response.isSuccessful -> {
                        if (versao != versaoLista) return
                        val lista = converterTarefas(response.body()).map { tarefa ->
                            val salvo = progressosSalvos[tarefa.idTarefaDestinatario ?: tarefa.id]
                            if (salvo != null && tarefa.getStatusFormatado() != "Concluída")
                                tarefa.copy(progresso = salvo) else tarefa
                        }
                        _uiState.value = estado.copy(isLoading = false, tarefas = lista, erroGeral = null)
                        lista.filter { it.getStatusFormatado() != "Concluída" }.forEach {
                            carregarProgressoSalvo(token, it, versao)
                        }
                    }
                    // o back responde 404 quando o paciente ainda não tem tarefas
                    response.code() == 404 -> {
                        _uiState.value = estado.copy(isLoading = false, tarefas = emptyList(), erroGeral = null)
                    }
                    else -> falhaAoCarregarLista(
                        mensagemDeErro(response, "Não foi possível carregar as tarefas")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                falhaAoCarregarLista("Sem conexão com o servidor.")
            }
        })
    }

    // A listagem pode não devolver o percentual. Reconstrói a partir das respostas persistidas.
    private fun carregarProgressoSalvo(token: String, tarefa: TarefaItemDto, versao: Int) {
        val idDestinatario = tarefa.idTarefaDestinatario?.takeIf { it > 0 } ?: tarefa.id
        service.listarPerguntasPorTarefa(tarefa.id, token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (versao != versaoLista || !response.isSuccessful) return
                val perguntas = converterPerguntas(response.body())
                if (perguntas.isEmpty()) return
                service.listarRespostasPorTarefaDestinatario(idDestinatario, token)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(c: Call<JsonObject>, r: Response<JsonObject>) {
                            if (versao != versaoLista || (!r.isSuccessful && r.code() != 404)) return
                            val respostas = findArray(r.body(), "resposta", "respostas", "response", "dados")
                                .filter { it.isJsonObject }.map { it.asJsonObject }
                                .groupBy { parseAttrInt(it, "id_pergunta") }
                            val respondidas = perguntas.count { pergunta ->
                                respostas[pergunta.id].orEmpty().any { resposta ->
                                    when (pergunta.tipoNormalizado()) {
                                        "humor", "unica_escolha", "multipla_escolha" ->
                                            parseAttrInt(resposta, "id_opcao_pergunta") > 0
                                        "escala" -> parseAttrString(resposta, "valor")?.toIntOrNull() in 0..10
                                        else -> !parseAttrString(resposta, "valor").isNullOrBlank()
                                    }
                                }
                            }
                            val progresso = respondidas * 100 / perguntas.size
                            progressosSalvos[idDestinatario] = progresso
                            _uiState.value = estado.copy(tarefas = estado.tarefas.map { item ->
                                if ((item.idTarefaDestinatario ?: item.id) == idDestinatario &&
                                    item.getStatusFormatado() != "Concluída") item.copy(progresso = progresso)
                                else item
                            })
                        }
                        override fun onFailure(c: Call<JsonObject>, t: Throwable) {
                            // Mantém o último percentual conhecido se não houver conexão.
                        }
                    })
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                // Mantém o último percentual conhecido se não houver conexão.
            }
        })
    }

    private fun falhaAoCarregarLista(mensagem: String) {
        _uiState.value = estado.copy(isLoading = false, erroGeral = mensagem)
    }

    private fun converterTarefas(json: JsonObject?): List<TarefaItemDto> {
        val array = findArray(json, "tarefa", "tarefas", "tarefaDestinatario", "response", "dados")
        val lista = mutableListOf<TarefaItemDto>()
        for (elem in array) {
            try {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject

                val status = parseAttrString(obj, "status") ?: "pendente"
                val concluida = status.lowercase().trim() in listOf("concluida", "concluido", "finalizado", "concluída")
                val doProfissional = parseAttrInt(obj, "id_profissional_criador") > 0
                val doPaciente = parseAttrInt(obj, "id_paciente_criador") > 0
                val nomeProfissional = parseAttrString(obj, "profissional_nome", "nome_profissional")
                    ?: if (doPaciente && !doProfissional) "Você" else "Seu profissional"
                val criadaPorMim = (doPaciente && !doProfissional) || nomeProfissional == "Você"

                lista.add(
                    TarefaItemDto(
                        id = parseAttrInt(obj, "id_tarefa", "id"),
                        titulo = parseAttrString(obj, "titulo") ?: "Tarefa sem título",
                        profissionalNome = nomeProfissional,
                        tipo = parseAttrString(obj, "tipo"),
                        instrucoes = parseAttrString(obj, "instrucoes"),
                        anexoArquivo = parseAttrString(obj, "anexo_arquivo"),
                        dataEnvio = parseAttrString(obj, "data_envio"),
                        idTarefaDestinatario = parseAttrInt(obj, "id_tarefa_destinatario", "id"),
                        prazo = parseAttrString(obj, "prazo"),
                        status = status,
                        dataConclusao = parseAttrString(obj, "data_conclusao"),
                        // a API não calcula progresso: só mostra o que vier no campo "progresso"
                        progresso = if (concluida) 100 else parseAttrInt(obj, "progresso").coerceIn(0, 100),
                        criadaPorMim = criadaPorMim
                    )
                )
            } catch (e: Exception) {
                // item mal formado: ignora
            }
        }
        return lista
    }

    fun setFiltro(filtro: String) {
        _uiState.value = estado.copy(filtroAtivo = filtro)
    }

    fun limparErroGeral() {
        _uiState.value = estado.copy(erroGeral = null)
    }

    // ---------- Abrir tarefa (perguntas + respostas) ----------

    fun abrirTarefa(token: String?, tarefa: TarefaItemDto) {
        respostasCarregadas = false
        _uiState.value = estado.copy(
            tarefaEmEdicao = tarefa,
            perguntas = emptyList(),
            respostasOpcaoUnica = emptyMap(),
            respostasMultiplasOpcoes = emptyMap(),
            respostasTexto = emptyMap(),
            respostasEscala = emptyMap(),
            isLoadingDetalhes = true,
            isEnviandoResposta = false,
            mostrarModalConfirmarEnvio = false,
            erroDetalhes = null
        )

        val idDestinatario = tarefa.idTarefaDestinatario?.takeIf { it > 0 } ?: tarefa.id

        service.listarPerguntasPorTarefa(tarefa.id, token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (estado.tarefaEmEdicao == null) return // o usuário já voltou para a lista

                when {
                    response.isSuccessful -> {
                        val perguntas = converterPerguntas(response.body())
                        carregarRespostasAnteriores(token, idDestinatario, perguntas)
                    }
                    // 404 = tarefa sem perguntas
                    response.code() == 404 -> {
                        carregarRespostasAnteriores(token, idDestinatario, emptyList())
                    }
                    else -> falhaAoCarregarDetalhes(
                        mensagemDeErro(response, "Não foi possível carregar as perguntas")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                if (estado.tarefaEmEdicao == null) return
                falhaAoCarregarDetalhes("Sem conexão com o servidor.")
            }
        })
    }

    private fun falhaAoCarregarDetalhes(mensagem: String) {
        _uiState.value = estado.copy(isLoadingDetalhes = false, erroDetalhes = mensagem)
    }

    private fun converterPerguntas(json: JsonObject?): List<PerguntaDto> {
        val array = findArray(json, "pergunta", "perguntas", "response", "dados")
        val lista = mutableListOf<Pair<Int, PerguntaDto>>()
        for (elem in array) {
            try {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject

                val opcoes = mutableListOf<Pair<Int, OpcaoPerguntaDto>>()
                for (oElem in findArray(obj, "opcoes", "opcao_pergunta", "opcoes_pergunta")) {
                    if (!oElem.isJsonObject) continue
                    val oObj = oElem.asJsonObject
                    opcoes.add(
                        parseAttrInt(oObj, "ordem") to OpcaoPerguntaDto(
                            id = parseAttrInt(oObj, "id"),
                            texto = parseAttrString(oObj, "texto", "opcao", "valor") ?: ""
                        )
                    )
                }

                lista.add(
                    parseAttrInt(obj, "ordem") to PerguntaDto(
                        id = parseAttrInt(obj, "id"),
                        enunciado = parseAttrString(obj, "enunciado", "texto", "pergunta") ?: "Pergunta",
                        tipo = parseAttrString(obj, "tipo") ?: "texto",
                        opcoes = opcoes.sortedBy { it.first }.map { it.second },
                        obrigatoria = parseAttrBool(obj, "obrigatoria", padrao = true)
                    )
                )
            } catch (e: Exception) {
                // pergunta mal formada: ignora
            }
        }
        return lista.sortedBy { it.first }.map { it.second }
    }

    private fun carregarRespostasAnteriores(token: String?, idDestinatario: Int, perguntas: List<PerguntaDto>) {
        service.listarRespostasPorTarefaDestinatario(idDestinatario, token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (estado.tarefaEmEdicao == null) return

                val mapaUnica = mutableMapOf<Int, Int>()
                val mapaMultipla = mutableMapOf<Int, MutableList<Int>>()
                val mapaTexto = mutableMapOf<Int, String>()
                val mapaEscala = mutableMapOf<Int, Int>()

                // 404 = ainda não existem respostas (normal para tarefa pendente)
                if (!response.isSuccessful && response.code() != 404) {
                    _uiState.value = estado.copy(
                        isLoadingDetalhes = false,
                        perguntas = perguntas,
                        erroDetalhes = mensagemDeErro(response, "Não foi possível carregar as respostas")
                    )
                    return
                }

                if (response.isSuccessful) {
                    val tipos = perguntas.associate { it.id to it.tipoNormalizado() }
                    val array = findArray(response.body(), "resposta", "respostas", "response", "dados")
                    for (elem in array) {
                        try {
                            if (!elem.isJsonObject) continue
                            val obj = elem.asJsonObject
                            val pId = parseAttrInt(obj, "id_pergunta")
                            val oId = parseAttrInt(obj, "id_opcao_pergunta")
                            val valor = parseAttrString(obj, "valor")

                            // cada resposta vai só para o mapa do tipo da sua pergunta (evita duplicar no reenvio)
                            when (tipos[pId]) {
                                "humor", "unica_escolha" -> if (oId > 0) mapaUnica[pId] = oId
                                "multipla_escolha" -> if (oId > 0) mapaMultipla.getOrPut(pId) { mutableListOf() }.add(oId)
                                "escala" -> valor?.toIntOrNull()?.let { if (it in 0..10) mapaEscala[pId] = it }
                                "texto" -> if (!valor.isNullOrBlank()) mapaTexto[pId] = valor
                                else -> { /* resposta de uma pergunta que não está na lista */ }
                            }
                        } catch (e: Exception) {
                            // resposta mal formada: ignora
                        }
                    }
                }

                _uiState.value = estado.copy(
                    isLoadingDetalhes = false,
                    perguntas = perguntas,
                    respostasOpcaoUnica = mapaUnica,
                    respostasMultiplasOpcoes = mapaMultipla,
                    respostasTexto = mapaTexto,
                    respostasEscala = mapaEscala
                )
                respostasCarregadas = true
                val tarefa = estado.tarefaEmEdicao ?: return
                if (tarefa.getStatusFormatado() != "Concluída") {
                    val progresso = if (perguntas.isEmpty()) tarefa.progresso
                        else perguntas.count { estado.perguntaRespondida(it) } * 100 / perguntas.size
                    _uiState.value = estado.copy(isEnviandoResposta = true)
                    service.atualizarStatusTarefaDestinatario(
                        idDestinatario, payloadStatus(tarefa, token, "em_andamento", progresso), token
                    ).enqueue(object : Callback<JsonObject> {
                        override fun onResponse(c: Call<JsonObject>, r: Response<JsonObject>) {
                            if (estado.tarefaEmEdicao?.id != tarefa.id) return
                            if (r.isSuccessful) {
                                val atualizada = tarefa.copy(status = "em_andamento", progresso = progresso)
                                _uiState.value = estado.copy(
                                    isEnviandoResposta = false,
                                    tarefaEmEdicao = atualizada,
                                    tarefas = estado.tarefas.map { if (it.id == tarefa.id) atualizada else it }
                                )
                            } else {
                                _uiState.value = estado.copy(isEnviandoResposta = false, erroDetalhes =
                                    mensagemDeErro(r, "Não foi possível iniciar a tarefa"))
                            }
                        }
                        override fun onFailure(c: Call<JsonObject>, t: Throwable) {
                            if (estado.tarefaEmEdicao?.id != tarefa.id) return
                            _uiState.value = estado.copy(isEnviandoResposta = false, erroDetalhes = "Sem conexão. Salve antes de sair.")
                        }
                    })
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                if (estado.tarefaEmEdicao == null) return
                _uiState.value = estado.copy(
                    isLoadingDetalhes = false,
                    perguntas = perguntas,
                    erroDetalhes = "Sem conexão com o servidor."
                )
            }
        })
    }

    fun fecharEdicaoTarefa() {
        _uiState.value = estado.copy(
            tarefaEmEdicao = null,
            perguntas = emptyList(),
            respostasOpcaoUnica = emptyMap(),
            respostasMultiplasOpcoes = emptyMap(),
            respostasTexto = emptyMap(),
            respostasEscala = emptyMap(),
            mostrarModalConfirmarEnvio = false,
            isLoadingDetalhes = false,
            isEnviandoResposta = false,
            erroDetalhes = null
        )
    }

    // ---------- Handlers de interação com as respostas ----------

    fun onOpcaoUnicaSelecionada(idPergunta: Int, idOpcao: Int) {
        if (estado.isEnviandoResposta) return
        val mapa = estado.respostasOpcaoUnica.toMutableMap()
        mapa[idPergunta] = idOpcao
        _uiState.value = estado.copy(respostasOpcaoUnica = mapa)
    }

    fun onOpcaoMultiplaToggled(idPergunta: Int, idOpcao: Int) {
        if (estado.isEnviandoResposta) return
        val mapa = estado.respostasMultiplasOpcoes.toMutableMap()
        val lista = mapa[idPergunta]?.toMutableList() ?: mutableListOf()
        if (lista.contains(idOpcao)) lista.remove(idOpcao) else lista.add(idOpcao)
        mapa[idPergunta] = lista
        _uiState.value = estado.copy(respostasMultiplasOpcoes = mapa)
    }

    fun onTextoRespostaChanged(idPergunta: Int, texto: String) {
        if (estado.isEnviandoResposta) return
        val mapa = estado.respostasTexto.toMutableMap()
        mapa[idPergunta] = texto
        _uiState.value = estado.copy(respostasTexto = mapa)
    }

    fun onEscalaRespostaSelected(idPergunta: Int, nivel: Int) {
        if (estado.isEnviandoResposta) return
        val mapa = estado.respostasEscala.toMutableMap()
        mapa[idPergunta] = nivel
        _uiState.value = estado.copy(respostasEscala = mapa)
    }

    // ---------- Envio das respostas ----------

    fun solicitarConfirmacaoEnvio() {
        // trava de segurança: a tela também desabilita o botão, mas o ViewModel não confia só nela
        if (!estado.podeEnviar) {
            _uiState.value = estado.copy(
                erroDetalhes = "Responda todas as perguntas obrigatórias (*) antes de enviar."
            )
            return
        }
        _uiState.value = estado.copy(mostrarModalConfirmarEnvio = true, erroDetalhes = null)
    }

    fun fecharModalConfirmacao() {
        _uiState.value = estado.copy(mostrarModalConfirmarEnvio = false)
    }

    fun salvarEVoltar(token: String?) {
        if (estado.isEnviandoResposta) return
        if (estado.tarefaEmEdicao?.getStatusFormatado() == "Concluída" ||
            estado.isLoadingDetalhes || !respostasCarregadas) {
            fecharEdicaoTarefa()
            return
        }
        enviarResposta(token, finalizar = false)
    }

    fun confirmarEEnviarResposta(token: String?) = enviarResposta(token, finalizar = true)

    private fun enviarResposta(token: String?, finalizar: Boolean) {
        val tarefa = estado.tarefaEmEdicao ?: return
        if (estado.isEnviandoResposta) return
        if (!respostasCarregadas) return
        if (token.isNullOrBlank()) {
            _uiState.value = estado.copy(erroDetalhes = "Sessão expirada. Entre novamente.")
            return
        }
        if (finalizar && !estado.podeEnviar) {
            _uiState.value = estado.copy(
                mostrarModalConfirmarEnvio = false,
                erroDetalhes = "Responda todas as perguntas obrigatórias (*) antes de enviar."
            )
            return
        }

        val idDestinatario = tarefa.idTarefaDestinatario?.takeIf { it > 0 } ?: tarefa.id
        _uiState.value = estado.copy(isEnviandoResposta = true, mostrarModalConfirmarEnvio = false, erroDetalhes = null)

        // Uma entrada por resposta, de acordo com o tipo de cada pergunta (sem duplicar)
        val arrayRespostas = JsonArray()
        for (p in estado.perguntas) {
            when (p.tipoNormalizado()) {
                "humor", "unica_escolha" -> estado.respostasOpcaoUnica[p.id]?.let { oId ->
                    arrayRespostas.add(JsonObject().apply {
                        addProperty("id_pergunta", p.id)
                        addProperty("id_opcao_pergunta", oId)
                    })
                }
                "multipla_escolha" -> estado.respostasMultiplasOpcoes[p.id].orEmpty().forEach { oId ->
                    arrayRespostas.add(JsonObject().apply {
                        addProperty("id_pergunta", p.id)
                        addProperty("id_opcao_pergunta", oId)
                    })
                }
                "escala" -> estado.respostasEscala[p.id]?.let { nivel ->
                    arrayRespostas.add(JsonObject().apply {
                        addProperty("id_pergunta", p.id)
                        addProperty("valor", nivel.toString())
                    })
                }
                else -> estado.respostasTexto[p.id]?.trim()?.takeIf { it.isNotBlank() }?.let { txt ->
                    arrayRespostas.add(JsonObject().apply {
                        addProperty("id_pergunta", p.id)
                        addProperty("valor", txt)
                    })
                }
            }
        }

        val payload = JsonObject().apply {
            addProperty("enviar_definitivamente", finalizar)
            add("respostas", arrayRespostas)
        }

        service.enviarRespostas(idDestinatario, payload, token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) {
                    // antes o app tratava qualquer resposta como sucesso; agora mostra o erro do back
                    _uiState.value = estado.copy(
                        isEnviandoResposta = false,
                        erroDetalhes = mensagemDeErro(response, "Não foi possível enviar as respostas")
                    )
                    return
                }

                // respostas salvas: marca a tarefa como concluída (se esta chamada falhar, a lista mostra o status real)
                val progresso = if (finalizar) 100 else estado.progressoRespostas
                val atualizacao = payloadStatus(tarefa, token, if (finalizar) "concluida" else "em_andamento", progresso)
                service.atualizarStatusTarefaDestinatario(idDestinatario, atualizacao, token)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(c: Call<JsonObject>, r: Response<JsonObject>) {
                            if (r.isSuccessful) {
                                progressosSalvos[idDestinatario] = progresso
                                _uiState.value = estado.copy(tarefas = estado.tarefas.map {
                                    if ((it.idTarefaDestinatario ?: it.id) == idDestinatario)
                                        it.copy(progresso = progresso, status = if (finalizar) "concluida" else "em_andamento")
                                    else it
                                })
                                finalizarEnvioSucesso(token, finalizar)
                            }
                            else _uiState.value = estado.copy(
                                isEnviandoResposta = false,
                                erroDetalhes = mensagemDeErro(r, "Não foi possível atualizar o status da tarefa")
                            )
                        }

                        override fun onFailure(c: Call<JsonObject>, t: Throwable) {
                            _uiState.value = estado.copy(isEnviandoResposta = false,
                                erroDetalhes = "Sem conexão. Tente salvar novamente.")
                        }
                    })
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(isEnviandoResposta = false, erroDetalhes = "Sem conexão com o servidor.")
            }
        })
    }

    private fun payloadStatus(tarefa: TarefaItemDto, token: String?, status: String, progresso: Int) =
        JsonObject().apply {
            addProperty("id_tarefa", tarefa.id)
            token?.let { idDoToken(it) }?.let { addProperty("id_paciente", it) }
            addProperty("status", status)
            addProperty("progresso", progresso)
            tarefa.prazo?.take(10)?.let { addProperty("prazo", it) }
            if (status == "concluida") addProperty("data_conclusao", agoraParaBanco())
        }

    private fun finalizarEnvioSucesso(token: String?, finalizar: Boolean) {
        _uiState.value = estado.copy(
            isEnviandoResposta = false,
            tarefaEmEdicao = null,
            perguntas = emptyList(),
            respostasOpcaoUnica = emptyMap(),
            respostasMultiplasOpcoes = emptyMap(),
            respostasTexto = emptyMap(),
            respostasEscala = emptyMap(),
            erroDetalhes = null,
            toastMensagem = if (finalizar) "Resposta enviada ao profissional" else "Progresso salvo. Continue quando quiser."
        )
        carregarTarefas(token)
    }

    // ---------- Nova tarefa criada pelo paciente ----------

    fun abrirModalNovaTarefa() {
        _uiState.value = estado.copy(mostrarModalNovaTarefa = true, erroNovaTarefa = null)
    }

    fun fecharModalNovaTarefa() {
        if (estado.isCriandoNovaTarefa) return
        _uiState.value = estado.copy(mostrarModalNovaTarefa = false, erroNovaTarefa = null)
    }

    fun criarNovaTarefaPaciente(
        context: Context,
        token: String?,
        titulo: String,
        prazoIso: String?,
        anexoUri: Uri?
    ) {
        if (estado.isCriandoNovaTarefa) return

        val tk = token?.takeIf { it.isNotBlank() }
        if (tk == null) {
            _uiState.value = estado.copy(erroNovaTarefa = "Sessão expirada. Entre novamente.")
            return
        }
        if (titulo.isBlank()) {
            _uiState.value = estado.copy(erroNovaTarefa = "Informe o título da tarefa.")
            return
        }
        if (prazoIso == null) {
            _uiState.value = estado.copy(
                erroNovaTarefa = "Informe uma data de entrega válida (dd/mm/aaaa), a partir de hoje."
            )
            return
        }

        _uiState.value = estado.copy(isCriandoNovaTarefa = true, erroNovaTarefa = null)
        val appContext = context.applicationContext

        // a leitura do arquivo não pode rodar na thread principal
        Thread {
            val anexo: MultipartBody.Part? = try {
                anexoUri?.let { lerAnexo(appContext, it) }
            } catch (e: AnexoInvalido) {
                _uiState.postValue(estado.copy(isCriandoNovaTarefa = false, erroNovaTarefa = e.message))
                return@Thread
            } catch (e: Exception) {
                _uiState.postValue(
                    estado.copy(isCriandoNovaTarefa = false, erroNovaTarefa = "Não foi possível ler o arquivo selecionado.")
                )
                return@Thread
            }
            enviarNovaTarefa(tk, titulo.trim(), prazoIso, anexo)
        }.start()
    }

    private fun enviarNovaTarefa(token: String, titulo: String, prazoIso: String, anexo: MultipartBody.Part?) {
        val texto = "text/plain".toMediaTypeOrNull()

        service.criarTarefaByToken(
            token = token,
            titulo = titulo.toRequestBody(texto),
            tipo = "texto".toRequestBody(texto),
            instrucoes = null,
            isRascunho = "0".toRequestBody(texto),
            // o back faz JSON.parse nesses dois campos: se faltarem, a requisição quebra
            pacienteIds = "[]".toRequestBody(texto),
            prazosPorPaciente = "[]".toRequestBody(texto),
            anexoArquivo = anexo,
            prazo = prazoIso.toRequestBody(texto)
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    _uiState.value = estado.copy(
                        isCriandoNovaTarefa = false,
                        mostrarModalNovaTarefa = false,
                        erroNovaTarefa = null,
                        toastMensagem = "Tarefa adicionada"
                    )
                    carregarTarefas(token)
                } else {
                    _uiState.value = estado.copy(
                        isCriandoNovaTarefa = false,
                        erroNovaTarefa = mensagemDeErro(response, "Não foi possível criar a tarefa")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(isCriandoNovaTarefa = false, erroNovaTarefa = "Sem conexão com o servidor.")
            }
        })
    }

    private class AnexoInvalido(mensagem: String) : Exception(mensagem)

    private fun lerAnexo(context: Context, uri: Uri): MultipartBody.Part {
        val resolver = context.contentResolver
        val nome = nomeDoArquivo(context, uri) ?: "anexo"
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw AnexoInvalido("Não foi possível ler o arquivo selecionado.")
        if (bytes.size > TAMANHO_MAX_ANEXO_BYTES) throw AnexoInvalido("O arquivo deve ter no máximo 10 MB.")

        // o back (multer) espera o campo com o nome "anexo_arquivo"
        return MultipartBody.Part.createFormData("anexo_arquivo", nome, bytes.toRequestBody(mime.toMediaTypeOrNull()))
    }

    fun limparToast() {
        _uiState.value = estado.copy(toastMensagem = null, toastErro = false)
    }

    // ---------- Auxiliares ----------

    /** O back devolve { message, field, ... } nos erros 4xx/5xx. */
    private fun mensagemDeErro(response: Response<*>, padrao: String): String {
        val json: JsonObject? = try {
            val errStr = response.errorBody()?.string()
            if (!errStr.isNullOrBlank()) JsonParser().parse(errStr).asJsonObject else null
        } catch (e: Exception) { null }

        val msg = json?.get("message")?.let { if (it.isJsonPrimitive) it.asString else null }
        val campo = json?.get("field")?.let { if (it.isJsonPrimitive) it.asString else null }
        val texto = listOfNotNull(msg, campo).joinToString(" ").trim()
        return if (texto.isNotBlank()) texto else "$padrao (erro ${response.code()})."
    }

    private fun findArray(node: JsonElement?, vararg keys: String): JsonArray {
        if (node == null || node.isJsonNull) return JsonArray()
        if (node.isJsonArray) return node.asJsonArray
        val obj = if (node.isJsonObject) node.asJsonObject else return JsonArray()
        for (key in keys) {
            if (obj.has(key)) {
                val e = obj.get(key)
                if (e.isJsonArray) return e.asJsonArray
                if (e.isJsonObject) return findArray(e, *keys)
            }
        }
        for (key in obj.keySet()) {
            val e = obj.get(key)
            if (e.isJsonArray) return e.asJsonArray
        }
        return JsonArray()
    }

    private fun parseAttrInt(obj: JsonObject, vararg keys: String): Int {
        for (k in keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                return try { obj.get(k).asInt } catch (e: Exception) { 0 }
            }
        }
        return 0
    }

    private fun parseAttrBool(obj: JsonObject, chave: String, padrao: Boolean): Boolean {
        if (!obj.has(chave) || obj.get(chave).isJsonNull) return padrao
        val s = runCatching { obj.get(chave).asString.trim().lowercase() }.getOrNull() ?: return padrao
        return when (s) {
            "1", "true" -> true
            "0", "false" -> false
            else -> padrao
        }
    }

    // evita dois PUTs seguidos se o paciente tocar mais de uma vez no botão
    private val concluindoIds = mutableSetOf<Int>()

    /**
     * Marca a tarefa como concluída. O PUT /tarefas-destinatarios/{id} do back substitui o registro inteiro e valida
     * status + id_tarefa + id_paciente, então enviamos todos os campos (mandar só "status" dava 400 e o app
     * mostrava "concluída" mesmo assim, porque a resposta nunca era conferida).
     */
    fun marcarComoConcluida(token: String?, tarefa: TarefaItemDto) {
        val tk = token?.takeIf { it.isNotBlank() }
        if (tk == null) {
            _uiState.value = estado.copy(toastMensagem = "Sessão expirada. Entre novamente.", toastErro = true)
            return
        }
        val idDestinatario = tarefa.idTarefaDestinatario?.takeIf { it > 0 } ?: tarefa.id
        if (!concluindoIds.add(idDestinatario)) return

        val atualizacao = JsonObject().apply {
            addProperty("status", "concluida")
            addProperty("id_tarefa", tarefa.id)
            addProperty("data_conclusao", agoraParaBanco())
            tarefa.prazo?.take(10)?.takeIf { it.length == 10 }?.let { addProperty("prazo", it) }
        }

        service.atualizarStatusTarefaDestinatario(idDestinatario, atualizacao, tk).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                concluindoIds.remove(idDestinatario)
                if (response.isSuccessful) {
                    // atualiza a lista na hora e confirma com o back em seguida
                    val lista = estado.tarefas.map { item ->
                        if (item.id == tarefa.id) item.copy(status = "concluida", progresso = 100) else item
                    }
                    _uiState.value = estado.copy(tarefas = lista, toastMensagem = "Tarefa concluída", toastErro = false)
                    carregarTarefas(tk)
                } else {
                    _uiState.value = estado.copy(
                        toastMensagem = mensagemDeErro(response, "Não foi possível concluir a tarefa"),
                        toastErro = true
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                concluindoIds.remove(idDestinatario)
                _uiState.value = estado.copy(toastMensagem = "Sem conexão com o servidor.", toastErro = true)
            }
        })
    }

    /** O JWT do back leva o id (e o nível) do usuário no payload; usado para preencher id_paciente. */
    private fun idDoToken(token: String): Int? = runCatching {
        val payload = token.split(".")[1]
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), Charsets.UTF_8)
        JsonParser().parse(json).asJsonObject.get("id").asInt
    }.getOrNull()?.takeIf { it > 0 }

    private fun agoraParaBanco(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    private fun parseAttrString(obj: JsonObject, vararg keys: String): String? {
        for (k in keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                val s = try { obj.get(k).asString.trim() } catch (e: Exception) { "" }
                if (s.isNotBlank() && s.lowercase() != "null") return s
            }
        }
        return null
    }
}

/** Nome exibível do arquivo escolhido no seletor (usado pela tela e pelo upload). */
internal fun nomeDoArquivo(context: Context, uri: Uri): String? =
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull() ?: uri.lastPathSegment