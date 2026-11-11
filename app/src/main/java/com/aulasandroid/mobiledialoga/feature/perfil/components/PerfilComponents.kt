package com.aulasandroid.mobiledialoga.feature.perfil.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aulasandroid.mobiledialoga.components.FormatoCampo
import com.aulasandroid.mobiledialoga.components.MensagemErroCampo
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private val CorFundoBloqueado = Color(0xFFF8F7FB)
private const val TAMANHO_MAX_FOTO_BYTES = 5 * 1024 * 1024 // 5 MB

// ---------------------------------------------------------------------------
// Estilos do cabeçalho (o paciente é rosa/lilás claro, o profissional é roxo)
// ---------------------------------------------------------------------------

data class EstiloCabecalhoPerfil(
    val fundo: Brush,
    val avatarFundo: Color,
    val avatarTexto: Color,
    val nome: Color,
    val seloFundo: Color,
    val seloTexto: Color,
    val dica: Color,
    val badgeFundo: Color,
    val badgeIcone: Color
)

val EstiloCabecalhoPaciente = EstiloCabecalhoPerfil(
    fundo = Brush.horizontalGradient(listOf(Color(0xFFDCD0EE), Color(0xFFFDBFBF))),
    avatarFundo = CorBranco,
    avatarTexto = CorPrimaria,
    nome = CorTexto,
    seloFundo = Color(0xFFFFFFFF).copy(alpha = 0.65f),
    seloTexto = CorPrimaria,
    dica = CorSecundaria,
    badgeFundo = CorPrimaria,
    badgeIcone = CorBranco
)

val EstiloCabecalhoProfissional = EstiloCabecalhoPerfil(
    fundo = Brush.verticalGradient(listOf(Color(0xFF7B4FA6), Color(0xFF5F3A86))),
    avatarFundo = Color(0xFFFFFFFF).copy(alpha = 0.22f),
    avatarTexto = CorBranco,
    nome = CorBranco,
    seloFundo = Color(0xFFFFFFFF).copy(alpha = 0.22f),
    seloTexto = CorBranco,
    dica = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    badgeFundo = CorBranco,
    badgeIcone = CorPrimaria
)

// ---------------------------------------------------------------------------
// Estrutura da tela
// ---------------------------------------------------------------------------

@Composable
fun PerfilTitulo(titulo: String, subtitulo: String) {
    Text(text = titulo, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = CorTexto)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = subtitulo, fontSize = 14.sp, color = CorSecundaria, lineHeight = 18.sp)
}

