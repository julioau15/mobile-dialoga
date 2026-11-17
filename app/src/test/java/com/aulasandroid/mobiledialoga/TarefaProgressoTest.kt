package com.aulasandroid.mobiledialoga

import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.TarefasPacienteUiState
import com.aulasandroid.mobiledialoga.feature.paciente.tarefas.model.PerguntaDto
import org.junit.Assert.assertEquals
import org.junit.Test

class TarefaProgressoTest {
    private val perguntas = listOf(
        PerguntaDto(1, "Texto", "texto"),
        PerguntaDto(2, "Escolha", "unica_escolha"),
        PerguntaDto(3, "Múltipla", "multipla_escolha"),
        PerguntaDto(4, "Escala", "escala")
    )

    @Test fun progressoContaPerguntasSemDuplicarOpcoesEConsideraEscalaZero() {
        val estado = TarefasPacienteUiState(
            perguntas = perguntas,
            respostasMultiplasOpcoes = mapOf(3 to listOf(10, 11, 12)),
            respostasEscala = mapOf(4 to 0)
        )
        assertEquals(50, estado.progressoRespostas)
    }

    @Test fun apagarRespostaReduzProgresso() {
        val estado = TarefasPacienteUiState(
            perguntas = perguntas,
            respostasTexto = mapOf(1 to "Resposta"),
            respostasOpcaoUnica = mapOf(2 to 10)
        )
        assertEquals(50, estado.progressoRespostas)
        assertEquals(25, estado.copy(respostasTexto = mapOf(1 to "  ")).progressoRespostas)
    }

    @Test fun semPerguntasTemProgressoZero() {
        assertEquals(0, TarefasPacienteUiState().progressoRespostas)
    }
}
