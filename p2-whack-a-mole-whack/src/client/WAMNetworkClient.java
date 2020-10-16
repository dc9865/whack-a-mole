package client;

import common.WhackException;
import javafx.application.Application;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static common.WAMProtocol.*;

/**
 * The client side network interface to a Whack-A-Mole game server.
 * Each of the two players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */
public class WAMNetworkClient
     {
    /** Turn on if standard output debug messages are desired. */
    private static final boolean DEBUG = false;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( WAMNetworkClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /** client socket to communicate with server */
    private Socket clientSocket;
    /** used to read requests from the server */
    private Scanner networkIn;
    /** Used to write responses to the server. */
    private PrintStream networkOut;
    /** the model which keeps track of the game */
    private Model model;
    /** sentinel loop used to control the main loop */
    private boolean go;
    private int rows, cols, players, player_number;
    private ArrayList<Integer> scoreList = new ArrayList<>();

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() { return this.go; }

    /**
     * Multithread-safe mutator
     */
    public synchronized void stop() {
        this.go = false;
    }

    /**
     * get rows
     * @return rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * get columns
     * @return rows
     */
    public int getCols() {
        return cols;
    }

    /**
     * get player numbers
     * @return rows
     */
    public int getPlayer_number() {
        return player_number;
    }

    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        WAMNetworkClient.dPrint( '!' + ERROR + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        this.model.error( arguments );
        this.stop();
    }

    /**
     * Hook up with a Whack-A-Mole game server already running and waiting for
     * two players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message (connect) from the server.  Afterwards a thread that listens for
     * server messages and forwards them to the game object is started.
     *
     * @param host  the name of the host running the server program
     * @param port  the port of the server socket on which the server is listening
     * @param model the local object holding the state of the game that
     *              must be updated upon receiving server messages
     * @throws WhackException If there is a problem opening the connection
     */
    public WAMNetworkClient(String host, int port, Model model)
            throws WhackException {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkIn = new Scanner(clientSocket.getInputStream());
            this.networkOut = new PrintStream(clientSocket.getOutputStream());
            this.model = model;
            this.go = true;

            // Block waiting for the CONNECT message from the server.
            String request = this.networkIn.next();
            String arguments = this.networkIn.nextLine();
            String[] fields = arguments.trim().split(" ");
            this.rows = Integer.parseInt(fields[0]);
            this.cols = Integer.parseInt(fields[1]);
            this.players = Integer.parseInt(fields[2]);
            this.player_number = Integer.parseInt(fields[3]);
            System.out.println(rows + " " + cols + " " + players + " " + player_number);
            if (!request.equals(WELCOME )) {
                throw new WhackException("Expected CONNECT from server");
            }
            WAMNetworkClient.dPrint("Connected to server " + this.clientSocket);
            makeWelcome(arguments); //directly call this method
        }
        catch(IOException e) {
            throw new WhackException(e);
        }
    }

    /**
     * Called from the GUI when it is ready to start receiving messages
     * from the server.
     */
    public void startListener() {
        new Thread(() -> this.run()).start();
    }

    /**
     * Called as soon as the server is connected to players.
     * @param arguments
     */
    public void makeWelcome( String arguments ) {
        //Send the message to players.
        WAMNetworkClient.dPrint( '!' + WELCOME + ',' + arguments );

        // Update the board model.
        this.model.makeWelcome(rows, cols, players, player_number);
    }


    /**
     * make moles pop up on the board model and is called by the server.
     * @param arguments
     */
    public void makePopup( String arguments ) {
        WAMNetworkClient.dPrint( '!' + MOLE_UP + ',' + arguments );

        String[] fields = arguments.trim().split( " " );
        int column = Integer.parseInt(fields[0]);

        // Update the board model.
        this.model.makePopup(column);
    }

    /**
     * make moles pop down on the board model and is called by the server.
     * @param arguments
     */
    public void makePopdown( String arguments ) {
        WAMNetworkClient.dPrint( '!' + MOLE_DOWN + ',' + arguments );

        String[] fields = arguments.trim().split( " " );
        int column = Integer.parseInt(fields[0]);

        // Update the board model.
        this.model.makePopdown(column);
    }

    /**
     * send a whack to server to calculate the player scores.
     * @param arguments
     */
    public void madeWhack( String arguments ) {
        String[] fields = arguments.trim().split(" ");
        int mole_num = Integer.parseInt(fields[0]);
        int player_number = Integer.parseInt(fields[1]);
        networkOut.println(WHACK + " " + mole_num + " " + player_number);
    }

    /**
     * Called when the server sends a message saying that the
     * model has been won by this player. Ends the game.
     */
    public void gameWon() {
        WAMNetworkClient.dPrint( '!' + GAME_WON );

        dPrint( "You won! Yay!" );
        this.model.gameWon();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by the other player. Ends the game.
     */
    public void gameLost() {
        WAMNetworkClient.dPrint( '!' + GAME_LOST );
        dPrint( "You lost! Boo!" );
        this.model.gameLost();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game is a tie. Ends the game.
     */
    public void gameTied() {
        WAMNetworkClient.dPrint( '!' + GAME_TIED );
        dPrint( "You tied! Meh!" );
        this.model.gameTied();
        this.stop();
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.clientSocket.close();
        }
        catch( IOException ioe ) {
            // squash
        }
        this.model.close();
    }

    /**
     * update the total score.
     * @param arguments
     */
    public void makeScore(String arguments) {
        String[] fields = arguments.trim().split(" ");
        for (int i = 0; i < players; i++) {
            scoreList.add(i, Integer.parseInt(fields[i]));
        }

        // Update the board model.
        this.model.makeScore(scoreList);
    }
        /**
         * Run the main client loop. Intended to be started as a separate
         * thread internally. This method is made private so that no one
         * outside will call it or try to start a thread on it.
         */
        public void run() {
        while (this.goodToGo()) {
            try {
                String request = this.networkIn.next();
                String arguments = this.networkIn.nextLine().trim();
                WAMNetworkClient.dPrint( "Net message in = \"" + request + '"' );

                switch ( request ) {
                    case MOLE_UP:
                        makePopup( arguments );
                        break;
                    case MOLE_DOWN:
                        makePopdown( arguments );
                        break;
                    case SCORE:
                        makeScore( arguments );
                        break;
                    case GAME_WON:
                        gameWon();
                        break;
                    case GAME_LOST:
                        gameLost();
                        break;
                    case GAME_TIED:
                        gameTied();
                        break;
                    case ERROR:
                        error( arguments );
                        break;
                    default:
                        System.err.println("Unrecognized request: " + request);
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }


    public static void main(String[] args){

        if(args.length != 2){
            System.out.println("Usage: 'Host' 'Port'");
            System.exit(-1);
        }
        else{
            Application.launch(args);
        }

    }

}

