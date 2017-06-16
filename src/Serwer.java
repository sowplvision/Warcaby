import Komponenty.Pakiet;
import Komponenty.Plansza;
import Komponenty.Protokol;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by Daniel K on 2017-06-13.
 */
public class Serwer extends JFrame{
    //elementy GUI
    private JTextArea log;
    private JTextField port, uzytkownicy;
    private JButton uruchom, zatrzymaj;

    //lista polaczonych klientow
    private Vector<Client> klienci;

    //status serwera
    private boolean uruchomiony = false;

    private Pakiet pakiet = new Pakiet();
    private Plansza warcaby = new Plansza();

    public Serwer(){
        //tworzenie GUI
        setTitle("Serwer warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        //inicjalizacja komponentow GUI serwera
        log = new JTextArea(15,20);
        port = new JTextField("2345",4);
        uruchom = new JButton("Uruchom serwer");
        zatrzymaj = new JButton("Zatrzymaj serwer");
        uzytkownicy = new JTextField("0", 2);

        //ustawienia elementow GUI
        zatrzymaj.setEnabled(false);
        uzytkownicy.setEditable(false);
        uzytkownicy.setHorizontalAlignment(JTextField.CENTER);

        //obsluga przyciskow uruchom, zatrzymaj
        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        uruchom.addActionListener(obslugaZdarzen);
        zatrzymaj.addActionListener(obslugaZdarzen);

        //inicjalizacja paneli serwera
        JPanel ustawieniaSerwera = new JPanel();
        JPanel panelStatusuSerwera = new JPanel();
        JScrollPane panelLogu = new JScrollPane(log);

        //ustawienia panelu logu
        panelLogu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLogu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //podazanie za ostatnia linia logu
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setEditable(false);
        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panelLogu.setViewportView(log);

        //dodawanie komponentow do paneli
        ustawieniaSerwera.add(new JLabel("Port:"));
        ustawieniaSerwera.add(port);
        ustawieniaSerwera.add(uruchom);
        ustawieniaSerwera.add(zatrzymaj);

        panelStatusuSerwera.add(new JLabel("Liczba użytkowników:"));
        panelStatusuSerwera.add(uzytkownicy);

        //dodawanie paneli do okna
        add(ustawieniaSerwera,BorderLayout.NORTH);
        add(panelStatusuSerwera, BorderLayout.CENTER);
        add(panelLogu,BorderLayout.SOUTH);

        //dopasowanie rozmiaru do zawartosci i pokazanie okna
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        //wyrazenie lambda uruchamiajace GUI serwera jako nowy watek
        new Thread(() -> new Serwer()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        private Server server;

        @Override
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals("Uruchom serwer")) {
                //uruchom serwer
                uruchomiony = true;
                warcaby.nowaGra();
                server = new Server();
                server.start();
                //wyczysc liste
                klienci = new Vector<Client>();
                uzytkownicy.setText("" + klienci.size());

                //zmien stan GUI
                port.setEnabled(false);
                uruchom.setEnabled(false);
                zatrzymaj.setEnabled(true);
                repaint();
            }
            if(event.getActionCommand().equals("Zatrzymaj serwer")) {
                //zatrzymaj serwer
                server.kill();
                uruchomiony = false;

                //zmien stan GUI
                port.setEnabled(true);
                uruchom.setEnabled(true);
                zatrzymaj.setEnabled(false);
                repaint();
            }
        }
    }

    private class Server extends Thread implements Protokol{
        private ServerSocket server;
        private int nrPortu;

