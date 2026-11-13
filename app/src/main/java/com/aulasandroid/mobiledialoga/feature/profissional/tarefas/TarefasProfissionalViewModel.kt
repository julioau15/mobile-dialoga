package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.nomeDoArquivo
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.PacienteOpcaoDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.ProfissionalTarefaDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.RespostaVisualizacaoDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.StatusDestinatario
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.TarefaDestinatarioProgressoDto
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

private const val TAMANHO_MAX_ANEXO_BYTES = 10 * 1024 * 1024 // 10 MB

enum class TelaProfissional { LISTA, ACOMPANHAMENTO, FORMULARIO, PREVIEW, RESPOSTAS }

/** Tipos de pergunta: rótulo da tela -> valor que a API espera em POST /tarefas/{id}/perguntas */
enum class TipoPerguntaForm(val rotulo: String, val api: String) {
    TEXTO("📝 Resposta em parágrafo", "texto"),
    UNICA("⭕ Escolha única", "unica_escolha"),
    MULTIPLA("☑️ Múltipla escolha", "multipla_escolha"),
    ESCALA("📊 Escala de 0 a 10", "escala");

    val temOpcoes: Boolean get() = this == UNICA || this == MULTIPLA

    companion object {
        fun daApi(valor: String?): TipoPerguntaForm = when (valor?.lowercase()?.trim()) {
            "unica_escolha", "escolha_unica", "unica" -> UNICA
            "multipla_escolha", "multipla" -> MULTIPLA
            "escala" -> ESCALA
            else -> TEXTO
        }
    }
}

// ---------- Estado do formulário "Nova tarefa" (Compose state: não perde o cursor ao digitar) ----------

class PerguntaForm(val chave: Long, tipoInicial: TipoPerguntaForm = TipoPerguntaForm.TEXTO) {
    var tipo by mutableStateOf(tipoInicial)
    var enunciado by mutableStateOf("")
    var obrigatoria by mutableStateOf(false)
    val opcoes = mutableStateListOf("", "")
}

class FormTarefa {
    var titulo by mutableStateOf("")
    var instrucoes by mutableStateOf("")
    var anexoUri by mutableStateOf<Uri?>(null)
    var anexoNome by mutableStateOf<String?>(null)
    var anexoUrlOriginal by mutableStateOf<String?>(null)
    var somenteLeitura by mutableStateOf(false)
    var idRascunhoOriginal by mutableStateOf<Int?>(null)
    val pacientes = mutableStateListOf<Int>()
    val perguntas = mutableStateListOf<PerguntaForm>()
    private var proximaChave = 1L

    fun novaPergunta(tipo: TipoPerguntaForm = TipoPerguntaForm.TEXTO) = PerguntaForm(proximaChave++, tipo)

    fun limpar() {
        titulo = ""; instrucoes = ""; anexoUri = null; anexoNome = null; anexoUrlOriginal = null
        somenteLeitura = false; idRascunhoOriginal = null
        pacientes.clear(); perguntas.clear()
        perguntas.add(novaPergunta())
    }

    fun duplicar(origem: PerguntaForm) {
        val copia = novaPergunta(origem.tipo).also {
            it.enunciado = origem.enunciado
            it.obrigatoria = origem.obrigatoria
            it.opcoes.clear(); it.opcoes.addAll(origem.opcoes)
        }
        val idx = perguntas.indexOf(origem)
        perguntas.add(if (idx >= 0) idx + 1 else perguntas.size, copia)
    }
}

