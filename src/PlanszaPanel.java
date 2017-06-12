import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class PlanszaPanel extends JComponent implements MouseListener{
    //klasa tworzaca plansze - rozmiar pola zalezny jest od rozmiaru pionka
    private static final int rozmiarPola = (int) (Pionek.getRozmiarPionka() * 1.25);
    //liczba wierszy i kolumn szachownicy
    private static final int liczbaWierszy = 8;
    private static final int liczbaKolumn = 8;
    //rozmiar calej planszy wyliczany na podstawie rozmiarow pol i liczby wierszy i kolumn
    private static final Dimension rozmiarPlanszy = new Dimension(rozmiarPola*liczbaKolumn,rozmiarPola*liczbaWierszy);

    public PlanszaPanel(){
        //ustaw rozmiar panelu na rozmiar planszy
        setPreferredSize(rozmiarPlanszy);
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        //narysuj komponenty planszy
        narysujPlansze(g);
    }

    public void narysujPlansze(Graphics g){
        //rysowanie szachownicy
        for(int i = 0; i < liczbaWierszy; i++){
            g.setColor(((i & 1) != 0) ? Color.DARK_GRAY : Color.WHITE);
            for (int j = 0; j < liczbaKolumn; j++)
            {
                g.fillRect(j * rozmiarPola, i * rozmiarPola, rozmiarPola, rozmiarPola);
                g.setColor((g.getColor() == Color.DARK_GRAY) ? Color.WHITE : Color.DARK_GRAY);
            }
        }

        //rysowanie pionkow
        for(int y = 0; y < liczbaWierszy; y++){
            for (int x = 0; x < liczbaKolumn; x ++){
                //Pionek pionek = new Pionek(6+rozmiarPola*x,6+rozmiarPola*y, "Czarny_pionek");
                //pionek.paintComponent(g);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() / rozmiarPola;
        int y = e.getY() / rozmiarPola;

        //wstepne pokazywanie ktore pole zostalo wybrane
        System.out.println("X:" + e.getX() + " Y:" + e.getY() + " Pole:" + x + " " + y);
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
