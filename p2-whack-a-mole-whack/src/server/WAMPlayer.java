package server;

import common.WAMProtocol;
import common.WhackException;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A class that manages the requests and responses to a single client.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */

/**
 * A class handling connections to clients.
 */
public class WAMPlayer implements WAMProtocol, Closeable {

    /**
     * Send requests to clients
     */
    private PrintStream printStream;
    /**
     * The Socket that will be used to communicate to clients
     */
    private Socket socket;

    /**
     * Scanner to be used to read Character responses.
     */
    private Scanner scanner;

    /**
     * The player number
     */
    private int playerNumber;
    /**
     * The total score of the player
     */
    private int tot_score;

    /**
     * The list of scores for all players
     */
    private ArrayList<Integer> scoreList;


    /**
     * Constructor of WhackPLayer
     * @param s
     * @throws WhackException
     */
    public WAMPlayer(Socket s) throws WhackException {
        this.socket = s;
        try{
            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
        }catch(IOException io){
            throw new WhackException(io);
        }

    }

    /**
     * Accept connection from client
     * @param rows
     * @param cols
     * @param players
     * @param player_number
     */
    public void connect(ArrayList<Integer> scoreList, int rows, int cols, int players, int player_number){
        //WELCOME message includes #rows, #columns, #players, player#
        this.playerNumber = player_number;
        this.scoreList = scoreList;
        printStream.println(WELCOME + " " + rows + " " + cols + " " + players + " " + player_number);
    }//from WAMProtocol.java

    /**
     * Sends the mole-up and its locations' message to the client.
     * @param col
     */
    public void mole_up(int col) {
        printStream.println(MOLE_UP + " " + col);
    }

    /**
     * Sends the mole-down and its locations' message to the client.
     * @param col
     */
    public void mole_down(int col) {
        printStream.println(MOLE_DOWN + " " + col);
        moleDownScore();
    }

    /**
     *when the whack happens, calculate the score accordingly.
     */
    public void whackScore() {
        tot_score += 2;
        scoreList.set(playerNumber, tot_score);
        sendScore();
    }

    /**
     * Sends scores to client.
     */
    public void sendScore() {
        String s = "";
        for (int i = 0; i < scoreList.size(); i++) {
            s += scoreList.get(i) + " ";
        }
        printStream.println(SCORE + " " + s);
    }
    /**
     *when the whack does not happen, calculate the score accordingly.
     */
    public void moleDownScore() {
        tot_score = tot_score - 1;
        scoreList.set(playerNumber, tot_score);
        sendScore();
    }

    /**
     * Checks whether mole is whacked.
     * @param col
     * @return true, or false
     */
    public boolean isMadeWhack(int col) {
        try {
            String request = this.scanner.hasNext() ? this.scanner.next() : "";
            if (!request.equals(WHACK)) return false;
            String arguments = this.scanner.nextLine();                 //no such elements found error place
            String[] fields = arguments.trim().split(" ");
            int mole_num = Integer.parseInt(fields[0]);
            int player_num = Integer.parseInt(fields[1]);
            if (request.equals(WHACK) && mole_num == col) {
                whackScore();
                printStream.println(MOLE_DOWN + " " + col);
                return true;
            }
        }
        catch(NoSuchElementException e){
            System.err.println(e);
        }
        return false;
    }

    /**
     * Close the connection once the game ends.
     */
    /**
     * Inform the client that they've lost the game.
     */
    public void lost(){printStream.println(GAME_LOST);}

    /**
     * Inform the client that they've won
     */
    public void won(){printStream.println(GAME_WON);}

    /**
     * Inform the client that the game ended in a draw.
     */
    public void draw(){printStream.println(GAME_TIED); }

    /**
     * Inform the client that an error has occured
     * @param error the message to be printed
     */
    public void error(String error){printStream.println(ERROR + " " + error);}

    /**
     * Close the connection
     */
    @Override
    public void close(){
        try{
            socket.close();
        }catch(IOException wac){
            System.out.println(wac);
        }
    }

}