data class TarefasProfissionalUiState(
    val tela: TelaProfissional = TelaProfissional.LISTA,
    val abaLista: Int = 0, // 0 = Enviadas, 1 = Rascunhos
    val tarefas: List<ProfissionalTarefaDto> = emptyList(),
    val acompanhamentos: List<TarefaDestinatarioProgressoDto> = emptyList(),
    val pacientes: List<PacienteOpcaoDto> = emptyList(),
    val isLoading: Boolean = false,
    val erroLista: String? = null,
    val toast: String? = null,
    // formulário
    val isCarregandoForm: Boolean = false,
    val isSalvandoRascunho: Boolean = false,
    val isEnviando: Boolean = false,
    val erroFormulario: String? = null,
    // filtros do acompanhamento
    val busca: String = "",
    val periodo: String = "Todos", // Todos | Dia | Mês | Ano
    val diaSelecionado: String? = null, // ISO
    val mesDigitos: String = "", // mmaaaa
    val anoSelecionado: String? = null,
    val statusFiltro: String = "Todos", // Todos | Em andamento | Finalizado | Não preenchido
    // respostas de um paciente
    val destinatarioSelecionado: TarefaDestinatarioProgressoDto? = null,
    val respostas: List<RespostaVisualizacaoDto> = emptyList(),
    val isCarregandoRespostas: Boolean = false,
    val erroRespostas: String? = null
) {
    val enviadas get() = tarefas.filter { !it.isRascunho }
    val rascunhos get() = tarefas.filter { it.isRascunho }

    fun acompanhamentosFiltrados(): List<TarefaDestinatarioProgressoDto> {
        val q = busca.trim().lowercase()
        // rascunhos têm destinatários gravados (usados para reabrir o rascunho), mas não são acompanhamento
        val idsRascunho = rascunhos.map { it.id }.toSet()
        return acompanhamentos.filter { d ->
            if (d.idTarefa in idsRascunho) return@filter false
            val passaBusca = q.isEmpty() ||
                    d.tituloTarefa.lowercase().contains(q) || d.nomePaciente.lowercase().contains(q)

            val passaStatus = when (statusFiltro) {
                "Em andamento" -> d.status() == StatusDestinatario.EM_ANDAMENTO
                "Finalizado" -> d.status() == StatusDestinatario.CONCLUIDA
                "Não preenchido" -> d.status() == StatusDestinatario.PENDENTE || d.status() == StatusDestinatario.ATRASADA
                else -> true
            }

            val data = d.dataModificacaoIso()
            val passaPeriodo = when (periodo) {
                "Dia" -> diaSelecionado == null || data == diaSelecionado
                "Mês" -> mesDigitos.length < 6 ||
                        (data != null && data.substring(5, 7) == mesDigitos.take(2) && data.substring(0, 4) == mesDigitos.drop(2))
                "Ano" -> anoSelecionado == null || data?.take(4) == anoSelecionado
                else -> true
            }
            passaBusca && passaStatus && passaPeriodo
        }.sortedByDescending { it.dataModificacaoIso() ?: "" }
    }
}

class TarefasProfissionalViewModel : ViewModel() {

    private val _uiState = MutableLiveData(TarefasProfissionalUiState())
    val uiState: LiveData<TarefasProfissionalUiState> = _uiState
    val form = FormTarefa().also { it.limpar() }

    private val estado: TarefasProfissionalUiState get() = _uiState.value ?: TarefasProfissionalUiState()
    private val main = Handler(Looper.getMainLooper())
    private val tarefaService by lazy { RetrofitFactory().getTarefaService() }
    private val profissionalService by lazy { RetrofitFactory().getProfissionalService() }

    /** Aplica a mudança de estado na thread principal (as chamadas de rede rodam em background). */
    private fun atualizar(bloco: TarefasProfissionalUiState.() -> TarefasProfissionalUiState) {
        if (Looper.myLooper() == Looper.getMainLooper()) _uiState.value = estado.bloco()
        else main.post { _uiState.value = estado.bloco() }
    }

    // =====================================================================
    // Navegação
    // =====================================================================

    fun voltar() {
        when (estado.tela) {
            TelaProfissional.RESPOSTAS -> atualizar { copy(tela = TelaProfissional.ACOMPANHAMENTO, destinatarioSelecionado = null, respostas = emptyList()) }
            TelaProfissional.ACOMPANHAMENTO -> atualizar { copy(tela = TelaProfissional.LISTA) }
            TelaProfissional.PREVIEW ->
                if (form.somenteLeitura) irParaLista() else atualizar { copy(tela = TelaProfissional.FORMULARIO) }
            TelaProfissional.FORMULARIO -> irParaLista()
            TelaProfissional.LISTA -> Unit
        }
    }

    private fun irParaLista() {
        form.limpar()
        atualizar { copy(tela = TelaProfissional.LISTA, erroFormulario = null) }
    }

