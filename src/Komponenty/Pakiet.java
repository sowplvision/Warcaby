package Komponenty;

import java.io.Serializable;

/**
 * Created by Daniel K on 2017-06-13.
 * Obiekt ktory słuzy do komunikacji pomiedzy klientem a serwerem, przesylane sa nim wyniki, polecenie dla klienta
 * lub serwera, kolor gracza otrzymany od serwera oraz ktory gracz treaz ma swoja kolej a takze zapis planszy w postaci
 * tablicy dwuwymiarowej typu int.
 */

public class Pakiet implements Serializable {
    private int[][] pionki;
    private String komenda;
    private int wynikGracza1;
    private int wynikGracza2;
    private String kolorGracza;
    private String kolejGracza;

    public Pakiet(){

    }

    public Pakiet(String komenda){
        this.komenda = komenda;
    }

    public int[][] getPionki() {
        return pionki;
    }

    public void setPionki(int[][] pionki) {
        this.pionki = pionki;
    }

    public String getKomenda() {
        return komenda;
    }

    public void setKomenda(String komenda) {
        this.komenda = komenda;
    }

    public int getWynikGracza1() {
        return wynikGracza1;
    }

    public int getWynikGracza2() {
        return wynikGracza2;
    }

    public void setWynikGracza1(int wynikGracza1) {
        this.wynikGracza1 = wynikGracza1;
    }

    public void setWynikGracza2(int wynikGracza2) {
        this.wynikGracza2 = wynikGracza2;
    }

    public String getKolorGracza() {
        return kolorGracza;
    }

    public void setKolorGracza(String kolorGracza) {
        this.kolorGracza = kolorGracza;
    }

    public void setKolejGracza(String kolejGracza) {
        this.kolejGracza = kolejGracza;
    }

    public String getKolejGracza() {
        return kolejGracza;
    }
}
