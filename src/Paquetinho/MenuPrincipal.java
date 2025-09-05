package Paquetinho;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MenuPrincipal extends BaseFrame {

    private JButton btnNuevo;
    private JButton btnAbrir;
    private JButton btnSalir;

    public MenuPrincipal() {
        super("Menu Principal", 500, 300);
        initComponents();
        setVisible(true);
    }

    public void initComponents() {
        JPanel panel = new JPanel(null);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        btnNuevo = new JButton("Nuevo archivo");
        btnNuevo.setBounds(150, 60, 200, 45);
        estilizarBoton(btnNuevo);
        panel.add(btnNuevo);

        btnAbrir = new JButton("Abrir archivo");
        btnAbrir.setBounds(150, 120, 200, 45);
        estilizarBoton(btnAbrir);
        panel.add(btnAbrir);

        btnSalir = new JButton("Salir");
        btnSalir.setBounds(150, 180, 200, 45);
        estilizarBoton(btnSalir);
        btnSalir.setBackground(new Color(220, 80, 80));
        btnSalir.setForeground(Color.WHITE);
        panel.add(btnSalir);

        setContentPane(panel);

        btnNuevo.addActionListener(e -> {
            setVisible(false);                        
            new EditorGUI(this);                      
        });

        btnAbrir.addActionListener(e -> abrirDocxEnEditor());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void estilizarBoton(JButton boton) {
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        boton.setBackground(new Color(240, 240, 250));
        boton.setForeground(new Color(50, 50, 80));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200), 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void abrirDocxEnEditor() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir .docx");
        fc.setFileFilter(new FileNameExtensionFilter("Documento Word (*.docx)", "docx"));
        int r = fc.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        setVisible(false);                             
        new EditorGUI(this, f);                        
    }

    public static void main(String[] args) {
        new MenuPrincipal();
    }
}
