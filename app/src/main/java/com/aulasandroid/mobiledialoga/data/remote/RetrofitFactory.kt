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
    fun getTarefaService(): TarefaService = retrofit.create(TarefaService::class.java)
    fun getConsultaService(): ConsultaService = retrofit.create(ConsultaService::class.java)
    fun getTratamentoService(): TratamentoService = retrofit.create(TratamentoService::class.java)
    fun getNotificacaoService(): NotificacaoService = retrofit.create(NotificacaoService::class.java)
    fun getRegistroHumorService(): RegistroHumorService = retrofit.create(RegistroHumorService::class.java)
    fun getAnotacaoService(): AnotacaoService = retrofit.create(AnotacaoService::class.java)
    fun getSolicitacaoAcessoService(): SolicitacaoAcessoService = retrofit.create(SolicitacaoAcessoService::class.java)
    fun getAgendaService(): AgendaService = retrofit.create(AgendaService::class.java)
}
