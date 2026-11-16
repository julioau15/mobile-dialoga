package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.DELETE
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
    fun aprovarSolicitacaoAcesso(
        @Header("x-access-token") token: String,
        @Path("id") id: Int,
        @Body body: JsonObject = JsonObject()
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("solicitacoes-acesso/{id}/aprovar")
    fun aprovarSolicitacaoAcessoComCategorias(
        @Header("x-access-token") token: String,
        @Path("id") id: Int,
        @Body body: JsonObject
    ): Call<JsonObject>

    @PUT("solicitacoes-acesso/{id}/recusar")
    fun recusarSolicitacaoAcesso(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @PUT("solicitacoes-acesso/{id}/categorias")
    fun editarCategorias(
        @Header("x-access-token") token: String,
        @Path("id") id: Int,
        @Body body: JsonObject
    ): Call<JsonObject>

    @DELETE("solicitacoes-acesso/{id}")
    fun removerAcesso(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @GET("pacientes/{id}/indicadores-compartilhados")
    fun indicadoresCompartilhados(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: Int
    ): Call<JsonObject>

    // O profissional logado pede acesso aos indicadores de um paciente
    @POST("pacientes/{id}/solicitacoes-acesso")
    fun solicitarAcessoAoPaciente(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: Int
    ): Call<JsonObject>
}