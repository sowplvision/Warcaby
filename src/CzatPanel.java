import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by Daniel K on 2017-06-11.
 */
public class CzatPanel extends JPanel {
    private JTextArea czatTA;
    private JTextField czatTF;

    public CzatPanel(){
        czatTA = new JTextArea(20,20);
        czatTF = new JTextField(10);

        setLayout(new BorderLayout());

        JScrollPane panelCzatu = new JScrollPane(czatTA);

        panelCzatu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelCzatu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        czatTA.setLineWrap(true);
        czatTA.setEditable(false);
        DefaultCaret caret = (DefaultCaret) czatTA.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panelCzatu.setViewportView(czatTA);

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
