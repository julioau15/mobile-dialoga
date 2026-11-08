package com.aulasandroid.mobiledialoga.feature.paciente.perfil

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun PerfilPacienteScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Perfil",
        descricao = "Gerenciamento de dados e perfil do paciente",
        icone = Icons.Default.Person,
        modifier = modifier
    )
}
