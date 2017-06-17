package Komponenty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Daniel K on 2017-06-15.
 * To obiekt ktory pelni funkcje Planszy na ktorej gracze rozgrywaja rozgrywke, plansza jest rysowana i w calosci nasluchiwana
 * poprzez MouseListener, dodatkowo obiekt ten rysuje pionki znajdujace sie na planszy na podstawie tablicy dwuwymiarowej
 * posiadanej lokalnie lub otrzymanej z zewnatrz (za posrednictwem przypisania tablicy - w tym przypadku tablicy obiektu Pakiet)
 * Klasa ta takze kontroluje poruszanie sie pionkow, ich zbijanie i zmienianie ich typu na inny oraz momentu zakonczenia gry.
 */

public class Plansza extends JComponent implements MouseListener {
    private int[][] pionki;

    private String kolorGracza;
    private boolean przesunietoPionek = false;
    private boolean graTrwa = false;

    private int pozostaleBialePionki;
    private int pozostaleCzarnePionki;

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
    }

    public void sprawdzCzyTrwa(){
        pozostaleBialePionki = 0;
        pozostaleCzarnePionki = 0;

        //policz pozostale pionki
        for(int y = 0;y < 8; y++){
            for(int x = 0;x < 8; x++){
                if(pionki[x][y] == bialyPionek || pionki[x][y] == bialaDamka){
                    pozostaleBialePionki = pozostaleBialePionki + 1;
                }
                if(pionki[x][y] == czarnyPionek || pionki[x][y] == czarnaDamka){
                    pozostaleCzarnePionki = pozostaleCzarnePionki + 1;
                }
            }
        }

        if(pozostaleBialePionki == 0 || pozostaleCzarnePionki == 0){
            System.out.println("END OF GAME");
            graTrwa = false;
        }
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
                //narysuj czarny pionek
                if(pionki[x][y] == czarnyPionek){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarny_pionek");
                    pionek.paintComponent(g);
                }

                //narysuj bialy pionek
                if(pionki[x][y] == bialyPionek){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Biały_pionek");
                    pionek.paintComponent(g);
                }

                //narysuj czarna damke
                if(pionki[x][y] == czarnaDamka){
                    pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarny_damka");
                    pionek.paintComponent(g);
                }

                //narysuj biala damke
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
            System.out.println();
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

        //pobierz wartosci kliknietego pola do zmiennych
        int x = e.getX()/rozmiarPola;
        int y = e.getY()/rozmiarPola;

        //jesli wybrane pole jest wylaczone (biale pole)
        if(pionki[x][y] == wylaczonePole) {
            //System.out.println("POLE WYŁĄCZONE");
        }

        //dla gracza czarnych
        if(kolorGracza.equals("Czarny")) {
            //jesli gracz klinkal na jakis swoj pionek pobierz jego pozycje
            if (pionki[x][y] == czarnyPionek || pionki[x][y] == czarnaDamka) {
                x1 = x;
                y1 = y;
                //System.out.println("Wybrano pionek X: " + (x1) + " Y:" + (y1));
            }
        }

        //dla gracza bialych
        if(kolorGracza.equals("Biały")) {
            //jesli gracz klinkal na jakis swoj pionek pobierz jego pozycje
            if (pionki[x][y] == bialyPionek || pionki[x][y] == bialaDamka) {
                x1 = x;
                y1 = y;
                //System.out.println("Wybrano pionek X: " + (x1) + " Y:" + (y1));
            }
        }

        //jesli gracz kliknal na wolne pole pobierz jego pozycje
        if(pionki[x][y] == wolnePole) {
            x2 = x;
            y2 = y;
            //System.out.println("Wybrano wolne pole X: " + (x2) + " Y:" + (y2));

            //przesun pionek w wybrane pole jesli to mozliwe - czy przesunieto zwroc do zmiennej (true/false)
            przesunietoPionek = przesunPionek();
        }
    }

    //nasluchuj plansze
    public void addMouseListener(){
        addMouseListener(this);
    }

    //przerwij nasluchiwanie planszy
    public void removeMouseListener(){
        removeMouseListener(this);
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

    public boolean przesunPionek(){
        //porusz czarnymi pionkami
        if(pionki[x1][y1] == czarnyPionek) {
            //sprawdz czy poruszamy sie w dozwolone dla tego typu pola i kierunek
            if (y2 - y1 <= 2 && y2 - y1 > 0 && y2 - y1 == Math.abs(x2 - x1)) {
                //przesun pionek o 2 pola i zbij pionek pomiedzy nimi
                if(y2 - y1 == 2) {
                    //oblicz pozycje teoretycznego przeciwnika
                    int x = (x1 + x2) /2;
                    int y = (y1 + y2) /2;

                    //wyswietla pozycje teorytcznego przeciwnika ktorego pokonujemy
                    //System.out.println("WROG X:" + x + "Y:" + y);

                    //sprawdz czy w tej pozycji jest przeciwnik
                    if(mozliweZbicie(x,y)){
                        //jesli jest to zbij pionek
                        zbijPionek(x,y);

                        //przesun pionek
                        pionki[x2][y2] = pionki[x1][y1];
                        pionki[x1][y1] = wolnePole;

                        //stworz damke jesli staniemy w odpowiednim polu
                        pionki[x2][y2] = stworzDamke(czarnyPionek);

                        //odswiez GUI
                        repaint();

                        //zwroc przesunieto pionek
                        return true;
                    }
                }

                if(y2 - y1 == 1) {
                    //przesun pionek o jedno pole - nic nie zbijaj
                    pionki[x2][y2] = pionki[x1][y1];
                    pionki[x1][y1] = wolnePole;

                    //stworz damke jesli stanieto na odpowiednim polu
                    pionki[x2][y2] = stworzDamke(czarnyPionek);

                    //odswiez GUI
                    repaint();

                    //zwroc przesunieto pionek
                    return true;
                }
            }
        }

        //porusz bialymi pionkami
        if (pionki[x1][y1] == bialyPionek) {
            //sprawdz czy poruszamy sie w dozwolone dla tego typu pola i kierunek
            if (y1 - y2 <= 2 && y1 - y2 > 0 && y1 - y2 == Math.abs(x1 - x2)) {
                //przesun pionek o 2 pola i zbij pionek pomiedzy nimi
                if(y1 - y2 == 2){
                    //oblicz pozycje pomiedzy polem poczatkowym a koncowym pionka
                    int x = (x1 + x2) /2;
                    int y = (y1 + y2) /2;

                    //wyswietla pozycje teorytcznego przeciwnika ktorego pokonujemy
                    //System.out.println("WROG X:" + x + "Y:" + y);

                    //sprawdz czy mozliwe jest zbicie pionka w wyliczonej pozycji - czy jest tam wgl wrogi pionek
                    if(mozliweZbicie(x,y)){
                        //jesli jest to zbij pionek
                        zbijPionek(x,y);

                        //przesun pionek
                        pionki[x2][y2] = pionki[x1][y1];
                        pionki[x1][y1] = wolnePole;

                        //zmien pionek w damke jesli stoi odpowiednim polu
                        pionki[x2][y2] = stworzDamke(bialyPionek);

                        //odswiez GUI
                        repaint();

                        //zwroc przesunieto pionek
                        return true;
                    }
                }

                //przesun pionek o jedno pole
                if(y1 - y2 == 1){
                    //przesun pionek
                    pionki[x2][y2] = pionki[x1][y1];
                    pionki[x1][y1] = wolnePole;

                    //stworz damke jesli stajemy na odpowiednim polu
                    pionki[x2][y2] = stworzDamke(bialyPionek);

                    //odswiez GUI
                    repaint();

                    //zwroc przesunieto pionek
                    return true;
                }
            }
        }

        //porusz czarna lub biala damke
        if(pionki[x1][y1] == czarnaDamka || pionki[x1][y1] == bialaDamka) {
            if (y2 - y1 == Math.abs(x2 - x1) || y1 - y2 == Math.abs(x1 - x2)) {

                int wektorX, wektorY;

                if(x1 > x2){
                    wektorX = -1;
                }
                else {
                    wektorX = 1;
                }

                if(y1 > y2){
                    wektorY = -1;
                }
                else {
                    wektorY = 1;
                }

                /**
                int temp[][] = wygenerujPustaPlansze();
                temp[x1][y1] = pionki[x1][y1];
                temp[x2][y2] = pionki[x1][y1];
                 */

                int x3 = x1;
                int y3 = y1;

                for(int i = 0;i < Math.abs(x1 - x2); i++){
                    x3 += wektorX;
                    y3 += wektorY;
                    System.out.println("X: " + x3 + " Y: " + y3);
                    if(mozliweZbicie(x3,y3)){
                        zbijPionek(x3,y3);
                    }
                }

                /**
                for(int y = 0;y < 8;y++){
                    System.out.println();
                    for (int x = 0;x < 8; x++){
                        System.out.print(" " + temp[x][y]);
                    }
                }
                System.out.println();
                 */

                //przesun damke w wybrane pole
                pionki[x2][y2] = pionki[x1][y1];
                pionki[x1][y1] = wolnePole;

                //odswiez GUI
                repaint();

                //zwroc przesunieto pionek
                return true;
            }
        }
        //zwroc nieprzesunieto zadnego pionka
        return false;
    }

    public void setPrzesunietoPionek(boolean przesunietoPionek) {
        this.przesunietoPionek = przesunietoPionek;
    }

    public boolean getPrzesunietoPionek(){
        return przesunietoPionek;
    }

    public void zbijPionek(int x, int y){
        //zbij pionek w polu x,y
        pionki[x][y] = wolnePole;
    }

    public boolean mozliweZbicie(int x, int y){
        if(kolorGracza.equals("Biały")){
            //zwroc prawde jesli pole x,y jest zajete przez czarny pionek, damke
            if(pionki[x][y] == czarnyPionek || pionki[x][y] == czarnaDamka){
                return true;
            }
        }
        if(kolorGracza.equals("Czarny")){
            //zwroc prawde jesli pole x,y jest zajete przez bialy pionek, damke
            if(pionki[x][y] == bialyPionek || pionki[x][y] == bialaDamka){
                return true;
            }
        }
        //niemozliwe jest zadne zbicie pionka
        return false;
    }

    public int stworzDamke(int typPionka){
        //w sytuacji kiedy nie zmieniamy pionka w damke - pionek pozostanie tego samego typu co byl
        int nowytypPionka = typPionka;

        //jesli biały pionek pokona cala plansze przypisz mu nowy typ - zmien w damke
        if(typPionka == bialyPionek && y2 == 0){
            nowytypPionka = bialaDamka;
        }

        //jesli czarny pionek pokona cala plansze przypisz mu nowy typ - zmien w damke
        if(typPionka == czarnyPionek && y2 == 7){
            nowytypPionka = czarnaDamka;
        }
        return nowytypPionka;
    }

    public boolean isGraTrwa() {
        return graTrwa;
    }

    public void setGraTrwa(boolean graTrwa) {
        this.graTrwa = graTrwa;
    }
}
