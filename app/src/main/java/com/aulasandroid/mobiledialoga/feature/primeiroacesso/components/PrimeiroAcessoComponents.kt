package com.aulasandroid.mobiledialoga.feature.primeiroacesso.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------- Cores do design (tela de primeiro acesso) ----------
internal val FundoLavanda = Color(0xFFF3EEFF)
internal val BordaSuave = Color(0xFFE3DBF0)
internal val SelecionadoFundo = Color(0xFFF1EAFB)
internal val TextoTitulo = Color(0xFF241B3A)
internal val TextoSuave = Color(0xFF4A4458)
internal val VerdeFundo = Color(0xFFEAF8EF)
internal val VerdeBorda = Color(0xFFB7E4C7)
internal val RosaFundo = Color(0xFFFDEDED)
internal val RosaBorda = Color(0xFFF5C2C2)
internal val CorErro = Color(0xFFDC2626)

/** Item retornado pela busca de profissionais. */
data class PessoaSugestao(
    val id: String,
    val nome: String,
    val detalhe: String
)

// ---------- Layout base ----------

@Composable
internal fun PrimeiroAcessoLayout(
    rotulos: List<String> = emptyList(),
    etapaAtual: Int = 1,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(FundoLavanda)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (rotulos.isEmpty()) Arrangement.Center else Arrangement.Top
        ) {
            if (rotulos.isNotEmpty()) {
                IndicadorDeEtapas(rotulos = rotulos, etapaAtual = etapaAtual)
                Spacer(modifier = Modifier.height(20.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, BordaSuave, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )

            if (rotulos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                PontosDeEtapa(total = rotulos.size, etapaAtual = etapaAtual)
            }
        }
    }
}

@Composable
internal fun IndicadorDeEtapas(rotulos: List<String>, etapaAtual: Int) {
    val primary = MaterialTheme.colorScheme.primary
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        rotulos.forEachIndexed { index, rotulo ->
            val numero = index + 1
            val ativa = numero <= etapaAtual
            val atual = numero == etapaAtual

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (atual) primary.copy(alpha = 0.15f) else Color.Transparent)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (ativa) primary else Color.White)
                            .border(1.dp, if (ativa) primary else BordaSuave, CircleShape)
                    ) {
                        if (numero < etapaAtual) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$numero",
                                color = if (ativa) Color.White else TextoSuave,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rotulo,
                    fontSize = 11.sp,
                    color = if (atual) primary else TextoSuave,
                    fontWeight = if (atual) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            if (index < rotulos.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .padding(top = 18.dp)
                        .height(2.dp)
                        .background(if (numero < etapaAtual) primary else BordaSuave)
                )
            }
        }
    }
}

@Composable
internal fun PontosDeEtapa(total: Int, etapaAtual: Int) {
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (numero in 1..total) {
            val atual = numero == etapaAtual
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (atual) 24.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (atual || numero < etapaAtual) primary else BordaSuave)
            )
        }
    }
}

// ---------- Cabeçalho ----------

@Composable
internal fun Cabecalho(
    @DrawableRes ilustracao: Int,
    titulo: String,
    descricao: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 160.dp, height = 120.dp)
            .clip(RoundedCornerShape(32.dp))
    ) {
        Image(
            painter = painterResource(ilustracao),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(90.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = titulo,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = TextoTitulo,
        lineHeight = 26.sp
    )
    if (descricao != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = descricao,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = TextoSuave,
            lineHeight = 20.sp
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
}

// ---------- Botões ----------

@Composable
internal fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primary,
            contentColor = Color.White
        ),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun BotaoTexto(texto: String, onClick: () -> Unit, destaque: Boolean = false) {
    TextButton(onClick = onClick) {
        Text(
            text = texto,
            fontSize = 14.sp,
            color = if (destaque) MaterialTheme.colorScheme.primary else TextoSuave,
            fontWeight = if (destaque) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Chips ----------

@Composable
internal fun OpcaoChip(
    texto: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val forma = RoundedCornerShape(10.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(forma)
            .background(if (selecionado) SelecionadoFundo else Color.White)
            .border(1.dp, if (selecionado) primary else BordaSuave, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selecionado) primary else TextoTitulo
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GrupoDeChips(
    opcoes: List<String>,
    selecionado: String?,
    onSelecionar: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcoes.forEach { opcao ->
            OpcaoChip(texto = opcao, selecionado = opcao == selecionado, onClick = { onSelecionar(opcao) })
        }
    }
}

@Composable
internal fun EscolhaBinaria(
    textoSim: String,
    textoNao: String,
    sim: Boolean?,
    onEscolher: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OpcaoChip(
            texto = textoSim,
            selecionado = sim == true,
            onClick = { onEscolher(true) },
            modifier = Modifier.weight(1f)
        )
        OpcaoChip(
            texto = textoNao,
            selecionado = sim == false,
            onClick = { onEscolher(false) },
            modifier = Modifier.weight(1f)
        )
    }
}

// ---------- Campos ----------

@Composable
internal fun RotuloCampo(texto: String, opcional: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoTitulo)
        if (opcional) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "(opcional)", fontSize = 12.sp, color = TextoSuave)
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
internal fun CampoDeTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    singleLine: Boolean = true,
    minLinhas: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    erro: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = (56 + (minLinhas - 1) * 22).dp),
            placeholder = { Text(text = placeholder, fontSize = 14.sp, color = TextoSuave) },
            leadingIcon = if (icone != null) {
                { Icon(imageVector = icone, contentDescription = null, tint = if (erro != null) CorErro else TextoSuave) }
            } else null,
            singleLine = singleLine,
            isError = erro != null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextoTitulo,
                unfocusedTextColor = TextoTitulo,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = BordaSuave,
                errorBorderColor = CorErro,
                focusedPlaceholderColor = TextoSuave,
                unfocusedPlaceholderColor = TextoSuave,
                errorLeadingIconColor = CorErro
            )
        )
        if (erro != null) {
            Text(
                text = erro,
                color = CorErro,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

internal class DataVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.take(8)
        val formatado = buildString {
            digitos.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('/')
                append(c)
            }
        }
        val mapa = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val t = when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    else -> offset + 2
                }
                return t.coerceAtMost(formatado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val o = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> offset - 2
                }
                return o.coerceIn(0, digitos.length)
            }
        }
        return TransformedText(AnnotatedString(formatado), mapa)
    }
}

