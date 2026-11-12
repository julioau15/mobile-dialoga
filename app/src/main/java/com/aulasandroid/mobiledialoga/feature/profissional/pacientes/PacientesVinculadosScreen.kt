package com.aulasandroid.mobiledialoga.feature.profissional.pacientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.perfil.components.AvisoPerfil
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalUiState
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.TarefasProfissionalViewModel
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
import com.aulasandroid.mobiledialoga.feature.vinculos.components.mesAnoCurto
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlinx.coroutines.delay

private sealed interface Dialogo {
    object Adicionar : Dialogo
    class NotaRapida(val paciente: PacienteVinculadoUi) : Dialogo
    object NovaNota : Dialogo
    class EditarNota(val nota: AnotacaoUi) : Dialogo
    class ExcluirNota(val nota: AnotacaoUi) : Dialogo
    object SolicitarAcesso : Dialogo
}

@Composable
fun PacientesVinculadosScreen(
    modifier: Modifier = Modifier,
    viewModel: PacientesProfissionalViewModel = viewModel(),
    // Mesmo ViewModel da tela de Tarefas: o histórico de tarefas do paciente vem dos dados já carregados lá
    tarefasViewModel: TarefasProfissionalViewModel = viewModel()
) {
    val authState = useAuth()
    val token = authState.auth?.token
    val estado = viewModel.state
    val tarefas by tarefasViewModel.uiState.observeAsState(TarefasProfissionalUiState())

    var abertoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var aba by rememberSaveable { mutableStateOf(0) }
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            viewModel.carregar(token)
            tarefasViewModel.carregarDados(token)
        }
    }

    // O aviso some sozinho depois de 3 segundos
    LaunchedEffect(estado.toast) {
        if (estado.toast != null) {
            delay(3000)
            viewModel.limparToast()
        }
    }

    BackHandler(enabled = abertoId != null) {
        abertoId = null
        aba = 0
    }

    val aberto = abertoId?.let { id -> estado.pacientes.firstOrNull { it.id == id } }

    Box(modifier = modifier.fillMaxSize()) {
        if (aberto == null) {
            ListaPacientes(
                estado = estado,
                onNovoPaciente = { dialogo = Dialogo.Adicionar },
                onAbrir = { p ->
                    abertoId = p.id
                    aba = 0
                    token?.let { viewModel.carregarAnotacoes(it, p.id) }
                },
                onAnotar = { p -> dialogo = Dialogo.NotaRapida(p) },
                onTentarNovamente = { token?.let { viewModel.carregar(it) } }
            )
        } else {
            PacienteDetalhe(
                paciente = aberto,
                estado = estado,
                tarefas = tarefas,
                aba = aba,
                onAba = { aba = it },
                onVoltar = {
                    abertoId = null
                    aba = 0
                },
                onNovaNota = { dialogo = Dialogo.NovaNota },
                onEditarNota = { dialogo = Dialogo.EditarNota(it) },
                onExcluirNota = { dialogo = Dialogo.ExcluirNota(it) },
                onRecarregarNotas = { token?.let { viewModel.carregarAnotacoes(it, aberto.id) } },
                onSolicitarAcesso = { dialogo = Dialogo.SolicitarAcesso }
            )
        }

        AvisoPerfil(
            mensagem = estado.toast,
            sucesso = estado.toastSucesso,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
        )
    }

    // ---- Modais ----
    val fechar = { dialogo = null }

    when (val d = dialogo) {
        Dialogo.Adicionar -> ModalNovoPaciente(
            estado = estado,
            onBuscar = { termo -> token?.let { viewModel.buscarParaAdicionar(it, termo) } },
            onFechar = {
                viewModel.limparBusca()
                fechar()
            },
            onAdicionar = { id -> token?.let { viewModel.adicionarPaciente(it, id) { fechar() } } }
        )

        is Dialogo.NotaRapida -> ModalAnotacao(
            titulo = "Anotar — ${d.paciente.nome}",
            salvando = estado.salvando,
            onFechar = fechar,
            onSalvar = { t, c -> token?.let { viewModel.salvarAnotacao(it, d.paciente.id, null, t, c) { fechar() } } }
        )

        Dialogo.NovaNota -> aberto?.let { p ->
            ModalAnotacao(
                titulo = "Nova anotação",
                salvando = estado.salvando,
                onFechar = fechar,
                onSalvar = { t, c -> token?.let { viewModel.salvarAnotacao(it, p.id, null, t, c) { fechar() } } }
            )
        }

        is Dialogo.EditarNota -> aberto?.let { p ->
            ModalAnotacao(
                titulo = "Editar anotação",
                tituloInicial = d.nota.titulo,
                conteudoInicial = d.nota.conteudo,
                salvando = estado.salvando,
                onFechar = fechar,
                onSalvar = { t, c -> token?.let { viewModel.salvarAnotacao(it, p.id, d.nota.id, t, c) { fechar() } } }
            )
        }

        is Dialogo.ExcluirNota -> aberto?.let { p ->
            ModalDialoga(
                titulo = "Excluir anotação",
                onFechar = fechar,
                bloqueado = estado.salvando,
                rodape = {
                    BotaoModalSecundario("Cancelar", onClick = fechar, habilitado = !estado.salvando)
                    BotaoModalPrincipal(
                        texto = "Excluir",
                        perigo = true,
                        habilitado = !estado.salvando,
                        onClick = { token?.let { viewModel.excluirAnotacao(it, p.id, d.nota.id) { fechar() } } }
                    )
                }
            ) {
                Text("Esta anotação será removida permanentemente.", color = CorSecundaria, fontSize = 14.sp)
            }
        }

        Dialogo.SolicitarAcesso -> aberto?.let { p ->
            ModalDialoga(
                titulo = "Solicitar acesso aos indicadores",
                onFechar = fechar,
                bloqueado = estado.salvando,
                rodape = {
                    BotaoModalSecundario("Cancelar", onClick = fechar, habilitado = !estado.salvando)
                    BotaoModalPrincipal(
                        texto = if (estado.salvando) "Enviando…" else "Enviar solicitação",
                        habilitado = !estado.salvando,
                        onClick = { token?.let { viewModel.solicitarAcesso(it, p.id) { fechar() } } }
                    )
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Será enviada uma notificação para ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = CorTexto)) { append(p.nome) }
                        append(" solicitando autorização para visualizar seus indicadores de humor, sono e aderência ao tratamento.")
                    },
                    color = CorSecundaria,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        null -> Unit
    }
}

