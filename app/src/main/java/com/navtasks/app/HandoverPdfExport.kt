package com.navtasks.app

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

/** Searchable A4 PDF for Windows, iOS and any standard PDF reader; fully offline. */
internal object HandoverPdfExport {
    fun export(sections: List<HandoverSection>, output: OutputStream) {
        val lines = handoverExportLines(sections)
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val left = 46f
        val maxY = 790f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            color = Color.rgb(31, 40, 53)
            textSize = 11f
        }
        var page: PdfDocument.Page? = null
        var number = 0
        var y = 55f
        fun nextPage() {
            page?.let { pdf.finishPage(it) }
            number++
            require(number <= 500) { "Handover PDF too long" }
            page = pdf.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, number).create())
            y = 55f
            val footer = Paint(paint).apply { textSize = 9f; color = Color.GRAY }
            page!!.canvas.drawText("NavTasks · Handover", left, 815f, footer)
            page!!.canvas.drawText(number.toString(), pageWidth - 52f, 815f, footer)
        }
        fun paragraph(text: String, indent: Float, lineHeight: Float, gap: Float) {
            val width = pageWidth - left * 2 - indent
            var textLeft = text.trimEnd().ifEmpty { " " }
            while (textLeft.isNotEmpty()) {
                val fit = paint.breakText(textLeft, true, width, null).coerceAtLeast(1)
                val cut = if (fit < textLeft.length) {
                    textLeft.lastIndexOf(' ', fit).takeIf { it > 0 } ?: fit
                } else fit
                if (y + lineHeight > maxY) nextPage()
                page!!.canvas.drawText(textLeft.substring(0, cut).trimEnd(),
                    left + indent, y, paint)
                y += lineHeight
                textLeft = textLeft.substring(cut).trimStart()
            }
            y += gap
        }
        try {
            nextPage()
            for (line in lines) when(line.kind) {
                HandoverExportKind.TITLE -> {
                    paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                    paint.color = Color.rgb(0, 112, 230)
                    paint.textSize = 25f
                    paragraph(line.text, 0f, 31f, 12f)
                }
                HandoverExportKind.META -> {
                    paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    paint.color = Color.GRAY
                    paint.textSize = 10f
                    paragraph(line.text, 0f, 15f, 11f)
                }
                HandoverExportKind.SECTION -> {
                    paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                    paint.color = Color.rgb(14, 72, 132)
                    paint.textSize = 16f
                    y += 10f
                    paragraph(line.text, 0f, 23f, 8f)
                }
                HandoverExportKind.ENTRY -> {
                    paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                    paint.color = Color.rgb(31, 40, 53)
                    paint.textSize = 12.5f
                    paragraph(line.text, 10f, 18f, 3f)
                }
                HandoverExportKind.NOTE -> {
                    paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                    paint.color = Color.rgb(31, 40, 53)
                    paint.textSize = 11f
                    paragraph(line.text, 18f, 16f, 2f)
                }
            }
            page?.let { pdf.finishPage(it) }
            page = null
            pdf.writeTo(output)
        } finally {
            pdf.close()
        }
    }
}
