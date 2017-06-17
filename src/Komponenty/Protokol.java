package Komponenty;

/**
 * Created by Daniel K on 2017-06-15.
 * To interfejst zawierajacy wszystkie polecenia dla serwera oraz klienta ktore wysyłane są poprzez obiekt Pakiet,
 * słuzu do kontrolowania serwera i klienta oraz wplywania na akcje ktore powinni podejmowac w danej sytuacji.
 */

public interface Protokol {
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String END_OF_GAME = "ENDGAME";
    public static final String GAME_START = "GAMESTART";
    public static final String CHECKER_MOVE = "MOVE";
    public static final String WAITING_FOR_MOVE = "WAITING";
    public static final String FULL_SERVER = "FULLSERVER";
}
