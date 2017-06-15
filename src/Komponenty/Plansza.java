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

        for(int y = 0;y < 8; y++){
            for(int x = 0;x < 8; x++){
                pionki[x][y] = 0;
            }
        }

        addMouseListener(this);
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
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarna_damka");
                    pionek.paintComponent(g);
                }

                if(pionki[x][y] == bialaDamka){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Biała_damka");
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
    }

    public int[][] getPionki() {
        return pionki;
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
        System.out.println("X: "+ (e.getX()/rozmiarPola) + " Y:" + (e.getY()/rozmiarPola));
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
}
