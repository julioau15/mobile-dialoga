package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import com.aulasandroid.mobiledialoga.feature.paciente.home.RegistroHumorApi
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Datas do relatório sempre em ISO (yyyy-MM-dd), que comparam certo como String. */
object DatasRelatorio {
    private val PT_BR = Locale("pt", "BR")
    private fun fmt() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun hoje(): String = fmt().format(Date())

    fun somarDias(iso: String, dias: Int): String {
        val c = Calendar.getInstance().apply { time = fmt().parse(iso) ?: Date() }
        c.add(Calendar.DAY_OF_YEAR, dias)
        return fmt().format(c.time)
    }

    fun doMes(ano: Int, mes: Int, dia: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia)

    fun mesDe(iso: String): MesVisivel {
        val p = iso.split("-")
        return MesVisivel(p[0].toInt(), p[1].toInt() - 1)
    }

    fun diasNoMes(m: MesVisivel): Int =
        Calendar.getInstance().apply { set(m.ano, m.mes, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

    /** 0 = domingo (a grade do protótipo começa em D). */
    fun diaDaSemanaDoPrimeiro(m: MesVisivel): Int =
        Calendar.getInstance().apply { set(m.ano, m.mes, 1) }.get(Calendar.DAY_OF_WEEK) - 1

    fun mesAnterior(m: MesVisivel) = if (m.mes == 0) MesVisivel(m.ano - 1, 11) else MesVisivel(m.ano, m.mes - 1)
    fun proximoMes(m: MesVisivel) = if (m.mes == 11) MesVisivel(m.ano + 1, 0) else MesVisivel(m.ano, m.mes + 1)

    fun tituloMes(m: MesVisivel): String {
        val c = Calendar.getInstance().apply { set(m.ano, m.mes, 1) }
        return SimpleDateFormat("MMMM yyyy", PT_BR).format(c.time).replaceFirstChar { it.uppercase() }
    }

    fun br(iso: String): String =
        iso.split("-").takeIf { it.size == 3 }?.let { "${it[2]}/${it[1]}/${it[0]}" } ?: iso

    fun diaMes(iso: String): String =
        iso.split("-").takeIf { it.size == 3 }?.let { "${it[2]}/${it[1]}" } ?: iso

    /**
     * Colunas DATETIME chegam como "2026-10-21T02:59:00.000Z" (UTC): converte para a data local.
     * Colunas DATE e textos sem "Z" só são cortados nos 10 primeiros caracteres.
     */
    fun dataLocal(bruto: String?): String? {
        if (bruto.isNullOrBlank()) return null
        if (!bruto.endsWith("Z") || bruto.length < 19) return bruto.take(10)
        val utc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val d = runCatching { utc.parse(bruto.take(19)) }.getOrNull() ?: return bruto.take(10)
        return fmt().format(d)
    }
}

/** Converte o texto escolhido no formulário de humor em nível 1..máximo (para as médias do relatório). */
object EscalasRelatorio {
    const val MAX_ENERGIA = 3
    const val MAX_SENTIMENTO = 4
    const val MAX_HUMOR = 5
    const val MAX_PENSAMENTOS = 4

    private fun norm(v: String?): String =
        Normalizer.normalize(v.orEmpty().trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    fun energia(v: String?): Int? = when (norm(v)) { "baixa" -> 1; "media" -> 2; "alta" -> 3; else -> null }

    fun sentimento(v: String?): Int? = when (norm(v)) {
        "triste", "irritado" -> 1
        "ansioso", "cansado" -> 2
        "calmo" -> 3
        "feliz", "animado" -> 4
        else -> null
    }

    fun humor(v: String?): Int? = when (norm(v)) {
        "muito mal", "pessimo" -> 1
        "mal", "ruim" -> 2
        "neutro", "regular" -> 3
        "bem", "bom" -> 4
        "muito bem", "otimo" -> 5
        else -> null
    }

    fun pensamentos(v: String?): Int? {
        val s = norm(v)
        return when {
            s.contains("negativ") -> 1
            s.contains("acelerad") -> 2
            s.contains("neutr") -> 3
            s.contains("positiv") -> 4
            else -> null
        }
    }
}

object RelatoriosMapper {

    // ---------- helpers de JSON (o back devolve { response: { <chave>: [...] } }) ----------

    private fun achaLista(no: JsonElement?, vararg chaves: String): JsonArray {
        if (no == null || no.isJsonNull) return JsonArray()
        if (no.isJsonArray) return no.asJsonArray
        if (!no.isJsonObject) return JsonArray()
        val obj = no.asJsonObject
        val todasChaves = chaves.toList() + listOf("profissional", "solicitacaoAcesso", "solicitacoesAcesso", "solicitacao", "response", "dados", "tarefa", "tarefas")
        for (k in todasChaves) {
            val e = obj.get(k) ?: continue
            if (e.isJsonArray) return e.asJsonArray
            if (e.isJsonObject) {
                val r = achaLista(e, *chaves)
                if (r.size() > 0) return r
            }
        }
        for (k in obj.keySet()) {
            val e = obj.get(k) ?: continue
            if (e.isJsonArray) return e.asJsonArray
            if (e.isJsonObject) {
                val r = achaLista(e, *chaves)
                if (r.size() > 0) return r
            }
        }
        return JsonArray()
    }

    private fun JsonObject.texto(vararg chaves: String): String? =
        chaves.firstNotNullOfOrNull { c ->
            get(c)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
        }

    private fun JsonObject.inteiro(vararg chaves: String): Int =
        chaves.firstNotNullOfOrNull { c ->
            get(c)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asInt }.getOrNull() }
        } ?: 0

    // ---------- humor ----------

    fun medias(registros: List<RegistroHumorApi>): List<IndicadorMedia> {
        fun media(f: (RegistroHumorApi) -> Int?) = registros.mapNotNull(f).takeIf { it.isNotEmpty() }?.average()
        return listOf(
            IndicadorMedia("Energia", "⚡", media { EscalasRelatorio.energia(it.energia) }, EscalasRelatorio.MAX_ENERGIA),
            IndicadorMedia("Sentimento", "💭", media { EscalasRelatorio.sentimento(it.sentimento) }, EscalasRelatorio.MAX_SENTIMENTO),
            IndicadorMedia("Humor", "😊", media { EscalasRelatorio.humor(it.humor) }, EscalasRelatorio.MAX_HUMOR),
            IndicadorMedia("Pensamentos", "🧠", media { EscalasRelatorio.pensamentos(it.pensamento) }, EscalasRelatorio.MAX_PENSAMENTOS)
        )
    }

    /** Um ponto por dia (o último registro do dia), em ordem cronológica. */
    fun sono(registros: List<RegistroHumorApi>): List<SonoPonto> =
        registros.filter { it.horasSono != null }
            .groupBy { it.data }
            .map { SonoPonto(it.key, it.value.last().horasSono!!) }
            .sortedBy { it.dataIso }

    fun resumoSono(pontos: List<SonoPonto>): ResumoSono? {
        if (pontos.isEmpty()) return null
        val h = pontos.map { it.horas }
        return ResumoSono(media = h.average(), minimo = h.minOrNull() ?: 0.0, maximo = h.maxOrNull() ?: 0.0)
    }

    // ---------- tarefas ----------

    fun tarefas(json: JsonObject?): List<TarefaRelatorio> =
        achaLista(json, "tarefa", "tarefas", "tarefaDestinatario", "response", "dados").mapNotNull { el ->
            val o = el.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val status = statusTarefa(o.texto("status", "situacao"))
            TarefaRelatorio(
                id = o.inteiro("id_tarefa", "id"),
                titulo = o.texto("titulo", "nome") ?: "Tarefa sem título",
                prazoIso = DatasRelatorio.dataLocal(o.texto("prazo", "data_limite")),
                dataEnvioIso = DatasRelatorio.dataLocal(o.texto("data_envio")),
                dataConclusaoIso = DatasRelatorio.dataLocal(o.texto("data_conclusao")),
                status = status,
                progresso = if (status == "Concluída") 100 else o.inteiro("progresso").coerceIn(0, 100)
            )
        }

    private fun statusTarefa(bruto: String?): String {
        val s = bruto?.lowercase().orEmpty()
        return when {
            s.contains("conclu") || s.contains("finaliz") -> "Concluída"
            s.contains("atras") -> "Em atraso"
            s.contains("andamento") -> "Em andamento"
            else -> "Pendente"
        }
    }

    /** Tarefas "do período": enviadas, com prazo ou concluídas dentro do intervalo escolhido. */
    fun tarefasDoPeriodo(tarefas: List<TarefaRelatorio>, inicio: String, fim: String): List<TarefaRelatorio> =
        tarefas.filter { t ->
            listOfNotNull(t.dataEnvioIso, t.prazoIso, t.dataConclusaoIso).any { it >= inicio && it <= fim }
        }.sortedBy { it.prazoIso ?: "9999-12-31" }

    // ---------- solicitações de acesso ----------

    fun solicitacoes(json: JsonObject?): List<SolicitacaoAcessoUi> =
        achaLista(json, "solicitacaoAcesso", "solicitacoesAcesso", "solicitacao", "response", "dados").mapNotNull { el ->
            val o = el.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = o.inteiro("id")
            if (id <= 0) return@mapNotNull null
            val s = o.texto("status").orEmpty().lowercase()
            SolicitacaoAcessoUi(
                id = id,
                idProfissional = o.inteiro("id_profissional"),
                // a listagem do back só devolve o id do profissional; o nome é completado depois
                nome = o.texto("profissional_nome", "nome_profissional", "nome_completo", "nome").orEmpty(),
                especialidade = o.texto("especialidade").orEmpty(),
                foto = o.texto("foto_avatar", "foto", "avatar", "imagem"),
                categorias = o.getAsJsonArray("categorias")?.mapNotNull {
                    it.takeIf { valor -> valor.isJsonPrimitive }?.asString
                }?.toSet().orEmpty(),
                status = when {
                    s.contains("aprov") || s.contains("aceit") -> "Aprovada"
                    s.contains("recus") || s.contains("reprov") -> "Recusada"
                    else -> "Pendente"
                }
            )
        }.sortedWith(compareByDescending<SolicitacaoAcessoUi> { it.pendente }.thenByDescending { it.id })

    /** Lê nome, especialidade e foto da resposta de GET /profissionais/{id}. */
    fun dadosProfissional(json: JsonObject?): Triple<String, String, String?>? {
        val o = achaLista(json, "profissional", "response", "dados").firstOrNull()
            ?.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        val nome = o.texto("nome_completo", "nome") ?: return null
        val esp = (o.get("especialidades") ?: o.get("especialidade"))
            ?.takeIf { it.isJsonArray }?.asJsonArray?.firstOrNull()
            ?.let { e ->
                when {
                    e.isJsonPrimitive -> e.asString
                    e.isJsonObject -> e.asJsonObject.texto("nome", "especialidade")
                    else -> null
                }
            } ?: o.texto("especialidade")
        val foto = o.texto("foto_avatar", "foto", "avatar", "imagem")
        return Triple(nome, esp.orEmpty(), foto)
    }
}
