package com.aulasandroid.mobiledialoga.feature.auth.cadastro.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.R
import com.aulasandroid.mobiledialoga.components.CabecalhoCard
import com.aulasandroid.mobiledialoga.components.DialogaButton
import com.aulasandroid.mobiledialoga.components.DialogaCard
import com.aulasandroid.mobiledialoga.components.DialogaSelectField
import com.aulasandroid.mobiledialoga.components.DialogaTextField
import com.aulasandroid.mobiledialoga.components.MascaraCelular
import com.aulasandroid.mobiledialoga.components.MascaraCpf
import com.aulasandroid.mobiledialoga.components.MascaraCrp
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.CadastroUiState
import com.aulasandroid.mobiledialoga.feature.auth.cadastro.TipoConta


// Etapa 1 do cadastro: "Criar conta"
@Composable
fun DadosPessoaisForm(
    estado: CadastroUiState,
    especialidades: List<String>,
    onTipoContaChange: (TipoConta) -> Unit,
    onNomeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onCpfChange: (String) -> Unit,
    onCrpChange: (String) -> Unit,
    onInstituicaoChange: (String) -> Unit,
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
            rotulo = "Celular (Telefone)",
            placeholder = "(11) 99999-9999",
            value = estado.celular,
            onValueChange = onCelularChange,
            icone = R.drawable.ic_phone,
            erro = estado.erroCelular,
            keyboardType = KeyboardType.Phone,
            visualTransformation = MascaraCelular,
            modifier = Modifier.fillMaxWidth()
        )

        // Campos específicos para Profissional (CPF, CRP, Instituição, Especialidade)
        if (estado.tipoConta == TipoConta.PROFISSIONAL) {
            Spacer(modifier = Modifier.height(16.dp))
            DialogaTextField(
                rotulo = "CPF",
                placeholder = "000.000.000-00",
                value = estado.cpf,
                onValueChange = onCpfChange,
                erro = estado.erroCpf,
                keyboardType = KeyboardType.Number,
                visualTransformation = MascaraCpf,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            DialogaTextField(
                rotulo = "CRP",
                placeholder = "00/000000-AA",
                value = estado.crp,
                onValueChange = onCrpChange,
                erro = estado.erroCrp,
                visualTransformation = MascaraCrp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            DialogaTextField(
                rotulo = "Instituição / Clínica (Opcional)",
                placeholder = "Nome da instituição",
                value = estado.instituicaoClinica,
                onValueChange = onInstituicaoChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            val opcoesEspec = if (estado.especialidadesList.isNotEmpty()) {
                estado.especialidadesList.map { it.nome }
            } else {
                especialidades
            }
            DialogaSelectField(
                rotulo = "Especialidade",
                opcoes = opcoesEspec,
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
