import java.io.IOException;
import java.net.Socket;

/**
 * Created by Daniel K on 2017-06-05.
 */
public class Obsluga extends Thread{
    private Socket socket;

    public Obsluga(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            //TODO
            socket.close();
        } catch (IOException e){}
    }
}
