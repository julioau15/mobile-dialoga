package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SolicitacaoAcessoService {
    @GET("solicitacoes-acesso")
    fun listarSolicitacoesAcesso(@Header("x-access-token") token: String): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("solicitacoes-acesso")
    fun solicitarAcesso(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @PUT("solicitacoes-acesso/{id}/aprovar")
    fun aprovarSolicitacaoAcesso(@Path("id") id: Int): Call<JsonObject>

    @PUT("solicitacoes-acesso/{id}/recusar")
    fun recusarSolicitacaoAcesso(@Path("id") id: Int): Call<JsonObject>
}
