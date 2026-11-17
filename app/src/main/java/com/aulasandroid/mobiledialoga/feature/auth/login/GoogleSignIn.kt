package com.aulasandroid.mobiledialoga.feature.auth.login

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONObject

data class GoogleUserData(
    val idGoogle: String,
    val email: String,
    val nome: String?,
    val fotoUrl: String?
)

/** Abre a bottom sheet nativa do Google e devolve os dados da conta (id_google, email, nome, fotoUrl). */
object GoogleSignIn {

    const val GOOGLE_WEB_CLIENT_ID = "710933688848-p6ptu9862r2trmq9il215o4vh2q9nbq4.apps.googleusercontent.com"

    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    suspend fun obterIdGoogle(context: Context): String {
        return obterDadosGoogle(context).idGoogle
    }

    suspend fun limparSessao(context: Context) {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { }
    }

    fun dadosDaContaDoAparelho(email: String) = GoogleUserData(
        idGoogle = "google_${Math.abs(email.hashCode())}",
        email = email,
        nome = email.substringBefore("@").replaceFirstChar { it.uppercase() },
        fotoUrl = null
    )

    suspend fun obterDadosGoogle(context: Context): GoogleUserData {
        val activity = context.findActivity() ?: context
        val gerenciador = CredentialManager.create(activity)

        // Fluxo explícito do botão: abre o seletor mesmo após dispensar a bottom sheet.
        val googleIdOption = GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID)
            .build()

        val pedido = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val resultado = try {
            gerenciador.getCredential(activity, pedido)
        } catch (e: GetCredentialCancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            throw e
        }

        val credencial = resultado.credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credencial.data)
            val idToken = googleCredential.idToken
            val jsonPayload = runCatching { extrairJsonJwt(idToken) }.getOrNull()

            val emailJwt = jsonPayload?.optString("email")?.takeIf { it.isNotBlank() }
            val subJwt = jsonPayload?.optString("sub")?.takeIf { it.isNotBlank() }
            val nameJwt = jsonPayload?.optString("name")?.takeIf { it.isNotBlank() }
            val pictureJwt = jsonPayload?.optString("picture")?.takeIf { it.isNotBlank() }

            val email = emailJwt
                ?: googleCredential.id.takeIf { it.contains("@") }
                ?: "usuario@gmail.com"

            val idGoogle = subJwt
                ?: googleCredential.id.ifBlank { "google_${Math.abs(email.hashCode())}" }

            val nome = googleCredential.displayName
                ?: nameJwt
                ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

            val fotoUrl = googleCredential.profilePictureUri?.toString()
                ?: pictureJwt

            return GoogleUserData(
                idGoogle = idGoogle,
                email = email,
                nome = nome,
                fotoUrl = fotoUrl
            )
        }
        throw IllegalStateException("Credencial do Google inválida.")
    }

    private fun extrairJsonJwt(idToken: String): JSONObject? {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) return null
            var base64 = parts[1]
            when (base64.length % 4) {
                2 -> base64 += "=="
                3 -> base64 += "="
            }
            val bytes = Base64.decode(base64, Base64.URL_SAFE or Base64.DEFAULT)
            JSONObject(String(bytes, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }
}
