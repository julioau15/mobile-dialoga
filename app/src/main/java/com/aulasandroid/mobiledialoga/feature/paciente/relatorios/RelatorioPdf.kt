package com.aulasandroid.mobiledialoga.feature.paciente.relatorios

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

/** Gera páginas A4, mantendo todo o conteúdo dos registros do período selecionado. */
internal object RelatorioPdf {
    fun gerar(context: Context, nome: String, state: RelatoriosState): File {
        val registros = state.registrosNoPeriodo
        val pontosSono = RelatoriosMapper.sono(registros)
        val resumo = RelatoriosMapper.resumoSono(pontosSono)
        val pasta = File(context.cacheDir, "relatorios").apply { check(mkdirs() || isDirectory) }
        val arquivo = File.createTempFile(
            "Dialoga_${state.inicioIso}_${state.periodoFim}_", ".pdf", pasta
        )
        try {
            val documento = PdfDocument()
            try {
                val escritor = Escritor(documento)
                escritor.titulo("Dialoga")
                escritor.texto("Relatório de saúde emocional")
                if (nome.isNotBlank()) escritor.texto("Paciente: $nome")
                escritor.texto("Período: ${DatasRelatorio.br(state.inicioIso)} a ${DatasRelatorio.br(state.periodoFim)}")
                escritor.texto("Registros de humor: ${registros.size}")

                escritor.titulo("Indicadores do período")
                RelatoriosMapper.medias(registros).forEach { indicador ->
                    val valor = indicador.media?.let { "${formatarMedia(it)}/${indicador.maximo}" }
                        ?: "Sem registros"
                    escritor.texto("${indicador.rotulo}: $valor")
                }

                escritor.titulo("Horas de sono")
                if (resumo == null) {
                    escritor.texto("Sem registros de sono neste período.")
                } else {
                    escritor.texto("Média: ${formatarHorasSono(resumo.media)} por dia")
                    escritor.texto("Mínimo: ${formatarHorasSono(resumo.minimo)} | Máximo: ${formatarHorasSono(resumo.maximo)}")
                    if (resumo.abaixo) escritor.texto("Abaixo do recomendado (7 a 9 horas).")
                    if (resumo.acima) escritor.texto("Acima do recomendado (7 a 9 horas).")
                    pontosSono.forEach {
                        escritor.texto("${DatasRelatorio.br(it.dataIso)}: ${formatarHorasSono(it.horas)}")
                    }
                }

                escritor.titulo("Anotações do período")
                if (registros.isEmpty()) escritor.texto("Nenhuma anotação neste período.")
                registros.forEach { registro ->
                    escritor.subtitulo(DatasRelatorio.br(registro.data))
                    escritor.texto("Sentimento: ${registro.sentimento ?: "-"} | Energia: ${registro.energia ?: "-"}")
                    escritor.texto("Humor: ${registro.humor ?: "-"} | Pensamentos: ${registro.pensamento ?: "-"}")
                    escritor.texto("Sono: ${registro.horasSono?.let { formatarHorasSono(it) } ?: "Não informado"}")
                    escritor.texto(registro.relato?.takeIf { it.isNotBlank() } ?: "Sem relato.")
                }

                escritor.titulo("Solicitações de acesso")
                if (state.solicitacoes.isEmpty()) escritor.texto("Nenhuma solicitação de acesso.")
                state.solicitacoes.forEach {
                    val especialidade = it.especialidade.takeIf(String::isNotBlank)?.let { valor -> " ($valor)" }.orEmpty()
                    escritor.texto("${it.nome.ifBlank { "Profissional" }}$especialidade: ${it.status}")
                }
                escritor.finalizar()
                FileOutputStream(arquivo).use { documento.writeTo(it) }
            } finally {
                documento.close()
            }
            return arquivo
        } catch (e: Exception) {
            arquivo.delete()
            throw e
        }
    }

    private class Escritor(private val documento: PdfDocument) {
        private var numero = 0
        private var pagina: PdfDocument.Page? = null
        private var y = 48
        private val margem = 40
        private val largura = 595
        private val limite = 790
        private val tinta = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(49, 35, 67)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        fun titulo(texto: String) {
            garantirEspaco(64)
            y += 12
            escrever(texto, 16f, true)
        }

        fun subtitulo(texto: String) {
            garantirEspaco(60)
            y += 6
            escrever(texto, 12f, true)
        }

        fun texto(texto: String) = escrever(texto, 11f, false)

        private fun garantirEspaco(altura: Int) {
            if (pagina == null || y + altura > limite) novaPagina()
        }

        private fun novaPagina() {
            finalizar()
            numero++
            pagina = documento.startPage(PdfDocument.PageInfo.Builder(largura, 842, numero).create())
            y = 48
        }

        private fun escrever(texto: String, tamanho: Float, negrito: Boolean) {
            tinta.textSize = tamanho
            tinta.typeface = Typeface.create(Typeface.SANS_SERIF, if (negrito) Typeface.BOLD else Typeface.NORMAL)
            val layout = StaticLayout.Builder.obtain(texto, 0, texto.length, tinta, largura - margem * 2)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(false)
                .setLineSpacing(3f, 1f)
                .build()
            var linha = 0
            while (linha < layout.lineCount) {
                garantirEspaco(layout.getLineBottom(linha) - layout.getLineTop(linha))
                val topo = layout.getLineTop(linha)
                var fim = linha
                while (fim + 1 < layout.lineCount && y + layout.getLineBottom(fim + 1) - topo <= limite) fim++
                val altura = layout.getLineBottom(fim) - topo
                val canvas = pagina!!.canvas
                canvas.save()
                canvas.clipRect(margem, y, largura - margem, y + altura)
                canvas.translate(margem.toFloat(), (y - topo).toFloat())
                layout.draw(canvas)
                canvas.restore()
                y += altura
                linha = fim + 1
            }
            y += 6
        }

        fun finalizar() {
            pagina?.let {
                val rodape = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    textSize = 9f
                }
                it.canvas.drawText("Dialoga | Página $numero", margem.toFloat(), 818f, rodape)
                documento.finishPage(it)
            }
            pagina = null
        }
    }
}
