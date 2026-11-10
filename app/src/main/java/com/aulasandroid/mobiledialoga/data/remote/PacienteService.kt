package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PacienteService {
    @Multipart
    @POST("pacientes")
    fun cadastrarPaciente(
        @Part("nome_completo") nome: RequestBody,
        @Part("email") email: RequestBody,
        @Part("celular") celular: RequestBody,
        @Part("senha") senha: RequestBody
    ): Call<JsonObject>

    @GET("pacientes/me")
    fun getPerfilPaciente(@Header("x-access-token") token: String): Call<JsonObject>

    @POST("pacientes/me/primeiro-acesso")
    fun concluirPrimeiroAcesso(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @POST("pacientes/me/profissionais/{id}")
    fun vincularProfissional(
        @Header("x-access-token") token: String,
        @Path("id") idProfissional: String
    ): Call<JsonObject>

    // O back só aceita Content-Type exatamente "application/json" (sem "; charset=UTF-8").
    @Headers("Content-Type: application/json")
    @POST("pacientes/me/registros-humor")
    fun registrarHumor(
        @Header("x-access-token") token: String,
        @Body corpo: JsonObject
    ): Call<JsonObject>

    @GET("pacientes/me/registros-humor")
    fun getMeusRegistrosHumor(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("pacientes/me/tratamentos")
    fun getMeusTratamentos(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("pacientes/me/profissionais")
    fun getMeusProfissionais(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("pacientes/busca")
    fun buscarPacientes(
        @Header("x-access-token") token: String?,
        @Query("q") query: String
    ): Call<JsonObject>

    // Endpoints genéricos que o paciente consome
    @GET("tarefas")
    fun getMinhasTarefas(@Header("x-access-token") token: String): Call<JsonObject>

    // Trocar por "pacientes/me/consultas" depois de criar a rota no back (ver correcoes-backend.md, item 3).
    @GET("consultas")
    fun getMinhasConsultas(@Header("x-access-token") token: String): Call<JsonObject>
}