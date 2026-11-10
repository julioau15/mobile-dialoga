package com.aulasandroid.mobiledialoga.feature.paciente.humor

/** Opções fixas para o registro de humor. */
object HumorOptions {
    val SENTIMENTOS = listOf("Feliz", "Triste", "Ansioso", "Irritado", "Calmo", "Cansado")
    val ENERGIAS = listOf("Baixa", "Média", "Alta")
    val HUMORES = listOf("Muito bem", "Bem", "Neutro", "Mal", "Muito mal")
    val PENSAMENTOS = listOf("Positivos", "Negativos", "Neutros")
}

/** Dados consolidados para o registro de humor realizado no onboarding ou acompanhamento. */
data class RegistroHumorInicial(
    val sentimento: String,
    val energia: String,
    val humor: String,
    val horasSono: Double,
    val relato: String?,
    val pensamento: String
)
