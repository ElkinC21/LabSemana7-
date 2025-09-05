package Paquetinho;

import javax.swing.JTextPane;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;

public class Convertidor {

    public static void docxARtf(File docxEntrada, File rtfSalida) throws Exception {
        JTextPane area = new JTextPane();
        area.setEditorKit(new RTFEditorKit());
        Wordimportar.cargar(area, docxEntrada);
        RTFEditorKit kit = (RTFEditorKit) area.getEditorKit();
        File destino = asegurarRtf(rtfSalida);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), "UTF-8")) {
            kit.write(w, area.getDocument(), 0, area.getDocument().getLength());
        }
    }

    public static void rtfADocx(File rtfEntrada, File docxSalida) throws Exception {                     
        JTextPane area = new JTextPane();
        area.setEditorKit(new RTFEditorKit());
        RTFEditorKit kit = (RTFEditorKit) area.getEditorKit();
        try (Reader r = new InputStreamReader(new FileInputStream(rtfEntrada), "UTF-8")) {
            kit.read(r, area.getDocument(), 0);
        }
        File destino = asegurarDocx(docxSalida);
        Wordexportar.guardar(area, destino);
    }

    private static File asegurarRtf(File f) {
        String p = f.getAbsolutePath();
        if (!p.toLowerCase().endsWith(".rtf")) p += ".rtf";
        return new File(p);
    }

    private static File asegurarDocx(File f) {
        String p = f.getAbsolutePath();
        if (!p.toLowerCase().endsWith(".docx")) p += ".docx";
        return new File(p);
    }
}
