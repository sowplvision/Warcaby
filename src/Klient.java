import Komponenty.Pionek;
import Komponenty.Plansza;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Klient extends JFrame{
    //elementy GUI
    private JTextField adres, port, wynikGracza1, wynikGracza2;
    private JButton polacz, rozlacz;
    private JLabel statusPolaczenia;

    private Warcaby warcaby;
    private Plansza plansza;

    private boolean polaczony = false;

    public Klient(){
        //tworzenie GUI
        setTitle("Klient warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        //inicjalizacja elementow GUI
        adres = new JTextField("localhost",10);
        port = new JTextField("2345", 4);
        wynikGracza1 = new JTextField("0",1);
        wynikGracza2 = new JTextField("0",1);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        statusPolaczenia = new JLabel("OFFLINE");

        //ustawienia elementow GUI
        rozlacz.setEnabled(false);
        statusPolaczenia.setForeground(Color.red);
        wynikGracza1.setEditable(false);
        wynikGracza1.setHorizontalAlignment(JTextField.CENTER);
        wynikGracza2.setEditable(false);
        wynikGracza2.setHorizontalAlignment(JTextField.CENTER);

        //obsluga przyciskow polacz, rozlacz
        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        rozlacz.addActionListener(obslugaZdarzen);

        //inicjalizacja paneli
        JPanel ustawieniaPolaczenia = new JPanel();
        JPanel panelStatusuPolaczenia = new JPanel();
        JPanel panelWynikow = new JPanel();
        warcaby = new Warcaby();

        //dodawanie komponentow do paneli
        ustawieniaPolaczenia.add(new JLabel("Adres serwera:"));
        ustawieniaPolaczenia.add(adres);
        ustawieniaPolaczenia.add(new JLabel("Port:"));
        ustawieniaPolaczenia.add(port);
        ustawieniaPolaczenia.add(polacz);
        ustawieniaPolaczenia.add(rozlacz);

        panelStatusuPolaczenia.add(new JLabel("Status połaczenia:"));
        panelStatusuPolaczenia.add(statusPolaczenia);

        panelWynikow.add(new JLabel("Gracz 1:"));
        panelWynikow.add(wynikGracza1);
        panelWynikow.add(new JLabel("Gracz 2:"));
        panelWynikow.add(wynikGracza2);

        //dodawanie paneli do okna
        add(ustawieniaPolaczenia, BorderLayout.NORTH);
        add(panelStatusuPolaczenia, BorderLayout.SOUTH);
        add(panelWynikow, BorderLayout.EAST);
        add(warcaby, BorderLayout.CENTER);

        //dopasowanie rozmiaru okna do zawartosci i pokazanie okna
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        //wyrazenie lambda uruchamiajace GUI klienta w nowym watku
        new Thread(() -> new Klient()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        private ObslugaKlienta obsluga;

        @Override
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals("Połącz")){
                //rozpocznij laczenie z serwerem
                obsluga = new ObslugaKlienta();
                obsluga.start();

                //zmien stan GUI
                adres.setEnabled(false);
                port.setEnabled(false);
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);
                repaint();
            }
            if(event.getActionCommand().equals("Rozłącz")){
                //przerwij polaczenie
                obsluga.kill();

                //zmien stan GUI
                adres.setEnabled(true);
                port.setEnabled(true);
                polacz.setEnabled(true);
                rozlacz.setEnabled(false);
                repaint();
            }
        }
    }

    private class ObslugaKlienta extends Thread{
        private Socket socket;
        private int nrPortu;
        private String adresSerwera;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        @Override
        public void run() {
            //po nacisnieciu polacz pobierz dane z pol adres, port i sprawdz czy spelniaja wymogi - jesli nie uzyj domyslnych
            try {
                if (adres.getText().equals("")) {
                    //domyslnie serwerem jest localhost
                    adresSerwera = "localhost";
                } else {
                    adresSerwera = adres.getText();
                }
                try {
                    if (Integer.parseInt(port.getText()) >= 0 && Integer.parseInt(port.getText()) <= 65535) {
                        nrPortu = Integer.parseInt(port.getText());
                    } else {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    //domyslny port
                    nrPortu = 2345;
                }
                //sprobuj nawiazac polaczenie z podanym adresem na podanym porcie
                socket = new Socket(adresSerwera, nrPortu);

                polaczony = true;

                statusPolaczenia.setForeground(Color.GREEN);
                statusPolaczenia.setText("ONLINE");

                while (polaczony){
                    ois = new ObjectInputStream(socket.getInputStream());
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    oos.flush();

                    plansza = (Plansza) ois.readObject();
                    warcaby.setPionki(plansza.getPionki());
                    repaint();

                    oos.writeObject(plansza);

                    ois.close();
                    oos.close();
                }
                socket.close();
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            } catch (ClassNotFoundException e){
            }
        }

        public void kill(){
            //obsluga przycisku rozlacz
            try{
                //rozlacz jesli socket nie byl pusty
                if(socket != null) {
                    socket.close();
                }

                //zmien status polaczenia
                polaczony = false;
                statusPolaczenia.setForeground(Color.RED);
                statusPolaczenia.setText("OFFLINE");

                //uruchom ponownie mozliwosci polaczenia z serwerem
                rozlacz.setEnabled(false);
                adres.setEnabled(true);
                port.setEnabled(true);
                polacz.setEnabled(true);
                repaint();
            } catch (IOException e) {}
        }
    }

    private class Warcaby extends JComponent implements MouseListener{
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

        public Warcaby(){
            setPreferredSize(rozmiarPlanszy);

            pionki = new int[8][8];

            for(int y = 0;y < 8; y++){
                for(int x = 0;x < 8; x++){
                    pionki[x][y] = 0;
                }
            }
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

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

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
}
