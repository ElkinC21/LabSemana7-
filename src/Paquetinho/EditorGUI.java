package Paquetinho;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class EditorGUI extends BaseFrame {

    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelCentro;

    private JComboBox<String> fontBox;
    private JComboBox<String> tamBox;
    private JButton btnColor;

    private JTextPane textPane;
    private StyledDocument doc;

    public EditorGUI() {
        super("Editor de texto", 800, 700);
    }

    @Override
    public void initComponents() {
        //panel principal
        panelPrincipal = new JPanel(new BorderLayout());

        //resto de paneles
        panelNorte = new JPanel(null);
        panelNorte.setPreferredSize(new Dimension(0, 60));
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        // Fuente
        fontBox = crearCboFuentes();

        JLabel lblFuente = crearLabel("Fuente:", 10, 15, 60, 25, java.awt.Font.PLAIN, 14f);
        panelNorte.add(lblFuente);
        panelNorte.add(fontBox);

        btnColor = crearBoton("Color", 400, 10, 80, 30);
        btnColor.addActionListener(e -> {
            java.awt.Color c = JColorChooser.showDialog(this, "Elegir color", textPane.getForeground());
            if (c != null) {
                aplicarColor(c);
            }
        });
        panelNorte.add(btnColor);

        // Tamaño
        tamBox = crearCboTamanios();

        JLabel lblTam = crearLabel("Tamaño:", 265, 15, 60, 25, java.awt.Font.PLAIN, 14f);
        panelNorte.add(lblTam);

        tamBox.setBounds(325, 10, 50, 30);
        panelNorte.add(tamBox);

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
        box.setBounds(70, 10, 180, 30);
        box.addActionListener(e -> {
            String family = (String) box.getSelectedItem();
            aplicarFuente(family);
        });
        return box;
    }

    private JComboBox<String> crearCboTamanios() {
        String tamanios[] = {"8", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "48", "72"};
        JComboBox<String> box = new JComboBox<>(tamanios);
        box.setEditable(true);

        box.addActionListener(e -> {
            Object sel = box.getSelectedItem();
            if (sel != null) {
                try {
                    int size = Integer.parseInt(sel.toString().trim());
                    aplicarTamanio(size);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Error: ingrese un número entero válido.");
                }
            }
        });
        return box;
    }

    private void aplicarTamanio(int size) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontSize(atributos, size);
        textPane.setCharacterAttributes(atributos, false);
    }

    private void aplicarFuente(String family) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setFontFamily(atributos, family);
        textPane.setCharacterAttributes(atributos, false);
    }

    public static void main(String[] args) {
        new EditorGUI().setVisible(true);
    }

    private void aplicarColor(Color c) {
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, c);
        textPane.setCharacterAttributes(atributos, false);
    }
}
