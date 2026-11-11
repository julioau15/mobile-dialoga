package com.aulasandroid.mobiledialoga.feature.paciente.consultas

// Campos do pop-up "Nova consulta"
data class NovaConsultaForm(
    val profissionalTexto: String = "",
    val profissional: ProfissionalVinculado? = null, // só vale quando o usuário escolhe um da lista
    val especialidade: String = "",
    val data: Dia? = null,
    val hora: String? = null,
    // O back ainda não guarda observações nem lembrete (a tabela de consultas não tem esses campos):
    // os dois ficam na tela como no protótipo, mas não são enviados.
    val observacoes: String = "",
    val lembrete: Boolean = true,
    val erroProfissional: String? = null,
    val erroData: String? = null,
    val erroHora: String? = null,
    val erroGeral: String? = null
)

// Aviso verde (ou vermelho, se [erro]) que aparece no canto da tela por alguns segundos
data class MensagemToast(
    val texto: String,
    val erro: Boolean = false,
    val id: Long = System.nanoTime() // dois avisos iguais seguidos contam como avisos diferentes
)

data class AgendaUiState(
    val carregando: Boolean = true,
    val erroCarregamento: String? = null,
    val consultas: List<Consulta> = emptyList(),
    val profissionais: List<ProfissionalVinculado> = emptyList(),
    val idsVinculos: Set<Int> = emptySet(),
    val visao: VisaoAgenda = VisaoAgenda.MES,
    val diaSelecionado: Dia = Dia.hoje(),
    val formulario: NovaConsultaForm? = null, // null = pop-up fechado
    val salvando: Boolean = false,
    val consultaParaCancelar: Consulta? = null, // null = pop-up fechado
    val cancelando: Boolean = false,
    val toast: MensagemToast? = null
)
