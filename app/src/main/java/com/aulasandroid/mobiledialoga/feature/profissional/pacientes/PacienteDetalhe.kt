package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalUiState
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.StatusDestinatario
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.TarefaDestinatarioProgressoDto
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.formatarDataBr
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.paraIso
import com.aulasandroid.mobiledialoga.feature.vinculos.components.AvatarIniciais
import com.aulasandroid.mobiledialoga.feature.vinculos.components.ChipStatus
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloTexto
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipCinzaFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeTexto
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorLinhaSuave
import com.aulasandroid.mobiledialoga.feature.vinculos.components.EspacoH
import com.aulasandroid.mobiledialoga.feature.vinculos.components.EspacoV
import com.aulasandroid.mobiledialoga.feature.vinculos.components.dataExtenso
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mesAnoExtenso
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

private val CorFundoAbas = Color(0xFFE9E3F4)
private val CorAzulFundo = Color(0xFFDDEAFE)
private val CorAzulTexto = Color(0xFF2563EB)
private val CorVermelhoFundo = Color(0xFFFEE2E2)
private val CorVermelhoTexto = Color(0xFFDC2626)

@Composable
internal fun PacienteDetalhe(
    paciente: PacienteVinculadoUi,
    estado: PacientesProfissionalState,
    tarefas: TarefasProfissionalUiState,
    aba: Int,
    onAba: (Int) -> Unit,
    onVoltar: () -> Unit,
    onNovaNota: () -> Unit,
    onEditarNota: (AnotacaoUi) -> Unit,
    onExcluirNota: (AnotacaoUi) -> Unit,
    onRecarregarNotas: () -> Unit,
    onSolicitarAcesso: () -> Unit
) {
    val acompanhamentos = tarefas.acompanhamentos.filter { it.idPaciente == paciente.id }
    val concluidas = acompanhamentos.count { it.status() == StatusDestinatario.CONCLUIDA }
    val taxa = if (acompanhamentos.isEmpty()) 0 else (concluidas * 100f / acompanhamentos.size).toInt()

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Voltar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onVoltar).padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = CorSecundaria, modifier = Modifier.size(18.dp))
            EspacoH(8.dp)
            Text("Voltar à lista", color = CorSecundaria, fontSize = 14.sp)
        }

        CabecalhoPaciente(paciente, estado, onSolicitarAcesso)

        // Indicadores rápidos
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CartaoNumero(estado.sessoes(paciente.id).toString(), "Sessões", Modifier.weight(1f))
            CartaoNumero(acompanhamentos.size.toString(), "Tarefas enviadas", Modifier.weight(1f))
            CartaoNumero("$taxa%", "Taxa de resposta", Modifier.weight(1f))
        }

        Abas(aba, onAba)

        when (aba) {
            0 -> AbaAnotacoes(estado, onNovaNota, onEditarNota, onExcluirNota, onRecarregarNotas)
            1 -> AbaIndicadores(paciente, estado, onSolicitarAcesso)
            else -> AbaHistoricoTarefas(acompanhamentos, tarefas)
        }

        EspacoV(24.dp)
    }
}

// ---------------------------------------------------------------------------
// Cabeçalho, números e abas
// ---------------------------------------------------------------------------

