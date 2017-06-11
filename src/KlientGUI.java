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
    private JPanel ustawieniaPolaczenia, panelBoczny, statusPolaczenia;
    private JTextArea czatTA;
    private JLabel polaczonyLabel;
    private Plansza plansza;
    private WynikiPanel wyniki;
    private CzatPanel czat;
    private JTextField adresTF, portTF;
    private JButton polacz, rozlacz;
    private boolean polaczony = false;

    public KlientGUI(){
        setTitle("Klient warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        ustawieniaPolaczenia = new JPanel(new FlowLayout());
        panelBoczny = new JPanel(new BorderLayout());
        statusPolaczenia = new JPanel();
        plansza = new Plansza();
        wyniki = new WynikiPanel();
        czat = new CzatPanel();

        adresTF = new JTextField("localhost",10);
        portTF = new JTextField("2345",4);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        polaczonyLabel = new JLabel("OFFLINE");
        czatTA = czat.getCzatTA();

        rozlacz.setEnabled(false);
        polaczonyLabel.setForeground(Color.red);

        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        rozlacz.addActionListener(obslugaZdarzen);

        ustawieniaPolaczenia.add(new JLabel("Adres serwera:"));
        ustawieniaPolaczenia.add(adresTF);
        ustawieniaPolaczenia.add(new JLabel("Port:"));
        ustawieniaPolaczenia.add(portTF);
        ustawieniaPolaczenia.add(polacz);
        ustawieniaPolaczenia.add(rozlacz);

        statusPolaczenia.add(new JLabel("Status połączenia:"));
        statusPolaczenia.add(polaczonyLabel);

        panelBoczny.add(wyniki, BorderLayout.NORTH);
        panelBoczny.add(statusPolaczenia, BorderLayout.CENTER);
        panelBoczny.add(czat, BorderLayout.SOUTH);

        add(ustawieniaPolaczenia, BorderLayout.NORTH);
        add(plansza, BorderLayout.CENTER);
        add(panelBoczny, BorderLayout.EAST);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {

        new Thread(() -> new KlientGUI()).run();
    }

    private class ObslugaZdarzen implements ActionListener{
        private Obsluga obsluga;

        @Override
        public void actionPerformed(ActionEvent event) {
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
            try {
                if(adresTF.getText().equals("")){
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
                    port = 2345;
                }
                socket = new Socket(adres,port);

                polaczony = true;

                new KontrolaPolaczenia(socket).start();

                polaczonyLabel.setText("ONLINE");
                polaczonyLabel.setForeground(Color.GREEN);
                rozlacz.setEnabled(true);
                adresTF.setEnabled(false);
                portTF.setEnabled(false);
                polacz.setEnabled(false);
                repaint();

                czatTA.append("Witamy na serwerze warcabów.\n");

                while (polaczony){
                    //TODO
                }
            } catch (IOException e) {
                czatTA.append("Brak połączenia z serwerem.\n");
            } finally {
                try {
                    if(socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {}
            }
        }

        public void rozlacz(){
            try{
                if(socket != null) {
                    socket.close();
                }
                polaczony = false;
                polaczonyLabel.setText("OFFLINE");
                polaczonyLabel.setForeground(Color.RED);
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
        Socket socket;
        public KontrolaPolaczenia(Socket socket){
            setDaemon(true);
            this.socket = socket;
        }

        @Override
        public void run() {
            while (polaczony){
                try {
                    if(socket.getInputStream().read() == -1){
                        czatTA.append("Połączenie zostało zerwane.\n");
                        polaczony = false;
                        polaczonyLabel.setText("OFFLINE");
                        polaczonyLabel.setForeground(Color.RED);
                        rozlacz.setEnabled(false);
                        adresTF.setEnabled(true);
                        portTF.setEnabled(true);
                        polacz.setEnabled(true);
                        repaint();

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
