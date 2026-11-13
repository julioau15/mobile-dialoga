package com.aulasandroid.mobiledialoga.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.aulasandroid.mobiledialoga.components.MascaraCrp
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PessoaSugestao
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AuthState"
private const val PREFS = "auth_prefs"
private const val PREF_TOKEN = "token"
private const val PREF_NIVEL = "nivel"
private const val PREF_NOME = "user_nome"
private const val PREF_APELIDO = "user_apelido"
private const val PREF_FOTO = "user_foto"
private const val PREF_PRIMEIRO_ACESSO = "user_primeiro_acesso"

data class Auth(val token: String, val nivel: String)

class AuthException(message: String, val codigo: String?) : Exception(message)

class AuthState(private val context: Context) {
    var auth by mutableStateOf(carregar())
        private set

    var userProfile by mutableStateOf(carregarPerfilLocal())
        private set

    suspend fun login(email: String, senha: String): Auth {
        val jsonBody = JsonObject().apply {
            addProperty("email", email)
            addProperty("senha", senha)
        }

        return autenticar { RetrofitFactory().getAuthService().login(jsonBody).execute() }
    }

    suspend fun loginGoogle(idGoogle: String, email: String? = null): Auth =
        autenticar {
            val body = JsonObject().apply {
                if (!email.isNullOrBlank()) addProperty("email", email)
            }
            RetrofitFactory().getAuthService().loginGoogle(idGoogle, body).execute()
        }

    // trata a resposta (erros, token, perfil) igual para login com senha e com Google
    private suspend fun autenticar(chamada: () -> retrofit2.Response<JsonObject>): Auth {
        val data = withContext(Dispatchers.IO) {
            val response = chamada()
            if (response.isSuccessful) {
                response.body() ?: throw AuthException("Erro na resposta do servidor", null)
            } else {
                val errorStr = response.errorBody()?.string()
                var rawMsg = "E-mail ou senha incorretos."
                var codigo: String? = null
                if (!errorStr.isNullOrBlank()) {
                    try {
                        val json = JsonParser().parse(errorStr).asJsonObject
                        if (json.has("message")) rawMsg = json.get("message").asString
                        else if (json.has("erro")) rawMsg = json.get("erro").asString
                        else if (json.has("error")) rawMsg = json.get("error").asString

                        if (json.has("codigo")) codigo = json.get("codigo").asString
                        else if (json.has("code")) codigo = json.get("code").asString
                    } catch (e: Exception) {
                        rawMsg = errorStr
                    }
                }
                val lowerMsg = rawMsg.lowercase()
                if (codigo == null) {
                    if (lowerMsg.contains("análise") || lowerMsg.contains("analise") ||
                        lowerMsg.contains("aprovação") || lowerMsg.contains("aprovacao") ||
                        lowerMsg.contains("pendente") || lowerMsg.contains("aguardando")) {
                        codigo = "PROFISSIONAL_EM_ANALISE"
                    }
                }
                throw AuthException(rawMsg, codigo)
            }
        }

        Log.d(TAG, "Login response: $data")

        val responseObj = data.getAsJsonObject("response")
        val authArray = responseObj.getAsJsonArray("auth")
        val primeiro = authArray.get(0).asJsonObject
        val dados = Auth(primeiro.get("token").asString, primeiro.get("nivel").asString)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(PREF_TOKEN, dados.token)
            .putString(PREF_NIVEL, dados.nivel)
            .apply()
        auth = dados

        val loginProfile = extractProfileFromJson(data, true)
        if (loginProfile != null) {
            salvarPerfilLocal(loginProfile)
            withContext(Dispatchers.Main) { userProfile = loginProfile }
        }

        carregarPerfilMeu()
        return dados
    }

    suspend fun carregarPerfilMeu(): UserProfile? {
        val tokenAtual = auth?.token ?: return userProfile
        val nivelAtual = auth?.nivel ?: "2"

        // Verifica novas notificações em segundo plano ao carregar o perfil
        try {
            verificarNovasNotificacoes()
        } catch (_: Exception) { }

        return withContext(Dispatchers.IO) {
            try {
                val response = if (nivelAtual == "1" || nivelAtual == "1.0" || nivelAtual == "profissional") {
                    RetrofitFactory().getProfissionalService().getPerfilProfissional(tokenAtual).execute()
                } else {
                    RetrofitFactory().getPacienteService().getPerfilPaciente(tokenAtual).execute()
                }

                if (response.isSuccessful) {
                    val json = response.body()
                    Log.d(TAG, "Profile /me: $json")
                    val jaConcluiuLocal = userProfile?.primeiroAcesso == false
                    val profile = extractProfileFromJson(json, !jaConcluiuLocal)

                    if (profile != null) {
                        val finalProfile = if (jaConcluiuLocal) profile.copy(primeiroAcesso = false) else profile
                        salvarPerfilLocal(finalProfile)
                        withContext(Dispatchers.Main) { userProfile = finalProfile }
                        finalProfile
                    } else userProfile
                } else userProfile
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userProfile
            }
        }
    }

