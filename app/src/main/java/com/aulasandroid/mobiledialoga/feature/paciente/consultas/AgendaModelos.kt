package com.aulasandroid.mobiledialoga.feature.paciente.consultas

import java.util.Calendar
import java.util.Locale

// Visões do calendário da Agenda (seletor "Mês | Semana | Dia")
enum class VisaoAgenda { MES, SEMANA, DIA }

/**
 * Data do calendário. [mes] vai de 1 a 12.
 *
 * Não usa java.time de propósito: o app aceita Android 7 (minSdk 24) e o java.time só existe
 * a partir do Android 8 sem o "core library desugaring". As contas abaixo seguem o algoritmo
 * de datas civis do Howard Hinnant e são testadas contra o java.time da JVM.
 */
data class Dia(val ano: Int, val mes: Int, val dia: Int) : Comparable<Dia> {

    override fun compareTo(other: Dia): Int =
        compareValuesBy(this, other, { it.ano }, { it.mes }, { it.dia })

    // Dias desde 01/01/1970
    fun diasDesdeEpoca(): Long {
        val a = if (mes <= 2) ano - 1 else ano
        val era = Math.floorDiv(a, 400)
        val anoDaEra = a - era * 400
        val diaDoAno = (153 * (mes + (if (mes > 2) -3 else 9)) + 2) / 5 + dia - 1
        val diaDaEra = anoDaEra * 365 + anoDaEra / 4 - anoDaEra / 100 + diaDoAno
        return era.toLong() * 146097 + diaDaEra - 719468
    }

    // 0 = domingo ... 6 = sábado
    fun diaDaSemana(): Int = Math.floorMod(diasDesdeEpoca() + 4, 7L).toInt()

    fun maisDias(n: Int): Dia = deEpoca(diasDesdeEpoca() + n)

    // Soma meses mantendo o dia (31/01 + 1 mês = 28/02 ou 29/02)
    fun maisMeses(n: Int): Dia {
        val total = ano * 12 + (mes - 1) + n
        val novoAno = Math.floorDiv(total, 12)
        val novoMes = Math.floorMod(total, 12) + 1
        return Dia(novoAno, novoMes, minOf(dia, diasNoMes(novoAno, novoMes)))
    }

    fun paraIso(): String = String.format(Locale.ROOT, "%04d-%02d-%02d", ano, mes, dia)

    companion object {

        fun diasNoMes(ano: Int, mes: Int): Int = when (mes) {
            2 -> if (anoBissexto(ano)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        fun anoBissexto(ano: Int): Boolean = (ano % 4 == 0 && ano % 100 != 0) || ano % 400 == 0

        fun deEpoca(diasDesdeEpoca: Long): Dia {
            val z = diasDesdeEpoca + 719468
            val era = Math.floorDiv(z, 146097L)
            val diaDaEra = (z - era * 146097).toInt()
            val anoDaEra = (diaDaEra - diaDaEra / 1460 + diaDaEra / 36524 - diaDaEra / 146096) / 365
            val diaDoAno = diaDaEra - (365 * anoDaEra + anoDaEra / 4 - anoDaEra / 100)
            val mp = (5 * diaDoAno + 2) / 153
            val dia = diaDoAno - (153 * mp + 2) / 5 + 1
            val mes = if (mp < 10) mp + 3 else mp - 9
            val ano = (anoDaEra + era * 400).toInt() + (if (mes <= 2) 1 else 0)
            return Dia(ano, mes, dia)
        }

        // Aceita "2026-09-02" e também "2026-09-02T00:00:00.000Z" (a API devolve a data assim): vale a parte da data.
        fun deIso(texto: String?): Dia? {
            if (texto == null || texto.length < 10) return null
            val partes = texto.substring(0, 10).split("-")
            if (partes.size != 3) return null
            val ano = partes[0].toIntOrNull() ?: return null
            val mes = partes[1].toIntOrNull() ?: return null
            val dia = partes[2].toIntOrNull() ?: return null
            if (mes !in 1..12 || dia !in 1..diasNoMes(ano, mes)) return null
            return Dia(ano, mes, dia)
        }

        // Data de hoje no fuso do aparelho
        fun hoje(): Dia {
            val agora = Calendar.getInstance()
            return Dia(agora.get(Calendar.YEAR), agora.get(Calendar.MONTH) + 1, agora.get(Calendar.DAY_OF_MONTH))
        }

        // Hora atual no fuso do aparelho, no formato "HH:mm"
        fun horaAgora(): String {
            val agora = Calendar.getInstance()
            return String.format(Locale.ROOT, "%02d:%02d", agora.get(Calendar.HOUR_OF_DAY), agora.get(Calendar.MINUTE))
        }
    }
}

// Uma consulta do paciente. [hora] fica no formato "HH:mm".
data class Consulta(
    val id: Int,
    val titulo: String,
    val especialidade: String?,
    val dia: Dia,
    val hora: String,
    val status: String,
    val idVinculo: Int,
    val criadoPor: String
) {
    // Hora cheia em que a consulta começa (14:30 -> 14), usada para achar a linha da grade
    val horaCheia: Int get() = hora.substring(0, 2).toInt()
}

// Profissional com quem o paciente tem vínculo ativo. A consulta é gravada no [idVinculo].
data class ProfissionalVinculado(
    val idVinculo: Int,
    val idProfissional: Int,
    val nome: String
)

// Vínculo do paciente como a API devolve
data class VinculoDoPaciente(
    val id: Int,
    val idProfissional: Int,
    val status: String
)

// Tudo o que a tela da Agenda precisa carregar
data class DadosAgenda(
    val consultas: List<Consulta>,
    val profissionais: List<ProfissionalVinculado>,
    val idsVinculos: Set<Int>
)
