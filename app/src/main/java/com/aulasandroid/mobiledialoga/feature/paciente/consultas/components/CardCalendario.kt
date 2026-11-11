package com.aulasandroid.mobiledialoga.feature.paciente.consultas.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaCalendario
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.AgendaRegras
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Consulta
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.Dia
import com.aulasandroid.mobiledialoga.feature.paciente.consultas.VisaoAgenda
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard

/**
 * Card do calendário: setas + título + seletor Mês/Semana/Dia e, embaixo, a visão escolhida.
 * Na visão Semana o título e o seletor ficam na mesma linha (como no protótipo).
 */
@Composable
fun CardCalendario(
    visao: VisaoAgenda,
    diaSelecionado: Dia,
    consultas: List<Consulta>,
    onMudarVisao: (VisaoAgenda) -> Unit,
    onNavegar: (Int) -> Unit,
    onSelecionarDia: (Dia) -> Unit,
    modifier: Modifier = Modifier
) {
    val titulo = AgendaCalendario.tituloDaVisao(visao, diaSelecionado)

    CardAgenda(modifier = modifier) {
        if (visao == VisaoAgenda.SEMANA) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 10.dp)
            ) {
                NavegacaoCalendario(
                    titulo = titulo,
                    onAnterior = { onNavegar(-1) },
                    onProximo = { onNavegar(1) },
                    compacto = true,
                    modifier = Modifier.weight(1f)
                )
                SeletorVisao(visao = visao, onMudar = onMudarVisao, compacto = true)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                NavegacaoCalendario(
                    titulo = titulo,
                    onAnterior = { onNavegar(-1) },
                    onProximo = { onNavegar(1) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SeletorVisao(visao = visao, onMudar = onMudarVisao)
            }
        }
        HorizontalDivider(color = CorBordaCard)

        when (visao) {
            VisaoAgenda.MES -> {
                val diasComConsulta = remember(consultas) { AgendaRegras.diasComConsulta(consultas) }
                CalendarioMes(
                    diaSelecionado = diaSelecionado,
                    diasComConsulta = diasComConsulta,
                    onSelecionarDia = onSelecionarDia
                )
            }

            VisaoAgenda.DIA -> {
                val doDia = remember(consultas, diaSelecionado) { AgendaRegras.doDia(consultas, diaSelecionado) }
                GradeDia(
                    dia = diaSelecionado,
                    consultasDoDia = doDia,
                    horas = AgendaCalendario.horasDaGrade(doDia)
                )
            }

            VisaoAgenda.SEMANA -> {
                val dias = remember(diaSelecionado) { AgendaCalendario.semanaDe(diaSelecionado) }
                val daSemana = remember(consultas, dias) { AgendaRegras.dosDias(consultas, dias) }
                GradeSemana(
                    dias = dias,
                    diaSelecionado = diaSelecionado,
                    consultasDaSemana = daSemana,
                    horas = AgendaCalendario.horasDaGrade(daSemana),
                    onSelecionarDia = onSelecionarDia
                )
            }
        }
    }
}
