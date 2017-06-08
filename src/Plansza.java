import javax.swing.*;
import java.awt.*;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class Plansza extends JPanel{
    private static final int rozmiarPola = (int) (Pionek.getRozmiarPionka() * 1.25);
    private static final int liczbaWierszy = 8;
    private static final int liczbaKolumn = 8;
    private static final Dimension rozmiarPlanszy = new Dimension(rozmiarPola*liczbaKolumn,rozmiarPola*liczbaWierszy);

    public Plansza(){
        setPreferredSize(rozmiarPlanszy);
    }

    @Override
    public void paintComponent(Graphics g) {
        narysujPlansze(g);
    }

    public void narysujPlansze(Graphics g){
        for(int i = 0; i < liczbaWierszy; i++){
            g.setColor(((i & 1) != 0) ? Color.DARK_GRAY : Color.WHITE);
            for (int j = 0; j < liczbaKolumn; j++)
            {
                g.fillRect(j * rozmiarPola, i * rozmiarPola, rozmiarPola, rozmiarPola);
                g.setColor((g.getColor() == Color.DARK_GRAY) ? Color.WHITE : Color.DARK_GRAY);
            }
        }
    }
}
