package Paquetinho;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import static javax.swing.text.StyleConstants.setComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public class EditorGUI extends BaseFrame {

    
    private final MenuPrincipal owner;

    private JPanel panelPrincipal;
    private JPanel panelNorte;
    private JPanel panelCentro;
    private JPanel panelSur;

    private JComboBox<String> cboFuentes;
    private JComboBox<String> cboTamanios;
    private JButton btnColor;

    private JButton btnGuardar;
    private JButton btnRegresar;

    private JTextPane areaTexto;

    private File archivoActual = null;

    private JPanel panelRecientes;
    private final List<Color> coloresRecientes = new ArrayList<>();
    private static final int MAX_RECENTES = 8;
    private static final int TAM_SL = 22;

    private final java.util.List<JTable> tablas = new java.util.ArrayList<>();

   

    
    public EditorGUI() {
        this(null, null);
    }

    
    public EditorGUI(MenuPrincipal owner) {
        this(owner, null);
    }

    
    public EditorGUI(MenuPrincipal owner, File fileToOpen) {
        super("Editor de texto", 800, 700);
        this.owner = owner;
        initComponents();
        if (fileToOpen != null) {
            cargarDocxEnEditor(fileToOpen);
            archivoActual = fileToOpen;
        }
        setVisible(true);
    }

    

    public void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout());
        setContentPane(panelPrincipal);

        panelNorte = new JPanel(null);
        panelNorte.setPreferredSize(new Dimension(0, 88));
        panelPrincipal.add(panelNorte, BorderLayout.NORTH);

        JLabel lblFuente = new JLabel("Fuente:");
        lblFuente.setBounds(10, 15, 60, 28);
        panelNorte.add(lblFuente);

        cboFuentes = crearComboFuentes();
        cboFuentes.setBounds(70, 15, 200, 28);
        panelNorte.add(cboFuentes);

        JLabel lblTamanio = new JLabel("Tamano:");
        lblTamanio.setBounds(285, 15, 60, 28);
        panelNorte.add(lblTamanio);

        cboTamanios = crearComboTamanios();
        cboTamanios.setBounds(345, 15, 70, 28);
        panelNorte.add(cboTamanios);

        btnColor = new JButton("Color");
        btnColor.setBounds(430, 15, 90, 28);
        btnColor.addActionListener(e -> {
            JColorChooser selectorColor = new JColorChooser(areaTexto.getForeground());
            selectorColor.setPreviewPanel(new JPanel());
            JDialog dialogColor = JColorChooser.createDialog(
                    this, "Elegir color", true, selectorColor,
                    ev -> {
                        Color colorElegido = selectorColor.getColor();
                        if (colorElegido != null) {
                            aplicarColor(colorElegido);
                            pushColorReciente(colorElegido);
                        }
                    },
                    null
            );
            dialogColor.setVisible(true);
        });
        panelNorte.add(btnColor);

        JButton btnTabla = new JButton("Tabla");
        btnTabla.setBounds(530, 15, 90, 28);
        btnTabla.addActionListener(e -> {
            DialogoTabla dlg = new DialogoTabla(this);
            dlg.setVisible(true);
            if (!dlg.isOk()) return;
            insertarTablaEnTexto(dlg.getFilas(), dlg.getCols());
        });
        panelNorte.add(btnTabla);

        JLabel lblUsados = new JLabel("Usados:");
        lblUsados.setBounds(10, 52, 60, 24);
        panelNorte.add(lblUsados);

        panelRecientes = new JPanel(null);
        panelRecientes.setBounds(70, 52, 740, 24);
        panelRecientes.setOpaque(false);
        panelNorte.add(panelRecientes);
        renderRecientes();

        panelCentro = new JPanel(null);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        areaTexto = new JTextPane();
        areaTexto.setEditorKit(new StyledEditorKit()); 

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

        btnRegresar.addActionListener(e -> {
            if (owner != null) owner.setVisible(true); 
            dispose(); 
        });
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
                    JOptionPane.showMessageDialog(this, "Error: ingrese un numero entero valido.");
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

    

    private void pushColorReciente(Color c) {
        if (c == null) return;
        for (int i = 0; i < coloresRecientes.size(); i++) {
            if (coloresRecientes.get(i).equals(c)) {
                coloresRecientes.remove(i);
                break;
            }
        }
        coloresRecientes.add(0, c);
        while (coloresRecientes.size() > MAX_RECENTES) {
            coloresRecientes.remove(coloresRecientes.size() - 1);
        }
        renderRecientes();
    }

    private void renderRecientes() {
        panelRecientes.removeAll();
        final int espaciado = 6;
        int x = 0;
        for (int i = 0; i < coloresRecientes.size(); i++) {
            Color c = coloresRecientes.get(i);
            JButton btn = crearSlot(c);
            btn.setBounds(x, 0, TAM_SL, TAM_SL);
            panelRecientes.add(btn);
            x += TAM_SL + espaciado;
        }
        if (coloresRecientes.isEmpty()) {
            JButton btn = crearSlot(null);
            btn.setBounds(0, 0, TAM_SL, TAM_SL);
            panelRecientes.add(btn);
        }
        panelRecientes.revalidate();
        panelRecientes.repaint();
    }

    private JButton crearSlot(Color c) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(70, 70, 70)));
        btn.setOpaque(true);
        if (c == null) {
            btn.setEnabled(false);
            btn.setBackground(new Color(235, 235, 235));
            btn.setToolTipText("Sin colores usados aún");
        } else {
            btn.setEnabled(true);
            btn.setBackground(c);
            btn.setToolTipText("#" + toHex(c));
            btn.addActionListener(e -> {
                aplicarColor(c);
                pushColorReciente(c);
            });
        }
        return btn;
    }

    private static String toHex(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    

    private void insertarTablaEnTexto(int filas, int cols) {
        DefaultTableModel modelo = new DefaultTableModel(filas, cols);
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int anchoColumna = 90;
        int altoEncabezado = tabla.getTableHeader().getPreferredSize().height;

        int anchoPreferido = cols * anchoColumna;
        int altoPreferido = altoEncabezado + filas * tabla.getRowHeight() + 2;

        for (int c = 0; c < cols; c++) {
            tabla.getColumnModel().getColumn(c).setPreferredWidth(anchoColumna);
        }

        JScrollPane sp = new JScrollPane(tabla);
        sp.setPreferredSize(new Dimension(anchoPreferido, altoPreferido));
        sp.setBorder(new LineBorder(new Color(160, 160, 160)));

        try {
            StyledDocument doc = areaTexto.getStyledDocument();
            int pos = areaTexto.getCaretPosition();

            doc.insertString(pos, "\n", null);
            pos++;

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            setComponent(attrs, sp);
            doc.insertString(pos, " ", attrs);
            pos++;

            doc.insertString(pos, "\n", null);

            tablas.add(tabla);
            areaTexto.requestFocusInWindow();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo insertar la tabla: " + ex.getMessage());
        }
    }

    

    private void cargarDocxEnEditor(File f) {
        try {
            
            Wordimportar.cargar(areaTexto, f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el DOCX: " + ex.getMessage());
        }
    }

    private void guardarDocxConChooser() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar como");
            fc.setFileFilter(new FileNameExtensionFilter("Documento Word (*.docx)", "docx"));

            if (archivoActual != null) {
                fc.setSelectedFile(archivoActual);
            }

            int r = fc.showSaveDialog(this);
            if (r != JFileChooser.APPROVE_OPTION) return;

            File seleccionado = fc.getSelectedFile();
            String ruta = seleccionado.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".docx")) {
                ruta += ".docx";
            }

            archivoActual = new File(ruta);
            Wordexportar.guardar(areaTexto, archivoActual);

            JOptionPane.showMessageDialog(this, "Guardado");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
        }
    }

    
}
