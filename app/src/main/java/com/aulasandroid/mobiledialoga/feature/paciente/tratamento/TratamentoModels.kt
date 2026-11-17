package com.aulasandroid.mobiledialoga.feature.paciente.tratamento

/** Opções do campo "Frequência"; cada uma define quantos horários o formulário pede. */
enum class FrequenciaTratamento(val rotulo: String, val doses: Int) {
    UMA("Uma vez ao dia", 1),
    DUAS("Duas vezes ao dia", 2),
    TRES("Três vezes ao dia", 3),
    QUATRO("Quatro vezes ao dia", 4);

    companion object {
        /** Casa pelo texto salvo; se for um texto livre, usa a quantidade de horários. */
        fun deTexto(texto: String?, qtdHorarios: Int): FrequenciaTratamento =
            values().firstOrNull { it.rotulo.equals(texto?.trim(), ignoreCase = true) }
                ?: values().first { it.doses == qtdHorarios.coerceIn(1, 4) }
    }
}

data class TratamentoUi(
    val id: Int,
    val nome: String,
    val dosagem: String,
    val frequencia: String,
    val lembreteAtivo: Boolean,
    val horarios: List<String> // "HH:mm", em ordem
)

/** Dados do formulário (novo ou edição). horarios tem um item por dose; null = ainda não escolhido. */
data class FormTratamento(
    val nome: String = "",
    val dosagem: String = "",
    val frequencia: FrequenciaTratamento = FrequenciaTratamento.UMA,
    val horarios: List<String?> = listOf(null),
    val lembreteAtivo: Boolean = true
) {
    companion object {
        fun de(t: TratamentoUi): FormTratamento {
            val freq = FrequenciaTratamento.deTexto(t.frequencia, t.horarios.size)
            val horarios = List(freq.doses) { i -> t.horarios.getOrNull(i) }
            return FormTratamento(t.nome, t.dosagem, freq, horarios, t.lembreteAtivo)
        }
    }

    fun comFrequencia(nova: FrequenciaTratamento) =
        copy(frequencia = nova, horarios = List(nova.doses) { i -> horarios.getOrNull(i) })
}

data class TratamentosState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val tratamentos: List<TratamentoUi> = emptyList(),
    val salvando: Boolean = false,
    val excluindoId: Int? = null,
    val aviso: String? = null
)
