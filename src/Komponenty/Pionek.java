package Komponenty;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Pionek extends JComponent {
    private static final int rozmiarPionka = 48;
    private int x,y;
    private String typ;
    private Color kolorBazowy, kolorDodatkowy;

    public Pionek(int x, int y, String typ){
        this.x = x;
        this.y = y;
        this.typ = typ;
    }

    public void paintComponent(Graphics g){
        if(typ.contains("pionek")) {
            if (typ.equals("Czarny_pionek")) {
                kolorBazowy = Color.black;
                kolorDodatkowy = Color.white;
            }
            if(typ.equals("Biały_pionek")) {
                kolorBazowy = Color.white;
                kolorDodatkowy = Color.black;
            }

            g.setColor(kolorBazowy);
            g.fillOval(x, y, rozmiarPionka, rozmiarPionka);
            g.setColor(kolorDodatkowy);
            g.fillOval(x + 6, y + 6, rozmiarPionka - 12, rozmiarPionka - 12);
            g.setColor(kolorBazowy);
            g.fillOval(x + 8, y + 8, rozmiarPionka - 16, rozmiarPionka - 16);
        }
        if(typ.contains("damka")){
            if (typ.equals("Czarna_damka")) {
                kolorBazowy = Color.black;
                kolorDodatkowy = Color.orange;
            }
            if(typ.equals("Biała_damka")) {
                kolorBazowy = Color.white;
                kolorDodatkowy = Color.orange;
            }

            g.setColor(kolorBazowy);
            g.fillOval(x, y, rozmiarPionka, rozmiarPionka);
            g.setColor(kolorDodatkowy);
            g.fillOval(x + 6, y + 6, rozmiarPionka - 12, rozmiarPionka - 12);
            g.setColor(kolorBazowy);
            g.fillOval(x + 8, y + 8, rozmiarPionka - 16, rozmiarPionka - 16);
        }
    }

    public static int getRozmiarPionka() {
        return rozmiarPionka;
    }
}
