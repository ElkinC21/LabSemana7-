package Paquetinho;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MenuPrincipal extends BaseFrame {

    private JButton btnNuevo;
    private JButton btnAbrir;
    private JButton btnSalir;

    public MenuPrincipal() {
        super("Menú Principal", 500, 350);
    }

    @Override
    public void initComponents() {

        JPanel panel = new JPanel(null) {
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = crearLabel("Editor de Texto", 100, 20, 300, 40, Font.BOLD, 26f);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setForeground(new Color(40, 40, 90));
        panel.add(lblTitulo);

        btnNuevo = crearBoton("Nuevo archivo", 150, 90, 200, 45);
        estilizarBoton(btnNuevo);
        panel.add(btnNuevo);

        btnAbrir = crearBoton("Abrir archivo", 150, 150, 200, 45);
        estilizarBoton(btnAbrir);
        panel.add(btnAbrir);

        btnSalir = crearBoton("Salir", 150, 210, 200, 45);
        estilizarBoton(btnSalir);
        btnSalir.setBackground(new Color(220, 80, 80));
        btnSalir.setForeground(Color.WHITE);
        panel.add(btnSalir);

        setContentPane(panel);
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

    public static void main(String[] args) {
        new MenuPrincipal().setVisible(true);
    }
}
