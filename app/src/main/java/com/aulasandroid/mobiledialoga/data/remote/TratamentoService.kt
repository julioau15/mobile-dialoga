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

interface TratamentoService {
    @GET("pacientes/me/tratamentos")
    fun listarTratamentosMe(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("tratamentos")
    fun listarTratamentos(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("tratamentos/{id}")
    fun buscarTratamentoPorId(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tratamentos")
    fun criarTratamento(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    /** Cria o tratamento do paciente logado (o POST /tratamentos puro não grava o id_paciente). */
    @Headers("Content-Type: application/json")
    @POST("pacientes/me/tratamentos")
    fun criarMeuTratamento(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("tratamentos/{id}")
    fun atualizarTratamento(
        @Header("x-access-token") token: String,
        @Path("id") id: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @DELETE("tratamentos/{id}")
    fun excluirTratamento(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("registros-dose")
    fun registrarDose(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>
}
