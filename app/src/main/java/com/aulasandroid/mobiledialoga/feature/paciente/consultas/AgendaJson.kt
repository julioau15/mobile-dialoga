package com.aulasandroid.mobiledialoga.feature.paciente.consultas

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Leitura das respostas da API e montagem do corpo do POST /consultas.
 * Sem nada de Android, para poder ser testado com as respostas reais da API.
 */
object AgendaJson {

    // ---------- Leitura ----------

    fun objeto(texto: String?): JsonObject? {
        if (texto.isNullOrBlank()) return null
        return try {
            @Suppress("DEPRECATION")
            val elemento = JsonParser().parse(texto)
            if (elemento.isJsonObject) elemento.asJsonObject else null
        } catch (e: Exception) {
            null
        }
    }

    // Mensagem de erro que a API manda no corpo ({ "status": false, "message": "..." })
    fun mensagemDeErro(texto: String?): String? = objeto(texto)?.texto("message")

    // Código que a API manda no corpo (não é o código HTTP). O 404 "nenhum registro" vem em JSON com status_code 404.
    fun statusCodeDoCorpo(texto: String?): Int? = objeto(texto)?.inteiro("status_code")

    // GET /pacientes/me -> id do paciente logado
    fun idDoPaciente(json: JsonObject?): Int? = lista(json, "paciente").firstOrNull()?.inteiro("id")

    // GET /vinculos -> só os vínculos DESTE paciente (a API devolve os de todos)
    fun vinculosDoPaciente(json: JsonObject?, idPaciente: Int): List<VinculoDoPaciente> =
        lista(json, "vinculo")
            .filter { it.inteiro("id_paciente") == idPaciente }
            .mapNotNull { item ->
                val id = item.inteiro("id") ?: return@mapNotNull null
                val idProfissional = item.inteiro("id_profissional") ?: return@mapNotNull null
                VinculoDoPaciente(id, idProfissional, item.texto("status") ?: "")
            }

    // GET /pacientes/me/profissionais -> { id do profissional: nome }
    fun nomesDosProfissionais(json: JsonObject?): Map<Int, String> =
        lista(json, "profissional")
            .mapNotNull { item ->
                val id = item.inteiro("id") ?: return@mapNotNull null
                val nome = item.texto("nome_completo") ?: return@mapNotNull null
                id to nome
            }
            .toMap()

    // GET /pacientes/me/profissionais -> { id do profissional: especialidades } (vem como "A, B" numa string só)
    fun especialidadesDosProfissionais(json: JsonObject?): Map<Int, List<String>> =
        lista(json, "profissional")
            .mapNotNull { item ->
                val id = item.inteiro("id") ?: return@mapNotNull null
                val nomes = item.texto("especialidades").orEmpty()
                    .split(",").map { it.trim() }.filter { it.isNotEmpty() }
                id to nomes
            }
            .toMap()

    // GET /consultas/vinculos/{id}/horarios -> dias e horários livres do profissional
    fun horarios(json: JsonObject?): HorariosDoProfissional? {
        val h = json?.get("response")?.takeIf { it.isJsonObject }?.asJsonObject
            ?.get("horarios")?.takeIf { it.isJsonObject }?.asJsonObject ?: return null

        val especialidades = h.get("especialidades")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.mapNotNull { if (it.isJsonPrimitive) it.asString else null }.orEmpty()

        val dias = h.get("dias")?.takeIf { it.isJsonArray }?.asJsonArray.orEmptyList().mapNotNull { el ->
            if (!el.isJsonObject) return@mapNotNull null
            val o = el.asJsonObject
            val dia = Dia.deIso(o.texto("data")) ?: return@mapNotNull null
            val horas = o.get("horarios")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.mapNotNull { if (it.isJsonPrimitive) it.asString.take(5) else null }.orEmpty()
            if (horas.isEmpty()) null else DiaLivre(dia, horas)
        }.sortedBy { it.dia }

        val cadastrou = h.get("cadastrou_disponibilidade")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true
        return HorariosDoProfissional(especialidades, dias, cadastrou)
    }

    private fun com.google.gson.JsonArray?.orEmptyList(): List<com.google.gson.JsonElement> =
        this?.toList().orEmpty()

    // GET /consultas -> as consultas (de todos): o filtro por vínculo é feito em [consultasDosVinculos]
    fun consultas(json: JsonObject?): List<Consulta> = lista(json, "consulta").mapNotNull { paraConsulta(it) }

    // Só as consultas dos vínculos do paciente
    fun consultasDosVinculos(consultas: List<Consulta>, idsVinculos: Set<Int>): List<Consulta> =
        consultas.filter { it.idVinculo in idsVinculos }

    private fun paraConsulta(item: JsonObject): Consulta? {
        val id = item.inteiro("id") ?: return null
        val idVinculo = item.inteiro("id_vinculo") ?: return null
        val dia = Dia.deIso(item.texto("data")) ?: return null
        val hora = item.texto("hora")?.take(5) ?: return null // "14:00:00" -> "14:00"
        if (hora.length != 5 || hora[2] != ':') return null
        return Consulta(
            id = id,
            // o back já devolveu este campo com dois nomes (um com erro de digitação): aceita os dois
            titulo = item.texto("nome_da_consulta") ?: item.texto("nome_da_conculta") ?: "Consulta",
            especialidade = item.texto("especialidade")?.takeIf { it.isNotBlank() },
            dia = dia,
            hora = hora,
            status = item.texto("status") ?: "",
            idVinculo = idVinculo,
            criadoPor = item.texto("criado_por") ?: ""
        )
    }

    // ---------- Montagem do corpo do POST /consultas ----------

    /**
     * O back-end tem dois detalhes que o app precisa respeitar (confirmados na API):
     *  - valida "nome_da_consulta", mas grava "nome_da_conculta" (com erro de digitação): é preciso enviar OS DOIS;
     *  - "especialidade" tem que existir. Não pode ser null: o Gson do Retrofit descarta campos nulos, o campo
     *    sumiria e a API responderia 500. Vazio ("") é aceito.
     * O status ("agendada") e quem criou ("paciente") quem define é le back, pelo token.
     */
    fun corpoDaNovaConsulta(
        profissional: ProfissionalVinculado,
        dia: Dia,
        hora: String,
        especialidade: String
    ): JsonObject = JsonObject().apply {
        addProperty("nome_da_consulta", profissional.nome)
        addProperty("nome_da_conculta", profissional.nome)
        addProperty("especialidade", especialidade.trim())
        addProperty("data", dia.paraIso())
        addProperty("hora", hora)
        addProperty("id_vinculo", profissional.idVinculo)
    }

    // ---------- Apoio ----------

    private fun lista(json: JsonObject?, chave: String): List<JsonObject> {
        val resposta = json?.get("response")?.takeIf { it.isJsonObject }?.asJsonObject ?: return emptyList()
        val itens = resposta.get(chave)?.takeIf { it.isJsonArray }?.asJsonArray ?: return emptyList()
        return itens.filter { it.isJsonObject }.map { it.asJsonObject }
    }

    private fun JsonObject.texto(chave: String): String? {
        val valor = get(chave)
        return if (valor != null && valor.isJsonPrimitive) valor.asString else null
    }

    private fun JsonObject.inteiro(chave: String): Int? {
        val valor = get(chave)
        if (valor == null || !valor.isJsonPrimitive) return null
        return try {
            valor.asInt
        } catch (e: NumberFormatException) {
            null
        }
    }
}
