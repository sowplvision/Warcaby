import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class Serwer {
    private static ServerSocket serwer;
    private static int port = 2345;

    public static void main(String[] args) {

        try{
            serwer = new ServerSocket(port);

            while (true){
                Socket socket = serwer.accept();
                //TODO
                new Obsluga(socket).start();
            }
        } catch (IOException e){}
    }
}