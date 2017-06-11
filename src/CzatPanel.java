import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by Daniel K on 2017-06-11.
 */
public class CzatPanel extends JPanel {
    //klasa tworzaca panel czatu i jego obsluge
    private JTextArea czatTA;
    private JTextField czatTF;

    public CzatPanel(){
        //inicjalizacja elementow GUI czatu
        czatTA = new JTextArea(20,20);
        czatTF = new JTextField(10);

        setLayout(new BorderLayout());

        //panel przewijany dla czatu
        JScrollPane panelCzatu = new JScrollPane(czatTA);

        //pasek przewijana
        panelCzatu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelCzatu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //zachowanie pola czatu i podazanie za ostatnia linia pojawiajaca sie na czacie
        czatTA.setLineWrap(true);
        czatTA.setEditable(false);
        DefaultCaret caret = (DefaultCaret) czatTA.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panelCzatu.setViewportView(czatTA);

        //dodawanie komponentow czatu
        add(panelCzatu, BorderLayout.CENTER);
        add(czatTF, BorderLayout.SOUTH);
    }

    public JTextArea getCzatTA() {
        return czatTA;
    }

    public JTextField getCzatTF() {
        return czatTF;
    }
}
