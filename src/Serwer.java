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
    private Vector<Client> klienci = new Vector<Client>();

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
                server = new Server();
                server.start();
                uruchomiony = true;

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
        private String kolorGracza = "TEST1";

        public Client(Socket socket){
            this.socket = socket;
            polaczony = true;

            synchronized (klienci){
                klienci.add(this);
                uzytkownicy.setText("" + klienci.size());
            }
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                klienci.trimToSize();

                while (uruchomiony && polaczony) {
                    try {

                        pakiet = (Pakiet) ois.readObject();

                        System.out.println(pakiet.getKomenda());

                        oos.flush();

                        if (pakiet.getKomenda().equals(LOGIN)) {
                            if(klienci.size() == 2){
                                pakiet.setKomenda(GAME_START);
                                pakiet.setPionki(warcaby.getPionki());
                            }

                            if(klienci.size() > 2){
                                pakiet.setKomenda(LOGOUT);
                            }
                            oos.writeObject(pakiet);
                        }

                        if (pakiet.getKomenda().equals(LOGOUT)) {
                            synchronized (klienci){
                                polaczony = false;
                                log.append("Rozłączono z użytkownikiem.\n");
                                klienci.remove(this);
                                uzytkownicy.setText("" + klienci.size());
                            }
                            oos.writeObject(pakiet);
                        }

                        if (pakiet.getKomenda().equals(GAME_START)) {
                            for(Client klient: klienci) {
                                pakiet.setPionki(warcaby.nowaGra());
                                klient.oos.writeObject(pakiet);
                            }
                        }

                        if (pakiet.getKomenda().equals(END_OF_GAME)) {

                        }

                        if (pakiet.getKomenda().equals(CHECKER_MOVE)) {

                        }

                        if (pakiet.getKomenda().equals(WAITING_FOR_MOVE)) {

                        }
                    } catch (IOException e){
                    } catch (ClassNotFoundException e){}
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