// ---------------------------------------------------------------------------
// Lista
// ---------------------------------------------------------------------------

@Composable
private fun ListaPacientes(
    estado: PacientesProfissionalState,
    onNovoPaciente: () -> Unit,
    onAbrir: (PacienteVinculadoUi) -> Unit,
    onAnotar: (PacienteVinculadoUi) -> Unit,
    onTentarNovamente: () -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf(0) } // 0 = Todos, 1 = Ativos, 2 = Inativos

    val filtrados = estado.pacientes.filter { p ->
        val bateBusca = busca.isBlank() || p.nome.contains(busca.trim(), ignoreCase = true)
        val bateFiltro = when (filtro) {
            1 -> p.ativo
            2 -> !p.ativo
            else -> true
        }
        bateBusca && bateFiltro
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Pacientes", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
                EspacoV(4.dp)
                val n = estado.pacientesAtivos
                Text(
                    text = if (n == 1) "1 paciente ativo" else "$n pacientes ativos",
                    fontSize = 14.sp,
                    color = CorSecundaria
                )
            }
            Button(
                onClick = onNovoPaciente,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                EspacoH(6.dp)
                Text("Novo paciente", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Busca + filtros
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoBusca(
                valor = busca,
                onValorChange = { busca = it },
                placeholder = "Buscar paciente…",
                modifier = Modifier.weight(1f)
            )
            listOf("Todos", "Ativos", "Inativos").forEachIndexed { i, rotulo ->
                FiltroChip(rotulo, selecionado = filtro == i) { filtro = i }
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
                    TextButton(onClick = onTentarNovamente) {
                        Text("Tentar novamente", color = CorPrimaria, fontWeight = FontWeight.Bold)
                    }
                }
            }

            filtrados.isEmpty() -> Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CorBranco),
                border = BorderStroke(1.dp, CorBordaCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (estado.pacientes.isEmpty())
                        "Você ainda não tem pacientes. Toque em \"Novo paciente\" para começar."
                    else "Nenhum paciente encontrado.",
                    color = CorSecundaria,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }

            else -> filtrados.forEach { p ->
                CartaoPaciente(p, onAbrir = { onAbrir(p) }, onAnotar = { onAnotar(p) })
            }
        }

        EspacoV(24.dp)
    }
}

