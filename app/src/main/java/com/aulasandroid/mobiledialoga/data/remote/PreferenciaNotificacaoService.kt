package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface PreferenciaNotificacaoService {
    // lista as preferências do usuário logado (paciente ou profissional, pelo token)
    @GET("preferencias-notificacao")
    fun listarPreferencias(@Header("x-access-token") token: String): Call<JsonObject>

    // substitui todas as preferências: { "preferencias": [ { categoria, tipo_especifico, ativo } ] }
    @PUT("preferencias-notificacao")
    fun salvarPreferencias(
        @Header("x-access-token") token: String,
        @Body body: JsonObject
    ): Call<JsonObject>
}