    fun setAbaLista(aba: Int) = atualizar { copy(abaLista = aba) }
    fun abrirAcompanhamento() = atualizar { copy(tela = TelaProfissional.ACOMPANHAMENTO) }
    fun abrirPreview() = atualizar { copy(tela = TelaProfissional.PREVIEW) }
    fun editarDoPreview() = atualizar { copy(tela = TelaProfissional.FORMULARIO) }
    fun limparToast() = atualizar { copy(toast = null) }

    fun novaTarefa() {
        form.limpar()
        atualizar { copy(tela = TelaProfissional.FORMULARIO, erroFormulario = null) }
    }

    /** Ícone de "acompanhar" no card: abre o acompanhamento já filtrado por aquela tarefa. */
    fun acompanharTarefa(titulo: String) = atualizar {
        copy(tela = TelaProfissional.ACOMPANHAMENTO, busca = titulo, periodo = "Todos", statusFiltro = "Todos",
            diaSelecionado = null, mesDigitos = "", anoSelecionado = null)
    }

    // ---------- Filtros do acompanhamento ----------
    fun setBusca(v: String) = atualizar { copy(busca = v) }
    fun setStatusFiltro(v: String) = atualizar { copy(statusFiltro = v) }
    fun setPeriodo(v: String) = atualizar { copy(periodo = v, diaSelecionado = null, mesDigitos = "", anoSelecionado = null) }
    fun setDia(iso: String?) = atualizar { copy(diaSelecionado = iso) }
    fun setMes(digitos: String) = atualizar { copy(mesDigitos = digitos.filter { it.isDigit() }.take(6)) }
    fun setAno(ano: String?) = atualizar { copy(anoSelecionado = ano) }

    // =====================================================================
    // GET /tarefas · GET /profissionais/me/pacientes · GET /tarefas-destinatarios
    // =====================================================================

    fun carregarDados(token: String?) {
        if (token.isNullOrBlank()) return
        atualizar { copy(isLoading = true, erroLista = null) }

        Thread {
            try {
                val respTarefas = tarefaService.listarTarefas(token).execute()
                if (!respTarefas.isSuccessful) {
                    atualizar { copy(isLoading = false, erroLista = mensagemDeErro(respTarefas, "Não foi possível carregar as tarefas")) }
                    return@Thread
                }
                val tarefas = converterTarefas(respTarefas.body())
                val pacientes = carregarPacientes(token)
                val destinatarios = carregarDestinatarios(token, tarefas, pacientes)

                val completas = tarefas.map { t ->
                    val total = if (t.totalPerguntas > 0) t.totalPerguntas else contarPerguntas(token, t.id)
                    t.copy(totalPerguntas = total, destinatarios = destinatarios.filter { it.idTarefa == t.id })
                }
                atualizar { copy(isLoading = false, tarefas = completas, pacientes = pacientes, acompanhamentos = destinatarios) }
            } catch (e: Exception) {
                atualizar { copy(isLoading = false, erroLista = "Erro ao conectar ao servidor.") }
            }
        }.start()
    }

    private fun converterTarefas(json: JsonObject?): List<ProfissionalTarefaDto> {
        val lista = mutableListOf<ProfissionalTarefaDto>()
        for (elem in findArray(json, "tarefa", "tarefas", "response", "dados")) {
            try {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject
                if (parseInt(obj, "id_paciente_criador") > 0) continue // tarefas criadas por pacientes não entram aqui

                val total = when {
                    parseInt(obj, "total_perguntas", "qtd_perguntas", "quantidade_perguntas", "perguntas_count") > 0 ->
                        parseInt(obj, "total_perguntas", "qtd_perguntas", "quantidade_perguntas", "perguntas_count")
                    obj.has("perguntas") && obj.get("perguntas").isJsonArray -> obj.getAsJsonArray("perguntas").size()
                    else -> 0
                }
                lista.add(
                    ProfissionalTarefaDto(
                        id = parseInt(obj, "id", "id_tarefa"),
                        titulo = parseString(obj, "titulo") ?: "Tarefa sem título",
                        tipo = parseString(obj, "tipo") ?: "formulario",
                        instrucoes = parseString(obj, "instrucoes"),
                        isRascunho = parseBool(obj, "is_rascunho", false),
                        anexoArquivo = parseString(obj, "anexo_arquivo"),
                        dataEnvio = parseString(obj, "data_envio"),
                        prazo = parseString(obj, "prazo", "data_prazo", "data_vencimento"),
                        totalPerguntas = total
                    )
                )
            } catch (_: Exception) { }
        }
        return lista.filter { it.id > 0 }
    }

