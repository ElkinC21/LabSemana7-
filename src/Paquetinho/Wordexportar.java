package Paquetinho;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

public class Wordexportar {

    public static void guardar(JTextPane areaTexto, File archivo) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archivo))) {
            escribir(zip, "[Content_Types].xml", contentTypes());
            escribir(zip, "_rels/.rels", rels());
            escribir(zip, "docProps/app.xml", appXml());
            escribir(zip, "docProps/core.xml", coreXml());
            escribir(zip, "word/_rels/document.xml.rels", docRels());
            escribir(zip, "word/document.xml", documentXml(areaTexto.getStyledDocument()));
        }
    }

    private static void escribir(ZipOutputStream zip, String ruta, String contenido) throws Exception {
        zip.putNextEntry(new ZipEntry(ruta));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
                + "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
                + "</Types>";
    }

    private static String rels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                + "</Relationships>";
    }

    private static String appXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
                + "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
                + "<Application>Editor</Application>"
                + "</Properties>";
    }

    private static String coreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" "
                + "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:title>Documento</dc:title>"
                + "</cp:coreProperties>";
    }

    private static String docRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>";
    }

    private static String documentXml(StyledDocument doc) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
        out.append("<w:body>");
        int len = doc.getLength();
        String texto = doc.getText(0, len);
        String[] parrafos = texto.split("\n", -1);
        int cursor = 0;
        for (String ptxt : parrafos) {
            int inicioParrafo = cursor;
            int finParrafo = cursor + ptxt.length();
            out.append("<w:p>");
            int k = inicioParrafo;
            while (k < finParrafo) {
                AttributeSet attrs = doc.getCharacterElement(k).getAttributes();
                int end = Math.min(finParrafo, doc.getCharacterElement(k).getEndOffset());
                String chunk = ptxt.substring(k - inicioParrafo, end - inicioParrafo);
                out.append(runXml(chunk, attrs));
                k = end;
            }
            out.append("</w:p>");
            cursor = finParrafo + 1;
        }
        out.append("<w:sectPr/>");
        out.append("</w:body></w:document>");
        return out.toString();
    }

    private static String runXml(String texto, AttributeSet a) {
        StringBuilder sb = new StringBuilder();
        sb.append("<w:r><w:rPr>");
        String fuente = StyleConstants.getFontFamily(a);
        int tamano = StyleConstants.getFontSize(a);
        Color color = StyleConstants.getForeground(a);
        boolean negrita = StyleConstants.isBold(a);
        boolean cursiva = StyleConstants.isItalic(a);
        boolean subrayado = StyleConstants.isUnderline(a);
        if (fuente != null && !fuente.isEmpty()) {
            sb.append("<w:rFonts w:ascii=\"").append(escapeXml(fuente)).append("\" w:hAnsi=\"").append(escapeXml(fuente)).append("\"/>");
        }
        if (tamano > 0) {
            sb.append("<w:sz w:val=\"").append(tamano * 2).append("\"/>");
        }
        if (color != null) {
            sb.append("<w:color w:val=\"").append(hex(color)).append("\"/>");
        }
        if (negrita) sb.append("<w:b/>");
        if (cursiva) sb.append("<w:i/>");
        if (subrayado) sb.append("<w:u w:val=\"single\"/>");
        sb.append("</w:rPr>");
        sb.append("<w:t xml:space=\"preserve\">").append(escapeXml(texto)).append("</w:t>");
        sb.append("</w:r>");
        return sb.toString();
    }

    private static String escapeXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private static String hex(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
