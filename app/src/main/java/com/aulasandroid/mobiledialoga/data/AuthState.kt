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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS = "auth"

data class Auth(val token: String, val nivel: String)

class AuthException(message: String, val codigo: String?) : Exception(message)

class AuthState(private val context: Context) {
    var auth by mutableStateOf(carregar())
        private set

    // Login utilizando Retrofit (seguindo o modelo de consumo de API do projeto modelo)
    suspend fun login(email: String, senha: String): Auth {
        // monta o body da requisição
        val jsonBody = JsonObject().apply {
            addProperty("email", email)
            addProperty("senha", senha)
        }

        // faz a requisição e trata tanto sucesso quanto erro retornado pelo backend
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

        return dados
    }

    fun logout() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        auth = null
    }

    private fun carregar(): Auth? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return null
        val nivel = prefs.getString("nivel", null) ?: return null
        return Auth(token, nivel)
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