    private fun carregarPacientes(token: String): List<PacienteOpcaoDto> {
        val lista = mutableListOf<PacienteOpcaoDto>()
        try {
            val resp = profissionalService.listarPacientesVinculados(token).execute()
            if (resp.isSuccessful) {
                for (elem in findArray(resp.body(), "pacientes", "response", "dados")) {
                    if (!elem.isJsonObject) continue
                    val obj = elem.asJsonObject
                    val id = parseInt(obj, "id_paciente", "id")
                    val nome = parseString(obj, "nome_completo", "nome") ?: "Paciente $id"
                    val statusVinculo = parseInt(obj, "status_vinculo", "status")
                    val statusAtividade = (parseString(obj, "status_atividade") ?: "").lowercase()

                    val ativo = statusAtividade.isEmpty() || statusAtividade.startsWith("ativ")
                    val vinculoAceito = statusVinculo == 1

                    if (id > 0 && vinculoAceito && ativo) {
                        lista.add(PacienteOpcaoDto(id, nome))
                    }
                }
            }
        } catch (_: Exception) { }
        return lista
    }

    private fun carregarDestinatarios(
        token: String,
        tarefas: List<ProfissionalTarefaDto>,
        pacientes: List<PacienteOpcaoDto>
    ): List<TarefaDestinatarioProgressoDto> {
        val lista = mutableListOf<TarefaDestinatarioProgressoDto>()
        try {
            val resp = tarefaService.filtrarTarefasDestinatarios(token = token).execute()
            if (resp.isSuccessful && resp.body() != null) {
                val idsDasMinhasTarefas = tarefas.map { it.id }.toSet()

                for (elem in findArray(resp.body(), "tarefaDestinatario", "tarefas", "response", "dados")) {
                    if (!elem.isJsonObject) continue
                    val obj = elem.asJsonObject
                    val idTarefa = parseInt(obj, "id_tarefa")
                    // o back pode devolver destinatários de outras tarefas: só vale o que é do profissional logado
                    if (idTarefa > 0 && idTarefa !in idsDasMinhasTarefas) continue

                    val tarefa = tarefas.find { it.id == idTarefa }
                    val idPaciente = parseInt(obj, "id_paciente")
                    lista.add(
                        TarefaDestinatarioProgressoDto(
                            idTarefaDestinatario = parseInt(obj, "id_tarefa_destinatario", "id"),
                            idTarefa = idTarefa,
                            tituloTarefa = parseString(obj, "titulo", "nome_tarefa") ?: tarefa?.titulo ?: "Tarefa",
                            idPaciente = idPaciente,
                            nomePaciente = parseString(obj, "nome_paciente", "paciente_nome", "nome_completo")
                                ?: pacientes.find { it.id == idPaciente }?.nome ?: "Paciente $idPaciente",
                            prazo = parseString(obj, "prazo") ?: tarefa?.prazo,
                            statusBruto = parseString(obj, "status"),
                            dataConclusao = parseString(obj, "data_conclusao"),
                            ultimaModificacao = parseString(
                                obj, "ultima_modificacao", "data_atualizacao", "updated_at", "data_resposta",
                                "data_conclusao", "data_envio", "data_criacao", "created_at"
                            ) ?: tarefa?.dataEnvio
                        )
                    )
                }
            }
        } catch (_: Exception) { }

        // Garantir que todas as tarefas enviadas apareçam no acompanhamento para os pacientes (fallback "Não preenchido")
        val tarefasEnviadas = tarefas.filter { !it.isRascunho }
        for (t in tarefasEnviadas) {
            val destinatariosDaTarefa = lista.filter { it.idTarefa == t.id }
            if (destinatariosDaTarefa.isEmpty() && pacientes.isNotEmpty()) {
                for (pac in pacientes) {
                    lista.add(
                        TarefaDestinatarioProgressoDto(
                            idTarefaDestinatario = t.id * 1000 + pac.id,
                            idTarefa = t.id,
                            tituloTarefa = t.titulo,
                            idPaciente = pac.id,
                            nomePaciente = pac.nome,
                            prazo = t.prazo,
                            statusBruto = "pendente", // Não preenchido
                            dataConclusao = null,
                            ultimaModificacao = t.dataEnvio
                        )
                    )
                }
            } else {
                for (d in destinatariosDaTarefa) {
                    if (d.prazo.isNullOrBlank() && !t.prazo.isNullOrBlank()) {
                        val idx = lista.indexOf(d)
                        if (idx >= 0) {
                            lista[idx] = d.copy(prazo = t.prazo)
                        }
                    }
                }
            }
        }

        return lista
    }