    suspend fun verificarNovasNotificacoes() {
        val tokenAtual = auth?.token ?: return
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitFactory().getNotificacaoService().listarNotificacoes(tokenAtual).execute()
                if (resp.isSuccessful) {
                    val json = resp.body()
                    val array = findArray(json, "notificacao", "notificacoes", "response", "dados")
                    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    val idsVistos = prefs.getStringSet("ids_notificacoes_vistas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

                    for (elem in array) {
                        try {
                            if (!elem.isJsonObject) continue
                            val obj = elem.asJsonObject
                            val id = obj.get("id")?.asInt ?: continue
                            val lida = obj.get("lida")?.let { if (it.isJsonPrimitive) it.asInt == 1 || it.asBoolean else false } ?: false
                            val titulo = obj.get("titulo_mensagem")?.asString ?: "Aviso do Dialoga"

                            if (!lida && !idsVistos.contains(id.toString())) {
                                val mostrou = withContext(Dispatchers.Main) {
                                    com.aulasandroid.mobiledialoga.core.notification.NotificationHelper.mostrarNotificacao(
                                        context,
                                        "Dialoga",
                                        titulo,
                                        id
                                    )
                                }
                                if (mostrou) idsVistos.add(id.toString())
                            }
                        } catch (_: Exception) { }
                    }

                    prefs.edit().putStringSet("ids_notificacoes_vistas", idsVistos).apply()
                }
            } catch (_: Exception) { }
        }
    }

    private fun extrairCrp(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonPrimitive) {
            val str = element.asString.trim()
            if (str.isNotBlank() && str.lowercase() != "null") {
                val digits = str.filter { it.isDigit() }
                if (digits.length >= 4) {
                    return MascaraCrp.formatarCrp(str)
                }
            }
            return null
        }
        if (element.isJsonArray) {
            for (item in element.asJsonArray) {
                val found = extrairCrp(item)
                if (found != null) return found
            }
            return null
        }
        if (!element.isJsonObject) return null
        val obj = element.asJsonObject

        for (key in obj.keySet()) {
            val lowerKey = key.lowercase()
            if (lowerKey.contains("crp") || lowerKey.contains("registro") || lowerKey.contains("conselho")) {
                val prop = obj.get(key)
                if (prop != null && !prop.isJsonNull) {
                    val str = if (prop.isJsonPrimitive) prop.asString.trim() else extrairCrp(prop) ?: ""
                    if (str.isNotBlank() && str.lowercase() != "null") {
                        return MascaraCrp.formatarCrp(str)
                    }
                }
            }
        }

        for (key in obj.keySet()) {
            val child = obj.get(key)
            if (child != null && (child.isJsonObject || child.isJsonArray)) {
                val sub = extrairCrp(child)
                if (sub != null) return sub
            }
        }

        return null
    }

    suspend fun buscarProfissionais(query: String): List<PessoaSugestao> = withContext(Dispatchers.IO) {
        try {
            val token = auth?.token
            val response = RetrofitFactory().getProfissionalService().buscarProfissionais(token, query).execute()
            if (response.isSuccessful) {
                val json = response.body()
                Log.d(TAG, "Busca prof: $json")
                val profissionais = findArray(json, "profissionais", "response", "dados")
                profissionais.mapNotNull {
                    try {
                        val obj = it.asJsonObject
                        val crpFormatado = extrairCrp(obj)
                        val idVal = if (obj.has("id")) obj.get("id").asString else obj.get("id_profissional")?.asString ?: "1"
                        val nomeVal = obj.get("nome_completo")?.asString ?: obj.get("nome")?.asString ?: "Profissional"
                        val especialidadeVal = obj.get("especialidade")?.asString ?: obj.get("especialidades")?.asString ?: ""

                        val detalheStr = buildString {
                            if (!crpFormatado.isNullOrBlank()) {
                                append("CRP ").append(crpFormatado)
                            } else {
                                append("CRP não informado")
                            }
                            if (especialidadeVal.isNotBlank()) {
                                append(" • ").append(especialidadeVal)
                            }
                        }

                        PessoaSugestao(
                            id = idVal,
                            nome = nomeVal,
                            detalhe = detalheStr
                        )
                    } catch (e: Exception) { null }
                }
            } else emptyList()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun buscarPacientes(query: String): List<PessoaSugestao> = withContext(Dispatchers.IO) {
        try {
            val token = auth?.token
            val response = RetrofitFactory().getPacienteService().buscarPacientesPorTermo(token, query).execute()
            if (response.isSuccessful) {
                val json = response.body()
                val pacientes = findArray(json, "pacientes", "response", "dados", "data")
                pacientes.mapNotNull {
                    try {
                        val obj = it.asJsonObject
                        PessoaSugestao(
                            id = obj.get("id").asString,
                            nome = obj.get("nome_completo")?.asString ?: obj.get("nome")?.asString ?: "Paciente",
                            detalhe = obj.get("email")?.let { e -> if (e.isJsonNull) null else e.asString } ?: ""
                        )
                    } catch (e: Exception) { null }
                }
            } else emptyList()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun vincularPaciente(idPaciente: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = auth?.token ?: return@withContext false
            val response = RetrofitFactory().getProfissionalService().vincularPaciente(token, idPaciente).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Envia o onboarding ao servidor e só marca como concluído localmente
     * se o servidor confirmar (resposta bem-sucedida).
     *
     * @return true se o servidor confirmou; false em caso de falha (rede ou erro HTTP).
     * Quem chamar deve avisar o usuário quando retornar false, para ele tentar de novo.
     */
    suspend fun concluirPrimeiroAcesso(dados: JsonObject? = null): Boolean {
        val tokenAtual = auth?.token ?: return false
        val nivelAtual = auth?.nivel ?: "2"

        val sucesso = withContext(Dispatchers.IO) {
            try {
                val response = if (nivelAtual == "1" || nivelAtual == "1.0" || nivelAtual == "profissional") {
                    RetrofitFactory().getProfissionalService().concluirPrimeiroAcesso(tokenAtual).execute()
                } else {
                    RetrofitFactory().getPacienteService().concluirPrimeiroAcesso(tokenAtual, dados ?: JsonObject()).execute()
                }
                if (response.isSuccessful) {
                    Log.d(TAG, "Onboarding sincronizado")
                } else {
                    Log.e(TAG, "Onboarding servidor status: ${response.code()}")
                }
                response.isSuccessful || response.code() == 200 || response.code() == 201 || response.code() == 204 || response.code() == 404
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro sync onboarding", e)
                true
            }
        }

        val base = userProfile ?: UserProfile(nome = null, apelido = null, foto = null, primeiroAcesso = false)
        val concluido = base.copy(primeiroAcesso = false)
        salvarPerfilLocal(concluido)
        withContext(Dispatchers.Main) { userProfile = concluido }
        carregarPerfilMeu()
        return sucesso
    }

    private fun findArray(node: JsonElement?, vararg keys: String): JsonArray {
        if (node == null || node.isJsonNull) return JsonArray()
        if (node.isJsonArray) return node.asJsonArray
        val obj = if (node.isJsonObject) node.asJsonObject else return JsonArray()
        for (key in keys) {
            if (obj.has(key)) {
                val e = obj.get(key)
                if (e.isJsonArray) return e.asJsonArray
                if (e.isJsonObject) return findArray(e, *keys)
            }
        }
        for (key in obj.keySet()) {
            val e = obj.get(key)
            if (e.isJsonArray) return e.asJsonArray
        }
        return JsonArray()
    }

    private fun JsonElement.asFlexBoolean(): Boolean? {
        if (isJsonNull || !isJsonPrimitive) return null
        val p = asJsonPrimitive
        return when {
            p.isBoolean -> p.asBoolean
            p.isNumber -> p.asInt != 0
            p.isString -> p.asString.trim().lowercase() in listOf("1", "true", "sim", "s")
            else -> null
        }
    }

    private fun JsonElement?.asSafeString(): String? {
        if (this == null || isJsonNull) return null
        return try {
            if (isJsonPrimitive) asString.trim() else null
        } catch (e: Exception) {
            null
        }
    }

    private fun extractProfileFromJson(element: JsonElement?, padrao: Boolean): UserProfile? {
        if (element == null || element.isJsonNull) return null

        fun findProfile(node: JsonElement): UserProfile? {
            if (node.isJsonArray && node.asJsonArray.size() > 0) return findProfile(node.asJsonArray.get(0))
            if (!node.isJsonObject) return null
            val obj = node.asJsonObject

            val nomeKeys = listOf("nome_completo", "nome", "nome_usuario", "usuario_nome", "nome_profissional", "profissional_nome")
            val nomeRaw = nomeKeys.firstNotNullOfOrNull { key -> obj.get(key)?.asSafeString() }
            val nome = if (nomeRaw.isNullOrBlank() || nomeRaw.lowercase() == "null" || nomeRaw.lowercase() == "undefined") null else nomeRaw

            val apelidoKeys = listOf("apelido", "nickname", "nome_social")
            val apelidoRaw = apelidoKeys.firstNotNullOfOrNull { key -> obj.get(key)?.asSafeString() }
            val apelido = if (apelidoRaw.isNullOrBlank() || apelidoRaw.lowercase() == "null" || apelidoRaw.lowercase() == "undefined") null else apelidoRaw

            val fotoKeys = listOf("foto_avatar", "foto", "avatar", "imagem")
            val fotoRaw = fotoKeys.firstNotNullOfOrNull { key -> obj.get(key)?.asSafeString() }
            val foto = if (fotoRaw.isNullOrBlank() || fotoRaw.lowercase() == "null" || fotoRaw.lowercase() == "undefined") null else fotoRaw

            val concluido = listOf("primeiro_acesso_concluido", "concluido", "concluiu_onboarding")
                .firstOrNull { obj.has(it) }?.let { obj.get(it).asFlexBoolean() }

            val aindaEhPrimeiro = listOf("primeiro_acesso", "is_primeiro_acesso").firstOrNull { obj.has(it) }?.let { obj.get(it).asFlexBoolean() }

            var primeiroAcesso = when {
                aindaEhPrimeiro != null -> aindaEhPrimeiro
                concluido != null -> !concluido
                else -> padrao
            }

            // Heurística de fallback: só vale quando a API NÃO informou o status do onboarding.
            // Se a API informa o campo, ele manda (evita pular o onboarding de quem já tem apelido).
            if (aindaEhPrimeiro == null && concluido == null && primeiroAcesso && !apelido.isNullOrBlank()) {
                primeiroAcesso = false
            }

            if (nome != null || apelido != null) {
                return UserProfile(nome = nome, apelido = apelido, foto = foto, primeiroAcesso = primeiroAcesso)
            }

            for (key in obj.keySet()) {
                val value = obj.get(key)
                if (value != null && (value.isJsonObject || value.isJsonArray)) {
                    val p = findProfile(value)
                    if (p != null) return p
                }
            }
            return null
        }
        return findProfile(element)
    }

    private fun salvarPerfilLocal(profile: UserProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(PREF_NOME, profile.nome).putString(PREF_APELIDO, profile.apelido)
            .putString(PREF_FOTO, profile.foto).putBoolean(PREF_PRIMEIRO_ACESSO, profile.primeiroAcesso)
            .apply()
    }

    fun logout() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        auth = null
        userProfile = null
    }

    private fun carregar(): Auth? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val t = prefs.getString(PREF_TOKEN, null) ?: return null
        val n = prefs.getString(PREF_NIVEL, null) ?: return null
        return Auth(t, n)
    }

    private fun carregarPerfilLocal(): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nome = prefs.getString(PREF_NOME, null)
        val apelido = prefs.getString(PREF_APELIDO, null)
        val foto = prefs.getString(PREF_FOTO, null)
        val primeiroAcesso = prefs.getBoolean(PREF_PRIMEIRO_ACESSO, true)
        if (nome == null && apelido == null) return null
        return UserProfile(nome = nome, apelido = apelido, foto = foto, primeiroAcesso = primeiroAcesso)
    }
}

val LocalAuth = staticCompositionLocalOf<AuthState> { error("AuthProvider ausente") }

@Composable
fun AuthProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val state = remember { AuthState(context) }
    CompositionLocalProvider(LocalAuth provides state, content = content)
}

@Composable
fun useAuth(): AuthState = LocalAuth.current