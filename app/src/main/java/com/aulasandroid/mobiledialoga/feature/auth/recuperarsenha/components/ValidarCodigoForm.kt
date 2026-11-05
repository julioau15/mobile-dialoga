package com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.MensagemErroCampo
import com.aulasandroid.mobiledialoga.feature.auth.recuperarsenha.RecuperarSenhaViewModel
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSucesso
import com.aulasandroid.mobiledialoga.ui.theme.CorSucessoClara
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto


// Etapa 2: "E-mail enviado!" + código
@Composable
fun ValidarCodigoForm(
    codigo: String,
    erroCodigo: String?,
    onCodigoChange: (String) -> Unit,
    onRedefinirSenha: () -> Unit,
    onReenviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogaCard(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(56.dp)
                .background(CorSucessoClara, RoundedCornerShape(16.dp))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = CorSucesso,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "E-mail enviado!",
            color = CorTexto,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Verifique sua caixa de entrada. O link para redefinir a senha expira em 30 minutos.",
            color = CorSecundaria,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CampoCodigo(
            codigo = codigo,
            onCodigoChange = onCodigoChange,
            tamanho = RecuperarSenhaViewModel.TAMANHO_CODIGO,
            comErro = erroCodigo != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )
        MensagemErroCampo(erroCodigo)

        Spacer(modifier = Modifier.height(16.dp))

        DialogaButton(texto = "Redefinir senha", onClick = onRedefinirSenha, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enviar novamente",
            color = CorSecundaria,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onReenviar)
        )
    }
}
