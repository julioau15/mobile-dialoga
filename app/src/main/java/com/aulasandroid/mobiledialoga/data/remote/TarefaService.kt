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

/**
 * No back, TODAS as rotas /tarefas e /tarefas-destinatarios passam pelo verifyJWT (header x-access-token).
 * Por isso cada chamada recebe o token. Nas funções antigas ele é o último parâmetro e opcional (null = não envia
 * o header), para o código do profissional continuar compilando; o ideal é passar o token em todas.
 */
interface TarefaService {
    @GET("tarefas")
    fun listarTarefas(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("tarefas/{id}")
    fun buscarTarefaPorId(
        @Path("id") id: Int,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

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
        @Part anexoArquivo: MultipartBody.Part? = null,
        // usado quando é o PACIENTE quem cria a tarefa (precisa do ajuste no back, ver correcoes-backend.md)
        @Part("prazo") prazo: RequestBody? = null,
        @Part("anexo_url") anexoUrl: RequestBody? = null
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tarefas/{id}/perguntas")
    fun adicionarPergunta(
        @Path("id") idTarefa: Int,
        @Body dados: JsonObject,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

    @GET("tarefas/{id}/perguntas")
    fun listarPerguntasPorTarefa(
        @Path("id") idTarefa: Int,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

    @PUT("tarefas/{id}/enviar")
    fun enviarTarefaParaPaciente(
        @Path("id") idTarefa: Int,
        @Header("x-access-token") token: String
    ): Call<JsonObject>

    @DELETE("tarefas/{id}")
    fun excluirTarefa(
        @Path("id") idTarefa: Int,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

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
        @Query("status") status: String? = null,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

    @GET("tarefas-destinatarios/{id}/respostas")
    fun listarRespostasPorTarefaDestinatario(
        @Path("id") idTarefaDestinatario: Int,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("tarefas-destinatarios/{id}/respostas")
    fun enviarRespostas(
        @Path("id") idTarefaDestinatario: Int,
        @Body dados: JsonObject,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("tarefas-destinatarios/{id}")
    fun atualizarStatusTarefaDestinatario(
        @Path("id") idTarefaDestinatario: Int,
        @Body dados: JsonObject,
        @Header("x-access-token") token: String? = null
    ): Call<JsonObject>
}