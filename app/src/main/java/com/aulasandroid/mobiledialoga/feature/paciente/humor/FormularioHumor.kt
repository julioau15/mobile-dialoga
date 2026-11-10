package com.aulasandroid.mobiledialoga.feature.paciente.humor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.gson.JsonObject

/**
 * Estado que gerencia os dados do formulário de humor.
 * Centraliza a lógica de alteração e validação.
 * (A UI do formulário fica em RegistroHumorDialog.kt)
 */
class HumorFormState(
    sentimentoInicial: String? = null,
    energiaInicial: String? = null,
    humorInicial: String? = null,
    horasSonoInicial: String = "",
    relatoInicial: String = "",
    pensamentoInicial: String? = null
) {
    var sentimento by mutableStateOf(sentimentoInicial)
    var energia by mutableStateOf(energiaInicial)
    var humor by mutableStateOf(humorInicial)
    var horasSono by mutableStateOf(horasSonoInicial)
    var relato by mutableStateOf(relatoInicial)
    var pensamentoSelecionado by mutableStateOf(pensamentoInicial)

    val horasNum: Double? get() = horasSono.replace(',', '.').toDoubleOrNull()

    val isValido: Boolean
        get() = sentimento != null &&
                energia != null &&
                humor != null &&
                pensamentoSelecionado != null &&
                horasNum != null && (horasNum!! in 0.0..24.0)

    /**
     * Corpo enviado para POST /pacientes/me/registros-humor.
     * As chaves precisam bater com o que o DAO do back usa em insertRegistroHumor:
     * sentimento, energia, humor, padrao_pensamentos, horas_sono, relato.
     *
     * - "padrao_pensamentos": antes era enviado como "pensamento", e o back ignorava.
     * - "relato": sempre enviado. Se fosse null, o Gson omitiria a chave, o DAO receberia
     *   undefined e o knex.raw falharia. Vazio vai como "".
     */
    fun toJsonObject(): JsonObject {
        return JsonObject().apply {
            addProperty("sentimento", sentimento.orEmpty())
            addProperty("energia", energia.orEmpty())
            addProperty("humor", humor.orEmpty())
            addProperty("padrao_pensamentos", pensamentoSelecionado.orEmpty())
            addProperty("horas_sono", horasNum ?: 0.0)
            addProperty("relato", relato.trim())
        }
    }
}

val HumorFormStateSaver = mapSaver(
    save = {
        mapOf(
            "sentimento" to it.sentimento,
            "energia" to it.energia,
            "humor" to it.humor,
            "horasSono" to it.horasSono,
            "relato" to it.relato,
            "pensamento" to it.pensamentoSelecionado
        )
    },
    restore = {
        HumorFormState(
            sentimentoInicial = it["sentimento"] as String?,
            energiaInicial = it["energia"] as String?,
            humorInicial = it["humor"] as String?,
            horasSonoInicial = it["horasSono"] as String? ?: "",
            relatoInicial = it["relato"] as String? ?: "",
            pensamentoInicial = it["pensamento"] as String?
        )
    }
)

@Composable
fun rememberHumorFormState(): HumorFormState {
    return rememberSaveable(saver = HumorFormStateSaver) {
        HumorFormState()
    }
}