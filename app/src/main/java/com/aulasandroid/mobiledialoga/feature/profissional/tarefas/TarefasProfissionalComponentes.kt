package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimariaClara
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

internal val FundoCampoSuave = Color(0xFFF8F7FB)
internal val BordaTracejada = Color(0xFFD5C8EA)
internal val DivisorModal = Color(0xFFEFEAF6)

// ---------- Estrutura ----------

@Composable
internal fun TituloTela(titulo: String, subtitulo: String) {
    Text(text = titulo, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto, lineHeight = 30.sp)
    Text(text = subtitulo, fontSize = 14.sp, color = CorSecundaria, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
internal fun LinkVoltar(texto: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = CorSecundaria, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = texto, fontSize = 14.sp, color = CorSecundaria, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun CartaoBranco(modifier: Modifier = Modifier, padding: Dp = 20.dp, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

@Composable
internal fun Selo(texto: String, fundo: Color, cor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cor, maxLines = 1, softWrap = false)
    }
}

/** Cores dos selos de status (iguais nas duas telas). */
internal fun coresSelo(rotulo: String): Pair<Color, Color> = when (rotulo) {
    "Respondida" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
    "Em andamento" -> Color(0xFFE0F2FE) to Color(0xFF1D4ED8)
    "Em atraso" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    "Não preenchido" -> Color(0xFFF1F0F4) to Color(0xFF6B6577)
    "Rascunho" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    else -> Color(0xFFEDE6F7) to CorPrimaria // Pendente
}

@Composable
internal fun SeloStatus(rotulo: String) {
    val (fundo, cor) = coresSelo(rotulo)
    Selo(rotulo, fundo, cor)
}

// ---------- Campos e botões ----------

@Composable
internal fun RotuloForm(texto: String, obrigatorio: Boolean = false, opcional: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CorTexto)
        if (obrigatorio) Text(text = " *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
        if (opcional) Text(text = " (opcional)", fontSize = 14.sp, color = CorSecundaria)
    }
}

@Composable
internal fun BotaoPilula(
    texto: String,
    onClick: () -> Unit,
    primario: Boolean,
    modifier: Modifier = Modifier,
    icone: (@Composable () -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val formato = RoundedCornerShape(12.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(46.dp)
            .clip(formato)
            .background(if (primario) (if (enabled) CorPrimaria else Color(0xFFD9CDE8)) else Color.White)
            .then(if (primario) Modifier else Modifier.border(1.dp, CorBorda, formato))
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = if (primario) Color.White else CorPrimaria, strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                icone?.invoke()
                if (icone != null) Spacer(Modifier.width(8.dp))
                Text(
                    text = texto, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (primario) Color.White else CorTexto, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Campo de seleção com o mesmo visual dos campos de texto. */
@Composable
internal fun CampoSelecao(
    texto: String,
    placeholder: Boolean,
    opcoes: List<String>,
    onSelecionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    altura: Dp = 52.dp
) {
    var aberto by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(altura)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, CorBorda, RoundedCornerShape(12.dp))
                .clickable(enabled = opcoes.isNotEmpty()) { aberto = true }
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = texto, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = if (placeholder) CorSecundaria else CorTexto
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CorTexto, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }, containerColor = Color.White) {
            opcoes.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op, fontSize = 14.sp, color = CorTexto) },
                    onClick = { onSelecionar(op); aberto = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LinhaChips(opcoes: List<String>, selecionada: String, onSelecionar: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        opcoes.forEach { op ->
            val ativo = op == selecionada
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (ativo) CorPrimaria else CorPrimariaClara)
                    .border(1.dp, if (ativo) CorPrimaria else CorBorda, RoundedCornerShape(10.dp))
                    .clickable { onSelecionar(op) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = op, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (ativo) Color.White else CorSecundaria
                )
            }
        }
    }
}

