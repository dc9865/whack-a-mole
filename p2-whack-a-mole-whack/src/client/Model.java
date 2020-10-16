package client;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

/**
 * The model for the Whack-A-Mole game.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */


public class Model {

    /**
     * The observers
     */
    private List<Observer<Model>> observers;
    private int mole_num;

    /** current game status */
    private Status status;
    /** total score */
    private ArrayList<Integer> tot_score;

    /** Possible statuses of game */
    public enum Status {
        WELCOME, SCORE, MOLE_UP, MOLE_DOWN, GAME_WON, GAME_LOST, GAME_TIED, ERROR;

        private String message = null;

        public void setMessage( String msg ) {
            this.message = msg;
        }

        @Override
        public String toString() {
            return super.toString() +
                    this.message == null ? "" : ( '(' + this.message + ')' );
        }
    }

    public Model(){

        this.observers = new LinkedList<>();

    }

    /**
     * The view calls this method to add themselves as an observer of the model.
     *
     * @param observer the observer
     */
    public void addObserver(Observer<Model> observer) {
        this.observers.add(observer);
    }

    /** when the model changes, the observers are notified via their update() method */
    private void alertObservers() {
        for (Observer<Model> obs: this.observers ) {
            obs.update(this);
        }
    }
    public void error(String arguments) {
        this.status = Status.ERROR;
        this.status.setMessage(arguments);
        alertObservers();
    }

    /**
     * get game status.
     * @return the Status object for the game
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * called when the game has been won by this player.
     */
    public void gameWon() {
        this.status = Status.GAME_WON;
        alertObservers();
    }

    /**
     * called when the game has been won by the other player.
     */
    public void gameLost() {
        this.status = Status.GAME_LOST;
        alertObservers();
    }

    /**
     * called when the game has been tied.
     */
    public void gameTied() {
        this.status = Status.GAME_TIED;
        alertObservers();
    }

    /**
     * called when welcoming message is sent by the server.
     * @param row
     * @param column
     * @param players
     * @param time
     */
    public void makeWelcome(int row, int column, int players, int time) {
        this.status = Status.WELCOME;
        alertObservers();

    }

    /**
     * called when score message is sent by the server.
     */
    public void makeScore(ArrayList<Integer> score) {
        this.status = Status.SCORE;
        this.tot_score = score;
        alertObservers();
    }

    /**
     * get total score.
     * @return total score
     */
    public ArrayList<Integer> getTot_score() { return tot_score; }

    /**
     * called when moles are up.
     * @param col
     */
    public void makePopup(int col) {
        this.status = Status.MOLE_UP;
        this.mole_num = col;
        alertObservers();
    }

    /**
     * get number of moles
     * @return mole numbers
     */
    public int getMole_num() {
        return mole_num;
    }

    /**
     * called when moles are poped down.
     * @param col
     */
    public void makePopdown(int col) {
        this.status = Status.MOLE_DOWN;
        this.mole_num = col;
        alertObservers();
    }

    /**
     * The user they may close at any time
     */
    public void close() {
        alertObservers();
    }
}