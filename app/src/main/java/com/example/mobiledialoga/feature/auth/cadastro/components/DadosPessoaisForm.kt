package com.example.mobiledialoga.feature.auth.cadastro.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mobiledialoga.R
import com.example.mobiledialoga.core.components.CabecalhoCard
import com.example.mobiledialoga.core.components.DialogaButton
import com.example.mobiledialoga.core.components.DialogaCard
import com.example.mobiledialoga.core.components.DialogaSelectField
import com.example.mobiledialoga.core.components.DialogaTextField
import com.example.mobiledialoga.core.components.MascaraCelular
import com.example.mobiledialoga.feature.auth.cadastro.CadastroUiState
import com.example.mobiledialoga.feature.auth.cadastro.TipoConta

// Etapa 1 do cadastro: "Criar conta"
@Composable
fun DadosPessoaisForm(
    estado: CadastroUiState,
    especialidades: List<String>,
    onTipoContaChange: (TipoConta) -> Unit,
    onNomeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onEspecialidadeChange: (String) -> Unit,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier
) {
    DialogaCard(modifier = modifier) {
        CabecalhoCard(titulo = "Criar conta", subtitulo = "Informe seus dados pessoais")

        SeletorTipoConta(selecionado = estado.tipoConta, onSelecionar = onTipoContaChange)

        Spacer(modifier = Modifier.height(16.dp))

        DialogaTextField(
            rotulo = "Nome completo",
            placeholder = "Seu nome completo",
            value = estado.nome,
            onValueChange = onNomeChange,
            icone = R.drawable.ic_person,
            erro = estado.erroNome,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DialogaTextField(
            rotulo = "E-mail",
            placeholder = "seu@email.com",
            value = estado.email,
            onValueChange = onEmailChange,
            icone = R.drawable.ic_email,
            erro = estado.erroEmail,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DialogaTextField(
            rotulo = "Celular",
            placeholder = "(11) 99999-9999",
            value = estado.celular,
            onValueChange = onCelularChange,
            icone = R.drawable.ic_phone,
            erro = estado.erroCelular,
            keyboardType = KeyboardType.Phone,
            visualTransformation = MascaraCelular,
            modifier = Modifier.fillMaxWidth()
        )

        // Especialidade só aparece para profissional
        if (estado.tipoConta == TipoConta.PROFISSIONAL) {
            Spacer(modifier = Modifier.height(16.dp))
            DialogaSelectField(
                rotulo = "Especialidade",
                opcoes = especialidades,
                selecionada = estado.especialidade,
                onSelecionar = onEspecialidadeChange,
                erro = estado.erroEspecialidade,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DialogaButton(texto = "Continuar", onClick = onContinuar, modifier = Modifier.fillMaxWidth())
    }
}
