package Paquetinho;

import java.awt.Font;
import javax.swing.*;

public class MenuPrincipal extends BaseFrame {

    private JButton btnNuevo;
    private JButton btnAbrir;
    private JButton btnSalir;

    public MenuPrincipal() {
        super("Menú Principal", 400, 250);
    }

    @Override
    public void initComponents() {
        JPanel panel = new JPanel(null);

        JLabel lblTitulo = crearLabel("Editor de Texto", 120, 0, 250, 40, Font.BOLD, 22f);
        panel.add(lblTitulo);

        btnNuevo = crearBoton("Nuevo archivo", 100, 50, 200, 40);
        panel.add(btnNuevo);

        btnAbrir = crearBoton("Abrir archivo", 100, 100, 200, 40);
        panel.add(btnAbrir);

        btnSalir = crearBoton("Salir", 100, 150, 200, 40);
        panel.add(btnSalir);

        setContentPane(panel);
    }

    public static void main(String[] args) {
        new MenuPrincipal().setVisible(true);
    }
}
