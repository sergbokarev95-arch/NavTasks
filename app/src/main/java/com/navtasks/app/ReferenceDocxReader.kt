package com.navtasks.app

import java.io.FilterInputStream
import java.io.InputStream
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

internal data class ReferenceDocxParagraph(val text: String, val heading: Boolean = false)

/**
 * Offline *simplified text* reader. Doesn't execute embedded scripts,
 * load linked files, call remote services or reinterpret Office macros.
 * Original DOCX is preserved byte-for-byte for exact layout elsewhere.
 */
internal fun extractReferenceDocxText(file: File): List<ReferenceDocxParagraph> {
    validateReferenceManualSignature(file, ReferenceManualMime.DOCX)
    val paragraphs = mutableListOf<ReferenceDocxParagraph>()
    ZipFile(file).use { zip ->
        val entry = zip.getEntry("word/document.xml")
            ?: throw IllegalArgumentException("Missing Word document XML")
        require(entry.size <= 24_000_000L) { "Word document XML too large" }
        val parser = SAXParserFactory.newInstance().apply {
            isNamespaceAware = true
            isValidating = false
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }.newSAXParser()
        var totalCharacters = 0
        val handler = object : DefaultHandler() {
            private val current = StringBuilder()
            private var inParagraph = false
            private var inText = false
            private var heading = false
            private fun name(local: String, qName: String): String =
                local.ifBlank { qName.substringAfter(':') }

            override fun startElement(uri: String, localName: String, qName: String, attrs: Attributes) {
                when (name(localName, qName)) {
                    "p" -> {
                        inParagraph = true
                        heading = false
                        current.clear()
                    }
                    "pStyle" -> {
                        if (inParagraph) {
                            val style = attrs.getValue("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val")
                                ?: attrs.getValue("w:val")
                                ?: attrs.getValue("val")
                                ?: ""
                            heading = style.startsWith("Heading", true) ||
                                style.startsWith("Title", true) ||
                                style.startsWith("Заголовок", true)
                        }
                    }
                    "t", "instrText" -> {
                        // Field instructions can contain external references; do not display or execute them.
                        if (name(localName, qName) == "t" && inParagraph) inText = true
                    }
                    "tab" -> if (inParagraph) current.append("    ")
                    "br", "cr" -> if (inParagraph) current.append('\n')
                }
            }
            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (inParagraph && inText) {
                    totalCharacters += length
                    require(totalCharacters <= 5_000_000) { "Word text extraction limit exceeded" }
                    current.append(ch, start, length)
                }
            }
            override fun endElement(uri: String, localName: String, qName: String) {
                when (name(localName, qName)) {
                    "t" -> inText = false
                    "p" -> {
                        val text = current.toString().trim()
                        if (text.isNotBlank()) {
                            require(paragraphs.size < 15000) { "Too many Word paragraphs" }
                            paragraphs += ReferenceDocxParagraph(text, heading)
                        }
                        inParagraph = false
                        inText = false
                    }
                }
            }
        }
        // Count decompressed bytes as well as declared ZIP size (which may be unknown).
        zip.getInputStream(entry).use { raw ->
            val bounded = object : FilterInputStream(raw) {
                var seen = 0L
                override fun read(): Int {
                    val n = super.read()
                    if (n >= 0) { seen++; require(seen <= 24_000_000L) }
                    return n
                }
                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val n = super.read(b, off, len)
                    if (n > 0) { seen += n; require(seen <= 24_000_000L) }
                    return n
                }
            }
            parser.parse(InputSource(bounded), handler)
        }
    }
    return paragraphs
}
