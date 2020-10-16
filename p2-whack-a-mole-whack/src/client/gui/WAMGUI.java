package client.gui;

import client.Model;
import client.WAMNetworkClient;
import common.WhackException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import client.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * A JavaFX GUI for the networked Whack-A-Mole game.
 *
 * @author Daniel Cho
 * @author Juan Patino
 */
public class WAMGUI extends Application implements Observer<Model> {

    /** the model */
    private Model model;
    /** MVC Components */
    private Button moleSpotButton[][];
    private TextField output1 = new TextField();
    private TextField message1 = new TextField();
    /** connection to network interface to server */
    private WAMNetworkClient serverConn;
    private ArrayList<Integer> score;
    private int rows, cols, player_number;

    @Override
    public void init(){
        try {
            // get the command line args
            List<String> args = getParameters().getRaw();
            // get host info and port from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            // create uninitialized model
            this.model = new Model();
            // add ourselves as an observer
            model.addObserver(this);

            // create the network connection
            this.serverConn = new WAMNetworkClient(host, port, this.model);
            this.rows = this.serverConn.getRows();
            this.cols = this.serverConn.getCols();
            this.player_number = this.serverConn.getPlayer_number();

        } catch(NumberFormatException |
                ArrayIndexOutOfBoundsException |
                WhackException e){
            System.err.println(e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Construct the layout for the game.
     *
     * @param stage container (window) in which to render the GUI
     * @throws Exception if there is a problem
     */
    public void start(Stage stage) throws Exception {

        GridPane gridPane = makeGridPane();
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setTitle("Whack-A-Mole"); //ideally we want to specify the player
        stage.show();

        // Start the network client listener thread
        this.serverConn.startListener();

        // Manually force a display of all board state, since it's too late
        // to trigger update().
        this.refresh();
    }

    /**
     * Which mole to appear/go up
     * @param mole_num calculate mole_row and mole_col
     */
    public void popUpAt(int mole_num){
        int mole_row = mole_num / cols; //solved both cases when number of rows and cols are different + when number of rows and cols are same
        int mole_col = (mole_num % cols) + 1;
        moleSpotButton[mole_row][mole_col].setDisable(false); //when button is popped up, it has to be enabled.
        revealImg(moleSpotButton[mole_row][mole_col], "noMole.png"); //ideally the only things that should change about moles are the image

        moleSpotButton[mole_row][mole_col].setOnAction(e -> {
            serverConn.madeWhack(mole_num + " " + player_number);
            revealImg(moleSpotButton[mole_row][mole_col], "yesMole.png");
            moleSpotButton[mole_row][mole_col].setDisable(true);
            score = model.getTot_score();
            if (score != null) output1.setText("You got " + score.get(player_number) + " scores.");
        });
    }

    /**
     * Which mole to appear/go down
     * @param mole_num calculate mole_row and mole_col
     */
    public void popDownAt(int mole_num){
        int mole_row = mole_num / cols; //solved both cases when number of rows and cols are different + when number of rows and cols are same
        int mole_col = (mole_num % cols) + 1;
        moleSpotButton[mole_row][mole_col].setDisable(true);
        score = model.getTot_score();
        if (score != null) output1.setText("You got " + score.get(player_number) + " scores.");
    }

    /**
     * reveals image for specific buttons.
     * @param b buttons getting clicked
     * @param fileName names of the files called
     */
    private void revealImg( Button b, String fileName ) {
        Image p = new Image(getClass().getResourceAsStream(fileName));
        ImageView icon = new ImageView(p);
        b.setText(null);
        b.setGraphic(icon);
    }

    /**
     * make a grid pane which displays mole images in it.
     * @return grid pane
     */
    public GridPane makeGridPane() {
        GridPane gridPane = new GridPane();
        Image p = new Image(getClass().getResourceAsStream("Score.png"));
        ImageView icon = new ImageView(p);
        gridPane.add(icon, 0, 0);
        Label label_score = new Label("            Score:  ");
        gridPane.add(label_score, 0, 1);
        output1.setEditable(false);
        gridPane.add(output1, 0, 2);
        Label label_message = new Label("            Message:  ");
        gridPane.add(label_message, 0, 3);
        message1.setEditable(false);
        gridPane.add(message1, 0, 4);

        //rows and cols variables come from server side.
        moleSpotButton = new Button[rows][cols + 1];
        for (int row = 0; row < rows; row++) {
            for (int col = 1; col < cols + 1; col++) {
                moleSpotButton[row][col] = new Button();
                gridPane.add(moleSpotButton[row][col], col, row);
                revealImg(moleSpotButton[row][col], "noMole.png");
                moleSpotButton[row][col].setDisable(true);
            }
        }
        return gridPane;
    }



    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        // TODO
        this.serverConn.close();
    }

    /**
     * Called by the model, server.Model, whenever there is a state change
     * that needs to be updated by the GUI.
     *
     * @param model
     */
    @Override
    public void update(Model model) {
        if ( Platform.isFxApplicationThread() ) {
            this.refresh();
        }
        else {
            Platform.runLater( () -> this.refresh() );
        }
    }

    /**
     * Do your GUI updates here.
     */
    private void refresh() {
        Model.Status status = model.getStatus();

        switch (status) {
            case ERROR:
                message1.setText(String.valueOf(status));
                this.endGame();
                break;

            case WELCOME:
                message1.setText("Start");
                break;

            case MOLE_UP:
                int mole_num1 = model.getMole_num();
                this.popUpAt(mole_num1);
                message1.setText("Whack");
                break;

            case MOLE_DOWN:
                int mole_num2 = model.getMole_num();
                this.popDownAt(mole_num2);
                break;

            case SCORE:
                score = model.getTot_score();
                output1.setText("You got " + score.get(player_number) + " scores.");
                break;

            case GAME_WON:
                message1.setText("You won. Yay!");
                this.endGame();
                break;

            case GAME_LOST:
                message1.setText("You lost. Boo!");
                this.endGame();
                break;

            case GAME_TIED:
                message1.setText("Tie game. Meh.");
                this.endGame();
                break;

            default:
        }
        score = model.getTot_score();
        if (score != null) output1.setText("You got " + score.get(player_number) + " scores.");
    }


    private synchronized void endGame() {
        this.notify();
    }




}
