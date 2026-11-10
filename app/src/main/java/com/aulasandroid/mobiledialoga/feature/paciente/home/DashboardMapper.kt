package com.aulasandroid.mobiledialoga.feature.paciente.home

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Registro de humor como vem da API (já normalizado). */
data class RegistroHumorApi(
    val data: String, // yyyy-MM-dd
    val sentimento: String?,
    val energia: String?,
    val humor: String?,
    val horasSono: Double?,
    val pensamento: String?
)

/** Converte o valor textual escolhido no formulário em 0-100 para as barras do dashboard. */
object HumorScores {
    /** minúsculas e sem acento: casa "Média"/"media" e "Ótimo"/"otimo". */
    private fun norm(v: String?): String =
        java.text.Normalizer.normalize(v.orEmpty().trim().lowercase(Locale.ROOT), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    fun energia(v: String?) = when (norm(v)) { "baixa" -> 25; "media" -> 60; "alta" -> 90; else -> 0 }

    fun sentimento(v: String?) = when (norm(v)) {
        "feliz", "animado" -> 90; "calmo" -> 80; "cansado" -> 40; "ansioso" -> 30; "triste", "irritado" -> 25
        else -> 0
    }

    fun humor(v: String?) = when (norm(v)) {
        "muito bem", "otimo" -> 95; "bem", "bom" -> 75; "neutro", "regular" -> 50
        "mal", "ruim" -> 30; "muito mal", "pessimo" -> 10
        else -> 0
    }

    /** Aceita o chip do app ("Positivos") e textos livres como "pensamentos positivos". */
    fun pensamentos(v: String?): Int {
        val s = norm(v)
        return when {
            s.contains("positiv") -> 90
            s.contains("neutr") -> 55
            s.contains("acelerad") -> 35
            s.contains("negativ") -> 20
            else -> 0
        }
    }
}


object DashboardMapper {
    private val PT_BR = Locale("pt", "BR")

    /**
     * O back devolve { status, status_code, message, response: { <chave>: [...], count } }.
     * A <chave> é sempre no singular e muda por controller:
     *   tratamento, tarefa, consulta, registroHumor e, em /pacientes/me/profissionais, paciente.
     * Essas chaves são sempre consideradas, além das passadas por quem chama.
     */
    private val CHAVES_API = arrayOf(
        "tratamento", "tarefa", "consulta", "registroHumor", "paciente", "vinculo", "profissional",
        "response", "dados", "data"
    )

    fun hojeIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // ---------- helpers de JSON ----------

    private fun JsonElement?.paraLista(vararg chaves: String): JsonArray {
        if (this == null || isJsonNull) return JsonArray()
        if (isJsonArray) return asJsonArray
        if (!isJsonObject) return JsonArray()
        val obj = asJsonObject
        for (k in chaves.toList() + CHAVES_API) {
            val e = obj.get(k) ?: continue
            if (e.isJsonArray) return e.asJsonArray
            if (e.isJsonObject) {
                val r = e.paraLista(*chaves)
                if (r.size() > 0) return r
            }
        }
        return JsonArray()
    }

    private fun JsonElement.objeto(): JsonObject? = if (isJsonObject) asJsonObject else null

    private fun JsonObject.texto(vararg chaves: String): String? =
        chaves.firstNotNullOfOrNull { c ->
            get(c)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        }

    private fun JsonObject.numero(vararg chaves: String): Double? =
        chaves.firstNotNullOfOrNull { c ->
            get(c)?.takeIf { it.isJsonPrimitive }
                ?.let { runCatching { it.asString.replace(',', '.').toDouble() }.getOrNull() }
        }

    private fun JsonObject.lista(chave: String): JsonArray =
        get(chave)?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()

    /**
     * Colunas TIME costumam chegar como "1970-01-01T08:00:00.000Z" (ou "08:00:00").
     * Devolve sempre "HH:mm".
     */
    private fun horaCurta(bruto: String): String {
        val t = if (bruto.contains('T')) bruto.substringAfter('T') else bruto.substringAfter(' ', bruto)
        return t.take(5)
    }

    /**
     * Colunas DATETIME (ex.: prazo da tarefa) chegam como "2026-10-21T02:59:00.000Z",
     * um instante em UTC. Converte para a data local do aparelho (yyyy-MM-dd).
     * Sem o "Z" (ex.: dateStrings ligado no back), só corta os 10 primeiros caracteres.
     * NÃO usar em colunas DATE (data da consulta, data do humor): essas só devem ser cortadas.
     */
    private fun dataLocal(bruto: String): String {
        if (!bruto.endsWith("Z") || bruto.length < 19) return bruto.take(10)
        val utc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val d = runCatching { utc.parse(bruto.take(19)) }.getOrNull() ?: return bruto.take(10)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d)
    }

    fun quantidade(json: JsonObject?, vararg chaves: String): Int = json.paraLista(*chaves).size()

    // ---------- tratamentos -> medicamentos ----------