@Composable
internal fun AbasPilula(abas: List<String>, selecionada: Int, onSelecionar: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE9E0F3))
            .padding(4.dp)
    ) {
        abas.forEachIndexed { i, nome ->
            val ativa = i == selecionada
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (ativa) Color.White else Color.Transparent)
                    .clickable { onSelecionar(i) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = nome, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (ativa) CorPrimaria else CorSecundaria
                )
            }
        }
    }
}

@Composable
internal fun Avatar(nome: String, cor: Color, tamanho: Dp = 44.dp) {
    val iniciais = nome.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        .let { p -> if (p.size >= 2) "${p.first().first()}${p.last().first()}" else p.firstOrNull()?.take(2).orEmpty() }
        .uppercase()
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(tamanho).clip(CircleShape).background(cor)) {
        Text(text = iniciais, color = Color.White, fontSize = (tamanho.value * 0.32f).sp, fontWeight = FontWeight.Bold)
    }
}

private val PaletaAvatar = listOf(
    Color(0xFF7B4FA6), Color(0xFF059669), Color(0xFFD97706), Color(0xFFDC2626), Color(0xFF2563EB), Color(0xFFDB2777)
)

internal fun corDoAvatar(chave: Int): Color = PaletaAvatar[Math.floorMod(chave, PaletaAvatar.size)]

@Composable
internal fun EstadoVazio(emoji: String, titulo: String, subtitulo: String, comCartao: Boolean) {
    val conteudo: @Composable ColumnScope.() -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = if (comCartao) 8.dp else 40.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (comCartao) Color.Transparent else Color(0xFFF3EDF9))
            ) { Text(text = emoji, fontSize = 30.sp) }
            Spacer(Modifier.height(12.dp))
            Text(text = titulo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitulo, fontSize = 13.sp, color = CorSecundaria, textAlign = TextAlign.Center)
        }
    }
    if (comCartao) CartaoBranco(content = conteudo) else Column(content = conteudo)
}

@Composable
internal fun CartaoErro(mensagem: String, onTentarNovamente: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(text = mensagem, color = Color(0xFFB91C1C), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        if (onTentarNovamente != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tentar novamente", color = CorPrimaria, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onTentarNovamente)
            )
        }
    }
}

@Composable
internal fun ToastSucesso(texto: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CorSucesso)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = texto, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

internal fun Modifier.bordaTracejada(cor: Color = BordaTracejada, raio: Dp = 12.dp): Modifier = this.drawBehind {
    drawRoundRect(
        color = cor,
        cornerRadius = CornerRadius(raio.toPx()),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f), 0f))
    )
}

// ---------- Ícones desenhados (não existem no Material "core") ----------

/** Pulso/atividade: ícone de "acompanhar respostas". */
@Composable
internal fun IconePulso(cor: Color = CorSecundaria, tamanho: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(tamanho)) {
        val w = size.width; val h = size.height
        val p = Path().apply {
            moveTo(w * 0.05f, h * 0.52f)
            lineTo(w * 0.28f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.18f)
            lineTo(w * 0.62f, h * 0.84f)
            lineTo(w * 0.74f, h * 0.52f)
            lineTo(w * 0.95f, h * 0.52f)
        }
        drawPath(p, cor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Duas folhas sobrepostas: duplicar pergunta. */
@Composable
internal fun IconeCopiar(cor: Color = CorSecundaria, tamanho: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(tamanho)) {
        val traco = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
        val raio = CornerRadius(2.5.dp.toPx())
        drawRoundRect(cor, topLeft = Offset(size.width * 0.32f, size.height * 0.32f), size = Size(size.width * 0.58f, size.height * 0.58f), cornerRadius = raio, style = traco)
        val p = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.68f)
            lineTo(size.width * 0.14f, size.height * 0.68f)
            lineTo(size.width * 0.14f, size.height * 0.14f)
            lineTo(size.width * 0.68f, size.height * 0.14f)
            lineTo(size.width * 0.68f, size.height * 0.22f)
        }
        drawPath(p, cor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
