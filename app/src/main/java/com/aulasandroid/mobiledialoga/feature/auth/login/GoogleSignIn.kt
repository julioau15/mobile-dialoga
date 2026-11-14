package com.aulasandroid.mobiledialoga.feature.auth.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONObject

data class GoogleUserData(
    val idGoogle: String,
    val email: String,
    val nome: String?
)

/** Abre o seletor de contas do Google e devolve os dados da conta (id_google, email, nome). */
object GoogleSignIn {

    // ID do cliente OAuth do tipo "Web application" (Google Cloud Console > APIs e serviços > Credenciais).
    // Não é o ID do cliente Android.
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

    /**
     * Esquece a conta Google escolhida neste app. Chamado no logout: assim o próximo "Entrar com Google"
     * mostra o seletor de contas de novo, em vez de reaproveitar a conta da vez anterior.
     */
    suspend fun limparSessao(context: Context) {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // não conseguir limpar não pode travar o logout
        }
    }

    suspend fun obterDadosGoogle(context: Context): GoogleUserData {
        val activity = context.findActivity() ?: context
        val gerenciador = CredentialManager.create(activity)

        // O seletor de contas SEMPRE aparece e a conta devolvida é a que a pessoa tocou.
        // Se a pessoa fechar o seletor, GetCredentialCancellationException sobe até o ViewModel (não é erro).
        // Não existe "plano B" que escolha uma conta sozinho: antes, quando o seletor falhava, o app pegava a
        // primeira conta do aparelho (AccountManager) e entrava sempre na mesma conta.
        val resultado = try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID).build()
            val pedido = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            gerenciador.getCredential(activity, pedido)
        } catch (e: GetCredentialCancellationException) {
            throw e
        } catch (e: NoCredentialException) {
            // Só quando o aparelho não devolve nenhuma conta pelo fluxo acima, tenta o seletor em folha
            // (também sem escolher conta automaticamente).
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()
            val pedidoFallback = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            gerenciador.getCredential(activity, pedidoFallback)
        }

        val credencial = resultado.credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credencial.data)
            val idToken = googleCredential.idToken
            val payload = extrairPayload(idToken)

            val email = googleCredential.id.ifBlank { payload.email }
            val nome = googleCredential.displayName ?: payload.nome

            return GoogleUserData(
                idGoogle = payload.idGoogle,
                email = email,
                nome = nome
            )
        }
        throw IllegalStateException("Credencial do Google inválida.")
    }

    // O id_token é um JWT (header.payload.assinatura); o "sub" no payload é o id único da conta Google.
    private fun extrairPayload(idToken: String): GoogleUserData {
        val payload = idToken.split(".").getOrNull(1)
            ?: throw IllegalStateException("Token do Google inválido.")
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        val obj = JSONObject(json)
        val sub = obj.getString("sub")
        val email = obj.optString("email", "")
        val nome = if (obj.has("name") && !obj.isNull("name")) obj.getString("name") else null
        val finalNome = nome ?: listOfNotNull(
            if (obj.has("given_name") && !obj.isNull("given_name")) obj.getString("given_name") else null,
            if (obj.has("family_name") && !obj.isNull("family_name")) obj.getString("family_name") else null
        ).joinToString(" ").ifBlank { null }
        return GoogleUserData(idGoogle = sub, email = email, nome = finalNome)
    }
}
