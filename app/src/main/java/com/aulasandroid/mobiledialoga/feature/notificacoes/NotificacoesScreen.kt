package com.aulasandroid.mobiledialoga.feature.notificacoes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorFundo
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlinx.coroutines.delay

private val CorAbaFundo = Color(0xFFEBE2F5)
private val CorBordaNaoLida = Color(0xFFD5C6EA)
private val CorTextoLida = Color(0xFF6A6080)
private val CorTrilhoOff = Color(0xFFE3DAEE)

/**
 * Tela de notificações (lista com abas + configurações), compartilhada por paciente e profissional.
 * O que muda entre os dois perfis é só o texto e os grupos de preferência.
 */
@Composable
fun NotificacoesScreen(
    perfil: PerfilNotificacao,
    onVer: (DestinoNotificacao) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificacoesViewModel = viewModel(key = perfil.name)
) {
    val authState = useAuth()
    val token = authState.auth?.token
    val estado by viewModel.uiState.observeAsState(NotificacoesUiState())

    LaunchedEffect(token) { viewModel.carregar(token) }

    // o aviso verde some sozinho depois de 3 segundos
    LaunchedEffect(estado.toast) {
        if (estado.toast != null) {
            delay(3000)
            viewModel.limparToast()
        }
    }

    BackHandler(enabled = estado.emConfig) { viewModel.fecharConfig() }

    Box(modifier = modifier.fillMaxSize().background(CorFundo)) {
        if (estado.emConfig) {
            ConfiguracoesNotificacoes(
                perfil = perfil,
                estado = estado,
                onVoltar = { viewModel.fecharConfig() },
                onAlterar = { chave, ativo -> viewModel.alterarPreferencia(chave, ativo) },
                onDesligarTodas = { viewModel.alternarDesligarTodas(it) },
                onSalvar = { viewModel.salvarPreferencias(perfil, token) }
            )
        } else {
            ListaNotificacoes(
                perfil = perfil,
                estado = estado,
                onAba = { viewModel.selecionarAba(it) },
                onMarcarTodas = { viewModel.marcarTodasComoLidas(token) },
                onAbrirConfig = { viewModel.abrirConfig(perfil, token) },
                onAbrirItem = { viewModel.marcarComoLida(it) },
                onVer = { item, destino ->
                    viewModel.marcarComoLida(item)
                    onVer(destino)
                },
                onTentarNovamente = { viewModel.carregar(token) }
            )
        }

        estado.toast?.let { msg ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .shadow(6.dp, RoundedCornerShape(10.dp))
                    .background(CorSucesso, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = CorBranco, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(msg, color = CorBranco, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// =================================================================================================
// Lista
// =================================================================================================

@Composable
private fun ListaNotificacoes(
    perfil: PerfilNotificacao,
    estado: NotificacoesUiState,
    onAba: (Boolean) -> Unit,
    onMarcarTodas: () -> Unit,
    onAbrirConfig: () -> Unit,
    onAbrirItem: (NotificacaoItem) -> Unit,
    onVer: (NotificacaoItem, DestinoNotificacao) -> Unit,
    onTentarNovamente: () -> Unit
) {
    val naoLidas = estado.naoLidas
    val lidas = estado.lidas
    val visiveis = if (estado.mostrandoHistorico) lidas else naoLidas

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))

        // Cabeçalho: título + contador | Marcar todas | engrenagem
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Notificações", color = CorTexto, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = when (naoLidas.size) {
                        0 -> "0 não lidas"
                        1 -> "1 não lida"
                        else -> "${naoLidas.size} não lidas"
                    },
                    color = CorSecundaria,
                    fontSize = 14.sp
                )
            }
            if (naoLidas.isNotEmpty()) {
                BotaoMarcarTodas(onClick = onMarcarTodas)
                Spacer(Modifier.width(8.dp))
            }
            BotaoEngrenagem(onClick = onAbrirConfig)
        }

        Spacer(Modifier.height(16.dp))
        AbasNotificacoes(
            historico = estado.mostrandoHistorico,
            naoLidas = naoLidas.size,
            lidas = lidas.size,
            onSelecionar = onAba
        )
        Spacer(Modifier.height(14.dp))

        when {
            estado.carregando && estado.notificacoes.isEmpty() -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) { CircularProgressIndicator(color = CorPrimaria) }

            estado.erro != null && estado.notificacoes.isEmpty() -> EstadoVazio(
                emoji = "⚠️",
                titulo = "Algo deu errado",
                texto = estado.erro,
                acao = "Tentar novamente",
                onAcao = onTentarNovamente
            )

            visiveis.isEmpty() -> if (estado.mostrandoHistorico) {
                EstadoVazio("📭", "Histórico vazio", "As notificações lidas aparecem aqui.")
            } else {
                EstadoVazio(
                    "🎉", "Tudo em dia!",
                    if (perfil == PerfilNotificacao.PACIENTE) "Você não tem notificações pendentes."
                    else "Nenhuma notificação pendente."
                )
            }

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visiveis, key = { it.id }) { item ->
                    CartaoNotificacao(
                        item = item,
                        perfil = perfil,
                        onClick = { onAbrirItem(item) },
                        onVer = { destino -> onVer(item, destino) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoMarcarTodas(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CorBranco)
            .border(1.dp, CorBordaNaoLida, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = CorPrimaria, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("Marcar todas", color = CorPrimaria, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BotaoEngrenagem(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CorBranco)
            .border(1.dp, CorBordaCard, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = "Configurações de notificações",
            tint = CorSecundaria,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AbasNotificacoes(
    historico: Boolean,
    naoLidas: Int,
    lidas: Int,
    onSelecionar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CorAbaFundo)
            .padding(3.dp)
    ) {
        Aba("Não lidas", naoLidas, selecionada = !historico, modifier = Modifier.weight(1f)) { onSelecionar(false) }
        Aba("Histórico", lidas, selecionada = historico, modifier = Modifier.weight(1f)) { onSelecionar(true) }
    }
}

@Composable
private fun Aba(
    titulo: String,
    contagem: Int,
    selecionada: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(10.dp)
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(if (selecionada) Modifier.shadow(2.dp, forma) else Modifier)
            .clip(forma)
            .background(if (selecionada) CorBranco else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp)
    ) {
        Text(
            titulo,
            color = if (selecionada) CorPrimaria else CorTextoLida,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (selecionada) Color(0xFFE4D6F3) else CorBranco)
                .padding(horizontal = 7.dp, vertical = 1.dp)
        ) {
            Text(
                contagem.toString(),
                color = if (selecionada) CorPrimaria else CorTextoLida,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CartaoNotificacao(
    item: NotificacaoItem,
    perfil: PerfilNotificacao,
    onClick: () -> Unit,
    onVer: (DestinoNotificacao) -> Unit
) {
    val visual = item.categoriaVisual()
    val forma = RoundedCornerShape(14.dp)
    val destino = item.destino()
    val naoLida = !item.lida

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (naoLida) 3.dp else 2.dp, forma)
            .clip(forma)
            .background(CorBranco)
            .border(if (naoLida) 1.5.dp else 1.dp, if (naoLida) CorBordaNaoLida else CorBordaCard, forma)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(visual.corIcone)
        ) { Text(visual.emoji, fontSize = 18.sp) }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.titulo(perfil),
                    color = if (naoLida) CorTexto else CorTextoLida,
                    fontSize = 15.sp,
                    fontWeight = if (naoLida) FontWeight.Bold else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (naoLida) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(9.dp).clip(CircleShape).background(CorPrimaria))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                item.rotuloCategoria(perfil),
                color = visual.corTextoChip,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(visual.corChip)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(item.mensagem, color = CorTextoLida, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatarDataNotificacao(item.dataCriacao),
                    color = CorTextoLida,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                if (destino != null) {
                    Text(
                        "Ver →",
                        color = CorPrimaria,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onVer(destino) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVazio(
    emoji: String,
    titulo: String,
    texto: String,
    acao: String? = null,
    onAcao: () -> Unit = {}
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma)
            .clip(forma)
            .background(CorBranco)
            .border(1.dp, CorBordaCard, forma)
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Text(emoji, fontSize = 34.sp)
        Spacer(Modifier.height(10.dp))
        Text(titulo, color = CorTexto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(texto, color = CorTextoLida, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (acao != null) {
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onAcao,
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria),
                shape = RoundedCornerShape(10.dp)
            ) { Text(acao, fontSize = 13.sp) }
        }
    }
}

// =================================================================================================
// Configurações
// =================================================================================================

@Composable
private fun ConfiguracoesNotificacoes(
    perfil: PerfilNotificacao,
    estado: NotificacoesUiState,
    onVoltar: () -> Unit,
    onAlterar: (String, Boolean) -> Unit,
    onDesligarTodas: (Boolean) -> Unit,
    onSalvar: () -> Unit
) {
    val grupos = remember(perfil) { gruposPreferencia(perfil) }
    // por padrão só o primeiro grupo vem aberto, como no protótipo
    var abertos by remember(perfil) { mutableStateOf(setOf(grupos.first().categoria)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Voltar",
            tint = CorSecundaria,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onVoltar)
                .padding(5.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Configurações de notificações",
            color = CorTexto,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
        )
        Text(
            if (perfil == PerfilNotificacao.PACIENTE) "Escolha quais alertas receber"
            else "Configure seus alertas profissionais",
            color = CorSecundaria,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(18.dp))

        // Desligar todas
        CartaoBranco {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = CorErro, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Desligar todas", color = CorTexto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Suspende todos os alertas e lembretes", color = CorTextoLida, fontSize = 12.sp)
                }
                InterruptorDialoga(checked = estado.desligarTodas, onChange = onDesligarTodas)
            }
        }

        if (estado.configCarregando) {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CorPrimaria, modifier = Modifier.size(28.dp))
            }
        }

        for (grupo in grupos) {
            Spacer(Modifier.height(12.dp))
            val ativas = grupo.itens.count { estado.prefs[chavePreferencia(grupo.categoria, it.tipo)] ?: it.padrao }
            val aberto = grupo.categoria in abertos

            CartaoBranco {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                abertos = if (aberto) abertos - grupo.categoria else abertos + grupo.categoria
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(grupo.emoji, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(grupo.titulo, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "($ativas/${grupo.itens.size} ativas)",
                            color = CorTextoLida,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (aberto) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CorSecundaria,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (aberto) {
                        for (item in grupo.itens) {
                            val chave = chavePreferencia(grupo.categoria, item.tipo)
                            HorizontalDivider(color = CorBordaCard)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(item.titulo, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(2.dp))
                                    Text(item.descricao, color = CorTextoLida, fontSize = 12.sp, lineHeight = 16.sp)
                                }
                                InterruptorDialoga(
                                    checked = estado.prefs[chave] ?: item.padrao,
                                    onChange = { onAlterar(chave, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        estado.configErro?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = CorErro, fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = onSalvar,
                enabled = !estado.configSalvando && !estado.configCarregando,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                if (estado.configSalvando) {
                    CircularProgressIndicator(color = CorBranco, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                } else {
                    Text("Salvar preferências", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun CartaoBranco(content: @Composable () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, forma)
            .clip(forma)
            .background(CorBranco)
            .border(BorderStroke(1.dp, CorBordaCard), forma)
    ) { content() }
}

@Composable
private fun InterruptorDialoga(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = CorBranco,
            checkedTrackColor = CorPrimaria,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = CorBranco,
            uncheckedTrackColor = CorTrilhoOff,
            uncheckedBorderColor = Color.Transparent
        )
    )
}
