package com.aulasandroid.mobiledialoga.feature.auth.login

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONObject

/** Abre o seletor de contas do Google e devolve o id da conta (claim "sub"), que o back usa como id_google. */
object GoogleSignIn {

    // ID do cliente OAuth do tipo "Web application" (Google Cloud Console > APIs e serviços > Credenciais).
    // Não é o ID do cliente Android.
    const val GOOGLE_WEB_CLIENT_ID = "710933688848-p6ptu9862r2trmq9il215o4vh2q9nbq4.apps.googleusercontent.com"

    suspend fun obterIdGoogle(context: Context): String {
        val opcao = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // mostra todas as contas do aparelho
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .build()
        val pedido = GetCredentialRequest.Builder().addCredentialOption(opcao).build()

        val resultado = CredentialManager.create(context).getCredential(context, pedido)
        val credencial = resultado.credential

        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idToken = GoogleIdTokenCredential.createFrom(credencial.data).idToken
            return extrairSub(idToken)
        }
        throw IllegalStateException("Credencial do Google inválida.")
    }

    // O id_token é um JWT (header.payload.assinatura); o "sub" no payload é o id único da conta Google.
    private fun extrairSub(idToken: String): String {
        val payload = idToken.split(".").getOrNull(1)
            ?: throw IllegalStateException("Token do Google inválido.")
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        return JSONObject(json).getString("sub")
    }
}
