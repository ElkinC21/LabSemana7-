package Paquetinho;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public final class Wordimportar {

    private Wordimportar() {}

    public static void cargar(JTextPane destino, File archivoDocx) throws IOException, BadLocationException {
        if (destino == null) throw new IllegalArgumentException("destino == null");
        if (archivoDocx == null) throw new IllegalArgumentException("docx == null");
        if (!archivoDocx.isFile()) throw new IOException("Archivo invalido: " + archivoDocx.getAbsolutePath());

        if (!(destino.getEditorKit() instanceof StyledEditorKit)) {
            destino.setEditorKit(new StyledEditorKit());
        }

        StyledDocument documento = destino.getStyledDocument();
        documento.remove(0, documento.getLength());

        String xmlDocumento = leerDocumentXml(archivoDocx);
        xmlDocumento = xmlDocumento.replace("</w:p>", "</w:p>\n");

        Pattern patronBloque = Pattern.compile("(<w:p[\\s\\S]*?</w:p>)|(<w:tbl[\\s\\S]*?</w:tbl>)");
        Matcher buscadorBloque = patronBloque.matcher(xmlDocumento);

        while (buscadorBloque.find()) {
            String bloque = buscadorBloque.group();

            if (bloque.startsWith("<w:p")) {
                procesarParrafoComoTexto(bloque, documento);
                documento.insertString(documento.getLength(), "\n", null);
            } else if (bloque.startsWith("<w:tbl")) {
                insertarTablaDesdeXml(destino, documento, bloque);
            }
        }

        destino.setCaretPosition(0);
    }

    private static void procesarParrafoComoTexto(String bloqueParrafo, StyledDocument documento) throws BadLocationException {
        Matcher buscadorRun = Pattern.compile("<w:r[\\s\\S]*?</w:r>").matcher(bloqueParrafo);
        while (buscadorRun.find()) {
            String bloqueRun = buscadorRun.group();

            String bloquePropiedadesRun = extraerPrimero(bloqueRun, "<w:rPr[\\s\\S]*?</w:rPr>");
            SimpleAttributeSet atributos = construirAtributosDesdeRPr(bloquePropiedadesRun);

            Matcher mTexto = Pattern.compile("<w:t[^>]*>([\\s\\S]*?)</w:t>").matcher(bloqueRun);
            boolean emitio = false;
            while (mTexto.find()) {
                String texto = quitarXml(mTexto.group(1));
                if (!texto.isEmpty()) {
                    documento.insertString(documento.getLength(), texto, atributos);
                    emitio = true;
                }
            }

            if (!emitio && contieneEtiqueta(bloqueRun, "<w:br\\b")) {
                documento.insertString(documento.getLength(), "\n", atributos);
            }

            Matcher mInstr = Pattern.compile("<w:instrText[^>]*>([\\s\\S]*?)</w:instrText>").matcher(bloqueRun);
            while (mInstr.find()) {
                String texto = quitarXml(mInstr.group(1));
                if (!texto.isEmpty()) documento.insertString(documento.getLength(), texto, atributos);
            }
        }
    }

    private static SimpleAttributeSet construirAtributosDesdeRPr(String bloqueRPr) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        if (bloqueRPr == null) return atributos;

        String fuente = extraerAtributo(bloqueRPr, "<w:rFonts[^>]*?\\bw:ascii\\s*=\\s*\"([^\"]+)\"");
        if (fuente == null) fuente = extraerAtributo(bloqueRPr, "<w:rFonts[^>]*?\\bw:hAnsi\\s*=\\s*\"([^\"]+)\"");
        if (fuente != null && !fuente.isEmpty()) StyleConstants.setFontFamily(atributos, fuente);

        String tam = extraerAtributo(bloqueRPr, "<w:sz[^>]*?\\bw:val\\s*=\\s*\"(\\d+)\"");
        if (tam != null) {
            try {
                int halfPoints = Integer.parseInt(tam);
                int puntos = Math.max(1, halfPoints / 2);
                StyleConstants.setFontSize(atributos, puntos);
            } catch (NumberFormatException ignored) {}
        }

        String col = extraerAtributo(bloqueRPr, "<w:color[^>]*?\\bw:val\\s*=\\s*\"([0-9A-Fa-f]{3,8}|auto)\"");
        if (col != null && !"auto".equalsIgnoreCase(col)) {
            Color color = parsearColorHex(col);
            if (color != null) StyleConstants.setForeground(atributos, color);
        }

        if (contieneEtiqueta(bloqueRPr, "<w:b\\b")) StyleConstants.setBold(atributos, true);
        if (contieneEtiqueta(bloqueRPr, "<w:i\\b")) StyleConstants.setItalic(atributos, true);
        if (contieneEtiqueta(bloqueRPr, "<w:u\\b")) StyleConstants.setUnderline(atributos, true);

        return atributos;
    }

    private static void insertarTablaDesdeXml(JTextPane destino, StyledDocument documento, String xmlTabla) {
        ArrayList<ArrayList<String>> filas = new ArrayList<>();

        Matcher buscadorTr = Pattern.compile("<w:tr[\\s\\S]*?</w:tr>").matcher(xmlTabla);
        while (buscadorTr.find()) {
            String bloqueFila = buscadorTr.group();
            ArrayList<String> celdas = new ArrayList<>();

            Matcher buscadorTc = Pattern.compile("<w:tc[\\s\\S]*?</w:tc>").matcher(bloqueFila);
            while (buscadorTc.find()) {
                String bloqueCelda = buscadorTc.group();
                String textoCelda = extraerTextoDeCelda(bloqueCelda);
                celdas.add(textoCelda);
            }

            if (!celdas.isEmpty()) filas.add(celdas);
        }

        if (filas.isEmpty()) return;

        int columnas = 0;
        for (ArrayList<String> fila : filas) columnas = Math.max(columnas, fila.size());
        int totalFilas = filas.size();

        DefaultTableModel modeloTabla = new DefaultTableModel(totalFilas, columnas);
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int anchoColumna = 120;
        for (int c = 0; c < columnas; c++) tabla.getColumnModel().getColumn(c).setPreferredWidth(anchoColumna);

        for (int r = 0; r < totalFilas; r++) {
            ArrayList<String> fila = filas.get(r);
            for (int c = 0; c < fila.size(); c++) {
                modeloTabla.setValueAt(fila.get(c), r, c);
            }
        }

        int altoEncabezado = tabla.getTableHeader().getPreferredSize().height;
        int anchoPreferido = columnas * anchoColumna;
        int altoPreferido = altoEncabezado + totalFilas * tabla.getRowHeight() + 2;

        JScrollPane panelConScroll = new JScrollPane(tabla);
        panelConScroll.setPreferredSize(new Dimension(anchoPreferido, altoPreferido));
        panelConScroll.setBorder(new LineBorder(new Color(160, 160, 160)));

        try {
            int posicion = documento.getLength();
            documento.insertString(posicion, "\n", null);
            posicion++;

            SimpleAttributeSet atributosComponente = new SimpleAttributeSet();
            StyleConstants.setComponent(atributosComponente, panelConScroll);
            documento.insertString(posicion, " ", atributosComponente);
            posicion++;

            documento.insertString(posicion, "\n", null);
        } catch (Exception ignored) {}
    }

    
    private static String extraerTextoDeCelda(String bloqueCelda) {
        StringBuilder acumulado = new StringBuilder();

       
        Matcher mT = Pattern.compile("<w:t[^>]*>([\\s\\S]*?)</w:t>").matcher(bloqueCelda);
        while (mT.find()) {
            if (acumulado.length() > 0) acumulado.append("\n");
            acumulado.append(quitarXml(mT.group(1)));
        }

        
        Matcher mInstr = Pattern.compile("<w:instrText[^>]*>([\\s\\S]*?)</w:instrText>").matcher(bloqueCelda);
        while (mInstr.find()) {
            if (acumulado.length() > 0) acumulado.append("\n");
            acumulado.append(quitarXml(mInstr.group(1)));
        }

        
        Matcher mDel = Pattern.compile("<w:delText[^>]*>([\\s\\S]*?)</w:delText>").matcher(bloqueCelda);
        while (mDel.find()) {
            if (acumulado.length() > 0) acumulado.append("\n");
            acumulado.append(quitarXml(mDel.group(1)));
        }

        String resultado = acumulado.toString().trim();

        
        if (resultado.contains("<w:") || resultado.matches(".*<[^>]+>.*")) {
           
            String limpio = resultado.replaceAll("<[^>]+>", "");
           
            limpio = quitarXml(limpio).trim();
            resultado = limpio;
        }

       
        if (resultado.isEmpty()) {
            String sinTags = bloqueCelda.replaceAll("<[^>]+>", "");
            sinTags = quitarXml(sinTags).trim();
            return sinTags;
        }

        return resultado;
    }

    private static String leerDocumentXml(File archivoDocx) throws IOException {
        try (ZipFile zip = new ZipFile(archivoDocx)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) throw new IOException("word/document.xml no encontrado en el .docx");
            return new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extraerPrimero(String fuente, String regex) {
        if (fuente == null) return null;
        Matcher m = Pattern.compile(regex).matcher(fuente);
        return m.find() ? m.group() : null;
    }

    private static String extraerAtributo(String fuente, String regex) {
        Matcher m = Pattern.compile(regex).matcher(fuente);
        return m.find() ? m.group(1) : null;
    }

    private static boolean contieneEtiqueta(String fuente, String regex) {
        return Pattern.compile(regex).matcher(fuente).find();
    }

    private static Color parsearColorHex(String hex) {
        try {
            String h = hex.trim();
            if (h.length() == 3) {
                h = "" + h.charAt(0) + h.charAt(0)
                   + h.charAt(1) + h.charAt(1)
                   + h.charAt(2) + h.charAt(2);
            }
            if (h.length() == 6) {
                int rgb = Integer.parseInt(h, 16);
                return new Color(rgb);
            }
            if (h.length() == 8) {
                long argb = Long.parseLong(h, 16);
                return new Color((int)(argb & 0xFFFFFF));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String quitarXml(String texto) {
        return texto.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");
    }
}

