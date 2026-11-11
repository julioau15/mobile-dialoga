package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.ProfissionalTarefaDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.formatarDataBr
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

/** Tela "Tarefas": abas Enviadas / Rascunhos. */
@Composable
internal fun ListaTarefasProfissional(
    estado: TarefasProfissionalUiState,
    onAcompanhamento: () -> Unit,
    onNovaTarefa: () -> Unit,
    onAba: (Int) -> Unit,
    onAcompanharTarefa: (ProfissionalTarefaDto) -> Unit,
    onAbrirTarefa: (ProfissionalTarefaDto) -> Unit,
    onExcluir: (ProfissionalTarefaDto) -> Unit,
    onRecarregar: () -> Unit
) {
    TituloTela("Tarefas", "Questionários enviados aos pacientes")

    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        BotaoPilula(
            texto = "Acompanhamento", onClick = onAcompanhamento, primario = false, modifier = Modifier.weight(1f),
            icone = { Icon(Icons.Default.Person, contentDescription = null, tint = CorPrimaria, modifier = Modifier.size(18.dp)) }
        )
        BotaoPilula(
            texto = "Nova tarefa", onClick = onNovaTarefa, primario = true, modifier = Modifier.weight(1f),
            icone = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        )
    }

    Spacer(Modifier.height(20.dp))
    AbasPilula(
        abas = listOf("Enviadas (${estado.enviadas.size})", "Rascunhos (${estado.rascunhos.size})"),
        selecionada = estado.abaLista,
        onSelecionar = onAba
    )
    Spacer(Modifier.height(20.dp))

    when {
        estado.isLoading && estado.tarefas.isEmpty() ->
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
            }

        estado.erroLista != null && estado.tarefas.isEmpty() -> CartaoErro(estado.erroLista, onRecarregar)

        else -> {
            if (estado.erroLista != null) {
                CartaoErro(estado.erroLista)
                Spacer(Modifier.height(12.dp))
            }
            val lista = if (estado.abaLista == 0) estado.enviadas else estado.rascunhos
            if (lista.isEmpty()) {
                if (estado.abaLista == 0) {
                    EstadoVazio("📋", "Nenhuma tarefa enviada", "Toque em \"Nova tarefa\" para enviar o primeiro questionário.", comCartao = false)
                } else {
                    EstadoVazio("📝", "Nenhum rascunho", "Salve tarefas como rascunho para enviar depois.", comCartao = false)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    lista.forEach { tarefa ->
                        CartaoTarefa(
                            tarefa = tarefa,
                            onAcompanhar = { onAcompanharTarefa(tarefa) },
                            onAbrir = { onAbrirTarefa(tarefa) },
                            onExcluir = { onExcluir(tarefa) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoTarefa(
    tarefa: ProfissionalTarefaDto,
    onAcompanhar: () -> Unit,
    onAbrir: () -> Unit,
    onExcluir: () -> Unit
) {
    val perguntas = if (tarefa.totalPerguntas == 1) "1 pergunta" else "${tarefa.totalPerguntas} perguntas"
    val detalhe = if (tarefa.isRascunho) perguntas else "$perguntas · Prazo: ${tarefa.prazoIso().formatarDataBr("sem prazo")}"

    CartaoBranco(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(modifier = Modifier.weight(1f).clickable(onClick = onAbrir)) {
                Text(
                    text = tarefa.titulo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(text = detalhe, fontSize = 12.sp, color = CorSecundaria, lineHeight = 16.sp)
            }

            SeloStatus(if (tarefa.isRascunho) "Rascunho" else tarefa.statusGeral().rotulo)

            if (!tarefa.isRascunho) {
                IconButton(onClick = onAcompanhar, modifier = Modifier.size(34.dp)) { IconePulso() }
            } else {
                IconButton(onClick = onAbrir, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar rascunho", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onExcluir, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir rascunho", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
