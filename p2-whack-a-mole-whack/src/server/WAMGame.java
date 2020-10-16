package server;

import client.Model;
import java.util.ArrayList;
import java.util.Random;

/**
 * Whack a mole game. An instance of this will be created by the server.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */
public class WAMGame
        implements  Runnable {

    private ArrayList<WAMPlayer> players;

    private Model game;
    private int rows;
    private int cols;
    private int game_time;
    private int playerAmt;
    private WAMPlayer player_1, player_2, player_3;
    private ArrayList<Integer> scoreList;

    /**
     * @param players ArrayList containing player objects.
     */
    public WAMGame(ArrayList<WAMPlayer> players, ArrayList<Integer> scoreList, int rows, int cols, int game_time) {

        this.players = players;
        this.rows = rows;
        this.cols = cols;
        this.game_time = game_time;
        this.scoreList = scoreList;
        //number of players size
        this.playerAmt = players.size();

        game = new Model();

    }

    @Override
    public void run() {
        boolean go = true;

        //start the clock.
        double start = System.currentTimeMillis();
        //while the game duration time
        while (go) {
            try {
                go = getElapsedTime(start); //changes the boolean type of go to true or false depending on the elapsed time.
                makeWhack(players);
            } catch (Exception e) {
                for (int i = 0; i < playerAmt; i++) {
                    players.get(i).error(e.getMessage());
                }
            }
        }
        go = false;
        game_result();
        close();

    }

    /**
     * close players.
     */
    public void close() {
        for (int i = 0; i < playerAmt; i++) {
            players.get(i).close();
        }
    }

    /**
     * Compute the elapsed time.
     *
     * @param start
     * @return
     */
    public boolean getElapsedTime(double start) {
        double elapsedTime = (System.currentTimeMillis() - start) / 1000.0;
        //System.out.println("Elapsed time: " +
        //        (System.currentTimeMillis() - start) / 1000.0 + " seconds.");
        if (elapsedTime <= game_time) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Make mole up thread work.
     */
    public void moleups(){
        Thread moleupThread = new Thread(() -> {
            MoleUpThread();
        });
        moleupThread.start();

    }

    /**
     * Calculate mole up thread time and make random numbers to happen.
     */
    public void MoleUpThread(){
        //the up time is between 3 and 5 seconds
        int min = 3, max = 5;
        int s = min + new Random().nextInt(max - min + 1);
        try {
            Thread.sleep(s * 1000); //the up time is between 3 and 5 seconds
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Make mole down thread work.
     */
    public void moledowns() {
        Thread moledownThread = new Thread(() -> {
            MoleDownThread();
        });
        moledownThread.start();
    }

    /**
     * Calculate mole down thread time and make random numbers to happen.
     */
    public void MoleDownThread(){
        int min = 2, max = 10;
        int s = min + new Random().nextInt(max - min + 1);
        try {
            Thread.sleep(s * 1000); //the down time is between 2 and 10 seconds
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * make a random number to use for mole numbers.
     *
     * @return
     */
    public int random_num() {
        Random r = new Random();
        int bound = rows * cols;
        int r_num = r.nextInt(bound); //when calling on r.nextInt(bound), it takes 0-24 numbers randomly.
        return r_num;
    }

    /**
     * make a whack in the game.
     * @param players
     */
    public void makeWhack(ArrayList<WAMPlayer> players) {
        int col = random_num();
        switch (playerAmt) {
            case 1: //when number of players is 1
                player_1 = players.get(0);
                player_1.mole_up(col);
                moleups();
                if (!player_1.isMadeWhack(col)) {
                    player_1.mole_down(col);
                    moledowns();
                }
                break;
            case 2: //when number of players is 2
                player_1 = players.get(0);
                player_2 = players.get(1);
                player_1.mole_up(col);
                player_2.mole_up(col);
                moleups();
                if (!player_1.isMadeWhack(col)) {
                    player_1.mole_down(col);
                }
                if (!player_2.isMadeWhack(col)) {
                    player_2.mole_down(col);
                }
                moledowns();
                break;
            case 3: //when number of players is 3
                player_1 = players.get(0);
                player_2 = players.get(1);
                player_3 = players.get(2);
                player_1.mole_up(col);
                player_2.mole_up(col);
                player_3.mole_up(col);
                moleups();
                if (!player_1.isMadeWhack(col)) {
                    player_1.mole_down(col);
                }
                if (!player_2.isMadeWhack(col)) {
                    player_2.mole_down(col);
                }
                if (!player_3.isMadeWhack(col)) {
                    player_3.mole_down(col);
                }
                moledowns();
                break;
            default:
        }
    }

    /**
     * inform the game result to each player after the game-duration time is over.
     */
    public void game_result() {
        switch (playerAmt) {
            case 1: //when there is one player.
                player_1.won();
                break;
            case 2: //when there are two players.
                if (scoreList.get(0) > scoreList.get(1)) {
                    player_1.won();
                    player_2.lost();
                } else if (scoreList.get(0) < scoreList.get(1)) {
                    player_1.lost();
                    player_2.won();
                } else {
                    player_1.draw();
                    player_2.draw();
                }
                break;
            case 3: //when there are three players.

                if (scoreList.get(0) > scoreList.get(1) && scoreList.get(0) > scoreList.get(2)) { //case1: player1 wins
                    player_1.won();
                    player_2.lost();
                    player_3.lost();
                } else if (scoreList.get(1) > scoreList.get(0) && scoreList.get(1) > scoreList.get(2)) { //case2: player2 wins
                    player_1.lost();
                    player_2.won();
                    player_3.lost();
                } else if (scoreList.get(2) > scoreList.get(0) && scoreList.get(2) > scoreList.get(1)) { //case3: player3 wins
                    player_1.lost();
                    player_2.lost();
                    player_3.won();
                } else if (scoreList.get(0) == scoreList.get(1) && scoreList.get(0) > scoreList.get(2)) { //case4: player1 and player 2 both win
                    player_1.won();
                    player_2.won();
                    player_3.lost();
                } else if (scoreList.get(0) == scoreList.get(2) && scoreList.get(0) > scoreList.get(1)) { //case5: player1 and player 3 both win
                    player_1.won();
                    player_2.lost();
                    player_3.won();
                } else if (scoreList.get(1) == scoreList.get(2) && scoreList.get(1) > scoreList.get(0)) { //case6: player2 and player 3 both win
                    player_1.lost();
                    player_2.won();
                    player_3.won();
                } else { //case4: all players tie
                    player_1.draw();
                    player_2.draw();
                    player_3.draw();
                }
                break;
            default:
        }
    }
}
