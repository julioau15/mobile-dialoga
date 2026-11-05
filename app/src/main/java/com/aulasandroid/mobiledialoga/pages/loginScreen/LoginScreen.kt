package com.aulasandroid.mobiledialoga.pages.loginScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorFundoLogin
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import com.aulasandroid.mobiledialoga.ui.theme.MobileDialogaTheme


/**
 * Tela de login — somente estilização (sem navegação, links ou chamadas à API).
 */
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val formato = RoundedCornerShape(14.dp)
    val camposCores = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CorPrimaria,
        unfocusedBorderColor = CorBorda,
        focusedContainerColor = CorBranco,
        unfocusedContainerColor = CorBranco,
        focusedTextColor = CorTexto,
        unfocusedTextColor = CorTexto,
        cursorColor = CorPrimaria,
        focusedPlaceholderColor = CorSecundaria,
        unfocusedPlaceholderColor = CorSecundaria,
        focusedLeadingIconColor = CorSecundaria,
        unfocusedLeadingIconColor = CorSecundaria,
        focusedTrailingIconColor = CorSecundaria,
        unfocusedTrailingIconColor = CorSecundaria
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoLogin)
            .systemBarsPadding()
    ) {
        // ---------- Conteúdo principal (rolável) ----------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Image(
                painter = painterResource(R.drawable.logo_dialoga),
                contentDescription = "Dialoga",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(44.dp))

            Text(
                text = "Bem-vindo de volta",
                color = CorTexto,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Entre na sua conta para continuar",
                color = CorSecundaria,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ---------- E-mail ----------
            Text(
                text = "E-mail",
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("seu@email.com", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                shape = formato,
                colors = camposCores,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(16.dp))

            // ---------- Senha ----------
            Text(
                text = "Senha",
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = formato,
                colors = camposCores,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Esqueceu sua senha?",
                color = CorPrimaria,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ---------- Entrar ----------
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp), ambientColor = CorPrimaria, spotColor = CorPrimaria),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CorPrimaria, contentColor = CorBranco)
            ) {
                Text("Entrar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Divisor ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = CorSecundaria.copy(alpha = 0.6f))
                Text(
                    text = "ou continue com",
                    color = CorSecundaria,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = CorSecundaria.copy(alpha = 0.6f))
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Google ----------
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CorPrimaria),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CorBranco, contentColor = CorPrimaria)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Continuar com Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(48.dp))

            // ---------- Cadastro ----------
            Row(horizontalArrangement = Arrangement.Center) {
                Text(text = "Não tem conta? ", color = CorSecundaria, fontSize = 14.sp)
                Text(
                    text = "Cadastre-se",
                    color = CorPrimaria,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ---------- Rodapé ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Termos de Uso", color = CorSecundaria, fontSize = 13.sp)
            Text(text = "  |  ", color = CorSecundaria, fontSize = 13.sp)
            Text(text = "Política de Privacidade", color = CorSecundaria, fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MobileDialogaTheme() {
        LoginScreen()
    }
}