// O Card recorta o conteúdo no formato arredondado, então o gradiente do topo acompanha os cantos
@Composable
fun PerfilCartao(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun PerfilCabecalho(
    estilo: EstiloCabecalhoPerfil,
    iniciais: String,
    nome: String,
    selo: String?,
    fotoUrl: String?,
    fotoLocal: Uri?,
    onTrocarFoto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(estilo.fundo)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Box(modifier = Modifier.size(84.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(estilo.avatarFundo)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clickable(onClick = onTrocarFoto)
            ) {
                val modeloFoto: Any? = fotoLocal ?: fotoUrl?.takeIf { it.isNotBlank() }
                if (modeloFoto != null) {
                    AsyncImage(
                        model = modeloFoto,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = iniciais,
                        color = estilo.avatarTexto,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(estilo.badgeFundo)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable(onClick = onTrocarFoto)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Alterar foto",
                    tint = estilo.badgeIcone,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = nome,
            color = estilo.nome,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )

        if (!selo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(estilo.seloFundo)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(text = selo, color = estilo.seloTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Clique na foto para alterar", color = estilo.dica, fontSize = 12.sp)
    }
}

// ---------------------------------------------------------------------------
// Campos
// ---------------------------------------------------------------------------

@Composable
fun RotuloPerfil(texto: String, opcional: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = texto.uppercase(),
            color = CorSecundaria,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        if (opcional) {
            Text(text = " (opcional)", color = CorSecundaria, fontSize = 12.sp)
        }
    }
}

@Composable
fun AjudaCampoPerfil(texto: String) {
    Text(
        text = texto,
        color = CorSecundaria,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(start = 2.dp, top = 6.dp)
    )
}

@Composable
private fun coresCampoPerfil(editando: Boolean): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CorPrimaria,
    unfocusedBorderColor = if (editando) CorPrimaria.copy(alpha = 0.55f) else CorBorda,
    errorBorderColor = CorErro,
    focusedContainerColor = CorBranco,
    unfocusedContainerColor = CorBranco,
    errorContainerColor = CorBranco,
    focusedTextColor = CorTexto,
    unfocusedTextColor = CorTexto,
    errorTextColor = CorTexto,
    cursorColor = CorPrimaria,
    focusedPlaceholderColor = CorSecundaria,
    unfocusedPlaceholderColor = CorSecundaria,
    focusedLeadingIconColor = CorPrimaria,
    unfocusedLeadingIconColor = CorPrimaria,
    errorLeadingIconColor = CorPrimaria
)

@Composable
private fun coresCampoBloqueado(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CorBorda,
    unfocusedBorderColor = CorBorda,
    focusedContainerColor = CorFundoBloqueado,
    unfocusedContainerColor = CorFundoBloqueado,
    focusedTextColor = CorSecundaria,
    unfocusedTextColor = CorSecundaria,
    focusedLeadingIconColor = CorSecundaria,
    unfocusedLeadingIconColor = CorSecundaria,
    focusedTrailingIconColor = CorSecundaria,
    unfocusedTrailingIconColor = CorSecundaria,
    cursorColor = Color.Transparent
)

/** Campo editável (só aceita digitação quando [editando] = true). */
@Composable
fun CampoPerfil(
    rotulo: String,
    valor: String,
    icone: ImageVector,
    editando: Boolean,
    modifier: Modifier = Modifier,
    onValorChange: (String) -> Unit = {},
    placeholder: String = "",
    opcional: Boolean = false,
    ajuda: String? = null,
    erro: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RotuloPerfil(rotulo, opcional)
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            readOnly = !editando,
            singleLine = true,
            leadingIcon = { Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp)) },
            placeholder = if (placeholder.isNotBlank()) {
                { Text(text = placeholder, fontSize = 14.sp) }
            } else null,
            isError = erro != null,
            shape = FormatoCampo,
            colors = coresCampoPerfil(editando),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth()
        )
        MensagemErroCampo(erro)
        if (erro == null && ajuda != null) AjudaCampoPerfil(ajuda)
    }
}

/** Campo que nunca pode ser editado pelo usuário (ex.: e-mail). */
@Composable
fun CampoPerfilBloqueado(
    rotulo: String,
    valor: String,
    icone: ImageVector,
    modifier: Modifier = Modifier,
    ajuda: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RotuloPerfil(rotulo)
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            leadingIcon = { Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp)) },
            trailingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Bloqueado", modifier = Modifier.size(18.dp)) },
            shape = FormatoCampo,
            colors = coresCampoBloqueado(),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth()
        )
        if (ajuda != null) AjudaCampoPerfil(ajuda)
    }
}

