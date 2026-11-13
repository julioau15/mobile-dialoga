package com.aulasandroid.mobiledialoga.feature.notificacoes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.data.remote.NotificacaoService
import com.aulasandroid.mobiledialoga.data.remote.PreferenciaNotificacaoService
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class NotificacoesUiState(
    val notificacoes: List<NotificacaoItem> = emptyList(),
    val mostrandoHistorico: Boolean = false,
    val carregando: Boolean = false,
    val erro: String? = null,
    val toast: String? = null,
    // tela de configurações
    val emConfig: Boolean = false,
    val configCarregando: Boolean = false,
    val configSalvando: Boolean = false,
    val configErro: String? = null,
    val prefs: Map<String, Boolean> = emptyMap(),
    val desligarTodas: Boolean = false
) {
    val naoLidas: List<NotificacaoItem> get() = notificacoes.filter { !it.lida }
    val lidas: List<NotificacaoItem> get() = notificacoes.filter { it.lida }
}

class NotificacoesViewModel : ViewModel() {

    private val _uiState = MutableLiveData(NotificacoesUiState())
    val uiState: LiveData<NotificacoesUiState> = _uiState

    private val estado: NotificacoesUiState get() = _uiState.value ?: NotificacoesUiState()

    private val service: NotificacaoService get() = RetrofitFactory().getNotificacaoService()
    private val prefService: PreferenciaNotificacaoService
        get() = RetrofitFactory().getPreferenciaNotificacaoService()

    // guarda os toggles de antes de "Desligar todas" para restaurar ao religar
    private var prefsAntesDeDesligar: Map<String, Boolean>? = null

    // ---------------------------------------------------------------- lista

    fun carregar(token: String?) {
        if (token.isNullOrBlank()) return
        _uiState.value = estado.copy(carregando = true, erro = null)

        service.listarNotificacoes(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                when {
                    response.isSuccessful -> _uiState.value = estado.copy(
                        carregando = false,
                        notificacoes = converter(response.body()),
                        erro = null
                    )
                    // o back responde 404 quando ainda não há nenhuma notificação
                    response.code() == 404 -> _uiState.value = estado.copy(
                        carregando = false, notificacoes = emptyList(), erro = null
                    )
                    else -> _uiState.value = estado.copy(
                        carregando = false,
                        erro = mensagemDeErro(response, "Não foi possível carregar as notificações")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(carregando = false, erro = "Sem conexão com o servidor.")
            }
        })
    }

    fun selecionarAba(historico: Boolean) {
        _uiState.value = estado.copy(mostrandoHistorico = historico)
    }

