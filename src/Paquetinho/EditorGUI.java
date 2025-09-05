package Paquetinho;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class EditorGUI extends BaseFrame {

    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelCentro;
    private JPanel panelSur;

    private JComboBox<String> cboFuentes;
    private JComboBox<String> cboTamanios;
    private JButton btnColor;
    private JButton btnInsertarTabla;

    private JButton btnGuardar;
    private JButton btnRegresar;

    private JTextPane areaTexto;

    private JTable ultimaTablaInsertada; // referencia a la última tabla creada
    private final Archivo archivo = new Archivo();

    public EditorGUI() {
        super("Editor de texto", 800, 700);
    }

    @Override
    public void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        setContentPane(panelPrincipal);

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
                    ev -> {
                        Color colorElegido = selectorColor.getColor();
                        if (colorElegido != null) {
                            aplicarColor(colorElegido);
                        }
                    },
                    null
            );
            dialogColor.setVisible(true);
        });
        panelNorte.add(btnColor);

        // 🔹 Botón Insertar Tabla
        btnInsertarTabla = new JButton("Insertar Tabla");
        btnInsertarTabla.setBounds(520, 15, 120, 28);
        btnInsertarTabla.addActionListener(e -> {
            try {
                int filas = Integer.parseInt(JOptionPane.showInputDialog(this, "Número de filas:"));
                int cols = Integer.parseInt(JOptionPane.showInputDialog(this, "Número de columnas:"));
                insertarTabla(filas, cols);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Entrada inválida.");
            }
        });
        panelNorte.add(btnInsertarTabla);

        panelCentro = new JPanel(null);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        areaTexto = new JTextPane();

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        scroll.setBounds(20, 20, 740, 520);
        panelCentro.add(scroll);

        panelSur = new JPanel();
        btnGuardar = new JButton("Guardar DOCX");
        btnRegresar = new JButton("Regresar");
        panelSur.add(btnGuardar);
        panelSur.add(btnRegresar);
        panelPrincipal.add(panelSur, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardarDocxConChooser());
        // btnRegresar sin lógica
    }

    private JComboBox<String> crearComboFuentes() {
        String[] familias = getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> combo = new JComboBox<>(familias);
        combo.setMaximumRowCount(12);
        combo.addActionListener(e -> {
            String familiaSeleccionada = (String) combo.getSelectedItem();
            if (familiaSeleccionada != null) {
                aplicarFuente(familiaSeleccionada);
            }
        });
        return combo;
    }

    private JComboBox<String> crearComboTamanios() {
        String[] tamanios = {"8", "10", "12", "14", "16", "18", "20", "24", "28", "32", "36", "48", "72"};
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

    // 🔹 Insertar tabla en el JTextPane
    private void insertarTabla(int filas, int columnas) {
        JTable tabla = new JTable(new DefaultTableModel(filas, columnas));
        tabla.setRowHeight(25);
        tabla.setGridColor(Color.BLACK);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new java.awt.Dimension(columnas * 80, filas * 30));

        try {
            areaTexto.insertComponent(scrollTabla);
            ultimaTablaInsertada = tabla; // guardamos referencia
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al insertar tabla: " + e.getMessage());
        }
    }

    // 🔹 Guardar documento (texto + tablas incrustadas) en DOCX/RTF
    private void guardarDocxConChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar como");
        fc.setFileFilter(new FileNameExtensionFilter("Documento Word (*.docx)", "docx"));
        int r = fc.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        File seleccionado = fc.getSelectedFile();
        String ruta = seleccionado.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".docx")) ruta += ".docx";

        try {
            archivo.crearArchivo(ruta);
            Wordexportar.guardar(areaTexto, new File(ruta));
            JOptionPane.showMessageDialog(this, "Guardado con tablas incrustadas");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new EditorGUI().setVisible(true);
    }
}
