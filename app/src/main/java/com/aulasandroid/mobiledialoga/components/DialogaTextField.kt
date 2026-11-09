package com.aulasandroid.mobiledialoga.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RotuloCampo
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


val FormatoCampo = RoundedCornerShape(12.dp)

@Composable
fun coresCampo(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CorPrimaria,
    unfocusedBorderColor = CorBorda,
    errorBorderColor = CorErro,
    focusedContainerColor = CorBranco,
    unfocusedContainerColor = CorBranco,
    errorContainerColor = CorBranco,
    focusedTextColor = CorTexto,
    unfocusedTextColor = CorTexto,
    cursorColor = CorPrimaria,
    focusedPlaceholderColor = CorSecundaria,
    unfocusedPlaceholderColor = CorSecundaria,
    focusedLeadingIconColor = CorSecundaria,
    unfocusedLeadingIconColor = CorSecundaria,
    errorLeadingIconColor = CorSecundaria,
    focusedTrailingIconColor = CorSecundaria,
    unfocusedTrailingIconColor = CorSecundaria,
    errorTrailingIconColor = CorSecundaria
)

// Rótulo que fica acima dos campos ("E-mail", "Senha"...)
@Composable
fun RotuloCampo(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = CorTexto,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun MensagemErroCampo(erro: String?) {
    if (erro != null) {
        Text(
            text = erro,
            color = CorErro,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

// Componente STATELESS: o valor vem de fora (value) e a mudança sobe por onValueChange
@Composable
fun DialogaTextField(
    rotulo: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icone: Int? = null,
    erro: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        RotuloCampo(rotulo)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, fontSize = 14.sp) },
            leadingIcon = icone?.let {
                { Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon = trailingIcon,
            isError = erro != null,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = FormatoCampo,
            colors = coresCampo(),
            modifier = Modifier.fillMaxWidth()
        )
        MensagemErroCampo(erro)
    }
}

// Campo de senha com cadeado e (opcionalmente) o olho para mostrar/ocultar
@Composable
fun DialogaPasswordField(
    rotulo: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    erro: String? = null,
    permitirMostrar: Boolean = true
) {
    // Estado só visual, não precisa subir para a ViewModel
    var visivel by remember { mutableStateOf(false) }

    DialogaTextField(
        rotulo = rotulo,
        placeholder = placeholder,
        value = value,
        onValueChange = onValueChange,
        icone = R.drawable.ic_lock,
        erro = erro,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (permitirMostrar) {
            {
                IconButton(onClick = { visivel = !visivel }) {
                    Icon(
                        painter = painterResource(if (visivel) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    )
}