/** "01022000" -> "2000-02-01" com validações de calendário (ex.: 31/02 é inválido) e de idade. */
internal fun dataDigitosParaIso(digitos: String): String? {
    if (digitos.length != 8) return null
    val dia = digitos.substring(0, 2).toIntOrNull() ?: return null
    val mes = digitos.substring(2, 4).toIntOrNull() ?: return null
    val ano = digitos.substring(4, 8).toIntOrNull() ?: return null

    // Validação básica
    if (mes !in 1..12 || dia !in 1..31) return null

    // Não permitir mais de 130 anos nem ano futuro
    val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
    if (ano > anoAtual || ano < anoAtual - 130) return null

    // Validação estrita de calendário (rejeita 31/02, 29/02 em ano não bissexto, etc.)
    val cal = Calendar.getInstance().apply {
        isLenient = false
        clear()
        set(ano, mes - 1, dia)
    }
    val data: Date = try {
        cal.time
    } catch (e: Exception) {
        return null
    }

    // Não permitir data futura
    if (data.after(Date())) return null

    return String.format(Locale.US, "%04d-%02d-%02d", ano, mes, dia)
}

// ---------- Busca ----------

@Composable
internal fun BuscaPessoa(
    rotulo: String,
    placeholder: String,
    busca: String,
    onBuscaChange: (String) -> Unit,
    resultados: List<PessoaSugestao>,
    selecionado: PessoaSugestao?,
    onSelecionar: (PessoaSugestao) -> Unit,
    carregando: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RotuloCampo(texto = rotulo)
        CampoDeTexto(
            valor = busca,
            onValorChange = onBuscaChange,
            placeholder = placeholder,
            icone = Icons.Default.Search
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (carregando) {
            Box(modifier = Modifier.fillMaxWidth().padding(10.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (selecionado != null) {
            LinhaPessoa(pessoa = selecionado, selecionada = true, onClick = null)
        } else if (busca.trim().length >= 2) {
            if (resultados.isEmpty()) {
                Text(
                    text = "Nenhum profissional encontrado",
                    fontSize = 13.sp,
                    color = TextoSuave,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resultados.forEach { pessoa ->
                        LinhaPessoa(pessoa = pessoa, selecionada = false, onClick = { onSelecionar(pessoa) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaPessoa(pessoa: PessoaSugestao, selecionada: Boolean, onClick: (() -> Unit)?) {
    val forma = RoundedCornerShape(10.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (selecionada) VerdeFundo else Color.White)
            .border(1.dp, if (selecionada) VerdeBorda else BordaSuave, forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(SelecionadoFundo)
        ) {
            Text(
                text = pessoa.nome.take(1).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = pessoa.nome, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoTitulo, maxLines = 1)
            Text(text = pessoa.detalhe, fontSize = 12.sp, color = TextoSuave, maxLines = 1)
        }
        if (selecionada) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF2E9E5B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun CartaoAviso(
    fundo: Color,
    borda: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(fundo)
            .border(1.dp, borda, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
internal fun DialogoVinculo(
    titulo: String,
    mensagem: String,
    detalhe: AnnotatedString,
    onEntendi: () -> Unit
) {
    Dialog(onDismissRequest = onEntendi) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoTitulo)
                IconButton(onClick = onEntendi) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextoSuave)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = mensagem, fontSize = 14.sp, color = TextoTitulo)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SelecionadoFundo)
                    .padding(12.dp)
            ) {
                Text(text = detalhe, fontSize = 13.sp, color = TextoSuave)
            }
            Spacer(modifier = Modifier.height(20.dp))
            BotaoPrimario(texto = "Entendi", onClick = onEntendi)
        }
    }
}

internal fun textoComNegrito(prefixo: String, negrito: String, sufixo: String): AnnotatedString =
    buildAnnotatedString {
        append(prefixo)
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(negrito) }
        append(sufixo)
    }