package com.aulasandroid.mobiledialoga.feature.paciente.consultas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aulasandroid.mobiledialoga.data.repository.AgendaException
import com.aulasandroid.mobiledialoga.data.repository.AgendaRepository
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val ERRO_SESSAO = "Sua sessão expirou. Faça login novamente."
private const val ERRO_GENERICO = "Algo deu errado. Tente novamente."

class AgendaViewModel : ViewModel() {

    private val repositorio = AgendaRepository()

    private val _uiState = MutableLiveData(AgendaUiState())
    val uiState: LiveData<AgendaUiState> = _uiState

    private val estado: AgendaUiState get() = _uiState.value ?: AgendaUiState()

    // ---------- Carregamento ----------

    fun carregar(token: String?) {
        if (token.isNullOrBlank()) {
            _uiState.value = estado.copy(carregando = false, erroCarregamento = ERRO_SESSAO)
            return
        }
        viewModelScope.launch {
            _uiState.value = estado.copy(carregando = true, erroCarregamento = null)
            try {
                val dados = repositorio.carregar(token)
                _uiState.value = estado.copy(
                    carregando = false,
                    consultas = dados.consultas,
                    profissionais = dados.profissionais,
                    idsVinculos = dados.idsVinculos
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = estado.copy(carregando = false, erroCarregamento = mensagemDe(e))
            }
        }
    }

    // ---------- Calendário ----------

    fun selecionarDia(dia: Dia) {
        _uiState.value = estado.copy(diaSelecionado = dia)
    }

    fun mudarVisao(visao: VisaoAgenda) {
        _uiState.value = estado.copy(visao = visao)
    }

    // Setas ‹ ›: direcao = -1 (anterior) ou +1 (próximo). Anda um mês, uma semana ou um dia, conforme a visão.
    fun navegar(direcao: Int) {
        _uiState.value = estado.copy(diaSelecionado = AgendaCalendario.mover(estado.visao, estado.diaSelecionado, direcao))
    }

    // ---------- Nova consulta ----------

    fun abrirNovaConsulta() {
        _uiState.value = estado.copy(formulario = NovaConsultaForm())
    }

    fun fecharNovaConsulta() {
        if (estado.salvando) return
        _uiState.value = estado.copy(formulario = null)
    }

    // Digitar de novo desfaz o profissional escolhido (a escolha vale pelo que está escrito no campo)
    fun onProfissionalTextoChange(texto: String) = atualizarFormulario {
        it.copy(profissionalTexto = texto, profissional = null, erroProfissional = null, erroGeral = null)
    }

    fun onProfissionalEscolhido(profissional: ProfissionalVinculado) = atualizarFormulario {
        it.copy(profissionalTexto = profissional.nome, profissional = profissional, erroProfissional = null, erroGeral = null)
    }

    fun onEspecialidadeChange(texto: String) = atualizarFormulario { it.copy(especialidade = texto, erroGeral = null) }

    fun onDataChange(dia: Dia) = atualizarFormulario { it.copy(data = dia, erroData = null, erroHora = null, erroGeral = null) }

    fun onHoraChange(hora: String) = atualizarFormulario { it.copy(hora = hora, erroHora = null, erroGeral = null) }

    fun onObservacoesChange(texto: String) = atualizarFormulario { it.copy(observacoes = texto) }

    fun onLembreteChange(ligado: Boolean) = atualizarFormulario { it.copy(lembrete = ligado) }

    private fun atualizarFormulario(mudar: (NovaConsultaForm) -> NovaConsultaForm) {
        val atual = estado.formulario ?: return
        _uiState.value = estado.copy(formulario = mudar(atual))
    }

    /**
     * "Salvar" (manterAberto = false) fecha o pop-up; "Adicionar outra" (manterAberto = true) limpa os
     * campos e deixa o pop-up aberto para a próxima consulta.
     */
    fun salvar(token: String?, manterAberto: Boolean) {
        val form = estado.formulario ?: return
        if (estado.salvando) return

        // Texto digitado que bate com o nome de um profissional vinculado vale como escolhido
        val profissional = form.profissional ?: estado.profissionais.firstOrNull {
            it.nome.equals(form.profissionalTexto.trim(), ignoreCase = true)
        }

        val erros = AgendaRegras.validarNovaConsulta(
            profissional = profissional,
            haProfissionais = estado.profissionais.isNotEmpty(),
            dia = form.data,
            hora = form.hora,
            hoje = Dia.hoje(),
            horaAgora = Dia.horaAgora()
        )
        _uiState.value = estado.copy(
            formulario = form.copy(
                profissional = profissional,
                erroProfissional = erros.profissional,
                erroData = erros.data,
                erroHora = erros.hora,
                erroGeral = null
            )
        )
        if (erros.temErro) return

        if (token.isNullOrBlank()) {
            atualizarFormulario { it.copy(erroGeral = ERRO_SESSAO) }
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(salvando = true)

            // 1) Agenda. Se falhar, o pop-up continua aberto com o erro.
            try {
                repositorio.agendar(token, profissional!!, form.data!!, form.hora!!, form.especialidade)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = estado.copy(salvando = false)
                atualizarFormulario { it.copy(erroGeral = mensagemDe(e)) }
                return@launch
            }

            // 2) Atualiza a lista. É um passo à parte: se falhar, a consulta JÁ foi agendada
            //    (não pode parecer erro de agendamento, senão o usuário tenta de novo e duplica).
            val atualizadas = try {
                repositorio.consultasDosVinculos(token, estado.idsVinculos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }

            _uiState.value = estado.copy(
                salvando = false,
                consultas = atualizadas ?: estado.consultas,
                formulario = if (manterAberto) NovaConsultaForm() else null,
                toast = if (atualizadas != null) {
                    MensagemToast("Consulta agendada!")
                } else {
                    MensagemToast("Consulta agendada, mas não foi possível atualizar a lista. Abra a Agenda de novo.", erro = true)
                }
            )
        }
    }

    // ---------- Cancelar consulta ----------

    fun pedirCancelamento(consulta: Consulta) {
        _uiState.value = estado.copy(consultaParaCancelar = consulta)
    }

    fun desistirDoCancelamento() {
        if (estado.cancelando) return
        _uiState.value = estado.copy(consultaParaCancelar = null)
    }

    fun confirmarCancelamento(token: String?) {
        val consulta = estado.consultaParaCancelar ?: return
        if (estado.cancelando) return

        if (token.isNullOrBlank()) {
            _uiState.value = estado.copy(consultaParaCancelar = null, toast = MensagemToast(ERRO_SESSAO, erro = true))
            return
        }

        viewModelScope.launch {
            _uiState.value = estado.copy(cancelando = true)
            try {
                repositorio.cancelar(token, consulta.id)
                _uiState.value = estado.copy(
                    cancelando = false,
                    consultaParaCancelar = null,
                    consultas = estado.consultas.filter { it.id != consulta.id },
                    toast = MensagemToast("Consulta cancelada!")
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = estado.copy(
                    cancelando = false,
                    consultaParaCancelar = null,
                    toast = MensagemToast(mensagemDe(e), erro = true)
                )
            }
        }
    }

    // ---------- Aviso ----------

    fun fecharToast() {
        _uiState.value = estado.copy(toast = null)
    }

    private fun mensagemDe(e: Exception): String =
        if (e is AgendaException) e.message ?: ERRO_GENERICO else ERRO_GENERICO
}
