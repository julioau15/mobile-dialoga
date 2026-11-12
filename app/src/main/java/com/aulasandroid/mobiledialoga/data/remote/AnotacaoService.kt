package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AnotacaoService {
    @GET("anotacoes")
    fun listarAnotacoes(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("anotacoes/{id}")
    fun buscarAnotacaoPorId(@Path("id") id: Int): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("anotacoes")
    fun criarAnotacao(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("anotacoes/{id}")
    fun atualizarAnotacao(
        @Path("id") id: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @DELETE("anotacoes/{id}")
    fun excluirAnotacao(@Path("id") id: Int): Call<JsonObject>

    // Anotações privadas do profissional logado sobre um paciente (o back responde 404 quando não há nenhuma)
    @GET("pacientes/{id}/anotacoes")
    fun listarAnotacoesDoPaciente(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: Int
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("pacientes/{id}/anotacoes")
    fun criarAnotacaoDoPaciente(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>
}