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

interface TarefaService {
    @GET("tarefas")
    fun listarTarefas(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("tarefas/{id}")
    fun buscarTarefaPorId(@Path("id") id: Int): Call<JsonObject>

    @Multipart
    @POST("tarefas")
    fun criarTarefaByToken(
        @Header("x-access-token") token: String,
        @Part("titulo") titulo: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("instrucoes") instrucoes: RequestBody?,
        @Part("is_rascunho") isRascunho: RequestBody?,
        @Part("paciente_ids") pacienteIds: RequestBody?,
        @Part("prazos_por_paciente") prazosPorPaciente: RequestBody?,
        @Part anexoArquivo: MultipartBody.Part? = null
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tarefas/{id}/perguntas")
    fun adicionarPergunta(
        @Path("id") idTarefa: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @GET("tarefas/{id}/perguntas")
    fun listarPerguntasPorTarefa(@Path("id") idTarefa: Int): Call<JsonObject>

    @PUT("tarefas/{id}/enviar")
    fun enviarTarefaParaPaciente(
        @Path("id") idTarefa: Int,
        @Header("x-access-token") token: String
    ): Call<JsonObject>

    @DELETE("tarefas/{id}")
    fun excluirTarefa(@Path("id") idTarefa: Int): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tarefas-destinatarios")
    fun atribuirTarefaDestinatario(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @GET("tarefas-destinatarios")
    fun filtrarTarefasDestinatarios(
        @Query("id_paciente") idPaciente: Int? = null,
        @Query("id_profissional") idProfissional: Int? = null,
        @Query("status") status: String? = null
    ): Call<JsonObject>

    @GET("tarefas-destinatarios/{id}/respostas")
    fun listarRespostasPorTarefaDestinatario(@Path("id") idTarefaDestinatario: Int): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tarefas-destinatarios/{id}/respostas")
    fun enviarRespostas(
        @Path("id") idTarefaDestinatario: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("tarefas-destinatarios/{id}")
    fun atualizarStatusTarefaDestinatario(
        @Path("id") idTarefaDestinatario: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>
}
