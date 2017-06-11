import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * Created by Daniel K on 2017-06-07.
 */
public class LogPanel extends JScrollPane{
    //klasa tworzaca panel logu serwera
    private JTextArea logTA;

    public LogPanel(){
        //inicjalizacja pola tekstowego logu
        logTA = new JTextArea(20,20);

        //zachowanie paskow przewijania
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //podazanie za ostatnia linia
        logTA.setLineWrap(true);
        logTA.setEditable(false);
        DefaultCaret caret = (DefaultCaret) logTA.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        setViewportView(logTA);
    }

    public JTextArea getLogTA() {
        return logTA;
    }
}