@Composable
private fun FiltroChip(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    val formato = RoundedCornerShape(10.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 58.dp, height = 40.dp)
            .clip(formato)
            .background(if (selecionado) CorPrimaria else CorBranco)
            .border(1.dp, if (selecionado) CorPrimaria else CorBorda, formato)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = rotulo,
            color = if (selecionado) CorBranco else CorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun CartaoPaciente(p: PacienteVinculadoUi, onAbrir: () -> Unit, onAnotar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp)
        ) {
            AvatarIniciais(p.nome, tamanho = 44.dp)
            EspacoH(12.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = p.nome,
                    color = CorTexto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                p.desdeIso.mesAnoCurto()?.let {
                    EspacoV(2.dp)
                    Text("Desde $it", color = CorSecundaria, fontSize = 12.sp)
                }
            }
            EspacoH(8.dp)
            StatusPaciente(p)
            IconButton(onClick = onAnotar) {
                Icon(Icons.Outlined.Description, contentDescription = "Anotar", tint = CorSecundaria, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun StatusPaciente(p: PacienteVinculadoUi) {
    when {
        p.aguardandoAceite -> ChipStatus("Aguardando aceite", CorChipAmareloFundo, CorChipAmareloTexto)
        p.ativo -> ChipStatus("Ativo", CorChipVerdeFundo, CorChipVerdeTexto)
        else -> ChipStatus("Inativo", CorChipCinzaFundo, CorSecundaria)
    }
}

// ---------------------------------------------------------------------------
// Modal "Adicionar paciente": busca na plataforma e escolhe quem adicionar
// ---------------------------------------------------------------------------

@Composable
private fun ModalNovoPaciente(
    estado: PacientesProfissionalState,
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
        titulo = "Adicionar paciente",
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
        Text("Buscar paciente cadastrado na plataforma", color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        EspacoV(8.dp)
        CampoBusca(
            valor = termo,
            onValorChange = {
                termo = it
                selecionado = null
            },
            placeholder = "Nome ou e-mail do paciente",
            modifier = Modifier.fillMaxWidth()
        )
        EspacoV(4.dp)
        Text(
            "O paciente receberá uma solicitação e o acompanhamento começa quando ele aceitar.",
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
                Text("Nenhum paciente encontrado.", color = CorSecundaria, fontSize = 13.sp)

            else -> estado.resultadosBusca.forEachIndexed { i, r ->
                if (i > 0) HorizontalDivider(color = CorLinhaSuave)
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
                        if (!r.email.isNullOrBlank()) Text(r.email, color = CorSecundaria, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Modal de anotação (nova, rápida da lista, ou edição)
// ---------------------------------------------------------------------------

@Composable
internal fun ModalAnotacao(
    titulo: String,
    salvando: Boolean,
    onSalvar: (String, String) -> Unit,
    onFechar: () -> Unit,
    tituloInicial: String = "",
    conteudoInicial: String = ""
) {
    var t by remember { mutableStateOf(tituloInicial) }
    var c by remember { mutableStateOf(conteudoInicial) }
    var tentou by remember { mutableStateOf(false) }

    val erroTitulo = if (tentou && t.isBlank()) "Informe um título" else null
    val erroConteudo = if (tentou && c.isBlank()) "Escreva a anotação" else null

    ModalDialoga(
        titulo = titulo,
        onFechar = onFechar,
        bloqueado = salvando,
        rodape = {
            BotaoModalSecundario("Cancelar", onClick = onFechar, habilitado = !salvando)
            BotaoModalPrincipal(
                texto = if (salvando) "Salvando…" else "Salvar nota",
                habilitado = !salvando,
                onClick = {
                    tentou = true
                    if (t.isNotBlank() && c.isNotBlank()) onSalvar(t, c)
                }
            )
        }
    ) {
        CampoModal("Título", t, { t = it.take(200) }, "Título da anotação", erro = erroTitulo)
        EspacoV(14.dp)
        CampoModal("Conteúdo", c, { c = it.take(1000) }, "Escreva sua anotação…", minLinhas = 4, erro = erroConteudo)
        EspacoV(4.dp)
        Text("${c.length}/1000", color = CorSecundaria, fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun CampoModal(
    rotulo: String,
    valor: String,
    onChange: (String) -> Unit,
    placeholder: String,
    minLinhas: Int = 1,
    erro: String? = null
) {
    Text(rotulo, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    EspacoV(6.dp)
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        singleLine = minLinhas == 1,
        minLines = minLinhas,
        isError = erro != null,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        shape = RoundedCornerShape(10.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CorPrimaria,
            unfocusedBorderColor = CorBorda,
            errorBorderColor = CorErro,
            focusedContainerColor = CorBranco,
            unfocusedContainerColor = CorBranco,
            errorContainerColor = CorBranco,
            focusedTextColor = CorTexto,
            unfocusedTextColor = CorTexto,
            cursorColor = CorPrimaria,
            focusedPlaceholderColor = CorSecundaria,
            unfocusedPlaceholderColor = CorSecundaria
        ),
        modifier = Modifier.fillMaxWidth()
    )
    if (erro != null) Text(erro, color = CorErro, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
}
