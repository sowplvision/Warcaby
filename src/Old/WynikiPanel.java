package Old;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Daniel K on 2017-06-07.
 */
public class WynikiPanel extends JPanel{
    //klasa pokazujaca wyniki rozgrywki oraz obslugujaca ich zmiany
    private JTextField gracz1Wynik, gracz2Wynik;

    public WynikiPanel(){
        //tworzenie GUI dla panelu wynikow
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //domyslny rozmiar panelu
        setPreferredSize(new Dimension(180,50));

        //inicjalizacja pol zawierajacych wynik
        gracz1Wynik = new JTextField("0",2);
        gracz2Wynik = new JTextField("0",2);

        //pola nie moga byc edytowana
        gracz1Wynik.setEditable(false);
        gracz2Wynik.setEditable(false);

        //umiejscowienie tekstu w polu
        gracz1Wynik.setHorizontalAlignment(JTextField.CENTER);
        gracz2Wynik.setHorizontalAlignment(JTextField.CENTER);

        //dodawanie komponentow
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Gracz 1:"),c);

        c.gridx = 1;
        c.gridy = 0;
        add(gracz1Wynik,c);

        c.gridx = 2;
        c.gridy = 0;
        add(new JLabel("Gracz 2:"),c);

        c.gridx = 3;
        c.gridy = 0;
        add(gracz2Wynik,c);
    }
}
