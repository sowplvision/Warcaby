import Komponenty.Pakiet;
import Komponenty.Plansza;
import Komponenty.Protokol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private Plansza warcaby;
    private Pakiet pakiet = new Pakiet();

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
        warcaby = new Plansza();

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

    private class ObslugaZdarzen implements ActionListener, Protokol{
        private ObslugaKlienta obsluga;

        @Override
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals("Połącz")){
                //rozpocznij laczenie z serwerem
                obsluga = new ObslugaKlienta();
                obsluga.start();

                pakiet.setKomenda(LOGIN);

                //zmien stan GUI
                adres.setEnabled(false);
                port.setEnabled(false);
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);

                statusPolaczenia.setForeground(Color.ORANGE);
                statusPolaczenia.setText("CONNECTING");
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

    private class ObslugaKlienta extends Thread implements Protokol{
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
                rozlacz.setEnabled(true);

                statusPolaczenia.setForeground(Color.GREEN);
                statusPolaczenia.setText("ONLINE");
                repaint();

                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                //wyslij pakiet
                oos.writeObject(pakiet);
                oos.flush();
               
                pakiet = null;
                while (polaczony){
                    try {
                        pakiet = (Pakiet) ois.readObject();

                        //chwilowe nasluchiwanie komend od serwera
                        System.out.println(pakiet.getKomenda());

                        //polecenie logout
                        if (pakiet.getKomenda().equals(LOGOUT)) {
                            polaczony = false;
                        }

                        //pelen serwer
                        if (pakiet.getKomenda().equals(FULL_SERVER)) {
                            polaczony = false;
                        }

                        //polecenie koniec gry
                        if (pakiet.getKomenda().equals(END_OF_GAME)) {
                            //pobierz nowa plansze
                            warcaby.setPionki(pakiet.getPionki());

                            //pobierz wyniki
                            wynikGracza1.setText("" + pakiet.getWynikGracza1());
                            wynikGracza2.setText("" + pakiet.getWynikGracza2());
                            repaint();
                        }

                        //polecenie rozpoeczecia nowej gry
                        if (pakiet.getKomenda().equals(GAME_START)) {
                            //pobierz plansze
                            warcaby.setPionki(pakiet.getPionki());

                            //pobierz kolor gracza
                            warcaby.setKolorGracza(pakiet.getKolorGracza());

                            //pobierz wyniki gry
                            wynikGracza1.setText("" + pakiet.getWynikGracza1());
                            wynikGracza2.setText("" + pakiet.getWynikGracza2());
                            repaint();

                            pakiet.setKomenda(WAITING_FOR_MOVE);
                            oos.writeObject(pakiet);
                            oos.flush();
                        }

                        //polecenie poruszania sie dla czarnych pionkow
                        if (pakiet.getKomenda().equals(MOVE_BLACK)) {

                            //upewnienie sie o kolor gracza
                            if(warcaby.getKolorGracza().equals("Czarny")) {

                                System.out.println("YOUR TURN");

                                //nasluchiwanie poruszania
                                warcaby.addMouseListener();

                                //oczekiwanie na ruch
                                while (!warcaby.getPrzesunietoPionek()) {
                                    try {
                                        sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }

                                //po wykonaniu ruchu nie nasluchuj wiecej planszy
                                warcaby.removeMouseListener();

                                //przygotuj pakiet
                                pakiet.setKomenda(WAITING_FOR_MOVE);
                                pakiet.setPionki(warcaby.getPionki());
                                pakiet.setKolejGracza("Biały");

                                //wyslij zmieniona plansza
                                oos.writeObject(pakiet);
                                oos.flush();

                                //pozwol kolejnemu graczowi na ruch
                                warcaby.setPrzesunietoPionek(false);
                            }
                        }

                        //polecenie poruszania bialymi pionkami
                        if (pakiet.getKomenda().equals(MOVE_WHITE)) {
                            //upewni sie koloru gracza
                            if(warcaby.getKolorGracza().equals("Biały")) {

                                System.out.println("YOUR TURN");

                                //nasluchuj plansze
                                warcaby.addMouseListener();

                                //czekaj na ruch
                                while (!warcaby.getPrzesunietoPionek()) {
                                    try {
                                        sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }

                                //przestan nasluchiwac po wykonaniu ruchu
                                warcaby.removeMouseListener();


                                //przygotuj pakiet
                                pakiet.setKomenda(WAITING_FOR_MOVE);
                                pakiet.setPionki(warcaby.getPionki());
                                pakiet.setKolejGracza("Czarny");

                                //wyslij pakiet z nowa plansza
                                oos.writeObject(pakiet);
                                oos.flush();

                                //pozwol na ruch nowemu graczowi
                                warcaby.setPrzesunietoPionek(false);
                            }
                        }

                        if (pakiet.getKomenda().equals(MOVEMENT)) {
                            //aktualizuj rozmieszczenie pionkow
                            warcaby.setPionki(pakiet.getPionki());
                            repaint();

                            //powroc do rozgrywki
                            pakiet.setKomenda(WAITING_FOR_MOVE);
                            oos.writeObject(pakiet);
                        }

                    } catch (IOException e){
                    } catch (ClassNotFoundException e){
                    }
                }
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            } finally {
                try {
                    kill();
                } catch (Exception e) {
                }
            }
        }

        public void kill(){
            //obsluga przycisku rozlacz
            try{
                if(oos != null){
                    pakiet = new Pakiet(LOGOUT);
                    oos.writeObject(pakiet);
                    oos.close();
                }

                if(ois != null) {
                    ois.close();
                }

                //rozlacz jesli socket nie byl pusty
                if(socket != null) {
                    socket.close();
                }

                warcaby.setPionki(warcaby.wygenerujPustaPlansze());

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
            } catch (IOException e) {
            }
        }
    }
}