    fun marcarComoLida(token: String?, item: NotificacaoItem) {
        if (item.lida) return
        definirLida(item.id, true)
        service.marcarLida(item.id, token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (!response.isSuccessful) definirLida(item.id, false)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                definirLida(item.id, false)
            }
        })
    }

    fun marcarTodasComoLidas(token: String?) {
        if (token.isNullOrBlank() || estado.naoLidas.isEmpty()) return
        service.marcarTodasLidas(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                _uiState.value = if (response.isSuccessful) {
                    estado.copy(
                        notificacoes = estado.notificacoes.map { it.copy(lida = true) },
                        toast = "Todas marcadas como lidas."
                    )
                } else {
                    estado.copy(toast = mensagemDeErro(response, "Não foi possível marcar como lidas"))
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(toast = "Sem conexão com o servidor.")
            }
        })
    }

    private fun definirLida(id: Int, lida: Boolean) {
        _uiState.value = estado.copy(
            notificacoes = estado.notificacoes.map { if (it.id == id) it.copy(lida = lida) else it }
        )
    }

    fun limparToast() {
        _uiState.value = estado.copy(toast = null)
    }

    // -------------------------------------------------------- configurações

    fun abrirConfig(perfil: PerfilNotificacao, token: String?) {
        val padroes = preferenciasPadrao(gruposPreferencia(perfil))
        prefsAntesDeDesligar = null
        _uiState.value = estado.copy(
            emConfig = true,
            configCarregando = !token.isNullOrBlank(),
            configErro = null,
            prefs = padroes,
            desligarTodas = false
        )
        if (token.isNullOrBlank()) return

        prefService.listarPreferencias(token).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val mescladas = mesclarComServidor(padroes, extrairLista(response.body(), "preferenciaNotificacao"))
                    // se tudo está desativado no servidor, o "Desligar todas" aparece ligado
                    val tudoOff = mescladas.isNotEmpty() && mescladas.values.none { it }
                    _uiState.value = estado.copy(
                        configCarregando = false,
                        prefs = mescladas,
                        desligarTodas = tudoOff
                    )
                } else if (response.code() == 404) {
                    // ainda não salvou nada: vale o padrão
                    _uiState.value = estado.copy(configCarregando = false)
                } else {
                    _uiState.value = estado.copy(
                        configCarregando = false,
                        configErro = mensagemDeErro(response, "Não foi possível carregar as preferências")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(configCarregando = false, configErro = "Sem conexão com o servidor.")
            }
        })
    }

    fun fecharConfig() {
        _uiState.value = estado.copy(emConfig = false, configErro = null)
    }

    fun alterarPreferencia(chave: String, ativo: Boolean) {
        // mexer em um item individual tira o "Desligar todas"
        _uiState.value = estado.copy(
            prefs = estado.prefs + (chave to ativo),
            desligarTodas = false
        )
    }

    fun alternarDesligarTodas(desligar: Boolean) {
        if (desligar) {
            prefsAntesDeDesligar = estado.prefs
            _uiState.value = estado.copy(
                desligarTodas = true,
                prefs = estado.prefs.mapValues { false }
            )
        } else {
            val restaurar = prefsAntesDeDesligar?.takeIf { antes -> antes.values.any { it } }
            _uiState.value = estado.copy(
                desligarTodas = false,
                prefs = restaurar ?: estado.prefs.mapValues { true }
            )
        }
    }

    fun salvarPreferencias(perfil: PerfilNotificacao, token: String?) {
        if (token.isNullOrBlank() || estado.configSalvando) return
        val lista = JsonArray()
        for (grupo in gruposPreferencia(perfil)) {
            for (item in grupo.itens) {
                val ativo = estado.prefs[chavePreferencia(grupo.categoria, item.tipo)] ?: item.padrao
                lista.add(JsonObject().apply {
                    addProperty("categoria", grupo.categoria)
                    addProperty("tipo_especifico", item.tipo)
                    addProperty("ativo", if (ativo) 1 else 0)
                })
            }
        }
        val body = JsonObject().apply { add("preferencias", lista) }
        _uiState.value = estado.copy(configSalvando = true, configErro = null)

        prefService.salvarPreferencias(token, body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                _uiState.value = if (response.isSuccessful) {
                    estado.copy(configSalvando = false, emConfig = false, toast = "Preferências salvas!")
                } else {
                    estado.copy(
                        configSalvando = false,
                        configErro = mensagemDeErro(response, "Não foi possível salvar as preferências")
                    )
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                _uiState.value = estado.copy(configSalvando = false, configErro = "Sem conexão com o servidor.")
            }
        })
    }

    // -------------------------------------------------------------- parsing

    private fun mesclarComServidor(padroes: Map<String, Boolean>, lista: JsonArray): Map<String, Boolean> {
        val resultado = padroes.toMutableMap()
        for (elem in lista) {
            if (!elem.isJsonObject) continue
            val obj = elem.asJsonObject
            val categoria = obj.get("categoria").texto() ?: continue
            val tipo = obj.get("tipo_especifico").texto()
            val ativo = obj.get("ativo").inteiro(1) != 0
            if (tipo.isNullOrBlank()) {
                // preferência da categoria inteira (ex.: ('tarefa', NULL, 0))
                resultado.keys.filter { it.startsWith("$categoria|") }.forEach { resultado[it] = ativo }
            } else {
                val chave = chavePreferencia(categoria, tipo)
                if (resultado.containsKey(chave)) resultado[chave] = ativo
            }
        }
        return resultado
    }

    private fun converter(body: JsonObject?): List<NotificacaoItem> {
        val lista = mutableListOf<NotificacaoItem>()
        for (elem in extrairLista(body, "notificacao")) {
            if (!elem.isJsonObject) continue
            val o = elem.asJsonObject
            lista.add(
                NotificacaoItem(
                    id = o.get("id").inteiro(),
                    mensagem = o.get("titulo_mensagem").texto().orEmpty(),
                    lida = o.get("lida").inteiro() != 0,
                    dataCriacao = o.get("data_criacao").texto().orEmpty(),
                    referencia = o.get("referencia_evento").texto(),
                    idCategoria = o.get("id_categoria").inteiro()
                )
            )
        }
        // mais recentes primeiro (o back já ordena, mas garante)
        return lista.sortedByDescending { it.dataCriacao }
    }

    private fun extrairLista(body: JsonObject?, chave: String): JsonArray {
        val resp = body?.get("response")?.takeIf { it.isJsonObject }?.asJsonObject
        val el = resp?.get(chave) ?: body?.get(chave)
        return if (el != null && el.isJsonArray) el.asJsonArray else JsonArray()
    }

    private fun JsonElement?.texto(): String? =
        if (this != null && !isJsonNull && isJsonPrimitive) asString else null

    private fun JsonElement?.inteiro(padrao: Int = 0): Int = try {
        if (this != null && !isJsonNull && isJsonPrimitive) {
            val p = asJsonPrimitive
            if (p.isBoolean) (if (p.asBoolean) 1 else 0) else p.asInt
        } else padrao
    } catch (e: Exception) { padrao }

    private fun mensagemDeErro(response: Response<*>, padrao: String): String {
        val json: JsonObject? = try {
            val errStr = response.errorBody()?.string()
            if (!errStr.isNullOrBlank()) JsonParser().parse(errStr).asJsonObject else null
        } catch (e: Exception) { null }

        val msg = json?.get("message")?.let { if (it.isJsonPrimitive) it.asString else null }
        return if (!msg.isNullOrBlank()) msg else "$padrao (erro ${response.code()})."
    }
}
