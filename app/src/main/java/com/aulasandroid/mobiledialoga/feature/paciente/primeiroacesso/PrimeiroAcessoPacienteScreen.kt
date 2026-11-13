package com.aulasandroid.mobiledialoga.feature.paciente.primeiroacesso

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.feature.paciente.humor.FormularioHumor
import com.aulasandroid.mobiledialoga.feature.paciente.humor.rememberHumorFormState
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BotaoPrimario
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BotaoTexto
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.BuscaPessoa
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.Cabecalho
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.CampoDeTexto
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.CartaoAviso
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.DataVisualTransformation
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.DialogoVinculo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.EscolhaBinaria
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PessoaSugestao
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.PrimeiroAcessoLayout
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RosaBorda
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RosaFundo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.RotuloCampo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.TextoSuave
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.TextoTitulo
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.dataDigitosParaIso
import com.aulasandroid.mobiledialoga.feature.primeiroacesso.components.textoComNegrito
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Saver para manter o profissional selecionado após rotação de tela. */
private val PessoaSugestaoSaver = listSaver<PessoaSugestao?, String>(
    save = { p -> if (p == null) emptyList() else listOf(p.id, p.nome, p.detalhe) },
    restore = { l -> if (l.size == 3) PessoaSugestao(id = l[0], nome = l[1], detalhe = l[2]) else null }
)

private val ROTULOS_ETAPAS = listOf("Pessoal", "Profissional", "Humor")

/**
 * Primeiro acesso do paciente.
 * [onFinalizar] envia os dados ao backend para salvar.
 */
