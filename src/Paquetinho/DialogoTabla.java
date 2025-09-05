package Paquetinho;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DialogoTabla extends JDialog {

    private final JSpinner spFilas = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JSpinner spCols = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
    private boolean ok = false;

    public DialogoTabla(Frame owner) {
        super(owner, "Insertar tabla", true);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Filas:"));
        top.add(spFilas);
        top.add(new JLabel("Columnas:"));
        top.add(spCols);
        add(top, BorderLayout.NORTH);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelar = new JButton("Cancelar");
        JButton aceptar = new JButton("Insertar");
        cancelar.addActionListener(e -> {
            ok = false;
            dispose();
        });
        aceptar.addActionListener(e -> {
            ok = true;
            dispose();
        });
        sur.add(cancelar);
        sur.add(aceptar);
        add(sur, BorderLayout.SOUTH);

        setSize(320, 150);
        setLocationRelativeTo(owner);
    }

    public boolean isOk() {
        return ok;
    }

    public int getFilas() {
        return (Integer) spFilas.getValue();
    }

    public int getCols() {
        return (Integer) spCols.getValue();
    }
}
