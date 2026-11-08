package com.aulasandroid.mobiledialoga.feature.profissional.tarefas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun TarefasProfissionalScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Tarefas",
        descricao = "Acompanhamento e gestão de tarefas dos pacientes",
        icone = Icons.Default.CheckCircle,
        modifier = modifier
    )
}