    private fun contarPerguntas(token: String, idTarefa: Int): Int = try {
        val resp = tarefaService.listarPerguntasPorTarefa(idTarefa, token).execute()
        if (resp.isSuccessful) findArray(resp.body(), "pergunta", "perguntas", "response", "dados").size() else 0
    } catch (_: Exception) { 0 }

    // =====================================================================
    // Abrir tarefa existente (lápis do card)
    // =====================================================================

    /** Rascunho -> abre o formulário preenchido. Enviada -> abre a pré-visualização (somente leitura). */
    fun abrirTarefa(token: String?, tarefa: ProfissionalTarefaDto) {
        if (token.isNullOrBlank()) return
        form.limpar()
        form.titulo = tarefa.titulo
        form.instrucoes = tarefa.instrucoes.orEmpty()
        form.anexoNome = tarefa.anexoArquivo?.substringAfterLast('/')
        form.anexoUrlOriginal = tarefa.anexoArquivo
        form.somenteLeitura = !tarefa.isRascunho
        form.idRascunhoOriginal = if (tarefa.isRascunho) tarefa.id else null
        val idsPacientesDestinatarios = estado.acompanhamentos
            .filter { it.idTarefa == tarefa.id }
            .map { it.idPaciente }
            .filter { it > 0 }
            .distinct()
        form.pacientes.addAll(if (idsPacientesDestinatarios.isNotEmpty()) idsPacientesDestinatarios else tarefa.destinatarios.map { it.idPaciente }.filter { it > 0 }.distinct())

        atualizar {
            copy(
                tela = if (tarefa.isRascunho) TelaProfissional.FORMULARIO else TelaProfissional.PREVIEW,
                isCarregandoForm = true, erroFormulario = null
            )
        }

        Thread {
            val perguntas = try {
                val resp = tarefaService.listarPerguntasPorTarefa(tarefa.id, token).execute()
                if (resp.isSuccessful) converterPerguntas(resp.body()) else emptyList()
            } catch (_: Exception) { emptyList() }

            main.post {
                form.perguntas.clear()
                if (perguntas.isEmpty() && tarefa.isRascunho) form.perguntas.add(form.novaPergunta())
                perguntas.forEach { p ->
                    form.perguntas.add(form.novaPergunta(TipoPerguntaForm.daApi(p.tipo)).also { pf ->
                        pf.enunciado = p.enunciado
                        pf.obrigatoria = p.obrigatoria
                        if (p.opcoes.isNotEmpty()) { pf.opcoes.clear(); pf.opcoes.addAll(p.opcoes.map { it.texto }) }
                    })
                }
                atualizar { copy(isCarregandoForm = false) }
            }
        }.start()
    }

    private class PerguntaApi(val id: Int, val enunciado: String, val tipo: String?, val obrigatoria: Boolean, val opcoes: List<OpcaoApi>)
    private class OpcaoApi(val id: Int, val texto: String)

    private fun converterPerguntas(json: JsonObject?): List<PerguntaApi> {
        val lista = mutableListOf<Pair<Int, PerguntaApi>>()
        for (elem in findArray(json, "pergunta", "perguntas", "response", "dados")) {
            try {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject
                val opcoes = mutableListOf<Pair<Int, OpcaoApi>>()
                for (o in findArray(obj, "opcoes", "opcao_pergunta", "opcoes_pergunta")) {
                    if (!o.isJsonObject) continue
                    val oo = o.asJsonObject
                    opcoes.add(parseInt(oo, "ordem") to OpcaoApi(parseInt(oo, "id"), parseString(oo, "texto", "opcao", "valor") ?: ""))
                }
                lista.add(
                    parseInt(obj, "ordem") to PerguntaApi(
                        id = parseInt(obj, "id"),
                        enunciado = parseString(obj, "enunciado", "texto", "pergunta") ?: "Pergunta",
                        tipo = parseString(obj, "tipo"),
                        obrigatoria = parseBool(obj, "obrigatoria", true),
                        opcoes = opcoes.sortedBy { it.first }.map { it.second }
                    )
                )
            } catch (_: Exception) { }
        }
        return lista.sortedBy { it.first }.map { it.second }
    }