@Composable
fun PrimeiroAcessoPacienteScreen(
    nomeCompleto: String? = null,
    onFinalizar: (JsonObject) -> Unit,
    buscarProfissionais: suspend (String) -> List<PessoaSugestao> = { emptyList() }
) {
    var etapa by rememberSaveable { mutableStateOf(1) }

    // Dados da Etapa 1
    var apelido by rememberSaveable { mutableStateOf("") }
    var nascimento by rememberSaveable { mutableStateOf("") }
    var nascimentoErro by remember { mutableStateOf<String?>(null) }

    // Dados da Etapa 2
    var temProfissional by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var buscaText by rememberSaveable { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    var resultados by remember { mutableStateOf(emptyList<PessoaSugestao>()) }
    var profissionalSelecionado by rememberSaveable(stateSaver = PessoaSugestaoSaver) {
        mutableStateOf<PessoaSugestao?>(null)
    }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    // Dados da Etapa 3 (Humor) - Centralizado na pasta humor
    val humorState = rememberHumorFormState()

    // Busca de profissional com debounce
    LaunchedEffect(buscaText) {
        if (profissionalSelecionado != null || buscaText.trim().length < 2) {
            resultados = emptyList()
            buscando = false
            return@LaunchedEffect
        }
        buscando = true
        delay(500)
        resultados = try {
            buscarProfissionais(buscaText.trim())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
        buscando = false
    }

    val finalizarComDados: () -> Unit = {
        val dados = JsonObject().apply {
            addProperty("apelido", apelido.trim().ifBlank { null })
            addProperty("data_nascimento", dataDigitosParaIso(nascimento))
            addProperty(
                "id_profissional",
                if (temProfissional == true) profissionalSelecionado?.id else null
            )
            // Usa o método do estado do humor para gerar o JSON
            add("registro_humor", humorState.toJsonObject())
        }
        onFinalizar(dados)
    }

    PrimeiroAcessoLayout(rotulos = ROTULOS_ETAPAS, etapaAtual = etapa) {
        when (etapa) {
            1 -> {
                Cabecalho(
                    ilustracao = R.drawable.ic_primeiroacesso_1,
                    titulo = "Vamos personalizar sua experiência",
                    descricao = "Algumas informações ajudarão o Dialoga a oferecer uma experiência mais adequada para você."
                )

                RotuloCampo(texto = "Como devemos chamar você?", opcional = true)
                CampoDeTexto(
                    valor = apelido,
                    onValorChange = { apelido = it },
                    placeholder = "Digite como prefere ser chamado",
                    icone = Icons.Default.Person
                )
                if (!nomeCompleto.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = textoComNegrito("Seu nome completo é ", nomeCompleto, "."),
                        fontSize = 11.sp,
                        color = TextoSuave,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                RotuloCampo(texto = "Data de nascimento", opcional = true)
                CampoDeTexto(
                    valor = nascimento,
                    onValorChange = { nova ->
                        nascimento = nova.filter { it.isDigit() }.take(8)
                        nascimentoErro = null
                    },
                    placeholder = "dd/mm/aaaa",
                    icone = Icons.Default.DateRange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DataVisualTransformation(),
                    erro = nascimentoErro
                )

                Spacer(modifier = Modifier.height(24.dp))
                val podeContinuar1 = apelido.trim().isNotBlank() && nascimento.length == 8 && dataDigitosParaIso(nascimento) != null
                BotaoPrimario(
                    texto = "Continuar →",
                    habilitado = podeContinuar1,
                    onClick = {
                        val iso = dataDigitosParaIso(nascimento)
                        if (nascimento.isNotEmpty() && iso == null) {
                            nascimentoErro = "Data inválida ou fora do limite permitido"
                        } else {
                            etapa = 2
                        }
                    }
                )
                BotaoTexto(texto = "Pular por enquanto", onClick = { etapa = 2 })
            }

            2 -> {
                Cabecalho(
                    ilustracao = R.drawable.ic_primeiroacesso_2,
                    titulo = "Sua equipe de saúde",
                    descricao = "Você possui acompanhamento profissional?"
                )

                EscolhaBinaria(
                    textoSim = "Sim, tenho",
                    textoNao = "Não por enquanto",
                    sim = temProfissional,
                    onEscolher = { escolha ->
                        temProfissional = escolha
                        if (!escolha) {
                            profissionalSelecionado = null
                            buscaText = ""
                            resultados = emptyList()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (temProfissional == true) {
                    BuscaPessoa(
                        rotulo = "Buscar profissional",
                        placeholder = "Digite o nome do profissional",
                        busca = buscaText,
                        onBuscaChange = { text ->
                            buscaText = text
                            if (profissionalSelecionado != null && text != profissionalSelecionado?.nome) {
                                profissionalSelecionado = null
                            }
                        },
                        resultados = resultados,
                        selecionado = profissionalSelecionado,
                        onSelecionar = { p ->
                            if (profissionalSelecionado == p) {
                                profissionalSelecionado = null
                                buscaText = ""
                            } else {
                                profissionalSelecionado = p
                                buscaText = p.nome
                            }
                        },
                        carregando = buscando,
                        mensagemVazia = "Nenhum profissional encontrado"
                    )
                } else if (temProfissional == false) {
                    CartaoAviso(fundo = RosaFundo, borda = RosaBorda) {
                        Text(
                            text = "Tudo bem! 💜",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoTitulo
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = textoComNegrito("Você pode adicionar um profissional a qualquer momento pelo menu ", "Profissionais", "."),
                            fontSize = 13.sp,
                            color = TextoSuave,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                val podeContinuar = when (temProfissional) {
                    null -> false
                    true -> profissionalSelecionado != null
                    false -> true
                }
                BotaoPrimario(
                    texto = "Continuar →",
                    habilitado = podeContinuar,
                    onClick = {
                        if (temProfissional == true && profissionalSelecionado != null) {
                            mostrarDialogo = true
                        } else {
                            etapa = 3
                        }
                    }
                )
                BotaoTexto(texto = "Pular por enquanto", onClick = { etapa = 3 })
                BotaoTexto(texto = "← Voltar", onClick = { etapa = 1 })
            }

            else -> {
                Cabecalho(
                    ilustracao = R.drawable.ic_primeiroacesso_3,
                    titulo = "Como você está hoje?",
                    descricao = "Registre como está se sentindo neste momento."
                )

                // Chama o formulário centralizado passando o estado
                FormularioHumor(state = humorState)

                Spacer(modifier = Modifier.height(24.dp))
                BotaoPrimario(
                    texto = "Salvar e acessar o Dialoga 💜",
                    habilitado = humorState.isValido,
                    onClick = finalizarComDados
                )
                BotaoTexto(texto = "Agora não — ir para o início", onClick = { onFinalizar(JsonObject()) })
                BotaoTexto(texto = "← Voltar", onClick = { etapa = 2 })
            }
        }
    }

    if (mostrarDialogo) {
        DialogoVinculo(
            titulo = "Profissional encontrado",
            mensagem = "Seu profissional foi localizado e recebeu uma notificação informando que você deseja adicioná-lo.",
            detalhe = textoComNegrito("Após a confirmação de ", profissionalSelecionado?.nome ?: "seu profissional", ", seu vínculo será criado."),
            onEntendi = {
                mostrarDialogo = false
                etapa = 3
            },
            onCancelar = {
                mostrarDialogo = false
            }
        )
    }
}
