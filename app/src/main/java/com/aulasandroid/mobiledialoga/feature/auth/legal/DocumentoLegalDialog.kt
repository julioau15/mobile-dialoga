package com.aulasandroid.mobiledialoga.feature.auth.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaDialogo
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorSecundaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto

/** Pop-up rolável com o Termos de Uso ou a Política de Privacidade. */
@Composable
fun DocumentoLegalDialog(
    tipo: TipoDocumento,
    onFechar: () -> Unit
) {
    val documento = documentoDe(tipo)

    DialogaDialogo(
        titulo = documento.titulo,
        onFechar = onFechar,
        rodape = {
            DialogaButton(texto = "Fechar", onClick = onFechar, modifier = Modifier.width(120.dp))
        }
    ) {
        Text(
            text = "Última atualização: ${documento.ultimaAtualizacao}  •  Versão ${documento.versao}",
            color = CorSecundaria,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        documento.introducao.forEach { texto ->
            Text(text = texto, color = CorTexto, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        documento.secoes.forEach { secao ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = secao.titulo,
                color = CorPrimaria,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            secao.blocos.forEach { bloco -> BlocoView(bloco) }
        }
    }
}

@Composable
private fun BlocoView(bloco: Bloco) {
    when (bloco) {
        is Bloco.Paragrafo -> {
            Text(
                text = bloco.texto,
                color = CorTexto,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        is Bloco.Subtitulo -> {
            Text(
                text = bloco.texto,
                color = CorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )
        }

        is Bloco.Lista -> {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                bloco.itens.forEach { item ->
                    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.Top) {
                        Text(text = "•", color = CorPrimaria, fontSize = 14.sp, modifier = Modifier.width(16.dp))
                        Text(text = item, color = CorTexto, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}
