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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val PREFS = "auth"

data class Auth(val token: String, val nivel: String)

class AuthState(private val context: Context) {
    var auth by mutableStateOf(carregar())
        private set

    // Cliente da API já autenticado com o token atual (use nas telas: useAuth().api.get(ApiRotas...))
    val api = ApiClient { auth?.token }

    suspend fun login(email: String, senha: String): Auth {
        val data = withContext(Dispatchers.IO) {
            val conn = URL(ApiRotas.BASE_URL + ApiRotas.Auth.LOGIN).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use {
                it.write(JSONObject().put("email", email).put("senha", senha).toString().toByteArray())
            }
            val stream = if (conn.responseCode < 400) conn.inputStream else conn.errorStream
            JSONObject(stream.bufferedReader().use { it.readText() })
        }
        if (!data.optBoolean("status")) throw Exception(data.optString("message"))

        val primeiro = data.getJSONObject("response").getJSONArray("auth").getJSONObject(0)
        val dados = Auth(primeiro.getString("token"), primeiro.getString("nivel"))
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
