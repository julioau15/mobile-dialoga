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

// Rotas usadas pela Agenda do paciente
interface AgendaService {

    // Quem está logado (traz o id do paciente)
    @GET("pacientes/me")
    fun getPaciente(@Header("x-access-token") token: String): Call<JsonObject>

    // Vínculos (a API devolve os de todos: o app filtra pelo id do paciente)
    @GET("vinculos")
    fun getVinculos(@Header("x-access-token") token: String): Call<JsonObject>

    // Profissionais do paciente (nome de cada um)
    @GET("pacientes/me/profissionais")
    fun getProfissionais(@Header("x-access-token") token: String): Call<JsonObject>

    // Consultas (a API devolve as de todos: o app filtra pelos vínculos do paciente)
    @GET("consultas")
    fun getConsultas(@Header("x-access-token") token: String): Call<JsonObject>

    // O back compara o Content-Type exatamente com "application/json" (sem charset): por isso o @Headers
    @Headers("Content-Type: application/json")
    @POST("consultas")
    fun criarConsulta(
        @Header("x-access-token") token: String,
        @Body body: JsonObject
    ): Call<JsonObject>

    // Cancelar = apagar: o back não tem outro jeito (o PUT não altera o status)
    @DELETE("consultas/{id}")
    fun cancelarConsulta(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @PUT("vinculos/{id}/aceitar")
    fun aceitarVinculo(
        @Header("x-access-token") token: String,
        @Path("id") idVinculo: Int
    ): Call<JsonObject>

    @PUT("vinculos/{id}/recusar")
    fun recusarVinculo(
        @Header("x-access-token") token: String,
        @Path("id") idVinculo: Int
    ): Call<JsonObject>
}