    // =====================================================================
    // POST /tarefas · POST /tarefas/{id}/perguntas · PUT /tarefas/{id}/enviar
    // =====================================================================

    /** Valida o formulário. Devolve a mensagem de erro (e já a mostra na tela) ou null se está ok. */
    fun validarFormulario(paraEnvio: Boolean): String? {
        val erro = when {
            form.titulo.isBlank() -> "Informe o nome da tarefa."
            paraEnvio && form.perguntas.none { it.enunciado.isNotBlank() } -> "Adicione ao menos uma pergunta com enunciado."
            paraEnvio && form.perguntas.any { it.enunciado.isBlank() } -> "Preencha o enunciado de todas as perguntas ou remova as vazias."
            paraEnvio && form.perguntas.any { it.tipo.temOpcoes && it.opcoes.count { o -> o.isNotBlank() } < 2 } ->
                "Perguntas de escolha precisam de pelo menos 2 opções preenchidas."
            paraEnvio && form.pacientes.isEmpty() && estado.pacientes.isEmpty() -> "Você ainda não tem pacientes vinculados."
            else -> null
        }
        atualizar { copy(erroFormulario = erro) }
        return erro
    }

    /** Pacientes que vão receber a tarefa: os escolhidos ou, se nenhum, todos os vinculados. */
    fun destinatariosDoEnvio(): List<PacienteOpcaoDto> {
        val todos = estado.pacientes
        return if (form.pacientes.isEmpty()) todos else todos.filter { it.id in form.pacientes }
    }

    fun salvar(context: Context, token: String?, rascunho: Boolean, prazos: Map<Int, String>) {
        if (token.isNullOrBlank() || estado.isSalvandoRascunho || estado.isEnviando) return
        if (validarFormulario(paraEnvio = !rascunho) != null) return

        val destinatarios = if (rascunho) estado.pacientes.filter { it.id in form.pacientes } else destinatariosDoEnvio()
        val idsPacientes = destinatarios.map { it.id }
        val titulo = form.titulo.trim()
        val instrucoes = form.instrucoes.trim()
        val anexoUri = form.anexoUri
        val originalId = form.idRascunhoOriginal
        val perguntas = form.perguntas
            .filter { it.enunciado.isNotBlank() }
            .map { p ->
                Triple(
                    p.tipo.api to p.enunciado.trim(),
                    p.obrigatoria,
                    if (p.tipo.temOpcoes) p.opcoes.filter { it.isNotBlank() }.map { it.trim() } else emptyList()
                )
            }

        atualizar { copy(isSalvandoRascunho = rascunho, isEnviando = !rascunho, erroFormulario = null) }
        val appContext = context.applicationContext

        Thread {
            try {
                val anexo = try {
                    anexoUri?.let { lerAnexo(appContext, it) }
                } catch (e: AnexoInvalido) {
                    atualizar { copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario = e.message) }
                    return@Thread
                }

                val texto = "text/plain".toMediaTypeOrNull()
                val prazosJson = if (prazos.isEmpty()) "[]" else JsonArray().also { arr ->
                    prazos.forEach { (idPaciente, iso) ->
                        arr.add(JsonObject().apply {
                            addProperty("paciente_id", idPaciente)
                            addProperty("prazo", iso)
                        })
                    }
                }.toString()

                val anexoUrlOriginal = form.anexoUrlOriginal
                val anexoUrlBody = if (anexo == null && !anexoUrlOriginal.isNullOrBlank()) anexoUrlOriginal.toRequestBody(texto) else null

                val respCriar = tarefaService.criarTarefaByToken(
                    token = token,
                    titulo = titulo.toRequestBody(texto),
                    tipo = "formulario".toRequestBody(texto),
                    instrucoes = if (instrucoes.isNotBlank()) instrucoes.toRequestBody(texto) else null,
                    isRascunho = (if (rascunho) "1" else "0").toRequestBody(texto),
                    pacienteIds = idsPacientes.toString().toRequestBody(texto),
                    prazosPorPaciente = prazosJson.toRequestBody(texto),
                    anexoArquivo = anexo,
                    anexoUrl = anexoUrlBody
                ).execute()

                if (!respCriar.isSuccessful) {
                    atualizar { copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario = mensagemDeErro(respCriar, "Erro ao salvar a tarefa")) }
                    return@Thread
                }
                val idTarefa = extrairId(respCriar.body())
                if (idTarefa <= 0) {
                    atualizar { copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario = "A tarefa foi criada, mas a API não devolveu o id. Atualize a lista.") }
                    carregarDados(token)
                    return@Thread
                }

