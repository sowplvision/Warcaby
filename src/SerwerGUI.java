import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.Vector;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class SerwerGUI extends JFrame{
    //klasa tworzaca GUI serwera - elementy GUI
    private JPanel ustawieniaSerwera, panelBoczny, liczbaUzytkownikow;
    private JLabel liczbaGraczy;
    private LogPanel log;
    private WynikiPanel wyniki;
    private PlanszaPanel plansza;
    private JTextField portTF;
    private JTextArea logTA;
    private JButton polacz, zatrzymaj;

    //lista klientow w postaci obiektow
    private Vector<Obsluga> klienci;
    //status serwera
    private boolean uruchomiony = false;

    public SerwerGUI(){
        //tworzenie GUI
        setTitle("Serwer warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        //inicjalizacja paneli
        ustawieniaSerwera = new JPanel(new FlowLayout());
        panelBoczny = new JPanel(new BorderLayout());
        plansza = new PlanszaPanel();
        log = new LogPanel();
        wyniki = new WynikiPanel();
        liczbaUzytkownikow = new JPanel();

        //inicjalizacja przyciskow i pol
        portTF = new JTextField("2345",4);
        polacz = new JButton("Uruchom serwer");
        zatrzymaj = new JButton("Zatrzymaj serwer");
        logTA = log.getLogTA();
        liczbaGraczy = new JLabel("0");

        //domyslnie serwer jest wylaczony wiec nie mozna go zatrzymywac ponownie
        zatrzymaj.setEnabled(false);

        //oblsuga przyciskow uruchom-zatrzymaj
        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        zatrzymaj.addActionListener(obslugaZdarzen);

        //panel gorny serwerowego GUI (port itd)
        ustawieniaSerwera.add(new Label("Port:"));
        ustawieniaSerwera.add(portTF);
        ustawieniaSerwera.add(polacz);
        ustawieniaSerwera.add(zatrzymaj);

        //liczba uzytkownikow
        liczbaUzytkownikow.add(new JLabel("Liczba użytkowników: "));
        liczbaUzytkownikow.add(liczbaGraczy);

        //panel boczny (wyniki,log)
        panelBoczny.add(wyniki, BorderLayout.NORTH);
        panelBoczny.add(liczbaUzytkownikow, BorderLayout.CENTER);
        panelBoczny.add(log, BorderLayout.SOUTH);

        //dodawanie komponentow do okienka
        add(ustawieniaSerwera,BorderLayout.NORTH);
        add(plansza, BorderLayout.CENTER);
        add(panelBoczny,BorderLayout.EAST);

        //dopasowanie rozmiaru okna do zawartosci oraz jego ujawnienie
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        //wyrazenie lambda - nowy watek dla GUI
        new Thread(() -> new SerwerGUI()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        //klasa nasluchujaca i obslugujaca przyciski uruchom - zatrzymaj
        private Serwer srw;
        private KontrolaPolaczenia kontrolaPolaczenia;

        @Override
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals("Uruchom serwer")) {
                //po nacisnieciu przycisku uruchom utworz nowa liste klientow
                klienci = new Vector<Obsluga>();

                //uruchom serwer
                srw = new Serwer();
                srw.start();
                uruchomiony = true;

                //kontroluj klientow czy dalej sa polaczeni
                kontrolaPolaczenia = new KontrolaPolaczenia();
                kontrolaPolaczenia.start();

                //wylacz elementy GUI odpowiedzialne za uruchamianie serwera
                zatrzymaj.setEnabled(true);
                polacz.setEnabled(false);
                portTF.setEnabled(false);
                repaint();
            }
            if(event.getActionCommand().equals("Zatrzymaj serwer")){
                //po nacisnieciu przycisku zatrzymaj serwer
                srw.zatrzymaj();
                uruchomiony = false;

                //zatrzymaj watek demoniczny
                kontrolaPolaczenia.interrupt();

                //wlacz ponownie GUI odpowiedzialne za uruchamianie serwera
                zatrzymaj.setEnabled(false);
                polacz.setEnabled(true);
                portTF.setEnabled(true);
                repaint();
            }
        }
    }

    private class Serwer extends Thread{
        //klasa tworzaca socket serwera oraz watki do obslugi nowych klientow - obsluguje klientow (wszystkich naraz)
        private ServerSocket serwer;
        private int port;

        @Override
        public void run() {
            //uruchamiam serwer - wyczysc log dla nowej sesji serwera
            logTA.setText("");
            try {
                try {
                    //sprawdz czy wartosc pola port jest liczba z zakresu dozwolonego dla portow
                    if(Integer.parseInt(portTF.getText())>=0 && Integer.parseInt(portTF.getText())<=65535){
                        port = Integer.parseInt(portTF.getText());
                    }
                    //jezeli nie jest liczba lub przekracza zakres wyrzuc wyjatek
                    else{
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e){
                    //poinformuj klienta o blednym porcie oraz uzyj portu 2345 jako domyslnego
                    logTA.append("Błędna wartość w polu port.\nUżywam portu domyślnego.\n");
                    port = 2345;
                }
                //inicjalizacja serwera
                serwer = new ServerSocket(port);

                logTA.append("Serwer uruchomiony na porcie: " + port + "\n");

                while (uruchomiony) {
                    //jezeli serwer uruchomiony akceptuj nowe polaczenia
                    Socket socket = serwer.accept();

                    logTA.append("Nowe połączenie:" + socket.getInetAddress() + "\n");

                    //nowy watek polaczenia dla nowego klienta
                    new Obsluga(socket).start();
                }
            } catch (IOException e) {
            } finally {
                try{
                    //alternatywne zamkniecie serwera w przypadku wystapienia wyjatku w trakcie akceptowania polaczen
                    if(serwer != null) serwer.close();
                } catch (IOException e) {}
            }
        }

        public void zatrzymaj(){
            //metoda do obslugi przycisku zatrzymaj
            try{
                //zatrzymaj przychodzace polaczenia
                serwer.close();
                logTA.append("Serwer zatrzymany.\n");

                //rozlacz z kazdym klientem z listy
                for (Obsluga klient: klienci){
                    try{
                        klient.socket.close();
                    }
                    catch (IOException e){}
                }
            } catch (IOException e) {}
        }
    }

    private class Obsluga extends Thread{
        //klasa obslugujaca pojedynczego klienta
        private Socket socket;

        public Obsluga(Socket socket){
            this.socket = socket;

            synchronized (klienci){
                //sprawdz czy polaczonych jest juz 2 klientow jeśli tak rozlacz z kolejnym klientem
                if(klienci.size()<2) {
                    //dodaj klienta do listy - obiekt
                    klienci.add(this);
                    liczbaGraczy.setText("" + klienci.size());
                }
                else {
                    logTA.append("Do serwera próbował dołączyć kolejny gracz.\n");
                    try {
                        //wywola to blad u klienta nr 3 i przerwie jego polaczenie
                        socket.close();
                    } catch (IOException e) {}
                }
            }
        }

        @Override
        public void run() {
            //TODO obsluga klienta kiedy polaczony tutaj ma odbywac sie calosc gry w warcaby i komunikacji miedzy klientami
        }
    }

    private class KontrolaPolaczenia extends Thread{

        public KontrolaPolaczenia(){
            setDaemon(true);
        }

        @Override
        public void run() {
            while(uruchomiony){
                try {
                    for (Obsluga klient : klienci) {
                        try {
                            if (klient.socket.getInputStream().read() == -1) {
                                logTA.append("Użytkownik rozłączył się.\n");
                                klienci.remove(klient);
                                liczbaGraczy.setText("" + klienci.size());
                            }
                        } catch (IOException e) {}
                    }
                    sleep(1000);
                } catch (InterruptedException e){}
                catch (ConcurrentModificationException e){}
            }
        }
    }
}