/** Caixa compacta usada em "Dados imutáveis" (CRP e CPF). */
@Composable
fun DadoImutavel(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(FormatoCampo)
            .background(CorFundoBloqueado)
            .border(1.dp, CorBorda, FormatoCampo)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = CorSecundaria,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = rotulo, color = CorSecundaria, fontSize = 12.sp)
            Text(
                text = valor.ifBlank { "—" },
                color = CorSecundaria,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Botões e aviso
// ---------------------------------------------------------------------------

private val FormatoBotao = RoundedCornerShape(12.dp)

@Composable
fun BotaoPerfilPrincipal(
    texto: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    carregando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !carregando,
        shape = FormatoBotao,
        colors = ButtonDefaults.buttonColors(
            containerColor = CorPrimaria,
            contentColor = CorBranco,
            disabledContainerColor = Color(0xFFE8E1F2),
            disabledContentColor = CorPrimaria.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .height(46.dp)
            .shadow(if (carregando) 0.dp else 4.dp, FormatoBotao, ambientColor = CorPrimaria, spotColor = CorPrimaria)
    ) {
        if (carregando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CorPrimaria, strokeWidth = 2.dp)
        } else {
            Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BotaoPerfilSecundario(
    texto: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = FormatoBotao,
        border = BorderStroke(1.dp, CorBorda),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CorBranco, contentColor = CorTexto),
        modifier = modifier.height(46.dp)
    ) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** "Cancelar" + "Salvar alterações" lado a lado, como no protótipo. */
@Composable
fun AcoesEdicaoPerfil(
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        BotaoPerfilSecundario(
            texto = "Cancelar",
            icone = Icons.Default.Close,
            onClick = onCancelar,
            habilitado = !salvando,
            modifier = Modifier.weight(1f)
        )
        BotaoPerfilPrincipal(
            texto = "Salvar alterações",
            icone = Icons.Default.Check,
            onClick = onSalvar,
            carregando = salvando,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/** Aviso flutuante ("Perfil atualizado!"). Quem usa decide a posição e quando some. */
@Composable
fun AvisoPerfil(mensagem: String?, sucesso: Boolean, modifier: Modifier = Modifier) {
    if (mensagem == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (sucesso) CorSucesso else CorErro)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = if (sucesso) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = mensagem, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------------------
// Foto de perfil
// ---------------------------------------------------------------------------

/** Abre a galeria; devolve uma função para chamar no clique da foto. */
@Composable
fun rememberSeletorFoto(onFotoEscolhida: (Uri) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onFotoEscolhida(uri)
    }
    return { launcher.launch("image/*") }
}

/** Lê a imagem escolhida e monta a parte "foto_avatar" do multipart. Devolve null se não der (ou passar de 5 MB). */
fun criarPartFoto(context: Context, uri: Uri): MultipartBody.Part? {
    return try {
        val resolver = context.contentResolver
        val tipo = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (bytes.isEmpty() || bytes.size > TAMANHO_MAX_FOTO_BYTES) return null
        val extensao = when (tipo) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        MultipartBody.Part.createFormData("foto_avatar", "avatar.$extensao", bytes.toRequestBody(tipo.toMediaTypeOrNull()))
    } catch (e: Exception) {
        null
    }
}

// ---------------------------------------------------------------------------
// Utilidades de dados
// ---------------------------------------------------------------------------

fun String.comoCorpoTexto(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

/** Lê um campo de texto do JSON, tratando null/"null"/"undefined" como ausente. */
fun JsonObject.texto(chave: String): String? {
    val elemento = get(chave) ?: return null
    if (elemento.isJsonNull || !elemento.isJsonPrimitive) return null
    val valor = elemento.asString.trim()
    return if (valor.isEmpty() || valor.equals("null", true) || valor.equals("undefined", true)) null else valor
}

/** Pega o primeiro item de response.<chaveLista> (ex.: response.paciente[0]). */
fun JsonObject.primeiroItem(chaveLista: String): JsonObject? {
    return try {
        val lista = getAsJsonObject("response")?.getAsJsonArray(chaveLista) ?: return null
        if (lista.size() == 0) null else lista.get(0).asJsonObject
    } catch (e: Exception) {
        null
    }
}

/** 12345678900 -> 123.456.789-00 */
fun formatarCpf(bruto: String): String {
    val d = bruto.filter { it.isDigit() }.take(11)
    if (d.length < 11) return d
    return "${d.substring(0, 3)}.${d.substring(3, 6)}.${d.substring(6, 9)}-${d.substring(9, 11)}"
}

fun traduzirErroPerfil(codigoHttp: Int?, falha: Throwable? = null, acao: String = "salvar o perfil"): String = when {
    falha != null -> "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."
    codigoHttp == 401 -> "Sua sessão expirou. Entre novamente."
    codigoHttp == 409 -> "Este celular já está cadastrado em outra conta."
    codigoHttp == 400 -> "Dados inválidos. Confira os campos e tente novamente."
    else -> "Não foi possível $acao. Tente novamente."
}