@Composable
private fun CabecalhoPaciente(
    paciente: PacienteVinculadoUi,
    estado: PacientesProfissionalState,
    onSolicitarAcesso: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AvatarIniciais(paciente.nome, tamanho = 56.dp)
                EspacoH(14.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(paciente.nome, color = CorTexto, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    EspacoV(6.dp)
                    StatusPaciente(paciente)
                    paciente.email?.let {
                        EspacoV(8.dp)
                        Text(it, color = CorSecundaria, fontSize = 13.sp)
                    }
                    paciente.celular?.let {
                        EspacoV(2.dp)
                        Text(formatarCelular(it), color = CorSecundaria, fontSize = 13.sp)
                    }
                    paciente.desdeIso.mesAnoExtenso()?.let {
                        EspacoV(6.dp)
                        Text("Desde $it", color = CorSecundaria, fontSize = 12.sp)
                    }
                    estado.proximaConsulta(paciente.id)?.let {
                        EspacoV(8.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, null, tint = CorPrimaria, modifier = Modifier.size(13.dp))
                            EspacoH(5.dp)
                            Text("Próx. ${it.formatarDataBr()}", color = CorPrimaria, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            EspacoV(14.dp)
            if (estado.acessoSolicitado.contains(paciente.id)) {
                ChipStatus("Solicitação de acesso enviada", CorChipAmareloFundo, CorChipAmareloTexto)
            } else {
                OutlinedButton(
                    onClick = onSolicitarAcesso,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CorLinhaSuave),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF3EEFA), contentColor = CorPrimaria)
                ) {
                    Text("Solicitar acesso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CartaoNumero(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp)) {
            Text(valor, color = CorPrimaria, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(rotulo, color = CorSecundaria, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun Abas(selecionada: Int, onSelecionar: (Int) -> Unit) {
    val rotulos = listOf("\uD83D\uDCDD Anotações", "\uD83D\uDCCA Indicadores", "\u2705 Histórico de tarefas")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CorFundoAbas)
            .padding(4.dp)
    ) {
        rotulos.forEachIndexed { i, r ->
            val ativa = i == selecionada
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (ativa) CorBranco else Color.Transparent)
                    .clickable { onSelecionar(i) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = r,
                    color = if (ativa) CorPrimaria else CorSecundaria,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Aba: Anotações privadas
// ---------------------------------------------------------------------------

@Composable
private fun AbaAnotacoes(
    estado: PacientesProfissionalState,
    onNovaNota: () -> Unit,
    onEditarNota: (AnotacaoUi) -> Unit,
    onExcluirNota: (AnotacaoUi) -> Unit,
    onRecarregar: () -> Unit
) {
    CartaoAba {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Outlined.Description, null, tint = CorPrimaria, modifier = Modifier.size(18.dp))
            EspacoH(8.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Anotações privadas", color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Visíveis apenas para você", color = CorSecundaria, fontSize = 11.sp)
            }
            if (estado.anotacoes.isNotEmpty()) {
                TextButton(onClick = onNovaNota) {
                    Icon(Icons.Default.Add, null, tint = CorPrimaria, modifier = Modifier.size(16.dp))
                    EspacoH(4.dp)
                    Text("Nova nota", color = CorPrimaria, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        HorizontalDivider(color = CorLinhaSuave)

        when {
            estado.carregandoAnotacoes -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(28.dp)) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }

            estado.erroAnotacoes != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Text(estado.erroAnotacoes, color = CorErro, fontSize = 13.sp, textAlign = TextAlign.Center)
                TextButton(onClick = onRecarregar) { Text("Tentar novamente", color = CorPrimaria, fontWeight = FontWeight.Bold) }
            }

            estado.anotacoes.isEmpty() -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                Text("\uD83D\uDCDD", fontSize = 30.sp)
                EspacoV(8.dp)
                Text("Sem anotações", color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Registre observações clínicas sobre este paciente.",
                    color = CorSecundaria,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                EspacoV(14.dp)
                Button(
                    onClick = onNovaNota,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco)
                ) { Text("Nova nota", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }

            else -> estado.anotacoes.forEachIndexed { i, n ->
                if (i > 0) HorizontalDivider(color = CorLinhaSuave)
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(n.titulo, color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            n.criadaIso.dataExtenso()?.let { Text(it, color = CorSecundaria, fontSize = 12.sp) }
                        }
                        IconButton(onClick = { onEditarNota(n) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Edit, "Editar", tint = CorSecundaria, modifier = Modifier.size(17.dp))
                        }
                        IconButton(onClick = { onExcluirNota(n) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Delete, "Excluir", tint = CorSecundaria, modifier = Modifier.size(17.dp))
                        }
                    }
                    EspacoV(8.dp)
                    Text(n.conteudo, color = CorTexto, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Aba: Indicadores (precisa do acesso autorizado pelo paciente)
// ---------------------------------------------------------------------------

@Composable
private fun AbaIndicadores(
    paciente: PacienteVinculadoUi,
    estado: PacientesProfissionalState,
    onSolicitarAcesso: () -> Unit
) {
    CartaoAba {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("\uD83D\uDCCA", fontSize = 30.sp)
            EspacoV(8.dp)
            Text("Indicadores do paciente", color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            EspacoV(4.dp)
            Text(
                "Humor, sono e aderência ao tratamento aparecem aqui depois que ${paciente.nome} autorizar o acesso. " +
                    "Esta área ainda será liberada nesta versão do app.",
                color = CorSecundaria,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            EspacoV(14.dp)
            if (estado.acessoSolicitado.contains(paciente.id)) {
                ChipStatus("Solicitação de acesso enviada", CorChipAmareloFundo, CorChipAmareloTexto)
            } else {
                Button(
                    onClick = onSolicitarAcesso,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco)
                ) { Text("Solicitar acesso", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Aba: Histórico de tarefas
// ---------------------------------------------------------------------------

@Composable
private fun AbaHistoricoTarefas(
    acompanhamentos: List<TarefaDestinatarioProgressoDto>,
    tarefas: TarefasProfissionalUiState
) {
    CartaoAba {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "Tarefas enviadas ao paciente",
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text("${acompanhamentos.size} total", color = CorSecundaria, fontSize = 12.sp)
        }
        HorizontalDivider(color = CorLinhaSuave)

        if (acompanhamentos.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                if (tarefas.isLoading) {
                    CircularProgressIndicator(color = CorPrimaria, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text("Nenhuma tarefa enviada a este paciente.", color = CorSecundaria, fontSize = 13.sp)
                }
            }
        } else {
            val ordenadas = acompanhamentos.sortedByDescending { d ->
                tarefas.tarefas.firstOrNull { it.id == d.idTarefa }?.dataEnvio.paraIso().orEmpty()
            }
            ordenadas.forEachIndexed { i, d ->
                if (i > 0) HorizontalDivider(color = CorLinhaSuave)
                val envio = tarefas.tarefas.firstOrNull { it.id == d.idTarefa }?.dataEnvio.paraIso()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(d.tituloTarefa, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (envio != null) Text("Enviada em ${envio.formatarDataBr()}", color = CorSecundaria, fontSize = 12.sp)
                        d.prazo.paraIso()?.let { Text("Prazo: ${it.formatarDataBr()}", color = CorSecundaria, fontSize = 12.sp) }
                    }
                    EspacoH(8.dp)
                    when (d.status()) {
                        StatusDestinatario.CONCLUIDA -> ChipStatus("Respondida", CorChipVerdeFundo, CorChipVerdeTexto)
                        StatusDestinatario.EM_ANDAMENTO -> ChipStatus("Em andamento", CorAzulFundo, CorAzulTexto)
                        StatusDestinatario.ATRASADA -> ChipStatus("Em atraso", CorVermelhoFundo, CorVermelhoTexto)
                        StatusDestinatario.PENDENTE -> ChipStatus("Não preenchido", CorChipCinzaFundo, CorSecundaria)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun CartaoAba(conteudo: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { conteudo() }
    }
}

/** 11987654321 -> (11) 98765-4321 */
private fun formatarCelular(bruto: String): String {
    val d = bruto.filter { it.isDigit() }
    return when (d.length) {
        11 -> "(${d.substring(0, 2)}) ${d.substring(2, 7)}-${d.substring(7)}"
        10 -> "(${d.substring(0, 2)}) ${d.substring(2, 6)}-${d.substring(6)}"
        else -> bruto
    }
}
