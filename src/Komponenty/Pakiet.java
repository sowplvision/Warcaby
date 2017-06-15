package Komponenty;

import java.io.Serializable;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Pakiet implements Serializable {
    private int[][] pionki;

    public Pakiet(){

    }

    public Pakiet(int[][] pionki){
        this.pionki = pionki;
    }

    public int[][] getPionki() {
        return pionki;
    }

    public void setPionki(int[][] pionki) {
        this.pionki = pionki;
    }
}
