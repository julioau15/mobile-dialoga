package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun login(@Body body: JsonObject): Call<JsonObject>

    @GET("auth/verificar-cadastro")
    fun verificarCadastro(
        @Query("email") email: String,
        @Query("celular") celular: String,
        @Query("cpf") cpf: String? = null,
        @Query("crp") crp: String? = null
    ): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("auth/recuperar-senha/solicitar")
    fun solicitarRecuperarSenha(@Body body: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("auth/recuperar-senha/validar-codigo")
    fun validarCodigoRecuperacao(@Body body: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("auth/recuperar-senha/redefinir")
    fun redefinirSenha(@Body body: JsonObject): Call<JsonObject>
}
