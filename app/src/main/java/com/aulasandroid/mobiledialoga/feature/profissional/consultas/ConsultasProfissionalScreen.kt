package com.aulasandroid.mobiledialoga.feature.profissional.consultas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.diaParaBr
import com.aulasandroid.mobiledialoga.components.isoParaDia
import com.aulasandroid.mobiledialoga.data.remote.RetrofitFactory
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.profissional.disponibilidade.DisponibilidadeSecao
import com.aulasandroid.mobiledialoga.feature.vinculos.components.listaEm
import com.aulasandroid.mobiledialoga.feature.vinculos.components.txt
import com.aulasandroid.mobiledialoga.ui.theme.CorFundo
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import retrofit2.awaitResponse

private data class ConsultaDoProfissional(val dia: Dia, val hora: String, val paciente: String, val especialidade: String?)

/**
 * Agenda do profissional: seus horários de atendimento (que definem quando o paciente pode marcar)
 * e as próximas consultas marcadas com os pacientes vinculados.
 */
@Composable
fun ConsultasProfissionalScreen(modifier: Modifier = Modifier) {
    val token = useAuth().auth?.token
    var consultas by remember { mutableStateOf<List<ConsultaDoProfissional>?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        if (token.isNullOrBlank()) return@LaunchedEffect
        try {
            val r = RetrofitFactory().getProfissionalService().getMinhasConsultas(token).awaitResponse()
            if (!r.isSuccessful && r.code() != 404) {
                erro = "Não foi possível carregar as consultas."
                return@LaunchedEffect
            }
            val hoje = Dia.hoje()
            val agora = Dia.horaAgora()
            consultas = (if (r.isSuccessful) r.body() else null).listaEm("consulta").mapNotNull { el ->
                try {
                    val o = el.asJsonObject
                    if ((o.txt("status") ?: "").lowercase().contains("cancel")) return@mapNotNull null
                    val dia = Dia.deIso(o.txt("data")) ?: return@mapNotNull null
                    val hora = (o.txt("hora") ?: return@mapNotNull null).take(5)
                    if (dia < hoje || (dia == hoje && hora < agora)) return@mapNotNull null
                    ConsultaDoProfissional(dia, hora, o.txt("paciente_nome") ?: "Paciente", o.txt("especialidade"))
                } catch (_: Exception) {
                    null
                }
            }.sortedWith(compareBy({ it.dia }, { it.hora }))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            erro = "Não foi possível conectar ao servidor."
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .background(CorFundo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        DisponibilidadeSecao(token = token)

        DialogaCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Próximas consultas", color = CorTexto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                val lista = consultas
                when {
                    erro != null -> Text(erro!!, color = CorSecundaria, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    lista == null -> Text("Carregando...", color = CorSecundaria, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    lista.isEmpty() -> Text("Nenhuma consulta marcada.", color = CorSecundaria, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    else -> lista.take(15).forEach { c ->
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(c.paciente, color = CorTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${diaParaBr(c.dia)} às ${c.hora}" + (c.especialidade?.let { " • $it" } ?: ""),
                                color = CorSecundaria, fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
