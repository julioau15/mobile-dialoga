package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificacaoService {
    @GET("notificacoes")
    fun listarNotificacoes(@Header("x-access-token") token: String): Call<JsonObject>

    @PUT("notificacoes/{id}/marcar-lida")
    fun marcarLida(@Path("id") id: Int): Call<JsonObject>

    @PUT("notificacoes/marcar-todas-lidas")
    fun marcarTodasLidas(@Header("x-access-token") token: String): Call<JsonObject>

    @DELETE("notificacoes/{id}")
    fun excluirNotificacao(@Path("id") id: Int): Call<JsonObject>
}
