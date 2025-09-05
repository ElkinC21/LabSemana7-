package Paquetinho;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Wordimportar {

    public static void cargar(JTextPane areaTexto, File archivo) throws Exception {
        try (ZipFile zip = new ZipFile(archivo)) {
            ZipEntry entrada = buscarEntrada(zip, "word/document.xml");
            if (entrada == null) throw new IllegalArgumentException("No se encontro word/document.xml");
            try (InputStream entradaStream = zip.getInputStream(entrada)) {
                Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entradaStream);
                xml.getDocumentElement().normalize();
                StyledDocument docSwing = areaTexto.getStyledDocument();
                docSwing.remove(0, docSwing.getLength());
                NodeList listaParrafos = xml.getElementsByTagNameNS("*", "p");
                for (int i = 0; i < listaParrafos.getLength(); i++) {
                    Element parrafo = (Element) listaParrafos.item(i);
                    NodeList listaRuns = parrafo.getElementsByTagNameNS("*", "r");
                    for (int j = 0; j < listaRuns.getLength(); j++) {
                        Element run = (Element) listaRuns.item(j);
                        SimpleAttributeSet atributos = obtenerAtributos(run);
                        String texto = obtenerTexto(run);
                        if (!texto.isEmpty()) docSwing.insertString(docSwing.getLength(), texto, atributos);
                    }
                    if (i < listaParrafos.getLength() - 1) docSwing.insertString(docSwing.getLength(), "\n", null);
                }
                areaTexto.setCaretPosition(0);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static ZipEntry buscarEntrada(ZipFile zip, String ruta) {
        Enumeration<? extends ZipEntry> entradas = zip.entries();
        while (entradas.hasMoreElements()) {
            ZipEntry z = entradas.nextElement();
            if (!z.isDirectory() && z.getName().equals(ruta)) return z;
        }
        return null;
    }

    private static String obtenerTexto(Element run) {
        StringBuilder sb = new StringBuilder();
        NodeList listaTexto = run.getElementsByTagNameNS("*", "t");
        for (int k = 0; k < listaTexto.getLength(); k++) {
            Node nodo = listaTexto.item(k);
            if (nodo != null && nodo.getTextContent() != null) sb.append(nodo.getTextContent());
        }
        return sb.toString();
    }

    private static SimpleAttributeSet obtenerAtributos(Element run) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        Element propiedades = hijoNS(run, "*", "rPr");
        String fuente = null;
        Integer tamano = null;
        Color color = null;
        boolean negrita = false;
        boolean cursiva = false;
        boolean subrayado = false;
        if (propiedades != null) {
            Element nodoFuente = hijoNS(propiedades, "*", "rFonts");
            if (nodoFuente != null) fuente = primeroNoVacio(
                    nodoFuente.getAttribute("w:ascii"),
                    nodoFuente.getAttribute("ascii"),
                    nodoFuente.getAttribute("w:hAnsi"),
                    nodoFuente.getAttribute("hAnsi")
            );
            Element nodoTamano = hijoNS(propiedades, "*", "sz");
            if (nodoTamano != null) {
                String valor = primeroNoVacio(nodoTamano.getAttribute("w:val"), nodoTamano.getAttribute("val"));
                if (valor != null && !valor.isBlank()) {
                    try {
                        int halfPts = Integer.parseInt(valor);
                        if (halfPts > 0) tamano = halfPts / 2;
                    } catch (NumberFormatException ignored) {}
                }
            }
            Element nodoColor = hijoNS(propiedades, "*", "color");
            if (nodoColor != null) {
                String valor = primeroNoVacio(nodoColor.getAttribute("w:val"), nodoColor.getAttribute("val"));
                Color c = convertirHexColor(valor);
                if (c != null) color = c;
            }
            if (hijoNS(propiedades, "*", "b") != null) negrita = true;
            if (hijoNS(propiedades, "*", "i") != null) cursiva = true;
            Element nodoSubrayado = hijoNS(propiedades, "*", "u");
            if (nodoSubrayado != null) {
                String valor = primeroNoVacio(nodoSubrayado.getAttribute("w:val"), nodoSubrayado.getAttribute("val"));
                subrayado = valor == null || valor.isBlank() || !valor.equalsIgnoreCase("none");
            }
        }
        if (fuente != null) StyleConstants.setFontFamily(atributos, fuente);
        if (tamano != null && tamano > 0) StyleConstants.setFontSize(atributos, tamano);
        if (color != null) StyleConstants.setForeground(atributos, color);
        StyleConstants.setBold(atributos, negrita);
        StyleConstants.setItalic(atributos, cursiva);
        StyleConstants.setUnderline(atributos, subrayado);
        return atributos;
    }

    private static Element hijoNS(Element padre, String ns, String local) {
        NodeList lista = padre.getElementsByTagNameNS(ns, local);
        for (int i = 0; i < lista.getLength(); i++) {
            Node n = lista.item(i);
            if (n instanceof Element && n.getParentNode() == padre) return (Element) n;
        }
        return null;
    }

    private static String primeroNoVacio(String... valores) {
        for (String v : valores) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static Color convertirHexColor(String v) {
        try {
            if (v == null || v.isBlank()) return null;
            String h = v.startsWith("#") ? v.substring(1) : v;
            if (h.length() == 6) {
                int r = Integer.parseInt(h.substring(0,2),16);
                int g = Integer.parseInt(h.substring(2,4),16);
                int b = Integer.parseInt(h.substring(4,6),16);
                return new Color(r,g,b);
            }
            if (h.length() == 8) {
                int r = Integer.parseInt(h.substring(2,4),16);
                int g = Integer.parseInt(h.substring(4,6),16);
                int b = Integer.parseInt(h.substring(6,8),16);
                return new Color(r,g,b);
            }
        } catch (Exception ignored) {}
        return null;
    }
}

