package Komponenty;

/**
 * Created by Daniel K on 2017-06-15.
 */
public interface Protokol {
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String END_OF_GAME = "ENDGAME";
    public static final String GAME_START = "GAMESTART";
    public static final String MOVE_BLACK = "BLACKMOVES";
    public static final String MOVE_WHITE = "WHITEMOVES";
    public static final String MOVEMENT = "PLAYERMOVED";
    public static final String WAITING_FOR_MOVE = "WAITING";
    public static final String FULL_SERVER = "FULLSERVER";
}
