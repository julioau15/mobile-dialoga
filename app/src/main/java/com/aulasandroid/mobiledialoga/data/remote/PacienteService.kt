package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PacienteService {
    @Multipart
    @POST("pacientes")
    fun cadastrarPaciente(
        @Part("nome_completo") nome: RequestBody,
        @Part("email") email: RequestBody,
        @Part("celular") celular: RequestBody,
        @Part("senha") senha: RequestBody
    ): Call<JsonObject>
}
