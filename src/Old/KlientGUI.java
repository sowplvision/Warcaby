package Old;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class KlientGUI extends JFrame{
    //klasa tworzaca GUI klienta - elementy GUI
    private JPanel ustawieniaPolaczenia, panelBoczny, statusPolaczenia;
    private JTextArea czatTA;
    private JLabel polaczonyLabel;
    private PlanszaPanel planszaGUI;
    private WynikiPanel wyniki;
    private CzatPanel czat;
    private JTextField adresTF, portTF, czatTF;
    private JButton polacz, rozlacz;

    //status polaczenia
    private boolean polaczony = false;

    public KlientGUI(){
        //tworzenie GUI
        setTitle("Klient warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        //inicjalizacja paneli
        ustawieniaPolaczenia = new JPanel(new FlowLayout());
        panelBoczny = new JPanel(new BorderLayout());
        statusPolaczenia = new JPanel();
        planszaGUI = new PlanszaPanel();
        wyniki = new WynikiPanel();
        czat = new CzatPanel();

        //inicjalizacja przyciskow i pol
        adresTF = new JTextField("localhost",10);
        portTF = new JTextField("2345",4);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        polaczonyLabel = new JLabel("OFFLINE");
        czatTA = czat.getCzatTA();
        czatTF = czat.getCzatTF();

        //domyslnie klient jest rozlaczony
        rozlacz.setEnabled(false);
        polaczonyLabel.setForeground(Color.red);

        //obsluga przyciskow polacz-rozlacz
        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        rozlacz.addActionListener(obslugaZdarzen);

        //panel gorny GUI (adres, port itd)
        ustawieniaPolaczenia.add(new JLabel("Adres serwera:"));
        ustawieniaPolaczenia.add(adresTF);
        ustawieniaPolaczenia.add(new JLabel("Port:"));
        ustawieniaPolaczenia.add(portTF);
        ustawieniaPolaczenia.add(polacz);
        ustawieniaPolaczenia.add(rozlacz);

        //panel pokazujacy jedynie informacje o statusie polaczenia
        statusPolaczenia.add(new JLabel("Status połączenia:"));
        statusPolaczenia.add(polaczonyLabel);

        //panel boczny (wyniki, status polaczenia, czat)
        panelBoczny.add(wyniki, BorderLayout.NORTH);
        panelBoczny.add(statusPolaczenia, BorderLayout.CENTER);
        panelBoczny.add(czat, BorderLayout.SOUTH);

        //dodaj kompnenty do okna
        add(ustawieniaPolaczenia, BorderLayout.NORTH);
        add(planszaGUI, BorderLayout.CENTER);
        add(panelBoczny, BorderLayout.EAST);

        //dopasuj rozmiar okna do zawartosci i pokaz okno
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        //wyrazenie lambda - nowy watek GUI klienta
        new Thread(() -> new KlientGUI()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        private Obsluga obsluga;

        @Override
        public void actionPerformed(ActionEvent event) {
            //obsluga przyciskow polacz-rozlacz - nowy watek po nacisnieciu
            if(event.getActionCommand().equals("Połącz")){
                obsluga = new Obsluga();
                obsluga.start();
            }
            if(event.getActionCommand().equals("Rozłącz")){
                obsluga.rozlacz();
            }
        }
    }

    private class Obsluga extends Thread{
        private Socket socket;
        private int port;
        private String adres;

        @Override
        public void run() {
            //po nacisnieciu polacz pobierz dane z pol adres, port i sprawdz czy spelniaja wymogi - jesli nie uzyj domyslnych
            try {
                if(adresTF.getText().equals("")){
                    //domyslnie serwerem jest localhost
                    adres = "localhost";
                }
                else {
                    adres = adresTF.getText();
                }
                try {
                    if(Integer.parseInt(portTF.getText())>=0 && Integer.parseInt(portTF.getText())<=65535){
                        port = Integer.parseInt(portTF.getText());
                    }
                    else{
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e){
                    //domyslny port
                    port = 2345;
                }
                //sprobuj nawiazac polaczenie z podanym adresem na podanym porcie
                socket = new Socket(adres,port);

                //jesli sie udalo zacznij kontrolowac polaczenie
                polaczony = true;

                new KontrolaPolaczenia(socket).start();

                //zmien status polaczenia
                polaczonyLabel.setText("ONLINE");
                polaczonyLabel.setForeground(Color.GREEN);

                //zablokuj elementy GUI dla nowego polaczenia
                rozlacz.setEnabled(true);
                adresTF.setEnabled(false);
                portTF.setEnabled(false);
                polacz.setEnabled(false);
                repaint();

                czatTA.append("Witamy na serwerze warcabów.\n");

                while (polaczony){
                    //TODO co klient ma robic kiedy jest polaczony

                    /**
                    try{
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                        Old.Rozgrywka rozgrywka = (Old.Rozgrywka) objectInputStream.readObject();
                        planszaGUI.setPionki(rozgrywka.getPionki());
                        repaint();

                    } catch (IOException e){
                    } catch (ClassNotFoundException e){}
                     */

                    //tymczasowe do usuniecia po wykonaniu - aby nie przeciazac pamieci
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                //jezeli nie udalo sie nawiazac polaczenia
                czatTA.append("Brak połączenia z serwerem.\n");
            } finally {
                try {
                    //zamknije nieudane polaczenie
                    if(socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {}
            }
        }

        public void rozlacz(){
            //obsluga przycisku zatrzymaj
            try{
                //rozlacz jesli socket nie byl pusty
                if(socket != null) {
                    socket.close();
                }

                //zmien status polaczenia
                polaczony = false;
                polaczonyLabel.setText("OFFLINE");
                polaczonyLabel.setForeground(Color.RED);

                //uruchom ponownie mozliwosci polaczenia z serwerem
                rozlacz.setEnabled(false);
                adresTF.setEnabled(true);
                portTF.setEnabled(true);
                polacz.setEnabled(true);
                repaint();

                czatTA.append("Rozłączono.\n");

            } catch (IOException e) {}
        }
    }

    private class KontrolaPolaczenia extends Thread{
        //klasa kontrolujaca czy polaczenie nadal jest nawiazane - watek demon
        Socket socket;
        public KontrolaPolaczenia(Socket socket){
            setDaemon(true);
            this.socket = socket;
        }

        @Override
        public void run() {
            //jezeli jest polaczenie to kontroluj je
            while (polaczony){
                try {
                    if(socket.getInputStream().read() == -1){
                        //jesli polaczenie zostalo zerwane - np serwer zostal zatrzyman
                        czatTA.append("Połączenie zostało zerwane.\n");

                        //zmien status na rozlaczony
                        polaczony = false;
                        polaczonyLabel.setText("OFFLINE");
                        polaczonyLabel.setForeground(Color.RED);

                        //przywroc mozliwosci ponownego polaczenia
                        rozlacz.setEnabled(false);
                        adresTF.setEnabled(true);
                        portTF.setEnabled(true);
                        polacz.setEnabled(true);
                        repaint();

                        //zamknij polaczenie
                        if(socket != null) {
                            socket.close();
                        }
                    }
                    sleep(1000);
                } catch (IOException e) {
                } catch (InterruptedException e) {}
            }
        }
    }
}
