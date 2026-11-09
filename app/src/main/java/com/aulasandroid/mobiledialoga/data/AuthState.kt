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
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PessoaSugestao
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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

        val data = withContext(Dispatchers.IO) {
            val response = RetrofitFactory().getAuthService().login(jsonBody).execute()
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Erro na resposta do servidor")
            } else {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "Erro ao realizar login")
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
                        PessoaSugestao(
                            id = obj.get("id").asString,
                            nome = obj.get("nome_completo")?.asString ?: obj.get("nome")?.asString ?: "Profissional",
                            detalhe = obj.get("crp")?.let { c -> if (c.isJsonNull) null else c.asString } ?: "CRP não informado"
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
            val response = RetrofitFactory().getPacienteService().buscarPacientes(query).execute()
            if (response.isSuccessful) {
                val json = response.body()
                val pacientes = findArray(json, "pacientes", "response", "dados")
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
                    Log.e(TAG, "Onboarding rejeitado pelo servidor: ${response.code()}")
                }
                response.isSuccessful
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro sync onboarding", e)
                false
            }
        }

        if (sucesso) {
            val base = userProfile ?: UserProfile(nome = null, apelido = null, foto = null, primeiroAcesso = false)
            val concluido = base.copy(primeiroAcesso = false)
            salvarPerfilLocal(concluido)
            withContext(Dispatchers.Main) { userProfile = concluido }
            // Atualiza apelido/foto vindos do servidor
            carregarPerfilMeu()
        }
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

    private fun extractProfileFromJson(element: JsonElement?, padrao: Boolean): UserProfile? {
        if (element == null || element.isJsonNull) return null

        fun findProfile(node: JsonElement): UserProfile? {
            if (node.isJsonArray && node.asJsonArray.size() > 0) return findProfile(node.asJsonArray.get(0))
            if (!node.isJsonObject) return null
            val obj = node.asJsonObject

            val nome = listOf("nome_completo", "nome").firstOrNull { obj.has(it) && !obj.get(it).isJsonNull }?.let { obj.get(it).asString }
            val apelido = listOf("apelido", "nickname").firstOrNull { obj.has(it) && !obj.get(it).isJsonNull }?.let { obj.get(it).asString }
            val foto = listOf("foto_avatar", "foto").firstOrNull { obj.has(it) && !obj.get(it).isJsonNull }?.let { obj.get(it).asString }

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

            for (key in listOf("response", "auth", "paciente", "profissional", "usuario", "dados", "data")) {
                if (obj.has(key)) {
                    val p = findProfile(obj.get(key))
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