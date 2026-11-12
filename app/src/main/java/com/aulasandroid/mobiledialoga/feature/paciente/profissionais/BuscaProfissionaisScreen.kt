package com.aulasandroid.mobiledialoga.feature.paciente.profissionais

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.perfil.components.AvisoPerfil
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.formatarDataBr
import com.aulasandroid.mobiledialoga.feature.vinculos.components.AvatarIniciais
import com.aulasandroid.mobiledialoga.feature.vinculos.components.BotaoModalPrincipal
import com.aulasandroid.mobiledialoga.feature.vinculos.components.BotaoModalSecundario
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CampoBusca
import com.aulasandroid.mobiledialoga.feature.vinculos.components.ChipStatus
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipAmareloTexto
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipCinzaFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeFundo
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorChipVerdeTexto
import com.aulasandroid.mobiledialoga.feature.vinculos.components.CorLinhaSuave
import com.aulasandroid.mobiledialoga.feature.vinculos.components.EspacoH
import com.aulasandroid.mobiledialoga.feature.vinculos.components.EspacoV
import com.aulasandroid.mobiledialoga.feature.vinculos.components.ModalDialoga
import com.aulasandroid.mobiledialoga.feature.vinculos.components.RotuloSecao
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mesAnoExtenso
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlinx.coroutines.delay

