package com.aulasandroid.mobiledialoga.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria


private val FormatoBotao = RoundedCornerShape(12.dp)

// Botão principal roxo ("Entrar", "Continuar", "Criar conta"...)
@Composable
fun DialogaButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = FormatoBotao,
        colors = ButtonDefaults.buttonColors(
            containerColor = CorPrimaria,
            contentColor = CorBranco,
            disabledContainerColor = Color(0xFFE8E1F2),
            disabledContentColor = CorPrimaria.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .height(46.dp)
            .shadow(if (enabled && !isLoading) 4.dp else 0.dp, FormatoBotao, ambientColor = CorPrimaria, spotColor = CorPrimaria)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = CorBranco,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Botão branco com borda ("Voltar", "Continuar com Google")
@Composable
fun DialogaOutlinedButton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    corConteudo: Color = CorSecundaria,
    corBorda: Color = CorBorda,
    @DrawableRes icone: Int? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = FormatoBotao,
        border = BorderStroke(1.dp, corBorda),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CorBranco,
            contentColor = corConteudo,
            disabledContainerColor = Color(0xFFF5F5F5),
            disabledContentColor = corConteudo.copy(alpha = 0.5f)
        ),
        modifier = modifier.height(46.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = corConteudo,
                strokeWidth = 2.dp
            )
        } else {
            if (icone != null) {
                Image(painter = painterResource(icone), contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
