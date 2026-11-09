package com.aulasandroid.mobiledialoga.feature.profissional.primeiroacesso

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BotaoPrimario
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BuscaPessoa
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.Cabecalho
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.CartaoAviso
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.DialogoVinculo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.EscolhaBinaria
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PessoaSugestao
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PrimeiroAcessoLayout
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.SelecionadoFundo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BordaSuave
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.TextoSuave
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.TextoTitulo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.textoComNegrito
import kotlinx.coroutines.delay

/**
 * Primeiro acesso do profissional (tela única).
 */
@Composable
fun PrimeiroAcessoProfissionalScreen(
    nome: String? = null,
    onFinalizar: () -> Unit,
    buscarPacientes: suspend (String) -> List<PessoaSugestao> = { emptyList() },
    onVincularPaciente: (PessoaSugestao) -> Unit = {}
) {
    var quer by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var busca by rememberSaveable { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    var resultados by remember { mutableStateOf(emptyList<PessoaSugestao>()) }
    var selecionado by remember { mutableStateOf<PessoaSugestao?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val primeiroNome = nome?.trim()?.split(" ")?.firstOrNull().orEmpty()
    val titulo = if (primeiroNome.isBlank()) "Bem-vindo ao Dialoga! 👋" else "Bem-vindo ao Dialoga, $primeiroNome! 👋"

    LaunchedEffect(busca) {
        if (selecionado != null || busca.trim().length < 2) {
            resultados = emptyList()
            buscando = false
            return@LaunchedEffect
        }
        buscando = true
        delay(500)
        resultados = try {
            buscarPacientes(busca.trim())
        } catch (e: Exception) {
            emptyList()
        }
        buscando = false
    }

    PrimeiroAcessoLayout {
        Cabecalho(
            ilustracao = R.drawable.ic_primeiroacesso_1, // Usando ícone padrão para prof
            titulo = titulo,
            descricao = "Aqui você acompanha seus pacientes, cria tarefas e gerencia sua agenda — tudo em um só lugar."
        )

        Text(
            text = "Você deseja informar um paciente agora?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoTitulo,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        EscolhaBinaria(
            textoSim = "✅ Sim, quero vincular",
            textoNao = "⏭️ Fazer depois",
            sim = quer,
            onEscolher = { quer = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (quer == true) {
            BuscaPessoa(
                rotulo = "Buscar paciente pelo nome ou e-mail",
                placeholder = "Nome ou e-mail do paciente",
                busca = busca,
                onBuscaChange = { nova ->
                    busca = nova
                    if (selecionado != null && nova != selecionado?.nome) selecionado = null
                },
                resultados = resultados,
                selecionado = selecionado,
                onSelecionar = { p ->
                    selecionado = p
                    busca = p.nome
                },
                carregando = buscando
            )
        } else if (quer == false) {
            CartaoAviso(fundo = SelecionadoFundo, borda = BordaSuave) {
                Text(
                    text = textoComNegrito(
                        "Tudo bem! Você pode vincular pacientes a qualquer momento pelo menu ",
                        "Pacientes",
                        "."
                    ),
                    fontSize = 13.sp,
                    color = TextoSuave,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        BotaoPrimario(
            texto = "Acessar o Dialoga →",
            onClick = {
                if (quer == true && selecionado != null) {
                    mostrarDialogo = true
                } else {
                    onFinalizar()
                }
            }
        )
    }

    if (mostrarDialogo) {
        val escolhido = selecionado
        DialogoVinculo(
            titulo = "Paciente encontrado",
            mensagem = "Seu paciente foi localizado e recebeu uma notificação informando que você deseja adicioná-lo.",
            detalhe = textoComNegrito(
                "Após a confirmação de ",
                escolhido?.nome ?: "seu paciente",
                ", seu vínculo será criado e você poderá compartilhar seus dados."
            ),
            onEntendi = {
                mostrarDialogo = false
                escolhido?.let(onVincularPaciente)
                onFinalizar()
            }
        )
    }
}