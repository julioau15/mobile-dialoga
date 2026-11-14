package com.aulasandroid.mobiledialoga.feature.vinculos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aulasandroid.mobiledialoga.feature.profissional.tarefas.model.paraIso
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import com.google.gson.JsonArray
import com.google.gson.JsonObject

val CorLinhaSuave = Color(0xFFEDE9F5)
val CorAvatarFundo = Color(0xFFE4DAF3)
val CorChipVerdeFundo = Color(0xFFDCF5E6)
val CorChipVerdeTexto = Color(0xFF16A34A)
val CorChipCinzaFundo = Color(0xFFEFEDF3)
val CorChipAmareloFundo = Color(0xFFFEF3C7)
val CorChipAmareloTexto = Color(0xFFB45309)
val CorPerigo = Color(0xFFDC2626)

// ---------------------------------------------------------------------------
// Peças visuais
// ---------------------------------------------------------------------------

@Composable
fun AvatarIniciais(nome: String, tamanho: Dp = 44.dp, modifier: Modifier = Modifier, fotoUrl: String? = null) {
    // aceita URL completa ou caminho relativo (mesma regra da foto do perfil)
    val url = com.aulasandroid.mobiledialoga.model.UserProfile(foto = fotoUrl).getFotoUrlCompleta()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(tamanho).clip(CircleShape).background(CorAvatarFundo)
    ) {
        // as iniciais ficam por baixo: se a foto não existir ou falhar ao carregar, elas continuam aparecendo
        Text(
            text = iniciaisDe(nome),
            color = CorPrimaria,
            fontSize = if (tamanho >= 56.dp) 18.sp else 13.sp,
            fontWeight = FontWeight.Bold
        )
        if (url != null) {
            coil.compose.AsyncImage(
                model = url,
                contentDescription = nome,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
fun ChipStatus(texto: String, fundo: Color, corTexto: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text = texto, color = corTexto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RotuloSecao(texto: String) {
    Text(
        text = texto.uppercase(),
        color = CorSecundaria,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
    )
}

@Composable
fun CampoBusca(
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
        placeholder = { Text(text = placeholder, fontSize = 14.sp, maxLines = 1) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CorPrimaria,
            unfocusedBorderColor = CorBorda,
            focusedContainerColor = CorBranco,
            unfocusedContainerColor = CorBranco,
            focusedTextColor = CorTexto,
            unfocusedTextColor = CorTexto,
            cursorColor = CorPrimaria,
            focusedPlaceholderColor = CorSecundaria,
            unfocusedPlaceholderColor = CorSecundaria,
            focusedLeadingIconColor = CorTexto,
            unfocusedLeadingIconColor = CorTexto
        ),
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Modal (mesmo visual dos modais do protótipo: título + X, corpo, rodapé com botões)
// ---------------------------------------------------------------------------

@Composable
fun ModalDialoga(
    titulo: String,
    onFechar: () -> Unit,
    bloqueado: Boolean = false,
    rodape: @Composable RowScope.() -> Unit,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = { if (!bloqueado) onFechar() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CorBranco)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CorTexto,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFechar, enabled = !bloqueado) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CorSecundaria)
                }
            }
            HorizontalDivider(color = CorLinhaSuave)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                content = conteudo
            )

            HorizontalDivider(color = CorLinhaSuave)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = rodape
            )
        }
    }
}

@Composable
fun BotaoModalSecundario(texto: String, onClick: () -> Unit, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CorLinhaSuave),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CorTexto, containerColor = CorBranco)
    ) {
        Text(text = texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun BotaoModalPrincipal(
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    perigo: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (perigo) CorPerigo else CorPrimaria,
            contentColor = CorBranco,
            disabledContainerColor = if (perigo) CorPerigo.copy(alpha = 0.4f) else CorPrimaria.copy(alpha = 0.4f),
            disabledContentColor = CorBranco
        )
    ) {
        Text(text = texto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// ---------------------------------------------------------------------------
// Datas (sem java.time, como o resto do app)
// ---------------------------------------------------------------------------

private val MESES = listOf(
    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
)
private val MESES_CURTOS = listOf(
    "jan.", "fev.", "mar.", "abr.", "mai.", "jun.", "jul.", "ago.", "set.", "out.", "nov.", "dez."
)

private fun partes(iso: String?): Triple<Int, Int, Int>? {
    val s = iso.paraIso() ?: return null
    return try {
        Triple(s.substring(0, 4).toInt(), s.substring(5, 7).toInt(), s.substring(8, 10).toInt())
    } catch (_: Exception) {
        null
    }
}

/** "2025-03-10..." -> "março de 2025" */
fun String?.mesAnoExtenso(): String? = partes(this)?.let { (a, m, _) -> "${MESES[m - 1]} de $a" }

/** "2025-03-10..." -> "mar. de 2025" */
fun String?.mesAnoCurto(): String? = partes(this)?.let { (a, m, _) -> "${MESES_CURTOS[m - 1]} de $a" }

/** "2025-03-15..." -> "15 de março de 2025" */
fun String?.dataExtenso(): String? = partes(this)?.let { (a, m, d) -> "$d de ${MESES[m - 1]} de $a" }

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

fun iniciaisDe(nome: String): String {
    val p = nome.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        p.isEmpty() -> "?"
        p.size == 1 -> p[0].take(2).uppercase()
        else -> "${p.first().first()}${p.last().first()}".uppercase()
    }
}

/** Procura o array pelas chaves dentro de "response"; se nenhuma existir, devolve o primeiro array que achar. */
fun JsonObject?.listaEm(vararg chaves: String): JsonArray {
    return try {
        val resp = this?.getAsJsonObject("response") ?: return JsonArray()
        for (c in chaves) {
            val e = resp.get(c)
            if (e != null && e.isJsonArray) return e.asJsonArray
        }
        for (k in resp.keySet()) {
            val e = resp.get(k)
            if (e.isJsonArray) return e.asJsonArray
        }
        JsonArray()
    } catch (_: Exception) {
        JsonArray()
    }
}

fun JsonObject.txt(chave: String): String? {
    val e = get(chave) ?: return null
    if (e.isJsonNull) return null
    val s = try { e.asString.trim() } catch (_: Exception) { "" }
    return if (s.isEmpty() || s.equals("null", true) || s.equals("undefined", true)) null else s
}

fun JsonObject.num(chave: String): Int {
    val e = get(chave) ?: return 0
    if (e.isJsonNull) return 0
    return try { e.asInt } catch (_: Exception) { e.asString.trim().toIntOrNull() ?: 0 }
}

@Composable
fun EspacoV(altura: Dp) = Spacer(modifier = Modifier.height(altura))

@Composable
fun EspacoH(largura: Dp) = Spacer(modifier = Modifier.width(largura))

fun mensagemErroHttp(codigo: Int, falhaRede: Boolean, acao: String): String = when {
    falhaRede -> "Não foi possível conectar ao servidor. Verifique sua conexão."
    codigo == 401 -> "Sua sessão expirou. Entre novamente."
    codigo == 409 -> "Essa ação já foi feita antes."
    else -> "Não foi possível $acao. Tente novamente."
}