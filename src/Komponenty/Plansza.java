package Komponenty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Daniel K on 2017-06-15.
 */
public class Plansza extends JComponent implements MouseListener {
    private int[][] pionki;

    private String kolorGracza;

    private int x1, x2, y1, y2;

    private int rozmiarPionka = Pionek.getRozmiarPionka();
    private int rozmiarPola = (int)(rozmiarPionka*1.25);

    private Dimension rozmiarPlanszy = new Dimension(rozmiarPola*8,rozmiarPola*8);

    private static final int wylaczonePole = 0;
    private static final int wolnePole = 1;
    private static final int czarnyPionek = 2;
    private static final int czarnaDamka = 3;
    private static final int bialyPionek = 4;
    private static final int bialaDamka = 5;

    public Plansza(){
        setPreferredSize(rozmiarPlanszy);

        pionki = new int[8][8];

        pionki  = wygenerujPustaPlansze();

        addMouseListener(this);
    }

    public int[][] wygenerujPustaPlansze(){
        int temp[][] = new int[8][8];

        for(int y = 0;y < 8; y++){
            for(int x = 0;x < 8; x++){
                temp[x][y] = 0;
            }
        }
        return temp;
    }

    @Override
    public void paintComponent(Graphics g) {
        narysujSzachownice(g);

        narysujPionki(g);
    }

    public void narysujSzachownice(Graphics g){
        //narysuj szachownice
        for(int y = 0; y < 8; y++){
            g.setColor(((y & 1) != 0) ? Color.DARK_GRAY : Color.WHITE);
            for (int x = 0; x < 8; x++)
            {
                g.fillRect(x * rozmiarPola, y * rozmiarPola, rozmiarPola, rozmiarPola);
                g.setColor((g.getColor() == Color.DARK_GRAY) ? Color.WHITE : Color.DARK_GRAY);
            }
        }
    }

    public void narysujPionki(Graphics g){
        //rysowanie pionkow
        for(int y = 0; y < 8; y++){
            for (int x = 0; x < 8; x ++){
                Pionek pionek;
                if(pionki[x][y] == czarnyPionek){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarny_pionek");
                    pionek.paintComponent(g);
                }

                if(pionki[x][y] == bialyPionek){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Biały_pionek");
                    pionek.paintComponent(g);
                }

                if(pionki[x][y] == czarnaDamka){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarny_damka");
                    pionek.paintComponent(g);
                }

                if(pionki[x][y] == bialaDamka){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Biały_damka");
                    pionek.paintComponent(g);
                }
            }
        }
    }

    public void setPionki(int[][] pionki) {
        this.pionki = pionki;
    }

    public void pokazSzachownice(){
        //pozawala zobaczyc stan tablicy pionki w konsoli
        for(int y = 0;y < 8;y++){
            System.out.println("");
            for (int x = 0;x < 8; x++){
                System.out.print(" " + pionki[x][y]);
            }
        }
        System.out.println();
    }

    public int[][] getPionki() {
        return pionki;
    }

    public String getKolorGracza() {
        return kolorGracza;
    }

    public void setKolorGracza(String kolorGracza) {
        this.kolorGracza = kolorGracza;
    }

    public int[][] nowaGra(){
        //rozrysowanie szachownicy wraz z polozeniami pionkow oraz polami po ktorych mozna sie poruszac
        for(int y = 0; y < 8; y++){
            int temp = (((y & 1) != 0) ? wolnePole : wylaczonePole);
            for (int x = 0; x < 8; x++){
                //inicjalizacja pol ktore wchodza w gre
                if(temp == wolnePole){
                    pionki[x][y] = wolnePole;
                }
                if(y<3 && temp == wolnePole){
                    pionki[x][y] = czarnyPionek;
                }
                if(y>4 && temp == wolnePole){
                    pionki[x][y] = bialyPionek;
                }
                temp = ((temp == wolnePole) ? wylaczonePole : wolnePole);
            }
        }
        return pionki;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        //przykladowe pobieranie pola

        int x = e.getX()/rozmiarPola;
        int y = e.getY()/rozmiarPola;

        if(pionki[x][y] == wylaczonePole) {
            System.out.println("POLE WYŁĄCZONE");
        }

        if(pionki[x][y] > wolnePole){
            x1 = x;
            y1 = y;
            System.out.println("Wybrano pionek X: "+ (x1) + " Y:" + (y1));
        }

        if(pionki[x][y] == wolnePole) {
            x2 = x;
            y2 = y;
            System.out.println("Wybrano wolne pole X: " + (x2) + " Y:" + (y2));
            przesunPionek();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void przesunPionek(){
        //porusz czarnymi pionkami
        if(pionki[x1][y1] == czarnyPionek) {
            if (y2 - y1 == 1 && y2 - y1 == Math.abs(x2 - x1)) {
                pionki[x2][y2] = pionki[x1][y1];
                pionki[x2][y2] = stworzDamke(czarnyPionek);
                pionki[x1][y1] = wolnePole;
            }
        }

        //porusz bialymi pionkami
        if (pionki[x1][y1] == bialyPionek) {
            if (y1 - y2 == 1 && y1 - y2 == Math.abs(x1 - x2)) {
                pionki[x2][y2] = pionki[x1][y1];
                pionki[x2][y2] = stworzDamke(bialyPionek);
                pionki[x1][y1] = wolnePole;
            }
        }

        //porusz czarna lub biala damke
        if(pionki[x1][y1] == czarnaDamka || pionki[x1][y1] == bialaDamka) {
            if (y2 - y1 == Math.abs(x2 - x1) || y1 - y2 == Math.abs(x1 - x2)) {
                pionki[x2][y2] = pionki[x1][y1];
                pionki[x1][y1] = wolnePole;
            }
        }

        repaint();
    }

    public void zbijPionek(){

    }

    public boolean mozliweZbicie(){
        return true;
    }

    public int stworzDamke(int typPionka){
        int nowytypPionka = typPionka;

        if(typPionka == bialyPionek && y2 == 0){
            nowytypPionka = bialaDamka;
        }

        if(typPionka == czarnyPionek && y2 == 7){
            nowytypPionka = czarnaDamka;
        }
        return nowytypPionka;
    }
}
