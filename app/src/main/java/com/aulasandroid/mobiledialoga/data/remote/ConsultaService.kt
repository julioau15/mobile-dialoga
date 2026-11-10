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
import retrofit2.http.Query

interface ConsultaService {
    @GET("consultas")
    fun listarConsultas(
        @Header("x-access-token") token: String,
        @Query("status") status: String? = null
    ): Call<JsonObject>

    @GET("consultas/{id}")
    fun buscarConsultaPorId(@Path("id") id: Int): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("consultas")
    fun agendarConsulta(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @PUT("consultas/{id}")
    fun atualizarConsulta(
        @Path("id") id: Int,
        @Body dados: JsonObject
    ): Call<JsonObject>

    @DELETE("consultas/{id}")
    fun cancelarConsulta(@Path("id") id: Int): Call<JsonObject>
}