    fun medicamentos(json: JsonObject?): List<MedicamentoDashboard> =
        json.paraLista("tratamentos").mapIndexedNotNull { i, el ->
            val o = el.objeto() ?: return@mapIndexedNotNull null
            val nome = o.texto("nome_medicamento", "nome", "medicamento") ?: return@mapIndexedNotNull null
            val dose = o.texto("dosagem", "dose")
            val horarios = o.lista("horarios").mapNotNull { h ->
                when {
                    h.isJsonPrimitive -> h.asString
                    h.isJsonObject -> h.asJsonObject.texto("horario", "hora")
                    else -> null
                }?.let(::horaCurta)
            }
            val frequencia = o.texto("frequencia") ?: when (horarios.size) {
                0 -> ""
                1 -> "Uma vez ao dia"
                2 -> "Duas vezes ao dia"
                else -> "${horarios.size} vezes ao dia"
            }
            MedicamentoDashboard(
                id = o.texto("id", "id_tratamento") ?: i.toString(),
                nome = if (dose != null && !nome.contains(dose)) "$nome $dose" else nome,
                dose = dose ?: "",
                frequencia = frequencia,
                horarios = horarios
            )
        }

    // ---------- consultas ----------

    /** Só consultas de hoje em diante, ordenadas. */
    fun consultas(json: JsonObject?): List<ConsultaDashboard> {
        val hoje = hojeIso()
        return json.paraLista("consultas").mapIndexedNotNull { i, el ->
            val o = el.objeto() ?: return@mapIndexedNotNull null
            val bruto = o.texto("data", "data_consulta", "data_hora", "inicio") ?: return@mapIndexedNotNull null
            val data = bruto.take(10)
            if (data < hoje) return@mapIndexedNotNull null
            // tbl_consulta.status começa como "agendada"
            if (o.texto("status")?.lowercase()?.contains("cancel") == true) return@mapIndexedNotNull null
            val horario = o.texto("hora", "horario", "hora_inicio")?.let(::horaCurta)
                ?: bruto.takeIf { it.length > 10 }?.let(::horaCurta)
                ?: ""
            val prof = o.get("profissional")?.takeIf { it.isJsonObject }?.asJsonObject
            ConsultaDashboard(
                id = o.texto("id", "id_consulta") ?: i.toString(),
                data = data,
                profissionalNome = o.texto("profissional_nome", "nome_profissional", "nome_completo")
                    ?: prof?.texto("nome_completo", "nome")
                    // a API ainda não devolve o profissional; a coluna do banco é "nome_da_conculta" (typo)
                    ?: o.texto("nome_da_conculta", "nome_da_consulta")
                    ?: "Consulta",
                especialidade = o.texto("especialidade") ?: prof?.texto("especialidade") ?: "",
                horario = horario
            )
        }.sortedWith(compareBy({ it.data }, { it.horario }))
    }

    // ---------- tarefas ----------

    fun tarefas(json: JsonObject?): List<TarefaDashboard> =
        json.paraLista("tarefas").mapIndexedNotNull { i, el ->
            val o = el.objeto() ?: return@mapIndexedNotNull null
            val titulo = o.texto("titulo", "nome", "descricao") ?: return@mapIndexedNotNull null
            TarefaDashboard(
                id = o.texto("id", "id_tarefa") ?: i.toString(),
                titulo = titulo,
                prazo = o.texto("prazo", "data_limite", "data_entrega", "data_fim")
                    ?.let { formatarData(dataLocal(it)) } ?: "não definido",
                status = normalizarStatus(o.texto("status", "situacao"))
            )
        }

    private fun normalizarStatus(bruto: String?): String {
        val s = bruto?.lowercase().orEmpty()
        return when {
            s.contains("conclu") || s.contains("respond") || s.contains("finaliz") -> "Concluída"
            s.contains("andamento") || s.contains("progress") -> "Em andamento"
            else -> "Pendente"
        }
    }

    private fun formatarData(iso: String): String {
        val p = iso.take(10).split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
    }

    // ---------- registros de humor ----------

    fun registrosHumor(json: JsonObject?): List<RegistroHumorApi> =
        json.paraLista("registros", "registros_humor").mapNotNull { el ->
            val o = el.objeto() ?: return@mapNotNull null
            // tbl_registro_humor.data é DATE (o insert grava NOW() truncado): só cortar, sem converter fuso
            val data = o.texto("data", "data_registro", "data_criacao", "created_at")?.take(10)
                ?: return@mapNotNull null
            RegistroHumorApi(
                data = data,
                sentimento = o.texto("sentimento"),
                energia = o.texto("energia"),
                humor = o.texto("humor"),
                horasSono = o.numero("horas_sono", "horasSono"),
                // o back chama esta coluna de padrao_pensamentos
                pensamento = o.texto("padrao_pensamentos", "pensamento", "pensamentos")
            )
        }.sortedBy { it.data }

    fun indicadoresHoje(registros: List<RegistroHumorApi>): IndicadoresHoje? =
        registros.lastOrNull { it.data == hojeIso() }?.let {
            IndicadoresHoje(
                energia = HumorScores.energia(it.energia),
                sentimento = HumorScores.sentimento(it.sentimento),
                humor = HumorScores.humor(it.humor),
                pensamentos = HumorScores.pensamentos(it.pensamento)
            )
        }

    /** Últimos 7 registros de sono (um por dia). O último é "hoje". */
    fun sonoSemana(registros: List<RegistroHumorApi>): List<SonoDia> =
        registros.filter { it.horasSono != null }
            .groupBy { it.data }
            .map { it.value.last() }
            .takeLast(7)
            .map { SonoDia(rotuloDia(it.data), it.horasSono!!) }

    private fun rotuloDia(iso: String): String {
        val data = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) }.getOrNull()
            ?: return iso
        return SimpleDateFormat("EEE", PT_BR).format(data)
            .replace(".", "").take(3).replaceFirstChar { it.uppercase() }
    }
}