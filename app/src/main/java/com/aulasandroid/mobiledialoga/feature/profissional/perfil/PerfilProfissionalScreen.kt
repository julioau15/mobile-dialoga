package com.aulasandroid.mobiledialoga.feature.profissional.perfil

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun PerfilProfissionalScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Perfil",
        descricao = "Dados do cadastro e perfil do profissional",
        icone = Icons.Default.Person,
        modifier = modifier
    )
}
