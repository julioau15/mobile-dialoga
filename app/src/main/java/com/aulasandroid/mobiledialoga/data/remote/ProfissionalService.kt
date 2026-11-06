package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
        @Part("id_especialidade") idEspecialidade: RequestBody
    ): Call<JsonObject>
}
