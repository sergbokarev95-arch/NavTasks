package com.navtasks.app

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Editable, offline Word OOXML. Does not need Word or a network on the phone. */
internal object HandoverDocxExport {
    private fun escape(value: String) = buildString {
        for (ch in value) when(ch) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&apos;")
            else -> if (ch == '\n' || ch == '\t' || ch >= ' ') append(ch)
        }
    }

    private fun para(line: HandoverExportLine): String {
        val style = when(line.kind) {
            HandoverExportKind.TITLE -> "Title"
            HandoverExportKind.SECTION -> "Heading1"
            HandoverExportKind.ENTRY -> "Heading2"
            else -> "Normal"
        }
        val content = line.text.ifEmpty { " " }
        return "<w:p><w:pPr><w:pStyle w:val=\"" + style +
            "\"/></w:pPr><w:r><w:t xml:space=\"preserve\">" +
            escape(content) + "</w:t></w:r></w:p>"
    }
    /** Word OOXML inline picture; anchored immediately after its own entry. */
    private fun pictureParagraph(photo: HandoverPhoto, index: Int): String {
        val width = photo.widthPx.toDouble()
        val height = photo.heightPx.toDouble()
        val scale = minOf(5_350_000.0 / width, 3_700_000.0 / height)
        val cx = (width * scale).toLong()
        val cy = (height * scale).toLong()
        val relId = "rId" + (index + 1) // rId1 belongs to styles.
        return """<w:p><w:pPr><w:spacing w:after="180"/></w:pPr>
<w:r><w:drawing>
<wp:inline distT="0" distB="0" distL="0" distR="0">
<wp:extent cx="$cx" cy="$cy"/><wp:docPr id="$index" name="Handover photo $index"/>
<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
<pic:nvPicPr><pic:cNvPr id="0" name="handover_$index.jpg"/><pic:cNvPicPr/></pic:nvPicPr>
<pic:blipFill><a:blip r:embed="$relId"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
<pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$cx" cy="$cy"/></a:xfrm>
<a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr>
</pic:pic></a:graphicData></a:graphic>
</wp:inline></w:drawing></w:r></w:p>"""
    }

    private fun write(zip: ZipOutputStream, name: String, text: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(text.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    fun export(sections: List<HandoverSection>, output: OutputStream) {
        val photos = mutableListOf<HandoverPhoto>()
        val body = handoverExportLines(sections).joinToString("") { line ->
            if (line.kind == HandoverExportKind.PHOTO) {
                val photo = requireNotNull(line.photo)
                photo.jpegBytes() // Fail closed on damaged attachments.
                photos += photo
                pictureParagraph(photo, photos.size)
            } else para(line)
        }
        val document = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" " +
            "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\">" +
            "<w:body>" + body + "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>" +
            "<w:pgMar w:top=\"1200\" w:right=\"1100\" w:bottom=\"1200\" w:left=\"1100\"/>" +
            "</w:sectPr></w:body></w:document>"
        val types = """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Default Extension="jpg" ContentType="image/jpeg"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""
        val rels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
        val docRels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
        val styles = """<?xml version="1.0" encoding="UTF-8"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:sz w:val="22"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:rPr><w:b/><w:color w:val="007AFF"/><w:sz w:val="44"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:rPr><w:b/><w:color w:val="19324C"/><w:sz w:val="32"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:rPr><w:b/><w:sz w:val="26"/></w:rPr></w:style>
</w:styles>"""
        ZipOutputStream(output).use { zip ->
            write(zip, "[Content_Types].xml", types)
            write(zip, "_rels/.rels", rels)
            val imageRels = photos.indices.joinToString("") { n ->
                val index = n + 1
                "<Relationship Id=\"rId" + (index + 1) +
                    "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\"" +
                    " Target=\"media/handover_" + index + ".jpg\"/>"
            }
            write(zip, "word/_rels/document.xml.rels",
                docRels.replace("</Relationships>", imageRels + "</Relationships>"))
            write(zip, "word/styles.xml", styles)
            write(zip, "word/document.xml", document)
            photos.forEachIndexed { index, photo ->
                zip.putNextEntry(ZipEntry("word/media/handover_" + (index + 1) + ".jpg"))
                zip.write(photo.jpegBytes())
                zip.closeEntry()
            }
        }
    }
}
