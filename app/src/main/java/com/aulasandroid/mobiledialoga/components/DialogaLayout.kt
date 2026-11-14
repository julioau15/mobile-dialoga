package com.aulasandroid.mobiledialoga.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.ui.theme.CorBranco
import com.aulasandroid.mobiledialoga.ui.theme.CorFundo
import com.aulasandroid.mobiledialoga.ui.theme.CorPrimaria
import com.aulasandroid.mobiledialoga.ui.theme.CorTexto
import kotlinx.coroutines.launch

data class DialogaMenuItem(
    val id: String,
    val titulo: String,
    @param:DrawableRes val iconeRes: Int? = null,
    val iconeVector: ImageVector? = null,
    val aviso: Boolean = false // bolinha: há novidade nesta área
)

// Bolinha de aviso (roxa/vermelha) que fica no canto de um ícone
@Composable
private fun BolinhaAviso(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(Color.White, CircleShape)
            .padding(1.5.dp)
            .background(Color(0xFFE5484D), CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogaLayout(
    tituloTela: String,
    nomeUsuario: String = "Ana",
    iniciaisUsuario: String = "AB",
    fotoUrl: String? = null,
    itensMenu: List<DialogaMenuItem>,
    itemSelecionadoId: String,
    onSelecionarItem: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Efeito de desfoque no fundo quando a sidebar está aberta
    val blurRadius by animateDpAsState(
        targetValue = if (drawerState.isOpen || drawerState.isAnimationRunning) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blurAnimation"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.35f),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CorBranco,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight(),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Topo da Sidebar (Logo Dialoga + Botão Fechar X)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DialogaLogo(altura = 36.dp)

                        IconButton(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar menu",
                                tint = Color(0xFF6B5A7D)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 2. Avatar e Nome do Usuário (Carrega Imagem se houver; senão desenha círculo com Iniciais)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!fotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = fotoUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFE8DDFA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = iniciaisUsuario,
                                    color = CorPrimaria,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = nomeUsuario,
                            color = CorTexto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 3. Lista de Itens do Menu
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        itensMenu.forEach { item ->
                            val selecionado = item.id == itemSelecionadoId

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = item.titulo,
                                        fontSize = 15.sp,
                                        fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                badge = {
                                    if (item.aviso) BolinhaAviso()
                                },
                                icon = {
                                    if (item.iconeRes != null) {
                                        Icon(
                                            painter = painterResource(item.iconeRes),
                                            contentDescription = item.titulo,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else if (item.iconeVector != null) {
                                        Icon(
                                            imageVector = item.iconeVector,
                                            contentDescription = item.titulo,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                selected = selecionado,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onSelecionarItem(item.id)
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = CorPrimaria,
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = Color(0xFF6B5A7D),
                                    unselectedTextColor = Color(0xFF4A3B5C)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // 4. Rodapé - Botão "Sair da conta"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onLogout()
                            }
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sidebar_sair),
                            contentDescription = "Sair da conta",
                            tint = Color(0xFF6B5A7D),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Sair da conta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A3B5C)
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = tituloTela,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CorTexto
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menu",
                                        tint = CorPrimaria
                                    )
                                    if (itensMenu.any { it.aviso }) {
                                        BolinhaAviso(Modifier.align(Alignment.TopEnd))
                                    }
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { onSelecionarItem("notificacoes") }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notificações",
                                        tint = CorPrimaria
                                    )
                                    if (itensMenu.any { it.id == "notificacoes" && it.aviso }) {
                                        BolinhaAviso(Modifier.align(Alignment.TopEnd))
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = CorBranco
                        )
                    )
                },
                containerColor = CorFundo,
                content = content
            )
        }
    }
}
