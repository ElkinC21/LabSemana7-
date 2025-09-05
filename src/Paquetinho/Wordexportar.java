package Paquetinho;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

public class Wordexportar {

    public static void guardar(JTextPane editor, File archivoDestino) throws Exception {
        Objects.requireNonNull(editor, "editor == null");
        Objects.requireNonNull(archivoDestino, "archivoDestino == null");

        StyledDocument documento = editor.getStyledDocument();

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archivoDestino))) {
            escribirEnZip(zip, "[Content_Types].xml", xmlContentTypes());
            escribirEnZip(zip, "_rels/.rels", xmlRelsRaiz());
            escribirEnZip(zip, "docProps/app.xml", xmlApp());
            escribirEnZip(zip, "docProps/core.xml", xmlCore());
            escribirEnZip(zip, "word/_rels/document.xml.rels", xmlRelsDocumento());
            escribirEnZip(zip, "word/document.xml", xmlDocumentoWord(documento));
        }
    }

    private static void escribirEnZip(ZipOutputStream zip, String ruta, String contenido) throws Exception {
        zip.putNextEntry(new ZipEntry(ruta));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String xmlContentTypes() {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                 <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                 <Default Extension="xml"  ContentType="application/xml"/>
                 <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                 <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
                 <Override PartName="/docProps/app.xml"  ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
               </Types>
               """;
    }

    private static String xmlRelsRaiz() {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                 <Relationship Id="rId1"
                   Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                   Target="word/document.xml"/>
               </Relationships>
               """;
    }

    private static String xmlApp() {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
                           xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
                 <Application>Editor</Application>
               </Properties>
               """;
    }

    private static String xmlCore() {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <cp:coreProperties
                   xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
                   xmlns:dc="http://purl.org/dc/elements/1.1/"
                   xmlns:dcterms="http://purl.org/dc/terms/"
                   xmlns:dcmitype="http://purl.org/dc/dcmitype/"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                 <dc:title>Documento</dc:title>
               </cp:coreProperties>
               """;
    }

    private static String xmlRelsDocumento() {
        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>
               """;
    }

    private static String xmlDocumentoWord(StyledDocument documento) throws Exception {
        StringBuilder salida = new StringBuilder(8_192);

        salida.append(""" 
            <?xml version="1.0" encoding="UTF-8"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
            """);

        Element raiz = documento.getDefaultRootElement();
        int totalParrafos = raiz.getElementCount();

        for (int iParrafo = 0; iParrafo < totalParrafos; iParrafo++) {
            Element parrafo = raiz.getElement(iParrafo);

            StringBuilder runsParrafo = new StringBuilder();
            boolean escribioTablaEnEsteBloque = false;

            int totalHojas = parrafo.getElementCount();
            for (int iHoja = 0; iHoja < totalHojas; iHoja++) {
                Element hoja = parrafo.getElement(iHoja);
                AttributeSet atributos = hoja.getAttributes();

                Component componente = StyleConstants.getComponent(atributos);
                if (componente != null) {
                    if (runsParrafo.length() > 0) {
                        salida.append("<w:p>").append(runsParrafo).append("</w:p>");
                        runsParrafo.setLength(0);
                    }

                    JTable tabla = extraerTablaDeComponente(componente);
                    if (tabla != null) {
                        String[][] celdas = extraerCeldasDeTabla(tabla);
                        String xmlTabla = construirTablaOOXML(celdas, null);
                        salida.append(xmlTabla);
                        escribioTablaEnEsteBloque = true;
                    }
                    continue;
                }

                int inicio = Math.max(0, hoja.getStartOffset());
                int fin = Math.min(documento.getLength(), hoja.getEndOffset());
                if (fin > inicio) {
                    String trozo = documento.getText(inicio, fin - inicio);
                    if ("\n".equals(trozo)) {
                        trozo = "";
                    }
                    if (!trozo.isEmpty()) {
                        runsParrafo.append(xmlRun(trozo, atributos));
                    }
                }
            }

            if (runsParrafo.length() > 0 || !escribioTablaEnEsteBloque) {
                salida.append("<w:p>").append(runsParrafo).append("</w:p>");
            }
        }

        salida.append("""
              <w:sectPr/>
              </w:body>
            </w:document>
            """);

        return salida.toString();
    }

    private static String xmlRun(String texto, AttributeSet atributos) {
        StringBuilder sb = new StringBuilder();

        sb.append("<w:r><w:rPr>");

        String fuente = StyleConstants.getFontFamily(atributos);
        int tamano = StyleConstants.getFontSize(atributos);
        Color color = StyleConstants.getForeground(atributos);
       

        if (fuente != null && !fuente.isEmpty()) {
            String f = escaparXml(fuente);
            sb.append("<w:rFonts w:ascii=\"").append(f).append("\" w:hAnsi=\"").append(f).append("\"/>");
        }
        if (tamano > 0) {
            sb.append("<w:sz w:val=\"").append(tamano * 2).append("\"/>"); 
        }
        if (color != null) {
            sb.append("<w:color w:val=\"").append(aHex(color)).append("\"/>");
        }
        
        
        

        sb.append("</w:rPr>");
        sb.append("<w:t xml:space=\"preserve\">").append(escaparXml(texto)).append("</w:t>");
        sb.append("</w:r>");

        return sb.toString();
    }

    private static String escaparXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String aHex(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static JTable extraerTablaDeComponente(Component componente) {
        if (componente instanceof JTable tabla) {
            return tabla;
        }
        if (componente instanceof JScrollPane panelConScroll) {
            Component vista = panelConScroll.getViewport().getView();
            if (vista instanceof JTable tablaVista) {
                return tablaVista;
            }
        }
        return null;
    }

    private static String[][] extraerCeldasDeTabla(JTable tabla) {
        TableModel modelo = tabla.getModel();
        int filas = modelo.getRowCount();
        int columnas = modelo.getColumnCount();
        String[][] celdas = new String[filas][columnas];

        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < columnas; c++) {
                Object v = modelo.getValueAt(r, c);
                celdas[r][c] = v == null ? "" : v.toString();
            }
        }
        return celdas;
    }

    private static String construirTablaOOXML(String[][] celdas, int[] anchosTwips) {
        int filas = celdas.length;
        int columnas = filas == 0 ? 0 : celdas[0].length;

        StringBuilder sb = new StringBuilder(2048);
        sb.append("<w:tbl xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");

        sb.append("<w:tblPr><w:tblBorders>")
                .append(xmlBorde("top"))
                .append(xmlBorde("left"))
                .append(xmlBorde("bottom"))
                .append(xmlBorde("right"))
                .append(xmlBorde("insideH"))
                .append(xmlBorde("insideV"))
                .append("</w:tblBorders></w:tblPr>");

        // Grid (anchos de columnas)
        sb.append("<w:tblGrid>");
        if (columnas > 0) {
            if (anchosTwips == null) {
                int anchoPorColumna = 2400; // ~1.67
                for (int i = 0; i < columnas; i++) {
                    sb.append("<w:gridCol w:w=\"").append(anchoPorColumna).append("\"/>");
                }
            } else {
                for (int ancho : anchosTwips) {
                    sb.append("<w:gridCol w:w=\"").append(ancho).append("\"/>");
                }
            }
        }
        sb.append("</w:tblGrid>");

        // Filas y celdas
        for (int r = 0; r < filas; r++) {
            sb.append("<w:tr>");
            for (int c = 0; c < columnas; c++) {
                String texto = celdas[r][c] == null ? "" : escaparXml(celdas[r][c]);
                sb.append("<w:tc><w:tcPr/>")
                        .append("<w:p><w:r><w:t xml:space=\"preserve\">")
                        .append(texto)
                        .append("</w:t></w:r></w:p></w:tc>");
            }
            sb.append("</w:tr>");
        }

        sb.append("</w:tbl>");
        return sb.toString();
    }

    private static String xmlBorde(String lado) {
        return "<w:" + lado + " w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"000000\"/>";
    }
}
