package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface RegistroHumorService {
    @GET("pacientes/me/registros-humor")
    fun listarRegistrosHumorMe(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("registros-humor")
    fun listarRegistrosHumor(@Header("x-access-token") token: String): Call<JsonObject>

    @GET("registros-humor/{id}")
    fun buscarRegistroHumorPorId(@Path("id") id: Int): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("registros-humor")
    fun criarRegistroHumor(
        @Header("x-access-token") token: String,
        @Body dados: JsonObject
    ): Call<JsonObject>
}
