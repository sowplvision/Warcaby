package Komponenty;

import java.io.Serializable;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Pakiet implements Serializable {
    private int[][] pionki;
    private String komenda;

    public Pakiet(){

    }

    public Pakiet(int[][] pionki){
        this.pionki = pionki;
    }

    public Pakiet(String komenda){
        this.komenda = komenda;
    }

    public Pakiet(int[][] pionki, String komenda){
        this.pionki = pionki;
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
}
