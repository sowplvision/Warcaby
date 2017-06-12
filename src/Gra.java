/**
 * Created by Daniel K on 2017-06-12.
 */
public class Gra {
    private int[][]pionki;
    private static final int wylaczonePole = 0;
    private static final int wolnePole = 1;
    private static final int czarnyPionek = 2;
    private static final int czarnaDamka = 3;
    private static final int bialyPionek = 4;
    private static final int bialaDamka = 5;

    public Gra(){
        pionki = new int[PlanszaPanel.getLiczbaKolumn()][PlanszaPanel.getLiczbaWierszy()];

        //wypelnia plansze polami wylaczonymi z gry - nie wyrysuje to pionkow i bedzie wykorzystane potem
        for(int y = 0;y < PlanszaPanel.getLiczbaWierszy(); y++){
            for (int x = 0;x < PlanszaPanel.getLiczbaKolumn(); x++){
                pionki[x][y] = wylaczonePole;
            }
        }
    }

    public int[][] nowaGra(){
        //rozrysowanie szachownicy wraz z polozeniami pionkow oraz polami po ktorych mozna sie poruszac
        for(int y = 0;y < PlanszaPanel.getLiczbaWierszy();y++){
            int temp = (((y & 1) != 0) ? wolnePole : wylaczonePole);
            for (int x = 0;x < PlanszaPanel.getLiczbaKolumn(); x++){
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

    public void pokazSzachownice(){
        //pozawala zobaczyc stan tablicy pionki
        for(int y = 0;y < PlanszaPanel.getLiczbaWierszy();y++){
            System.out.println("");
            for (int x = 0;x < PlanszaPanel.getLiczbaKolumn(); x++){
                System.out.print(" " + pionki[x][y]);
            }
        }
    }
}
