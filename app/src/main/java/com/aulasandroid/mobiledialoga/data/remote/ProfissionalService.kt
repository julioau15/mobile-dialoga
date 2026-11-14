package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfissionalService {
    @Multipart
    @POST("profissionais")
    fun cadastrarProfissional(
        @Part("nome_completo") nome: RequestBody,
        @Part("email") email: RequestBody,
        @Part("celular") celular: RequestBody,
        @Part("senha") senha: RequestBody,
        @Part("cpf") cpf: RequestBody,
        @Part("crp") crp: RequestBody,
        @Part("instituicao_clinica") instituicao: RequestBody?,
        @Part("id_especialidade") idEspecialidade: RequestBody,
        @Part("id_google") idGoogle: RequestBody? = null,
        @Part("foto_avatar") fotoAvatar: RequestBody? = null
    ): Call<JsonObject>

    @GET("profissionais/me")
    fun getPerfilProfissional(@Header("x-access-token") token: String): Call<JsonObject>

    @POST("profissionais/me/primeiro-acesso")
    fun concluirPrimeiroAcesso(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("profissionais/me/pacientes")
    fun listarPacientesVinculados(@Header("x-access-token") token: String): Call<JsonObject>

    @POST("profissionais/me/pacientes/{id}")
    fun vincularPaciente(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: String
    ): Call<JsonObject>

    @GET("profissionais/me/consultas")
    fun getMinhasConsultas(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("profissionais/me/disponibilidades")
    fun listarDisponibilidadesMe(@Header("x-access-token") token: String): Call<JsonObject>

    @POST("profissionais/me/disponibilidades")
    fun criarDisponibilidadeMe(
        @Header("x-access-token") token: String,
        @Body corpo: JsonObject
    ): Call<JsonObject>

    @DELETE("profissionais/me/disponibilidades/{id}")
    fun excluirDisponibilidade(
        @Header("x-access-token") token: String,
        @Path("id") id: Int
    ): Call<JsonObject>

    @GET("especialidades")
    fun listarEspecialidades(): Call<JsonObject>

    @GET("profissionais/busca")
    fun buscarProfissionais(
        @Header("x-access-token") token: String?,
        @Query("q") query: String
    ): Call<JsonObject>

    @GET("pacientes/busca")
    fun buscarPacientes(
        @Header("x-access-token") token: String?,
        @Query("busca") busca: String
    ): Call<JsonObject>

    @Multipart
    @PUT("profissionais/me")
    fun atualizarPerfilProfissionalMe(
        @Header("x-access-token") token: String,
        @Part("nome_completo") nome: RequestBody,
        @Part("instituicao_clinica") instituicao: RequestBody? = null,
        @Part("celular") celular: RequestBody,
        @Part("id_especialidade") idEspecialidade: RequestBody? = null,
        @Part fotoAvatar: MultipartBody.Part? = null
    ): Call<JsonObject>

    @DELETE("profissionais/me/pacientes/{id}")
    fun desvincularPaciente(
        @Header("x-access-token") token: String,
        @Path("id") idPaciente: String
    ): Call<JsonObject>
}
