package server;
import common.WAMProtocol;
import common.WhackException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The {@link WAMServer} waits for incoming client connections and
 * pairs them off to play {@link WAMServer games}.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */
public class WAMServer implements WAMProtocol, Runnable {

    private ServerSocket serverSocket;
    private int rows;
    private int cols;
    private int players;
    private int game_time;
    private int port;
    private ArrayList<Integer> scoreList = new ArrayList<>();
    /**
     * Creates a new {@link WAMServer} that listens for incoming
     * connections on the specified port.
     *
     * @param port The port
     * @throws WhackException If there is an error making the ServerSocket
     *
     */
    public WAMServer(int port) throws WhackException {
        this.port = port;
        try{
            serverSocket = new ServerSocket(port);
        }catch (IOException e){
            throw new WhackException(e);
        }
    }

    public static void main(String[] args) throws WhackException{
        if(args.length != 5){
            System.out.println("Usage: game-port#  #rows  #columns  #players  game-duration-seconds");
            System.exit(1);
        }
        //takes arguments: game-port#, #rows, #columns, #players, game-duration-seconds
        int port = Integer.parseInt(args[0]);
        WAMServer whack = new WAMServer(port);
        whack.rows = Integer.parseInt(args[1]);
        whack.cols = Integer.parseInt(args[2]);
        whack.players = Integer.parseInt(args[3]);
        whack.game_time = Integer.parseInt(args[4]);
        whack.run();
    }

    /**
     * initialize the score as zero for each player
     */
    public void initialize() {
        for (int i = 0; i < players; i++) {
            scoreList.add(i , 0);
        }
    }

    @Override
    public void run(){

        //try for loop for every player to connect to server.
        try {
            ArrayList<WAMPlayer> playerList = new ArrayList<WAMPlayer>();
            initialize();
            for(int i = 0; i < players; i ++){
                System.out.println("Waiting for players [0/" + players + "]");
                Socket sock = serverSocket.accept();
                WAMPlayer p = new WAMPlayer(sock);
                playerList.add(p);
                p.connect(scoreList, rows, cols, players, i);
                System.out.println("Player [" + (i + 1) + "/" + players + "] here!");
            }
            WAMGame game = new WAMGame(playerList, scoreList, rows, cols, game_time);
            new Thread(game).run();
        } catch (IOException io) {
            System.err.println("OOPS");
            io.printStackTrace();
        } catch (WhackException e) {
            System.err.println("Could not initialize player!");
            e.printStackTrace();
        }
    }

}





