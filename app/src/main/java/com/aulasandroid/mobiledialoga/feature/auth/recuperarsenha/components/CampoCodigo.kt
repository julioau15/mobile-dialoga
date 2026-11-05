package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Um único campo de texto desenhado como caixinhas, uma por dígito
@Composable
fun CampoCodigo(
    codigo: String,
    onCodigoChange: (String) -> Unit,
    tamanho: Int,
    modifier: Modifier = Modifier,
    comErro: Boolean = false
) {
    BasicTextField(
        value = codigo,
        onValueChange = onCodigoChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(tamanho) { i ->
                    // Destaca a próxima caixa a ser preenchida
                    val corBorda = when {
                        comErro -> CorErro
                        i == codigo.length -> CorPrimaria
                        else -> CorBorda
                    }
                    val formato = RoundedCornerShape(12.dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(CorBranco, formato)
                            .border(1.dp, corBorda, formato)
                    ) {
                        Text(
                            text = codigo.getOrNull(i)?.toString().orEmpty(),
                            color = CorTexto,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}
