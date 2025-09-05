package Paquetinho;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class EditorControl {

    private final JTextPane editor;
    private final Archivo archivo;
    private String fuente = "Arial";
    private int tamanio = 16;
    private Color color = Color.BLACK;

    public EditorControl(JTextPane editor, Archivo archivo) {
        this.editor = editor;
        this.archivo = archivo;
        this.editor.setEditorKit(new RTFEditorKit());
    }

    public void setFuente(String fuente) {
        if (fuente != null && !fuente.isBlank()) {
            this.fuente = fuente;
        }
    }

    public void setTamano(int tamanio) {
        if (tamanio > 0) {
            this.tamanio = tamanio;
        }
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    public void aplicar() {
        int inicio = editor.getSelectionStart();
        int fin = editor.getSelectionEnd();
        if (inicio == fin) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fuente);
        StyleConstants.setFontSize(attrs, tamanio);
        StyleConstants.setForeground(attrs, color);
        
        editor.getStyledDocument().setCharacterAttributes(inicio, fin - inicio, attrs, false);
    }

    public void abrir() throws IOException {
        String rtf = archivo.leerArchivo();
        try {
            Document doc = editor.getDocument();
            RTFEditorKit kit = (RTFEditorKit) editor.getEditorKit();
            kit.read(new StringReader(rtf), doc, 0);
        } catch (Exception e) {
            throw new IOException("Error al cargar RTF: " + e.getMessage(), e);
        }
    }

    public void guardar() throws IOException {
        try {
            StringWriter sw = new StringWriter();
            RTFEditorKit kit = (RTFEditorKit) editor.getEditorKit();
            kit.write(sw, editor.getDocument(), 0, editor.getDocument().getLength());
            archivo.guardarContenido(sw.toString());
        } catch (Exception e) {
            throw new IOException("Error al guardar RTF: " + e.getMessage(), e);
        }
    }
}
