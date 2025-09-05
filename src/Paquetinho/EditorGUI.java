package Paquetinho;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class EditorGUI extends BaseFrame {

    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelCentro;

    private JComboBox<String> cboFuentes;
    private JComboBox<String> cboTamanios;
    private JButton btnColor;

    private JTextPane areaTexto;
    private StyledDocument documento;

    public EditorGUI() {
        super("Editor de texto", 800, 700);
    }

    @Override
    public void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());

        panelNorte = new JPanel(null);
        panelNorte.setPreferredSize(new Dimension(0, 60));
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        JLabel lblFuente = new JLabel("Fuente:");
        lblFuente.setBounds(10, 15, 60, 28);
        panelNorte.add(lblFuente);

        cboFuentes = crearComboFuentes();
        cboFuentes.setBounds(70, 15, 200, 28);
        panelNorte.add(cboFuentes);

        JLabel lblTamanio = new JLabel("Tamaño:");
        lblTamanio.setBounds(285, 15, 60, 28);
        panelNorte.add(lblTamanio);

        cboTamanios = crearComboTamanios();
        cboTamanios.setBounds(345, 15, 70, 28);
        panelNorte.add(cboTamanios);

        btnColor = new JButton("Color");
        btnColor.setBounds(430, 15, 80, 28);
        btnColor.addActionListener(e -> {
            JColorChooser selectorColor = new JColorChooser(areaTexto.getForeground());
            selectorColor.setPreviewPanel(new JPanel());

            JDialog dialogColor = JColorChooser.createDialog(
                    this, "Elegir color", true, selectorColor,
                    eventoAceptar -> {
                        Color colorElegido = selectorColor.getColor();
                        if (colorElegido != null) aplicarColor(colorElegido);
                    },
                    null
            );
            dialogColor.setVisible(true);
        });
        
        panelNorte.add(btnColor);

        panelCentro = new JPanel(null);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        areaTexto = new JTextPane();
        documento = areaTexto.getStyledDocument();

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        scroll.setBounds(20, 20, 740, 580);
        panelCentro.add(scroll);

        setContentPane(panelPrincipal);
    }

    private JComboBox<String> crearComboFuentes() {
        String[] familias = getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> combo = new JComboBox<>(familias);
        combo.setMaximumRowCount(12);
        combo.addActionListener(e -> {
            String familiaSeleccionada = (String) combo.getSelectedItem();
            if (familiaSeleccionada != null) aplicarFuente(familiaSeleccionada);
        });
        return combo;
    }

    private JComboBox<String> crearComboTamanios() {
        String[] tamanios = {"8","10","12","14","16","18","20","24","28","32","36","48","72"};
        JComboBox<String> combo = new JComboBox<>(tamanios);
        combo.setEditable(true);
        combo.addActionListener(e -> {
            Object seleccion = combo.getSelectedItem();
            if (seleccion != null) {
                try {
                    int tamanio = Integer.parseInt(seleccion.toString().trim());
                    aplicarTamanio(tamanio);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Error: ingrese un número entero válido.");
                }
            }
        });
        return combo;
    }

    private void aplicarFuente(String familia) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, familia);
        areaTexto.setCharacterAttributes(attrs, false);
    }

    private void aplicarTamanio(int tamanio) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, tamanio);
        areaTexto.setCharacterAttributes(attrs, false);
    }

    private void aplicarColor(Color color) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        areaTexto.setCharacterAttributes(attrs, false);
    }

    public static void main(String[] args) {
        new EditorGUI().setVisible(true);
    }
}
