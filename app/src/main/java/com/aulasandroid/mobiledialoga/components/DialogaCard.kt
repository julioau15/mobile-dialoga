package com.aulasandroid.mobiledialoga.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.ui.theme.CorBordaCard
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Card branco das telas de autenticação
@Composable
fun DialogaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CorBranco),
        border = BorderStroke(1.dp, CorBordaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp), content = content)
    }
}

// Título + subtítulo do topo do card ("Criar conta" / "Informe seus dados pessoais")
@Composable
fun CabecalhoCard(titulo: String, subtitulo: String) {
    Text(text = titulo, color = CorTexto, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    Text(
        text = subtitulo,
        color = CorSecundaria,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
    )
}
