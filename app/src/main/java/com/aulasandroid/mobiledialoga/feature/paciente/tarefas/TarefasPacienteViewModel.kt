package com.aulasandroid.mobiledialoga.feature.paciente.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.OpcaoPerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.PerguntaDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.RespostaItemDto
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.TarefaItemDto
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class TarefasPacienteUiState(
    val tarefas: List<TarefaItemDto> = emptyList(),
    val filtroAtivo: String = "Todas", // "Todas", "Pendentes", "Em andamento", "Concluídas"
    val isLoading: Boolean = false,
    val erroGeral: String? = null,
    val mensagemSucesso: String? = null,
    val tarefaSelecionada: TarefaItemDto? = null,
    val perguntas: List<PerguntaDto> = emptyList(),
    val respostasFormulario: Map<Int, RespostaItemDto> = emptyMap(),
    val isLoadingDetalhes: Boolean = false,
    val isEnviandoResposta: Boolean = false
)

class TarefasPacienteViewModel : ViewModel() {

    private val _uiState = MutableLiveData(TarefasPacienteUiState())
    val uiState: LiveData<TarefasPacienteUiState> = _uiState

    private val estado: TarefasPacienteUiState get() = _uiState.value ?: TarefasPacienteUiState()

    fun carregarTarefas(token: String?) {
        if (token.isNullOrBlank()) return
        _uiState.value = estado.copy(isLoading = true, erroGeral = null, mensagemSucesso = null)

        RetrofitFactory().getTarefaService().listarTarefas(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val json = response.body()
                    val array = findArray(json, "response", "tarefas", "tarefaDestinatario", "dados")
                    val lista = mutableListOf<TarefaItemDto>()
                    for (elem in array) {
                        try {
                            if (!elem.isJsonObject) continue
                            val obj = elem.asJsonObject
                            val item = TarefaItemDto(
                                id = parseAttrInt(obj, "id_tarefa", "id"),
                                titulo = parseAttrString(obj, "titulo") ?: "Tarefa",
                                tipo = parseAttrString(obj, "tipo"),
                                instrucoes = parseAttrString(obj, "instrucoes"),
                                anexoArquivo = parseAttrString(obj, "anexo_arquivo"),
                                dataEnvio = parseAttrString(obj, "data_envio"),
                                idTarefaDestinatario = parseAttrInt(obj, "id_tarefa_destinatario", "id"),
                                prazo = parseAttrString(obj, "prazo"),
                                status = parseAttrString(obj, "status") ?: "pendente",
                                dataConclusao = parseAttrString(obj, "data_conclusao")
                            )
                            lista.add(item)
                        } catch (e: Exception) { null }
                    }
                    _uiState.value = estado.copy(isLoading = false, tarefas = lista, erroGeral = null)
                } else {
                    val msg = parseErrorMsg(response.errorBody()?.string())
                    _uiState.value = estado.copy(isLoading = false, erroGeral = msg)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(isLoading = false, erroGeral = "Erro ao conectar ao servidor.")
            }
        })
    }

    fun setFiltro(filtro: String) {
        _uiState.value = estado.copy(filtroAtivo = filtro)
    }

    fun abrirTarefa(token: String?, tarefa: TarefaItemDto) {
        _uiState.value = estado.copy(
            tarefaSelecionada = tarefa,
            perguntas = emptyList(),
            respostasFormulario = emptyMap(),
            isLoadingDetalhes = true,
            erroGeral = null
        )

        val idTarefa = tarefa.id
        val idDestinatario = tarefa.idTarefaDestinatario ?: tarefa.id

        // Busca perguntas da tarefa
        RetrofitFactory().getTarefaService().listarPerguntasPorTarefa(idTarefa).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val listaPerguntas = mutableListOf<PerguntaDto>()
                if (response.isSuccessful) {
                    val json = response.body()
                    val array = findArray(json, "pergunta", "perguntas", "response", "dados")
                    for (elem in array) {
                        try {
                            if (!elem.isJsonObject) continue
                            val obj = elem.asJsonObject
                            val pId = parseAttrInt(obj, "id")
                            val pEnunciado = parseAttrString(obj, "enunciado", "pergunta", "texto") ?: "Pergunta"
                            val pTipo = parseAttrString(obj, "tipo") ?: "texto"

                            val opcoesList = mutableListOf<OpcaoPerguntaDto>()
                            val opcoesArray = findArray(obj, "opcoes", "opcao_pergunta", "opcoes_pergunta")
                            for (oElem in opcoesArray) {
                                if (oElem.isJsonObject) {
                                    val oObj = oElem.asJsonObject
                                    opcoesList.add(
                                        OpcaoPerguntaDto(
                                            id = parseAttrInt(oObj, "id"),
                                            texto = parseAttrString(oObj, "texto", "opcao", "valor") ?: ""
                                        )
                                    )
                                }
                            }
                            listaPerguntas.add(
                                PerguntaDto(
                                    id = pId,
                                    enunciado = pEnunciado,
                                    tipo = pTipo,
                                    opcoes = opcoesList
                                )
                            )
                        } catch (e: Exception) { null }
                    }
                }

                // Busca respostas anteriores (se houver)
                carregarRespostasAnteriores(token, idDestinatario, listaPerguntas)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                carregarRespostasAnteriores(token, idDestinatario, emptyList())
            }
        })
    }

    private fun carregarRespostasAnteriores(token: String?, idDestinatario: Int, perguntas: List<PerguntaDto>) {
        RetrofitFactory().getTarefaService().listarRespostasPorTarefaDestinatario(idDestinatario).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val mapaRespostas = mutableMapOf<Int, RespostaItemDto>()
                if (response.isSuccessful) {
                    val json = response.body()
                    val array = findArray(json, "resposta", "respostas", "response", "dados")
                    for (elem in array) {
                        try {
                            if (!elem.isJsonObject) continue
                            val obj = elem.asJsonObject
                            val pId = parseAttrInt(obj, "id_pergunta")
                            val oId = if (obj.has("id_opcao_pergunta") && !obj.get("id_opcao_pergunta").isJsonNull) obj.get("id_opcao_pergunta").asInt else null
                            val valStr = parseAttrString(obj, "valor")
                            mapaRespostas[pId] = RespostaItemDto(idPergunta = pId, idOpcaoPergunta = oId, valor = valStr)
                        } catch (e: Exception) { null }
                    }
                }
                _uiState.value = estado.copy(
                    isLoadingDetalhes = false,
                    perguntas = perguntas,
                    respostasFormulario = mapaRespostas
                )
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(
                    isLoadingDetalhes = false,
                    perguntas = perguntas
                )
            }
        })
    }

    fun fecharTarefaDetalhes() {
        _uiState.value = estado.copy(
            tarefaSelecionada = null,
            perguntas = emptyList(),
            respostasFormulario = emptyMap(),
            erroGeral = null
        )
    }

    fun onOpcaoSelecionada(idPergunta: Int, idOpcao: Int) {
        val mapa = estado.respostasFormulario.toMutableMap()
        mapa[idPergunta] = RespostaItemDto(idPergunta = idPergunta, idOpcaoPergunta = idOpcao)
        _uiState.value = estado.copy(respostasFormulario = mapa)
    }

    fun onTextoRespostaChanged(idPergunta: Int, texto: String) {
        val mapa = estado.respostasFormulario.toMutableMap()
        mapa[idPergunta] = RespostaItemDto(idPergunta = idPergunta, valor = texto)
        _uiState.value = estado.copy(respostasFormulario = mapa)
    }

    fun submeterRespostas(token: String?, enviarDefinitivamente: Boolean) {
        val t = estado.tarefaSelecionada ?: return
        val idDestinatario = t.idTarefaDestinatario ?: t.id
        _uiState.value = estado.copy(isEnviandoResposta = true, erroGeral = null)

        val arrayRespostas = JsonArray()
        for ((_, item) in estado.respostasFormulario) {
            val rObj = JsonObject().apply {
                addProperty("id_pergunta", item.idPergunta)
                if (item.idOpcaoPergunta != null) addProperty("id_opcao_pergunta", item.idOpcaoPergunta)
                if (!item.valor.isNullOrBlank()) addProperty("valor", item.valor)
            }
            arrayRespostas.add(rObj)
        }

        val payload = JsonObject().apply {
            addProperty("enviar_definitivamente", enviarDefinitivamente)
            add("respostas", arrayRespostas)
        }

        RetrofitFactory().getTarefaService().enviarRespostas(idDestinatario, payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val isSuccess = response.isSuccessful
                if (isSuccess) {
                    if (enviarDefinitivamente) {
                        // Atualiza o status do destinatário para "concluida"
                        val updatePayload = JsonObject().apply { addProperty("status", "concluida") }
                        RetrofitFactory().getTarefaService().atualizarStatusTarefaDestinatario(idDestinatario, updatePayload).enqueue(object : Callback<JsonObject> {
                            override fun onResponse(c: Call<JsonObject>, r: Response<JsonObject>) {
                                finalizarEnvioSucesso(token, "Resposta enviada com sucesso! 💚")
                            }

                            override fun onFailure(c: Call<JsonObject>, t: Throwable) {
                                finalizarEnvioSucesso(token, "Resposta enviada com sucesso! 💚")
                            }
                        })
                    } else {
                        finalizarEnvioSucesso(token, "Rascunho salvo com sucesso!")
                    }
                } else {
                    val msg = parseErrorMsg(response.errorBody()?.string()) ?: "Erro ao enviar resposta da tarefa."
                    _uiState.value = estado.copy(isEnviandoResposta = false, erroGeral = msg)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(isEnviandoResposta = false, erroGeral = "Erro ao conectar ao servidor.")
            }
        })
    }

    private fun finalizarEnvioSucesso(token: String?, msg: String) {
        _uiState.value = estado.copy(
            isEnviandoResposta = false,
            tarefaSelecionada = null,
            perguntas = emptyList(),
            respostasFormulario = emptyMap(),
            mensagemSucesso = msg
        )
        carregarTarefas(token)
    }

    fun limparMensagemSucesso() {
        _uiState.value = estado.copy(mensagemSucesso = null)
    }

    // ---------- Auxiliares de parsing JSON ----------

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

    private fun parseAttrString(obj: JsonObject, vararg keys: String): String? {
        for (k in keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                val s = obj.get(k).asString.trim()
                if (s.isNotBlank() && s.lowercase() != "null") return s
            }
        }
        return null
    }

    private fun parseErrorMsg(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JsonParser().parse(errorBody).asJsonObject
            when {
                json.has("message") -> json.get("message").asString
                json.has("erro") -> json.get("erro").asString
                json.has("error") -> json.get("error").asString
                else -> null
            }
        } catch (e: Exception) { null }
    }
}
