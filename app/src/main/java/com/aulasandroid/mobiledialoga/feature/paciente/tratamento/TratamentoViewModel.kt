package com.aulasandroid.mobiledialoga.feature.paciente.tratamento

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.AuthState
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class TratamentoViewModel : ViewModel() {

    var state by mutableStateOf(TratamentosState())
        private set

    private val service get() = RetrofitFactory().getTratamentoService()

    fun carregar(authState: AuthState) {
        val token = authState.auth?.token ?: return
        state = state.copy(carregando = true, erro = null)
        viewModelScope.launch {
            try {
                val resp = service.listarTratamentosMe(token).awaitResponse()
                state = when {
                    resp.isSuccessful ->
                        state.copy(carregando = false, tratamentos = TratamentoMapper.lista(resp.body()))
                    resp.code() == 404 -> // o back responde 404 quando não há tratamentos
                        state.copy(carregando = false, tratamentos = emptyList())
                    else -> state.copy(carregando = false, erro = "Não foi possível carregar os tratamentos.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = state.copy(carregando = false, erro = "Sem conexão com o servidor. Verifique sua internet.")
            }
        }
    }

    /** [id] nulo cria; com id, edita. [onSucesso] roda depois de salvar e recarregar a lista. */
    fun salvar(authState: AuthState, id: Int?, form: FormTratamento, onSucesso: () -> Unit) {
        val token = authState.auth?.token ?: return
        if (state.salvando) return
        state = state.copy(salvando = true)

        viewModelScope.launch {
            try {
                val corpo = TratamentoMapper.corpo(form)
                val resp = if (id == null) service.criarMeuTratamento(token, corpo).awaitResponse()
                else service.atualizarTratamento(token, id, corpo).awaitResponse()

                if (resp.isSuccessful) {
                    state = state.copy(
                        salvando = false,
                        aviso = if (id == null) "Tratamento adicionado!" else "Tratamento atualizado!"
                    )
                    carregar(authState)
                    onSucesso()
                } else {
                    state = state.copy(salvando = false, aviso = "Não foi possível salvar o tratamento. Tente novamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = state.copy(salvando = false, aviso = "Sem conexão com o servidor. Tente novamente.")
            }
        }
    }

    fun excluir(authState: AuthState, id: Int) {
        val token = authState.auth?.token ?: return
        if (state.excluindoId != null) return
        state = state.copy(excluindoId = id)

        viewModelScope.launch {
            try {
                val resp = service.excluirTratamento(token, id).awaitResponse()
                state = if (resp.isSuccessful) {
                    state.copy(
                        excluindoId = null,
                        tratamentos = state.tratamentos.filterNot { it.id == id },
                        aviso = "Tratamento excluído!"
                    )
                } else {
                    state.copy(excluindoId = null, aviso = "Não foi possível excluir o tratamento.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state = state.copy(excluindoId = null, aviso = "Sem conexão com o servidor. Tente novamente.")
            }
        }
    }

    fun limparAviso() {
        state = state.copy(aviso = null)
    }
}
