package com.aulasandroid.mobiledialoga.feature.paciente.consultas

/**
 * Regras da Agenda do paciente (sem nada de Android, para poder ser testado).
 */
object AgendaRegras {

    const val STATUS_AGENDADA = "agendada"
    const val STATUS_CANCELADA = "cancelada"

    // O banco guarda o status do vínculo como número (0 pendente, 1 ativo, 2 recusado),
    // mas o seed de teste usa texto: aceitamos os dois.
    fun vinculoAtivo(status: String): Boolean = status.lowercase().let { it == "1" || it == "ativo" }

    // Consultas canceladas não aparecem no calendário
    fun visiveis(consultas: List<Consulta>): List<Consulta> =
        consultas.filter { it.status != STATUS_CANCELADA }

    fun doDia(consultas: List<Consulta>, dia: Dia): List<Consulta> =
        visiveis(consultas).filter { it.dia == dia }.sortedBy { it.hora }

    // Consultas visíveis dentro de um conjunto de dias (os 7 dias da visão Semana)
    fun dosDias(consultas: List<Consulta>, dias: List<Dia>): List<Consulta> =
        visiveis(consultas).filter { it.dia in dias }

    // Dias com pelo menos uma consulta (bolinha roxa no calendário do mês)
    fun diasComConsulta(consultas: List<Consulta>): Set<Dia> =
        visiveis(consultas).map { it.dia }.toSet()

    // Próximas consultas: só as agendadas que ainda não aconteceram, da mais próxima para a mais distante
    fun proximas(consultas: List<Consulta>, hoje: Dia, horaAgora: String): List<Consulta> =
        consultas
            .filter { it.status == STATUS_AGENDADA }
            .filter { it.dia > hoje || (it.dia == hoje && it.hora >= horaAgora) }
            .sortedWith(compareBy({ it.dia }, { it.hora }))

    // Só dá para cancelar o que ainda está agendado
    fun podeCancelar(consulta: Consulta): Boolean = consulta.status == STATUS_AGENDADA

    // Profissionais com vínculo ativo, sem repetir (vale o vínculo mais recente de cada um), em ordem alfabética
    fun profissionaisVinculados(
        vinculos: List<VinculoDoPaciente>,
        nomes: Map<Int, String>,
        especialidades: Map<Int, List<String>> = emptyMap()
    ): List<ProfissionalVinculado> =
        vinculos
            .filter { vinculoAtivo(it.status) && nomes.containsKey(it.idProfissional) }
            .sortedByDescending { it.id }
            .distinctBy { it.idProfissional }
            .map {
                ProfissionalVinculado(it.id, it.idProfissional, nomes.getValue(it.idProfissional), especialidades[it.idProfissional].orEmpty())
            }
            .sortedBy { it.nome.lowercase() }

    // Sugestões do campo "Profissional": quem tem o texto digitado no nome (sem diferenciar maiúsculas)
    fun sugestoes(profissionais: List<ProfissionalVinculado>, texto: String): List<ProfissionalVinculado> {
        val busca = texto.trim().lowercase()
        return if (busca.isEmpty()) profissionais else profissionais.filter { it.nome.lowercase().contains(busca) }
    }

    // Erros de cada campo do formulário "Nova consulta" (null = campo ok)
    data class ErrosDaConsulta(
        val profissional: String? = null,
        val data: String? = null,
        val hora: String? = null
    ) {
        val temErro: Boolean get() = profissional != null || data != null || hora != null
    }

    fun validarNovaConsulta(
        profissional: ProfissionalVinculado?,
        haProfissionais: Boolean,
        dia: Dia?,
        hora: String?,
        hoje: Dia,
        horaAgora: String,
        horarios: HorariosDoProfissional? = null // dias/horários que o profissional atende (null = não carregou)
    ): ErrosDaConsulta {
        val erroProfissional = when {
            !haProfissionais -> "Você ainda não tem um profissional vinculado."
            profissional == null -> "Escolha um dos seus profissionais vinculados."
            else -> null
        }
        val erroData = when {
            dia == null -> "Informe a data."
            dia < hoje -> "A data não pode estar no passado."
            horarios != null && dia !in horarios.diasLivres -> "O profissional não atende nesta data."
            else -> null
        }
        val erroHora = when {
            hora == null -> "Informe o horário."
            dia != null && dia == hoje && hora <= horaAgora -> "Esse horário já passou."
            horarios != null && dia != null && hora !in horarios.horariosDo(dia) -> "Esse horário não está disponível."
            else -> null
        }
        return ErrosDaConsulta(erroProfissional, erroData, erroHora)
    }
}
