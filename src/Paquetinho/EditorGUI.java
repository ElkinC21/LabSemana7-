package Paquetinho;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import javax.swing.*;
import javax.swing.text.*;

public class EditorGUI extends BaseFrame {

    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelCentro;

    private JToolBar toolBar;
    private JComboBox<String> fontBox;

    private JTextPane textPane;
    private StyledDocument doc;

    public EditorGUI() {
        super("Editor de texto", 800, 700);
    }

    @Override
    public void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());

        panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        panelNorte.setPreferredSize(new Dimension(0, 56));
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        panelNorte.add(toolBar);

        fontBox = crearCboFuentes();
        toolBar.add(new JLabel("Fuente: "));
        toolBar.add(fontBox);

        panelCentro = new JPanel(null);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        textPane = new JTextPane();
        doc = textPane.getStyledDocument();

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setBounds(20, 20, 720, 550);
        panelCentro.add(scroll);

        setContentPane(panelPrincipal);
    }

    private JComboBox<String> crearCboFuentes() {
        String families[] = getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        JComboBox<String> box = new JComboBox<>(families);
        box.setMaximumRowCount(10);
        box.addActionListener(e -> {
            String family = (String) box.getSelectedItem();
                aplicarFuente(family);
        });
        return box;
    }

    private void aplicarFuente(String family) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, family);
        textPane.setCharacterAttributes(atributos, false);
    }

    public static void main(String[] args) {
        new EditorGUI().setVisible(true);
    }
}
