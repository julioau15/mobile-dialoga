package com.aulasandroid.mobiledialoga.data.remote

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface EspecialidadeService {
    @GET("especialidades")
    fun listarEspecialidades(): Call<JsonObject>
}
