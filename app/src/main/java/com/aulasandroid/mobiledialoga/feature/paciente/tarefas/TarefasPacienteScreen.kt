package com.aulasandroid.mobiledialoga.feature.paciente.tarefas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun TarefasPacienteScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Tarefas",
        descricao = "Tarefas e atividades recomendadas para o paciente",
        icone = Icons.Default.CheckCircle,
        modifier = modifier
    )
}
