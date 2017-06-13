package Komponenty;

import java.io.Serializable;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Plansza implements Serializable {
    private int[][] pionki;

    public Plansza(){

    }

    public int[][] getPionki() {
        return pionki;
    }

    public void setPionki(int[][] pionki) {
        this.pionki = pionki;
    }
}
