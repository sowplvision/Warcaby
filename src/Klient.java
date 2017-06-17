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
 * Klasa tworzaca interfejs GUI klienta oraz wszelkie z nim zwiazane zdarzenia, komunikuje się ona z Serwerem i
 * wymienia z nim Pakiety wysylajac w nich odpowiednie tresci oraz polecenia dla innych graczy
 */

public class Klient extends JFrame{
    //elementy GUI
    private JTextField adres, port, wynikGracza1, wynikGracza2;
    private JButton polacz, rozlacz;
    private JLabel statusPolaczenia, kolorGracza;

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
        kolorGracza = new JLabel("");

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
        JPanel panelDolny = new JPanel();
        JPanel panelBoczny = new JPanel(new BorderLayout());
        panelBoczny.setPreferredSize(new Dimension(200,400));
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

        panelDolny.add(new JLabel("Kolor gracza: "));
        panelDolny.add(kolorGracza);

        panelBoczny.add(panelWynikow, BorderLayout.NORTH);
        panelBoczny.add(panelDolny, BorderLayout.SOUTH);

        //dodawanie paneli do okna
        add(ustawieniaPolaczenia, BorderLayout.NORTH);
        add(panelStatusuPolaczenia, BorderLayout.SOUTH);
        add(panelBoczny, BorderLayout.EAST);
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
                            wynikGracza1.setText("" + (Integer.parseInt(wynikGracza1.getText()) + pakiet.getWynikGracza1()));
                            wynikGracza2.setText("" + (Integer.parseInt(wynikGracza2.getText()) + pakiet.getWynikGracza2()));
                            repaint();
                        }

                        //polecenie rozpoeczecia nowej gry
                        if (pakiet.getKomenda().equals(GAME_START)) {
                            //rozpocznij gre
                            warcaby.setGraTrwa(true);

                            //pobierz plansze
                            warcaby.setPionki(pakiet.getPionki());

                            //pobierz kolor gracza
                            warcaby.setKolorGracza(pakiet.getKolorGracza());
                            kolorGracza.setText(pakiet.getKolorGracza());

                            //pobierz wyniki gry
                            wynikGracza1.setText("" + pakiet.getWynikGracza1());
                            wynikGracza2.setText("" + pakiet.getWynikGracza2());
                            repaint();

                            pakiet.setKomenda(WAITING_FOR_MOVE);
                            oos.writeObject(pakiet);
                            oos.flush();
                        }

                        if(pakiet.getKomenda().equals(CHECKER_MOVE)){
                            //upewnienie sie o kolor gracza
                            if(warcaby.getKolorGracza().equals(pakiet.getKolejGracza())) {

                                warcaby.setPionki(pakiet.getPionki());
                                repaint();

                                System.out.println("YOUR TURN");

                                //nasluchiwanie poruszania
                                warcaby.addMouseListener();

                                //oczekiwanie na ruch
                                while (!warcaby.getPrzesunietoPionek() && polaczony && warcaby.isGraTrwa()) {
                                    try {
                                        sleep(100);
                                    } catch (InterruptedException e) {
                                    }
                                }

                                //po wykonaniu ruchu nie nasluchuj wiecej planszy
                                warcaby.removeMouseListener();

                                warcaby.sprawdzCzyTrwa();

                                if(warcaby.isGraTrwa()) {
                                    //przygotuj pakiet
                                    pakiet.setKomenda(WAITING_FOR_MOVE);
                                    pakiet.setPionki(warcaby.getPionki());

                                    if (pakiet.getKolejGracza().equals("Czarny")) {
                                        pakiet.setKolejGracza("Biały");
                                    } else {
                                        pakiet.setKolejGracza("Czarny");
                                    }
                                }
                                else {
                                    pakiet.setKomenda(END_OF_GAME);
                                }

                                //wyslij zmieniona plansza
                                oos.reset();
                                oos.writeObject(pakiet);
                                oos.flush();

                                //pozwol kolejnemu graczowi na ruch
                                warcaby.setPrzesunietoPionek(false);
                            }
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
