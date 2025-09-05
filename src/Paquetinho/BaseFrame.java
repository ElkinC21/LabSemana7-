package Paquetinho;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public abstract class BaseFrame extends JFrame {

    public BaseFrame(String titulo, int width, int height) {
        super(titulo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);
    }

    abstract void initComponents();

    protected JLabel crearLabel(String texto, int x, int y, int ancho, int alto, int estilo, float tamaño) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(estilo, tamaño));
        label.setBounds(x, y, ancho, alto);
        return label;
    }

    protected JTextField crearTextField(int x, int y, int ancho, int alto) {
        JTextField textField = new JTextField();
        textField.setBounds(x, y, ancho, alto);
        return textField;
    }

    protected JButton crearBoton(String texto, int x, int y, int ancho, int alto) {
        JButton boton = new JButton(texto);
        boton.setBounds(x, y, ancho, alto);
        return boton;
    }
}
