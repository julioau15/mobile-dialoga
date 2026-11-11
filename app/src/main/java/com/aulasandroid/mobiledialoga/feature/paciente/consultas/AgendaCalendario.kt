package com.aulasandroid.mobiledialoga.feature.paciente.consultas

/**
 * Contas e textos do calendário da Agenda (sem nada de Android, para poder ser testado).
 * Os textos seguem o protótipo: "Setembro 2026", "31/8 – 6/9/2026", "Quarta-Feira, 2 De Setembro De 2026"...
 */
object AgendaCalendario {

    private val MESES = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    )
    private val DIAS_DA_SEMANA = listOf(
        "domingo", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado"
    )

    // Cabeçalho do calendário do mês (a semana começa no domingo) e da semana (começa na segunda)
    val DIAS_CURTOS_DOMINGO_PRIMEIRO = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
    val DIAS_CURTOS_SEGUNDA_PRIMEIRO = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

    private val MESES_CURTOS = listOf(
        "Jan.", "Fev.", "Mar.", "Abr.", "Mai.", "Jun.", "Jul.", "Ago.", "Set.", "Out.", "Nov.", "Dez."
    )

    // Primeira hora da grade de Dia/Semana (o protótipo começa em 07:00) e a última exibida
    const val HORA_INICIAL = 7
    const val HORA_FINAL = 21

    // ---------- Grades ----------

    // Semanas do mês, começando no domingo. Dias de fora do mês ficam null (como no protótipo).
    fun gradeDoMes(ano: Int, mes: Int): List<List<Dia?>> {
        val primeiro = Dia(ano, mes, 1)
        val total = Dia.diasNoMes(ano, mes)
        val celulas = MutableList<Dia?>(primeiro.diaDaSemana()) { null }
        for (d in 1..total) celulas.add(Dia(ano, mes, d))
        while (celulas.size % 7 != 0) celulas.add(null)
        return celulas.chunked(7)
    }

    // Os 7 dias da semana do [dia], de segunda a domingo
    fun semanaDe(dia: Dia): List<Dia> {
        val segunda = dia.maisDias(-((dia.diaDaSemana() + 6) % 7))
        return List(7) { segunda.maisDias(it) }
    }

    // Faixa de horas da grade: 07..21, ampliada se houver consulta fora dessa faixa
    fun horasDaGrade(consultas: List<Consulta>): IntRange {
        val primeira = minOf(HORA_INICIAL, consultas.minOfOrNull { it.horaCheia } ?: HORA_INICIAL)
        val ultima = maxOf(HORA_FINAL, consultas.maxOfOrNull { it.horaCheia } ?: HORA_FINAL)
        return primeira..ultima
    }

    fun rotuloDaHora(hora: Int): String = String.format(java.util.Locale.ROOT, "%02d:00", hora)

    // ---------- Navegação (setas ‹ ›) ----------

    // direcao: -1 anterior, +1 próximo. Mês anda de mês em mês, semana de 7 em 7 dias, dia de 1 em 1.
    fun mover(visao: VisaoAgenda, dia: Dia, direcao: Int): Dia = when (visao) {
        VisaoAgenda.MES -> dia.maisMeses(direcao)
        VisaoAgenda.SEMANA -> dia.maisDias(7 * direcao)
        VisaoAgenda.DIA -> dia.maisDias(direcao)
    }

    // ---------- Textos ----------

    fun nomeDoMes(mes: Int): String = MESES[mes - 1]

    fun nomeDoDiaDaSemana(dia: Dia): String = DIAS_DA_SEMANA[dia.diaDaSemana()]

    fun mesCurto(mes: Int): String = MESES_CURTOS[mes - 1]

    // "Setembro 2026"
    fun tituloMes(dia: Dia): String = "${maiuscula(nomeDoMes(dia.mes))} ${dia.ano}"

    // "2 de setembro de 2026"
    fun tituloDia(dia: Dia): String = "${dia.dia} de ${nomeDoMes(dia.mes)} de ${dia.ano}"

    // "31/8 – 6/9/2026" (se a semana cruza o ano: "29/12/2026 – 4/1/2027")
    fun tituloSemana(dia: Dia): String {
        val semana = semanaDe(dia)
        val inicio = semana.first()
        val fim = semana.last()
        return if (inicio.ano == fim.ano) {
            "${inicio.dia}/${inicio.mes} – ${fim.dia}/${fim.mes}/${fim.ano}"
        } else {
            "${inicio.dia}/${inicio.mes}/${inicio.ano} – ${fim.dia}/${fim.mes}/${fim.ano}"
        }
    }

    // Título do calendário conforme a visão
    fun tituloDaVisao(visao: VisaoAgenda, dia: Dia): String = when (visao) {
        VisaoAgenda.MES -> tituloMes(dia)
        VisaoAgenda.SEMANA -> tituloSemana(dia)
        VisaoAgenda.DIA -> tituloDia(dia)
    }

    // Faixa da visão Dia: "Quarta-Feira, 2 De Setembro De 2026" (cada palavra com inicial maiúscula, como no protótipo)
    fun tituloDiaCompleto(dia: Dia): String =
        "${nomeDoDiaDaSemana(dia)}, ${tituloDia(dia)}"
            .split(" ")
            .joinToString(" ") { palavra -> palavra.split("-").joinToString("-") { maiuscula(it) } }

    // Card do dia selecionado: "quarta-feira, 2 de setembro"
    fun tituloDiaSelecionado(dia: Dia): String =
        "${nomeDoDiaDaSemana(dia)}, ${dia.dia} de ${nomeDoMes(dia.mes)}"

    fun quantidadeDeConsultas(quantidade: Int): String = "$quantidade consulta(s)"

    // "02/09/2026" (campo Data do formulário)
    fun dataCurta(dia: Dia): String =
        String.format(java.util.Locale.ROOT, "%02d/%02d/%04d", dia.dia, dia.mes, dia.ano)

    private fun maiuscula(texto: String): String =
        if (texto.isEmpty()) texto else texto.substring(0, 1).uppercase() + texto.substring(1)
}