@Composable
fun BuscaProfissionaisScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfissionaisPacienteViewModel = viewModel()
) {
    val authState = useAuth()
    val token = authState.auth?.token
    val estado = viewModel.state

    var mostrarAdicionar by remember { mutableStateOf(false) }
    var profissionalParaRemover by remember { mutableStateOf<ProfissionalVinculadoUi?>(null) }

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) viewModel.carregar(token)
    }

    // O aviso some sozinho depois de 3 segundos
    LaunchedEffect(estado.toast) {
        if (estado.toast != null) {
            delay(3000)
            viewModel.limparToast()
        }
    }

    val atuais = estado.profissionais.filter { it.atual }
    val historico = estado.profissionais.filter { !it.atual }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Título + botão
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Profissionais", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
                    EspacoV(4.dp)
                    Text("Seus acompanhantes de saúde", fontSize = 14.sp, color = CorSecundaria, lineHeight = 18.sp)
                }
                Button(
                    onClick = { mostrarAdicionar = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    EspacoH(6.dp)
                    Text("Novo profissional", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            when {
                !estado.carregou && estado.erro == null -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                        CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
                    }
                }

                estado.erro != null && !estado.carregou -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text(estado.erro, color = CorErro, fontSize = 14.sp)
                        TextButton(onClick = { token?.let { viewModel.carregar(it) } }) {
                            Text("Tentar novamente", color = CorPrimaria, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    RotuloSecao("Acompanhamento atual")
                    if (atuais.isEmpty()) {
                        CartaoVazio("Você ainda não tem profissionais. Toque em \"Novo profissional\" para começar.")
                    } else {
                        atuais.forEach { p ->
                            CartaoProfissional(p, onRemover = { profissionalParaRemover = p })
                        }
                    }

                    if (historico.isNotEmpty()) {
                        EspacoV(4.dp)
                        RotuloSecao("Histórico")
                        historico.forEach { p -> CartaoProfissional(p, onRemover = null) }
                    }
                }
            }

            EspacoV(24.dp)
        }

        AvisoPerfil(
            mensagem = estado.toast,
            sucesso = estado.toastSucesso,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
        )
    }

    if (mostrarAdicionar) {
        ModalNovoProfissional(
            estado = estado,
            onBuscar = { termo -> token?.let { viewModel.buscar(it, termo) } },
            onFechar = {
                viewModel.limparBusca()
                mostrarAdicionar = false
            },
            onAdicionar = { id ->
                token?.let { viewModel.adicionar(it, id) { mostrarAdicionar = false } }
            }
        )
    }

    profissionalParaRemover?.let { p ->
        ModalDialoga(
            titulo = "Excluir profissional",
            onFechar = { profissionalParaRemover = null },
            bloqueado = estado.salvando,
            rodape = {
                BotaoModalSecundario("Cancelar", onClick = { profissionalParaRemover = null }, habilitado = !estado.salvando)
                BotaoModalPrincipal(
                    texto = "Excluir",
                    perigo = true,
                    habilitado = !estado.salvando,
                    onClick = {
                        token?.let { t -> viewModel.remover(t, p.idProfissional) { profissionalParaRemover = null } }
                    }
                )
            }
        ) {
            Text(
                text = "Tem certeza que deseja remover ${p.nome} do acompanhamento atual? Ele deixará de acompanhar você.",
                color = CorSecundaria,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cartões
// ---------------------------------------------------------------------------

@Composable
private fun CartaoVazio(texto: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = texto,
            color = CorSecundaria,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun CartaoProfissional(p: ProfissionalVinculadoUi, onRemover: (() -> Unit)?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            AvatarIniciais(p.nome, tamanho = 48.dp)
            EspacoH(14.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = p.nome,
                    color = CorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                EspacoV(4.dp)
                when (p.statusVinculo) {
                    1 -> ChipStatus("Ativo", CorChipVerdeFundo, CorChipVerdeTexto)
                    0 -> ChipStatus("Aguardando aceite", CorChipAmareloFundo, CorChipAmareloTexto)
                    else -> ChipStatus("Recusado", CorChipCinzaFundo, CorSecundaria)
                }
                if (!p.especialidades.isNullOrBlank()) {
                    EspacoV(6.dp)
                    Text(p.especialidades, color = CorSecundaria, fontSize = 14.sp)
                }
                p.desdeIso.mesAnoExtenso()?.let {
                    EspacoV(4.dp)
                    Text("Desde $it", color = CorSecundaria, fontSize = 12.sp)
                }
                if (p.proximaIso != null && p.statusVinculo == 1) {
                    EspacoV(8.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, null, tint = CorPrimaria, modifier = Modifier.size(13.dp))
                        EspacoH(5.dp)
                        Text(
                            "Próx. ${p.proximaIso.formatarDataBr()}",
                            color = CorPrimaria,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (onRemover != null) {
                IconButton(onClick = onRemover, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remover", tint = CorSecundaria, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Modal "Novo profissional": busca na plataforma e escolhe quem adicionar
// ---------------------------------------------------------------------------

@Composable
private fun ModalNovoProfissional(
    estado: ProfissionaisPacienteState,
    onBuscar: (String) -> Unit,
    onFechar: () -> Unit,
    onAdicionar: (Int) -> Unit
) {
    var termo by remember { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<Int?>(null) }

    // Espera a pessoa parar de digitar antes de consultar o back
    LaunchedEffect(termo) {
        delay(400)
        onBuscar(termo)
    }

    ModalDialoga(
        titulo = "Novo profissional",
        onFechar = onFechar,
        bloqueado = estado.salvando,
        rodape = {
            BotaoModalSecundario("Cancelar", onClick = onFechar, habilitado = !estado.salvando)
            BotaoModalPrincipal(
                texto = if (estado.salvando) "Enviando…" else "Adicionar",
                habilitado = selecionado != null && !estado.salvando,
                onClick = { selecionado?.let(onAdicionar) }
            )
        }
    ) {
        Text("Buscar profissional na plataforma", color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        EspacoV(8.dp)
        CampoBusca(
            valor = termo,
            onValorChange = {
                termo = it
                selecionado = null
            },
            placeholder = "Nome ou e-mail do profissional",
            modifier = Modifier.fillMaxWidth()
        )
        EspacoV(4.dp)
        Text(
            "Ele receberá uma solicitação e o acompanhamento começa quando aceitar.",
            color = CorSecundaria,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        EspacoV(12.dp)

        when {
            estado.buscando -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                CircularProgressIndicator(color = CorPrimaria, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            }

            termo.trim().length < 2 -> Unit

            estado.buscaFeita && estado.resultadosBusca.isEmpty() ->
                Text("Nenhum profissional encontrado.", color = CorSecundaria, fontSize = 13.sp)

            else -> estado.resultadosBusca.forEachIndexed { i, r ->
                if (i > 0) androidx.compose.material3.HorizontalDivider(color = CorLinhaSuave)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selecionado = r.id }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = selecionado == r.id,
                        onClick = { selecionado = r.id },
                        colors = RadioButtonDefaults.colors(selectedColor = CorPrimaria)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.nome, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        val detalhe = listOfNotNull(r.especialidades, r.crp?.let { "CRP $it" }).joinToString(" · ")
                        if (detalhe.isNotBlank()) Text(detalhe, color = CorSecundaria, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