        @Override
        public void run() {
            log.setText("");
            try {
                try {
                    //sprawdz czy wartosc pola port jest liczba z zakresu dozwolonego dla portow
                    if(Integer.parseInt(port.getText())>=0 && Integer.parseInt(port.getText())<=65535){
                        nrPortu = Integer.parseInt(port.getText());
                    }
                    //jezeli nie jest liczba lub przekracza zakres wyrzuc wyjatek
                    else{
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e){
                    //poinformuj klienta o blednym porcie oraz uzyj portu 2345 jako domyslnego
                    log.append("Błędna wartość w polu port.\nUżywam portu domyślnego.\n");
                    nrPortu = 2345;
                }
                //inicjalizacja serwera
                server = new ServerSocket(nrPortu);

                log.append("Serwer uruchomiony na porcie: " + nrPortu + "\n");

                while (uruchomiony) {
                    //jezeli serwer uruchomiony akceptuj nowe polaczenia
                    Socket socket = server.accept();

                    log.append("Nowe połączenie:" + socket.getInetAddress().getHostAddress() + "\n");

                    //nowy watek polaczenia dla nowego klienta
                    new Client(socket).start();
                }
            } catch (IOException e) {
            } finally {
                try{
                    //zamykanie serwera
                    if(server != null) server.close();
                } catch (IOException e) {
                }
            }
        }

        public void kill(){
            try{
                //zatrzymaj przychodzace polaczenia
                if(server != null) server.close();
                log.append("Serwer zatrzymany.\n");

                //rozlacz z kazdym klientem z listy
                for (Client klient: klienci){
                    try{
                        klient.socket.close();
                    }
                    catch (IOException e){
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    private class Client extends Thread implements Protokol{
        private Socket socket;
        private boolean polaczony = false;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        public Client(Socket socket){
            this.socket = socket;
            polaczony = true;
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                oos.flush();

                pakiet = null;
                while (uruchomiony && polaczony) {
                    try {

                        //odbierz pakiet od klienta
                        pakiet = (Pakiet) ois.readObject();

                        //chwilowe nasluchowanie komendy otrzymanej od klienta
                        System.out.println(pakiet.getKomenda());


                        //otrzymano polecenie login
                        if (pakiet.getKomenda().equals(LOGIN)) {
                            synchronized (klienci){
                                klienci.add(this);
                                uzytkownicy.setText("" + klienci.size());
                            }

                            //jesli jest 2 graczy
                            if(klienci.size() == 2){
                                //gra rozpoczyna się
                                pakiet.setKomenda(GAME_START);

                                pakiet.setWynikGracza1(0);
                                pakiet.setWynikGracza2(0);

                                pakiet.setGraTrwa(true);

                                pakiet.setKolejGracza("Biały");

                                //wygeneruj nowa plansze z pionkami
                                pakiet.setPionki(warcaby.getPionki());

                                //przydziel kolory graczom
                                for (Client klient : klienci) {
                                    if(klient.getId() < klienci.lastElement().getId()){
                                        pakiet.setKolorGracza("Biały");
                                    }
                                    else {
                                        pakiet.setKolorGracza("Czarny");
                                    }
                                    //wyslij gotowy pakiet
                                    klient.oos.writeObject(pakiet);
                                    klient.oos.flush();
                                }
                                log.append("Nowa gra rozpoczyna się.\n");
                            }

                            //kazdy nadmiarowy gracz
                            if(klienci.size() > 2){
                                pakiet.setKomenda(FULL_SERVER);
                                oos.writeObject(pakiet);
                                oos.flush();
                            }               
                        }


                        //polecenie logout
                        if (pakiet.getKomenda().equals(LOGOUT)) {
                            //usun klienta z listy polaczonych
                            synchronized (klienci){
                                int temp = (int)(getId());

                                klienci.remove(this);

                                log.append("Rozłączono z użytkownikiem.\n");

                                uzytkownicy.setText("" + klienci.size());
                                polaczony = false;

                                //klient opuscil gre w trakcie
                                if(klienci.size() < 2 && klienci.size() > 0) {
                                    pakiet.setKomenda(END_OF_GAME);
                                    //ustal id gracza i przypisz mu przegrana
                                    if (temp > klienci.firstElement().getId()) {
                                        pakiet.setWynikGracza1(1);
                                        pakiet.setWynikGracza2(0);

                                        log.append("Zwycięża gracz 1.\n");
                                    } else {
                                        pakiet.setWynikGracza1(0);
                                        pakiet.setWynikGracza2(1);

                                        log.append("Zwycięża gracz 2.\n");
                                    }

                                    //wygeneruj pusta plansze
                                    pakiet.setPionki(warcaby.wygenerujPustaPlansze());
                                    //wyslij pozostalemu klientowi wyniki i nowa plansze
                                    klienci.firstElement().oos.writeObject(pakiet);
                                    klienci.firstElement().oos.flush();
                                }
                            }
                            oos.writeObject(pakiet);
                            oos.flush();
                        }


                        //polecenie zakonczenia gry
                        if (pakiet.getKomenda().equals(END_OF_GAME)) {

                        }

                        /**
                        //polecenie przesuniecia pionka
                        if (pakiet.getKomenda().equals(CHECKER_MOVE)) {
                            System.out.println("MOVEMENT FROM " + pakiet.getKolorGracza());

                            warcaby.setPionki(pakiet.getPionki());
                            warcaby.pokazSzachownice();

                            for (Client klient : klienci) {
                                klient.oos.writeObject(pakiet);
                                klient.oos.flush();
                            }
                        }
                         */

                        if (pakiet.getKomenda().equals(MOVEMENT)) {
                            /**
                             if(!pakiet.getKolejGracza().equals("Czarny")){
                             pakiet.setKolejGracza("Biały");
                             }
                             else {
                             pakiet.setKolejGracza("Czarny");
                             }

                             if(pakiet.getKolejGracza().equals(pakiet.getKolorGracza())) {
                             oos.writeObject(pakiet);
                             oos.flush();
                             }
                             */

                            warcaby.setPionki(pakiet.getPionki());
                            System.out.println(pakiet.getKolejGracza());
                            pakiet.setPionki(warcaby.getPionki());

                            oos.writeObject(pakiet);
                        }


                        //polecenie oczekiwanie na gracza
                        if (pakiet.getKomenda().equals(WAITING_FOR_MOVE)) {
                            if(pakiet.getKolejGracza().equals("Czarny")){
                                pakiet.setKomenda(MOVE_BLACK);
                            }
                            if(pakiet.getKolejGracza().equals("Biały")){
                                pakiet.setKomenda(MOVE_WHITE);
                            }
                            oos.writeObject(pakiet);
                            oos.flush();
                        }

                        if (pakiet.getKomenda().equals(MOVE_BLACK)) {

                        }

                        if (pakiet.getKomenda().equals(MOVE_WHITE)) {

                        }
                    } catch (IOException e){
                    } catch (ClassNotFoundException e){
                    }
                }

            } catch (IOException e) {
            } finally {
                try {
                    ois.close();
                    oos.close();
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
