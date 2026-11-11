package com.aulasandroid.mobiledialoga.data.remote

import com.aulasandroid.mobiledialoga.data.ApiRotas
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiRotas.BASE_URL + "/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getAuthService(): AuthService = retrofit.create(AuthService::class.java)
    fun getEspecialidadeService(): EspecialidadeService = retrofit.create(EspecialidadeService::class.java)
    fun getPacienteService(): PacienteService = retrofit.create(PacienteService::class.java)
    fun getProfissionalService(): ProfissionalService = retrofit.create(ProfissionalService::class.java)
    fun getAgendaService(): AgendaService = retrofit.create(AgendaService::class.java)
}
