package Paquetinho;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.swing.JTextPane;
import javax.swing.text.rtf.RTFEditorKit;

public class Convertidor {

    public static void docxARtf(File docxEntrada, File rtfSalida) throws Exception {
        if (docxEntrada == null) {
            throw new IllegalArgumentException("docxEntrada == null");
        }
        if (rtfSalida == null) {
            throw new IllegalArgumentException("rtfSalida == null");
        }

        JTextPane editor = new JTextPane();
        editor.setEditorKit(new RTFEditorKit());

        Wordimportar.cargar(editor, docxEntrada);

        RTFEditorKit kit = (RTFEditorKit) editor.getEditorKit();
        File destino = asegurarRtf(rtfSalida);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {
            kit.write(writer, editor.getDocument(), 0, editor.getDocument().getLength());
        }
    }

    public static void rtfADocx(File rtfEntrada, File docxSalida) throws Exception {
        if (rtfEntrada == null) {
            throw new IllegalArgumentException("rtfEntrada == null");
        }
        if (docxSalida == null) {
            throw new IllegalArgumentException("docxSalida == null");
        }

        JTextPane editor = new JTextPane();
        editor.setEditorKit(new RTFEditorKit());
        RTFEditorKit kit = (RTFEditorKit) editor.getEditorKit();

        try (Reader reader = new InputStreamReader(new FileInputStream(rtfEntrada), StandardCharsets.UTF_8)) {
            kit.read(reader, editor.getDocument(), 0);
        }

        File destino = asegurarDocx(docxSalida);
        Wordexportar.guardar(editor, destino);
    }

    private static File asegurarRtf(File archivo) {
        String ruta = archivo.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".rtf")) {
            ruta += ".rtf";
        }
        return new File(ruta);
    }

    private static File asegurarDocx(File archivo) {
        String ruta = archivo.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".docx")) {
            ruta += ".docx";
        }
        return new File(ruta);
    }
}