                // 2) perguntas, na ordem da tela
                for ((i, p) in perguntas.withIndex()) {
                    val (tipoEEnunciado, obrigatoria, opcoes) = p
                    val body = JsonObject().apply {
                        addProperty("texto", tipoEEnunciado.second)
                        addProperty("enunciado", tipoEEnunciado.second)
                        addProperty("tipo", tipoEEnunciado.first)
                        addProperty("obrigatoria", obrigatoria)
                        addProperty("ordem", i + 1)
                        if (opcoes.isNotEmpty()) {
                            val arr = JsonArray()
                            for ((optIdx, optTxt) in opcoes.withIndex()) {
                                arr.add(JsonObject().apply {
                                    addProperty("texto", optTxt)
                                    addProperty("ordem", optIdx + 1)
                                })
                            }
                            add("opcoes", arr)
                        }
                    }
                    val respPergunta = tarefaService.adicionarPergunta(idTarefa, body, token).execute()
                    if (!respPergunta.isSuccessful) {
                        atualizar {
                            copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario =
                                "Tarefa salva como rascunho, mas a pergunta ${i + 1} falhou: " +
                                        mensagemDeErro(respPergunta, "erro ao adicionar"))
                        }
                        carregarDados(token)
                        return@Thread
                    }
                }

                // 3) envio aos pacientes
                if (!rascunho) {
                    val respEnviar = tarefaService.enviarTarefaParaPaciente(idTarefa, token).execute()
                    if (!respEnviar.isSuccessful) {
                        atualizar {
                            copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario =
                                "Tarefa salva como rascunho, mas o envio falhou: " + mensagemDeErro(respEnviar, "erro ao enviar"))
                        }
                        carregarDados(token)
                        return@Thread
                    }
                }

                // 4) se estava editando um rascunho, o antigo sai (a edição recria a tarefa)
                if (originalId != null) {
                    try { tarefaService.excluirTarefa(originalId, token).execute() } catch (_: Exception) { }
                }

                main.post { form.limpar() }
                atualizar {
                    copy(
                        isSalvandoRascunho = false, isEnviando = false, tela = TelaProfissional.LISTA,
                        abaLista = if (rascunho) 1 else 0,
                        toast = if (rascunho) "Rascunho salvo" else "Questionário enviado ao(s) paciente(s)!"
                    )
                }
                carregarDados(token)
            } catch (e: Exception) {
                atualizar { copy(isSalvandoRascunho = false, isEnviando = false, erroFormulario = "Erro ao conectar ao servidor.") }
            }
        }.start()
    }

    fun selecionarAnexo(context: Context, uri: Uri?) {
        form.anexoUri = uri
        form.anexoNome = uri?.let { nomeDoArquivo(context, it) }
    }

    fun excluirTarefa(token: String?, idTarefa: Int) {
        if (token.isNullOrBlank()) return
        Thread {
            try {
                val resp = tarefaService.excluirTarefa(idTarefa, token).execute()
                if (resp.isSuccessful) {
                    atualizar { copy(toast = "Tarefa excluída") }
                    carregarDados(token)
                } else {
                    atualizar { copy(erroLista = mensagemDeErro(resp, "Erro ao excluir a tarefa")) }
                }
            } catch (_: Exception) {
                atualizar { copy(erroLista = "Erro de conexão com o servidor.") }
            }
        }.start()
    }

    // =====================================================================
    // GET /tarefas-destinatarios/{id}/respostas (+ perguntas da tarefa)
    // =====================================================================

    fun abrirRespostas(token: String?, destinatario: TarefaDestinatarioProgressoDto) {
        if (token.isNullOrBlank()) return
        atualizar {
            copy(tela = TelaProfissional.RESPOSTAS, destinatarioSelecionado = destinatario,
                respostas = emptyList(), isCarregandoRespostas = true, erroRespostas = null)
        }

        Thread {
            try {
                val perguntas = try {
                    val r = tarefaService.listarPerguntasPorTarefa(destinatario.idTarefa, token).execute()
                    if (r.isSuccessful) converterPerguntas(r.body()) else emptyList()
                } catch (_: Exception) { emptyList() }

                val resp = tarefaService.listarRespostasPorTarefaDestinatario(destinatario.idTarefaDestinatario, token).execute()
                if (!resp.isSuccessful) {
                    atualizar { copy(isCarregandoRespostas = false, erroRespostas = mensagemDeErro(resp, "Não foi possível carregar as respostas")) }
                    return@Thread
                }
                val objetos = findArray(resp.body(), "resposta", "respostas", "response", "dados")
                    .filter { it.isJsonObject }.map { it.asJsonObject }

                val resultado = mutableListOf<RespostaVisualizacaoDto>()
                if (perguntas.isNotEmpty()) {
                    // casa cada pergunta com as respostas dela (id_pergunta); opção vira o texto da opção
                    perguntas.forEach { p ->
                        val dela = objetos.filter { parseInt(it, "id_pergunta") == p.id }
                        val valores = dela.mapNotNull { o ->
                            val idOpcao = parseInt(o, "id_opcao_pergunta")
                            if (idOpcao > 0) p.opcoes.find { it.id == idOpcao }?.texto ?: parseString(o, "opcao_texto")
                            else parseString(o, "valor", "texto_resposta")
                        }
                        resultado.add(
                            RespostaVisualizacaoDto(p.id, p.enunciado, if (valores.isEmpty()) "Sem resposta" else valores.joinToString(", "))
                        )
                    }
                } else {
                    // sem perguntas: usa o que a própria resposta trouxer
                    objetos.forEach { o ->
                        resultado.add(
                            RespostaVisualizacaoDto(
                                parseInt(o, "id"),
                                parseString(o, "enunciado", "pergunta") ?: "Pergunta",
                                parseString(o, "valor", "texto_resposta", "opcao_texto") ?: "Sem resposta"
                            )
                        )
                    }
                }
                atualizar { copy(isCarregandoRespostas = false, respostas = resultado) }
            } catch (_: Exception) {
                atualizar { copy(isCarregandoRespostas = false, erroRespostas = "Erro ao conectar ao servidor.") }
            }
        }.start()
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

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

    private fun extrairId(json: JsonObject?): Int {
        if (json == null) return 0
        val direto = parseInt(json, "id", "id_tarefa", "insertId")
        if (direto > 0) return direto
        for (chave in listOf("tarefa", "response", "dados", "data")) {
            val e = json.get(chave)
            if (e != null && e.isJsonObject) {
                val id = extrairId(e.asJsonObject)
                if (id > 0) return id
            }
        }
        return 0
    }

    /** O back devolve { message, field, ... } nos erros. */
    private fun mensagemDeErro(response: Response<*>, padrao: String): String {
        val json: JsonObject? = try {
            val s = response.errorBody()?.string()
            if (!s.isNullOrBlank()) JsonParser().parse(s).asJsonObject else null
        } catch (_: Exception) { null }
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

    private fun parseInt(obj: JsonObject, vararg keys: String): Int {
        for (k in keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                return try { obj.get(k).asInt } catch (_: Exception) { 0 }
            }
        }
        return 0
    }

    private fun parseString(obj: JsonObject, vararg keys: String): String? {
        for (k in keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull) {
                val s = try { obj.get(k).asString.trim() } catch (_: Exception) { "" }
                if (s.isNotBlank() && s.lowercase() != "null") return s
            }
        }
        return null
    }

    private fun parseBool(obj: JsonObject, chave: String, padrao: Boolean): Boolean {
        val e = obj.get(chave)
        if (e == null || e.isJsonNull) return padrao
        return try {
            if (e.isJsonPrimitive && e.asJsonPrimitive.isBoolean) e.asBoolean
            else when (e.asString.trim().lowercase()) { "1", "true" -> true; "0", "false" -> false; else -> padrao }
        } catch (_: Exception) { padrao }
    }
}