package Old;

import java.io.Serializable;

/**
 * Created by Daniel K on 2017-06-12.
 */
public class Rozgrywka implements Serializable{
    //tablica zawierajaca polozenie pionkow oraz pol na ktorych mozna stawiac pionki
    private int[][] pionki;

    public int[][] getPionki() {
        return pionki;
    }

    public void setPionki(int[][] pionki) {
        this.pionki = pionki;
    }
}
