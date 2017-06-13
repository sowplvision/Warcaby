import Komponenty.Plansza;

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

    private Plansza plansza = new Plansza();
    private Warcaby warcaby = new Warcaby();

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

    private class Server extends Thread{
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

    private class Client extends Thread{
        private Socket socket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        public Client(Socket socket){
            this.socket = socket;

            synchronized (klienci){
                klienci.add(this);
                uzytkownicy.setText("" + klienci.size());
            }
        }

        @Override
        public void run() {
            try {
                warcaby = new Warcaby();
                plansza.setPionki(warcaby.nowaGra());

                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                while (uruchomiony) {
                    try {
                        oos.flush();

                        oos.writeObject(plansza);
                        oos.flush();

                        plansza = (Plansza) ois.readObject();

                    } catch (IOException e){
                    } catch (ClassNotFoundException e){}
                }
                ois.close();
                oos.close();
                socket.close();
            } catch (IOException e) {
            }
            synchronized (klienci){
                klienci.remove(this);
                uzytkownicy.setText("" + klienci.size());
            }
        }
    }

    private class Warcaby{
        private int[][] pionki;
        private static final int wylaczonePole = 0;
        private static final int wolnePole = 1;
        private static final int czarnyPionek = 2;
        private static final int czarnaDamka = 3;
        private static final int bialyPionek = 4;
        private static final int bialaDamka = 5;

        public Warcaby(){
            pionki = new int[8][8];
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

        public int[][] nowaGra(){
            //rozrysowanie szachownicy wraz z polozeniami pionkow oraz polami po ktorych mozna sie poruszac
            for(int y = 0; y < 8; y++){
                int temp = (((y & 1) != 0) ? wolnePole : wylaczonePole);
                for (int x = 0; x < 8; x++){
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

        public int[][] getPionki() {
            return pionki;
        }

        public void setPionki(int[][] pionki) {
            this.pionki = pionki;
        }
    }
}