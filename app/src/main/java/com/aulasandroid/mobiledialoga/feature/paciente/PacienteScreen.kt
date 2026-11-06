package com.aulasandroid.mobiledialoga.feature.paciente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.data.useAuth

@Composable
fun PacienteScreen(onLogout: () -> Unit) {
    val authState = useAuth()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bem-vindo(a), Paciente!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { authState.logout(); onLogout() }) {
            Text("Sair")
        }
    }
}
