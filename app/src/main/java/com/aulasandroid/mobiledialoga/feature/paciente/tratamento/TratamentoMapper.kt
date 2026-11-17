package com.aulasandroid.mobiledialoga.feature.paciente.tratamento

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object TratamentoMapper {

    /** O back devolve { response: { tratamento: [ { ..., horarios: [ { id, horario: "08:00:00" } ] } ] } }. */
    private fun achaLista(no: JsonElement?): JsonArray {
        if (no == null || no.isJsonNull) return JsonArray()
        if (no.isJsonArray) return no.asJsonArray
        if (!no.isJsonObject) return JsonArray()
        val obj = no.asJsonObject
        for (k in listOf("tratamento", "tratamentos", "response", "dados")) {
            val e = obj.get(k) ?: continue
            if (e.isJsonArray) return e.asJsonArray
            if (e.isJsonObject) achaLista(e).takeIf { it.size() > 0 }?.let { return it }
        }
        return JsonArray()
    }

    private fun JsonObject.texto(vararg chaves: String): String? =
        chaves.firstNotNullOfOrNull { c ->
            get(c)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        }

    private fun JsonObject.ativo(chave: String): Boolean {
        val p = get(chave)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return false
        return when {
            p.isBoolean -> p.asBoolean
            p.isNumber -> p.asInt != 0
            else -> p.asString.let { it == "1" || it.equals("true", ignoreCase = true) }
        }
    }

    /** "08:00:00" (ou qualquer texto com hh:mm) -> "08:00". */
    fun horaCurta(bruto: String): String? {
        val m = Regex("(\\d{1,2}):(\\d{2})").find(bruto) ?: return null
        return "%02d:%s".format(m.groupValues[1].toInt(), m.groupValues[2])
    }

    fun lista(json: JsonObject?): List<TratamentoUi> =
        achaLista(json).mapNotNull { el ->
            val o = el.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = o.get("id")?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asInt }.getOrNull() }
                ?: return@mapNotNull null
            val nome = o.texto("nome_medicamento", "nome") ?: return@mapNotNull null

            val horarios = (o.get("horarios")?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()).mapNotNull { h ->
                when {
                    h.isJsonPrimitive -> h.asString
                    h.isJsonObject -> h.asJsonObject.texto("horario", "hora")
                    else -> null
                }?.let(::horaCurta)
            }.sorted()

            TratamentoUi(
                id = id,
                nome = nome,
                dosagem = o.texto("dosagem", "dose").orEmpty(),
                frequencia = o.texto("frequencia").orEmpty(),
                lembreteAtivo = o.ativo("lembrete_ativo"),
                horarios = horarios
            )
        }

    /** Corpo do POST /pacientes/me/tratamentos e do PUT /tratamentos/{id} (o PUT exige "horarios"). */
    fun corpo(form: FormTratamento): JsonObject = JsonObject().apply {
        addProperty("nome_medicamento", form.nome.trim())
        addProperty("dosagem", form.dosagem.trim())
        addProperty("frequencia", form.frequencia.rotulo)
        addProperty("lembrete_ativo", if (form.lembreteAtivo) 1 else 0)
        add("horarios", JsonArray().apply {
            form.horarios.filterNotNull().forEach { h ->
                add(JsonObject().apply { addProperty("horario", "$h:00") })
            }
        })
    }
}
