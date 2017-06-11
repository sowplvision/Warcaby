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
    private JPanel ustawieniaSerwera, panelBoczny;
    private LogPanel log;
    private WynikiPanel wyniki;
    private Plansza plansza;
    private JTextField portTF;
    private JTextArea logTA;
    private JButton polacz, zatrzymaj;

    private Vector<Obsluga> klienci;
    private boolean uruchomiony = false;

    public SerwerGUI(){
        setTitle("Serwer warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        ustawieniaSerwera = new JPanel(new FlowLayout());
        panelBoczny = new JPanel(new BorderLayout());
        plansza = new Plansza();
        log = new LogPanel();
        wyniki = new WynikiPanel();

        portTF = new JTextField("2345",4);
        polacz = new JButton("Uruchom serwer");
        zatrzymaj = new JButton("Zatrzymaj serwer");
        logTA = log.getLogTA();

        zatrzymaj.setEnabled(false);

        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        zatrzymaj.addActionListener(obslugaZdarzen);

        ustawieniaSerwera.add(new Label("Port:"));
        ustawieniaSerwera.add(portTF);
        ustawieniaSerwera.add(polacz);
        ustawieniaSerwera.add(zatrzymaj);

        panelBoczny.add(wyniki, BorderLayout.NORTH);
        panelBoczny.add(log, BorderLayout.CENTER);

        add(ustawieniaSerwera,BorderLayout.NORTH);
        add(plansza, BorderLayout.CENTER);
        add(panelBoczny,BorderLayout.EAST);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        new Thread(() -> new SerwerGUI()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        private Serwer srw;
        private KontrolaPolaczenia kontrolaPolaczenia;

        @Override
        public void actionPerformed(ActionEvent event) {
            if(event.getActionCommand().equals("Uruchom serwer")) {
                klienci = new Vector<Obsluga>();
                srw = new Serwer();
                srw.start();
                kontrolaPolaczenia = new KontrolaPolaczenia();
                kontrolaPolaczenia.start();
                uruchomiony = true;
                zatrzymaj.setEnabled(true);
                polacz.setEnabled(false);
                portTF.setEnabled(false);
                repaint();
            }
            if(event.getActionCommand().equals("Zatrzymaj serwer")){
                srw.zatrzymaj();
                kontrolaPolaczenia.interrupt();
                uruchomiony = false;
                zatrzymaj.setEnabled(false);
                polacz.setEnabled(true);
                portTF.setEnabled(true);
                repaint();
            }
        }
    }

    private class Serwer extends Thread{
        private ServerSocket serwer;
        private int port;

        @Override
        public void run() {
            logTA.setText("");
            try {
                try {
                    if(Integer.parseInt(portTF.getText())>=0 && Integer.parseInt(portTF.getText())<=65535){
                        port = Integer.parseInt(portTF.getText());
                    }
                    else{
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e){
                    logTA.append("Błędna wartość w polu port.\nUżywam portu domyślnego.\n");
                    port = 2345;
                }
                serwer = new ServerSocket(port);

                logTA.append("Serwer uruchomiony na porcie: " + port + "\n");

                while (uruchomiony) {
                    Socket socket = serwer.accept();

                    socket.setSoTimeout(1000);

                    logTA.append("Nowe połączenie:" + socket.getInetAddress() + "\n");

                    new Obsluga(socket).start();
                }
            } catch (IOException e) {
            } finally {
                try{
                    if(serwer != null) serwer.close();
                } catch (IOException e) {}
            }
        }
        public void zatrzymaj(){
            try{
                serwer.close();
                logTA.append("Serwer zatrzymany.\n");

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
        private Socket socket;

        public Obsluga(Socket socket){
            this.socket = socket;

            synchronized (klienci){
                if(klienci.size()<2) {
                    klienci.add(this);
                }
                else {
                    logTA.append("Do serwera próbował dołączyć kolejny gracz.");
                    try {
                        socket.getInputStream().close();
                    } catch (IOException e) {}
                }
            }
        }

        @Override
        public void run() {
            //TODO obsluga klienta kiedy polaczony
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
                    if (klienci != null) {
                        for (Obsluga klient : klienci) {
                            try {
                                if (klient.socket.getInputStream().read() == -1) {
                                    logTA.append("Użytkownik rozłączył się.\n");
                                    klienci.remove(klient);
                                }
                            } catch (IOException e) {}
                        }
                    }
                    logTA.append("Połączonych klientów: " + klienci.size() + "\n");
                    sleep(10000);
                } catch (InterruptedException e){}
                catch (ConcurrentModificationException e){}
            }
        }
    }
}