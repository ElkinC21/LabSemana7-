package Paquetinho;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DocxLeer {

    
    private static final Pattern PATRON_TEXTO = Pattern.compile("<w:t[^>]*>(.*?)</w:t>");


    public static String leerTextoPlano(File archivoDocx) throws IOException {
        validarArchivoDocx(archivoDocx);

        String xmlDocumento = leerXmlPrincipal(archivoDocx);

        String xmlConSaltos = xmlDocumento.replaceAll("</w:p>", "\n");

        StringBuilder textoPlano = new StringBuilder();
        Matcher buscador = PATRON_TEXTO.matcher(xmlConSaltos);
        while (buscador.find()) {
            String contenido = buscador.group(1);
            textoPlano.append(desescaparXml(contenido));
        }
        return textoPlano.toString();
    }

   
    private static void validarArchivoDocx(File archivoDocx) throws IOException {
        if (archivoDocx == null) {
            throw new IllegalArgumentException("archivoDocx == null");
        }
        if (!archivoDocx.isFile()) {
            throw new IOException("Archivo invalido: " + archivoDocx.getAbsolutePath());
        }
    }

    private static String leerXmlPrincipal(File archivoDocx) throws IOException {
        try (ZipFile zip = new ZipFile(archivoDocx)) {
            ZipEntry entrada = zip.getEntry("word/document.xml");
            if (entrada == null) {
                throw new IOException("word/document.xml no encontrado en el .docx");
            }
            byte[] bytes = zip.getInputStream(entrada).readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static String desescaparXml(String texto) {
        return texto.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");
    }
}
