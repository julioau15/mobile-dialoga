package com.aulasandroid.mobiledialoga.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

@Composable
fun ScreenContentCard(
    nomeTela: String,
    descricao: String? = null,
    icone: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        DialogaCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (icone != null) {
                    Icon(
                        imageVector = icone,
                        contentDescription = nomeTela,
                        tint = CorPrimaria,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = nomeTela,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CorTexto,
                    textAlign = TextAlign.Center
                )
                if (!descricao.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
