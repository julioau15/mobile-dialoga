package com.aulasandroid.mobiledialoga.feature.paciente.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aulasandroid.mobiledialoga.components.MascaraCelular
import com.aulasandroid.mobiledialoga.data.useAuth
import com.aulasandroid.mobiledialoga.feature.perfil.components.AcoesEdicaoPerfil
import com.aulasandroid.mobiledialoga.feature.perfil.components.AvisoPerfil
import com.aulasandroid.mobiledialoga.feature.perfil.components.BotaoPerfilPrincipal
import com.aulasandroid.mobiledialoga.feature.perfil.components.CampoPerfil
import com.aulasandroid.mobiledialoga.feature.perfil.components.CampoPerfilBloqueado
import com.aulasandroid.mobiledialoga.feature.perfil.components.EstiloCabecalhoPaciente
import com.aulasandroid.mobiledialoga.feature.perfil.components.PerfilCabecalho
import com.aulasandroid.mobiledialoga.feature.perfil.components.PerfilCartao
import com.aulasandroid.mobiledialoga.feature.perfil.components.PerfilTitulo
import com.aulasandroid.mobiledialoga.feature.perfil.components.rememberSeletorFoto
import com.aulasandroid.mobiledialoga.model.UserProfile
import com.aulasandroid.mobiledialoga.ui.theme.CorBorda
import com.aulasandroid.mobiledialoga.ui.theme.CorErro
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria

@Composable
fun PerfilPacienteScreen(
    modifier: Modifier = Modifier,
    viewModel: PerfilPacienteViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState = useAuth()
    val token = authState.auth?.token
    val estado by viewModel.uiState.observeAsState(PerfilPacienteUiState())

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) viewModel.carregar(token)
    }

    // Depois de salvar, atualiza nome/foto que aparecem no menu lateral
    LaunchedEffect(estado.versaoSalva) {
        if (estado.versaoSalva > 0) authState.carregarPerfilMeu()
    }

    // O aviso some sozinho depois de 3 segundos
    LaunchedEffect(estado.toastMensagem) {
        if (estado.toastMensagem != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.limparToast()
        }
    }

    val abrirGaleria = rememberSeletorFoto { uri -> viewModel.onFotoEscolhida(uri) }

    val original = estado.original
    val perfilParaIniciais = UserProfile(
        nome = original?.nome ?: estado.nome,
        apelido = original?.apelido
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            PerfilTitulo("Meu perfil", "Suas informações pessoais")
            Spacer(modifier = Modifier.height(20.dp))

            PerfilCartao {
                PerfilCabecalho(
                    estilo = EstiloCabecalhoPaciente,
                    iniciais = perfilParaIniciais.getIniciais(),
                    nome = original?.nome?.ifBlank { null } ?: "Paciente",
                    selo = "Paciente",
                    fotoUrl = estado.fotoUrl,
                    fotoLocal = estado.fotoLocal,
                    onTrocarFoto = abrirGaleria
                )

                when {
                    original == null && estado.erroGeral == null -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth().padding(40.dp)
                        ) {
                            CircularProgressIndicator(color = CorPrimaria, strokeWidth = 3.dp)
                        }
                    }

                    original == null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Text(
                                text = estado.erroGeral ?: "Não foi possível carregar o perfil.",
                                color = CorErro
                            )
                            TextButton(onClick = { token?.let { viewModel.carregar(it, forcar = true) } }) {
                                Text("Tentar novamente", color = CorPrimaria)
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            CampoPerfil(
                                rotulo = "Nome completo",
                                valor = estado.nome,
                                onValorChange = viewModel::onNomeChange,
                                icone = Icons.Outlined.Person,
                                editando = estado.editando,
                                erro = estado.erroNome
                            )

                            // O apelido só aparece na edição (como no protótipo)
                            if (estado.editando) {
                                CampoPerfil(
                                    rotulo = "Como prefere ser chamado?",
                                    opcional = true,
                                    valor = estado.apelido,
                                    onValorChange = viewModel::onApelidoChange,
                                    placeholder = "Apelido ou nome preferido",
                                    icone = Icons.Outlined.Person,
                                    editando = true
                                )
                            }

                            CampoPerfilBloqueado(
                                rotulo = "E-mail",
                                valor = estado.email,
                                icone = Icons.Outlined.Email,
                                ajuda = "O e-mail não pode ser alterado por questões de segurança."
                            )

                            CampoPerfil(
                                rotulo = "Celular",
                                valor = estado.celular,
                                onValorChange = viewModel::onCelularChange,
                                icone = Icons.Outlined.Phone,
                                editando = estado.editando,
                                keyboardType = KeyboardType.Phone,
                                visualTransformation = MascaraCelular,
                                erro = estado.erroCelular
                            )

                            HorizontalDivider(color = CorBorda)

                            if (estado.editando) {
                                AcoesEdicaoPerfil(
                                    salvando = estado.salvando,
                                    onCancelar = viewModel::cancelarEdicao,
                                    onSalvar = { token?.let { viewModel.salvar(context, it) } }
                                )
                            } else {
                                BotaoPerfilPrincipal(
                                    texto = "Editar perfil",
                                    icone = Icons.Outlined.Edit,
                                    onClick = viewModel::iniciarEdicao
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        AvisoPerfil(
            mensagem = estado.toastMensagem,
            sucesso = estado.toastSucesso,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
}