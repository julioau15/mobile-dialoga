package com.aulasandroid.mobiledialoga.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS = "auth"
private const val PREF_NOME = "user_nome"
private const val PREF_APELIDO = "user_apelido"
private const val PREF_FOTO = "user_foto"

data class Auth(val token: String, val nivel: String)

class AuthException(message: String, val codigo: String?) : Exception(message)

class AuthState(private val context: Context) {
    var auth by mutableStateOf(carregar())
        private set

    var userProfile by mutableStateOf(carregarPerfilLocal())
        private set

    // Login utilizando Retrofit
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
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val errorJson = JsonParser().parse(errorBody).asJsonObject
                        val msg = if (errorJson.has("message")) errorJson.get("message").asString else "Erro ao realizar login"
                        val codigo = if (errorJson.has("codigo")) errorJson.get("codigo").asString else null
                        throw AuthException(msg, codigo)
                    } catch (e: AuthException) {
                        throw e
                    } catch (e: Exception) {
                        throw Exception(errorBody)
                    }
                } else {
                    throw Exception("Erro no servidor (Código ${response.code()})")
                }
            }
        }

        if (!data.get("status").asBoolean) {
            val msg = if (data.has("message")) data.get("message").asString else "Erro ao realizar login"
            val codigo = if (data.has("codigo")) data.get("codigo").asString else null
            throw AuthException(msg, codigo)
        }

        val primeiro = data.getAsJsonObject("response").getAsJsonArray("auth").get(0).asJsonObject
        val dados = Auth(primeiro.get("token").asString, primeiro.get("nivel").asString)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("token", dados.token)
            .putString("nivel", dados.nivel)
            .apply()
        auth = dados

        // Extrai dados de perfil diretamente da resposta do login
        val loginProfile = extractProfileFromJson(data)
        if (loginProfile != null) {
            salvarPerfilLocal(loginProfile)
            withContext(Dispatchers.Main) {
                userProfile = loginProfile
            }
        }

        // Tenta atualizar chamando a rota me do backend
        carregarPerfilMeu()

        return dados
    }

    suspend fun carregarPerfilMeu(): UserProfile? {
        val tokenAtual = auth?.token ?: return userProfile
        val nivelAtual = auth?.nivel ?: "2"

        return withContext(Dispatchers.IO) {
            try {
                val cleanToken = if (tokenAtual.startsWith("Bearer ")) tokenAtual.removePrefix("Bearer ").trim() else tokenAtual.trim()
                val response = if (nivelAtual == "1" || nivelAtual == "1.0" || nivelAtual == "profissional") {
                    RetrofitFactory().getProfissionalService().getPerfilProfissional(cleanToken).execute()
                } else {
                    RetrofitFactory().getPacienteService().getPerfilPaciente(cleanToken).execute()
                }

                if (response.isSuccessful) {
                    val json = response.body()
                    val profile = extractProfileFromJson(json)
                    if (profile != null) {
                        salvarPerfilLocal(profile)
                        withContext(Dispatchers.Main) {
                            userProfile = profile
                        }
                        profile
                    } else userProfile
                } else userProfile
            } catch (e: Exception) {
                userProfile
            }
        }
    }

    private fun extractProfileFromJson(element: JsonElement?): UserProfile? {
        if (element == null || element.isJsonNull) return null

        fun findInObject(obj: JsonObject): UserProfile? {
            for (key in listOf("response", "auth", "paciente", "profissional", "usuario", "dados", "data")) {
                if (obj.has(key)) {
                    val child = obj.get(key)
                    if (child.isJsonObject) {
                        val p = findInObject(child.asJsonObject)
                        if (p != null) return p
                    } else if (child.isJsonArray && child.asJsonArray.size() > 0) {
                        for (item in child.asJsonArray) {
                            if (item.isJsonObject) {
                                val p = findInObject(item.asJsonObject)
                                if (p != null) return p
                            }
                        }
                    }
                }
            }

            val nome = listOf("nome_completo", "nomeCompleto", "nome", "name")
                .mapNotNull { if (obj.has(it) && !obj.get(it).isJsonNull) obj.get(it).asString else null }
                .firstOrNull { it.isNotBlank() }

            val apelido = listOf("apelido", "nickname")
                .mapNotNull { if (obj.has(it) && !obj.get(it).isJsonNull) obj.get(it).asString else null }
                .firstOrNull { it.isNotBlank() }

            val foto = listOf("foto_avatar", "foto", "foto_url", "fotoUrl", "url_foto", "imagem", "avatar", "image")
                .mapNotNull { if (obj.has(it) && !obj.get(it).isJsonNull) obj.get(it).asString else null }
                .firstOrNull { it.isNotBlank() }

            if (nome != null || apelido != null || foto != null) {
                return UserProfile(nome = nome, apelido = apelido, foto = foto)
            }
            return null
        }

        return when {
            element.isJsonObject -> findInObject(element.asJsonObject)
            element.isJsonArray && element.asJsonArray.size() > 0 -> {
                for (item in element.asJsonArray) {
                    if (item.isJsonObject) {
                        val p = findInObject(item.asJsonObject)
                        if (p != null) return p
                    }
                }
                null
            }
            else -> null
        }
    }

    private fun salvarPerfilLocal(profile: UserProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(PREF_NOME, profile.nome)
            .putString(PREF_APELIDO, profile.apelido)
            .putString(PREF_FOTO, profile.foto)
            .apply()
    }

    fun logout() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        auth = null
        userProfile = null
    }

    private fun carregar(): Auth? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return null
        val nivel = prefs.getString("nivel", null) ?: return null
        return Auth(token, nivel)
    }

    private fun carregarPerfilLocal(): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nome = prefs.getString(PREF_NOME, null)
        val apelido = prefs.getString(PREF_APELIDO, null)
        val foto = prefs.getString(PREF_FOTO, null)
        if (nome == null && apelido == null && foto == null) return null
        return UserProfile(nome = nome, apelido = apelido, foto = foto)
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
