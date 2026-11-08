package com.aulasandroid.mobiledialoga.feature.profissional.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aulasandroid.mobiledialoga.components.ScreenContentCard

@Composable
fun ProfissionalHomeScreen(modifier: Modifier = Modifier) {
    ScreenContentCard(
        nomeTela = "Dashboard",
        descricao = "Painel principal do profissional de saúde",
        icone = Icons.Default.Home,
        modifier = modifier
    )
}
