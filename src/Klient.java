import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class Klient extends JFrame{
    private static String adres = "localhost";
    private static int port = 2345;

    public Klient(){
        //TODO
    }

    public static void main(String[] args) {

        try {
            Socket socket = new Socket(adres,port);
            //TODO
            socket.close();
        } catch (IOException e) {}
    }
}
