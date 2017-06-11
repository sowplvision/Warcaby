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
    private JPanel ustawieniaPolaczenia;
    private Plansza plansza;
    private JTextField adresTF, portTF;
    private JButton polacz, rozlacz;
    private boolean polaczony = false;

    public KlientGUI(){
        setTitle("Klient warcabów");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ustawieniaPolaczenia = new JPanel(new FlowLayout());
        plansza = new Plansza();

        adresTF = new JTextField("localhost",10);
        portTF = new JTextField("2345",4);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");

        rozlacz.setEnabled(false);

        ObslugaZdarzen obslugaZdarzen = new ObslugaZdarzen();

        polacz.addActionListener(obslugaZdarzen);
        rozlacz.addActionListener(obslugaZdarzen);

        ustawieniaPolaczenia.add(new JLabel("Adres serwera:"));
        ustawieniaPolaczenia.add(adresTF);
        ustawieniaPolaczenia.add(new JLabel("Port:"));
        ustawieniaPolaczenia.add(portTF);
        ustawieniaPolaczenia.add(polacz);
        ustawieniaPolaczenia.add(rozlacz);

        add(ustawieniaPolaczenia, BorderLayout.NORTH);
        add(plansza, BorderLayout.CENTER);

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
                rozlacz.setEnabled(true);
                adresTF.setEnabled(false);
                portTF.setEnabled(false);
                polacz.setEnabled(false);
                repaint();
            }
            if(event.getActionCommand().equals("Rozłącz")){
                obsluga.rozlacz();
                rozlacz.setEnabled(false);
                adresTF.setEnabled(true);
                portTF.setEnabled(true);
                polacz.setEnabled(true);
                repaint();
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

                while (polaczony){
                    //TODO
                }
            } catch (IOException e) {
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
            } catch (IOException e) {}
        }
    }
}
