
import java.awt.Toolkit;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class Minesweeper extends Application {
	
	//Dimensions of the board and mines in the board
	public int ROWS = 0;
	public int COLS = 0;
	public int MINES = 0;
	
	//Used to set up the main scene
	private boolean firstCall = true;
	
	//Board of the game
	private MinesweeperBoard board;
	
	//Algorithm and whether to deploy algorithm
	private MinesweeperAlg alg;
	private boolean deployAlg = false;
	
	//Whether game is still active, used to allow or reject player movement
	private boolean solved = false;
	
	//Current position of player, defaulted to start at bottom left
	private int[] currentPos = new int[] {ROWS,COLS};
	
	//Pane containing the following labels about game status, instructions, and the creator
	private VBox vb = new VBox();
	private HBox hb = new HBox();
	private HBox hb2 = new HBox();
	
	//Pane that contains the VBox vb and all the labels on the screen
	private Pane pane = new Pane();
	
	//Buttons to restart the current game, create new game, activate algorithm, and reveal. 
	private Button newGame = new Button("New Game");
	private Button restart = new Button("Restart");
	private Button hints = new Button("Enable Algorithm");
	private int cheatsUsed = 0;
	private Button cheat = new Button("Reveal a Square ("+(3-cheatsUsed)+" left)");
	
	//The following labels are self explanatory
	private Label uBad = new Label("You stepped on a mine!");
	private Label uWin = new Label("You win!");
	private Label creator = new Label(/*"Created by Hai/Puzzling Expert/hhx"*/);
	
	//Contains contents of Pane pane
	private Scene scene;
	
	//Displays the Scene scene
	private Stage stage;
	
	/**
	 * Main function that is called in the beginning after main()
	 */
	@Override
	public void start(Stage arg0) throws Exception {
		
		//Initialize stage
		stage = arg0;
		stage.setTitle("Minesweeper V1 - HHX");
		
		showMenu();
	}
	
	private void updateBoard() {
		if (deployAlg)
			board.updateStatus(this, alg);
		else
			board.updateStatus(this);
	}
	
	/**
	 * Set up main Scene. Should be only called ONCE. 
	 */
	private void setupScene1() {
		
		//Initialize looks of the labels on the bottom
		creator.setStyle("-fx-text-fill: blue;-fx-font-weight: bold;");
		uBad.setVisible(false);
		uBad.setStyle("-fx-font-weight: bold;");
		uWin.setVisible(false);
		uWin.setStyle("-fx-font-weight: bold;");
		
		//Initialize vb
		vb.setStyle("-fx-alignment: center;-fx-spacing: 15;");
		vb.setLayoutX(0);
		vb.setLayoutY(90+30*ROWS);
		vb.setMinSize(60+30*COLS, 0);
		
		newGame.setOnAction(e -> {
			showMenu();
			pane.getChildren().clear();
			pane.getChildren().add(vb);
			updateBoard();
			solved = false;
			uBad.setVisible(false);
			uWin.setVisible(false);
		});
		
		restart.setOnAction(e -> {
			board.restart();
			cheatsUsed = 0;
			cheat.setVisible(true);
			cheat.setText("Reveal a Square (3 left)");
			currentPos = new int[] {ROWS, COLS};
			updateBoard();
			solved = false;
			uBad.setVisible(false);
			uWin.setVisible(false);
		});
		
		hints.setOnAction(e -> {
			if (!solved) 
				deployAlg = !deployAlg;
			if (deployAlg) {
				board.updateStatus(this, alg);
				hints.setText("Disable Algorithm");
			} else {
				board.updateStatus(this);
				hints.setText("Enable Algorithm");
			}
		});
		
		cheat.setOnAction(e -> {
			if (solved) return;
			cheatsUsed++;
			board.useCheat();
			cheat.setText("Reveal a Square ("+(3-cheatsUsed)+" left)");
			if (cheatsUsed == 3)
				cheat.setVisible(false);
			updateBoard();
		});
		
		hb = new HBox();
		hb.setStyle("-fx-alignment: center;-fx-spacing: 15;");
		hb.getChildren().addAll(newGame, restart);
		hb.setMinSize(60+30*COLS, 30);
		hb2 = new HBox();
		hb2.setStyle("-fx-alignment: center;-fx-spacing: 15;");
		hb2.getChildren().addAll(hints, cheat);
		hb2.setMinSize(60+30*COLS, 30);
		//Add the labels into the VBox vb
		vb.getChildren().addAll(hb, hb2, uBad, uWin, creator);
		
		//Add vb to the pane
		pane.getChildren().add(vb);
		
		//Initialize scene
		scene = new Scene(pane, 60+30*COLS, 300+30*ROWS);
		initListener();
		
		//Ensure this method is never called again. 
		firstCall = false;
	}
	
	private void showMenu() {
		VBox vb = new VBox();
		vb.setMinSize(150, 150);
		Label chooseDifficulty = new Label("Choose Difficulty");
		Button ez = new Button("Easy");
		Button mi = new Button("Medium");
		Button hd = new Button("Hard");
		ez.setOnAction(e -> {
			int n = (int)(Math.random()*3)+8;
			ROWS = n;
			COLS = n;
			MINES = 10;
			showMenuHelper();
		});
		mi.setOnAction(e -> {
			ROWS = 13+(int)(Math.random() * 4);
			COLS = 15+(int)(Math.random() * 2);
			MINES = 40;
			showMenuHelper();
		});
		hd.setOnAction(e -> {
			ROWS = 16;
			COLS = 30;
			MINES = 100;
			showMenuHelper();
		});
		vb.getChildren().addAll(chooseDifficulty, ez,mi,hd);
		vb.setStyle("-fx-alignment: center;-fx-spacing: 15;");
		Scene scene2 = new Scene(vb, 300, 300);
		stage.setScene(scene2);
		stage.setMinWidth(300);
		stage.setMinHeight(300);
		stage.setMaxWidth(300);
		stage.setMaxHeight(300);
		//stage.setX((Toolkit.getDefaultToolkit().getScreenSize().getWidth()-stage.getMinWidth())/2);
		//stage.setY((Toolkit.getDefaultToolkit().getScreenSize().getHeight()-stage.getMinHeight())/2);
		stage.show();
	}
	
	private void showMenuHelper() {
		board = new MinesweeperBoard(ROWS,COLS,MINES);
		currentPos = new int[] {ROWS,COLS};
		if (firstCall) setupScene1();
		startGame();
		stage.setMinWidth(60+30*COLS);
		stage.setMaxWidth(60+30*COLS);
		stage.setMinHeight(250+30*ROWS);
		stage.setMaxHeight(250+30*ROWS);
		//stage.setX((Toolkit.getDefaultToolkit().getScreenSize().getWidth()-stage.getMinWidth())/2);
		//stage.setY((Toolkit.getDefaultToolkit().getScreenSize().getHeight()-stage.getMinHeight())/2);
		this.vb.setMinSize(60+30*COLS, 0);
		this.vb.setMaxSize(60+30*COLS, 200);
		this.vb.setLayoutY(60+30*ROWS);
	}
	
	/**
	 * Reset the board and create new board. 
	 */
	private void startGame() {
		
		//Reset number of cheats used. 
		cheatsUsed = 0;
		cheat.setVisible(true);
		cheat.setText("Reveal a Square (3 left)");
		
		//Initialize the main aspects of the game
		board = new MinesweeperBoard(ROWS, COLS, MINES);
		
		//set up algorithm
		alg = new MinesweeperAlg(this, board, ROWS, COLS);
		
		initPane();
		
		//update board
		updateBoard();
		
		stage.setScene(scene);
		stage.show();
	}
	
	/**
	 * Add labels to the pane
	 */
	private void initPane() {
		Label[][] lbls = board.getLabels();
		for (int i=0;i<lbls.length;i++)
			pane.getChildren().addAll(lbls[i]);
	}
	
	/**
	 * Set up listener for the scene to accept user clicks
	 */
	private void initListener() {
		
		scene.setOnMouseClicked(e -> {
			if (!solved) {
				int x = (int)((e.getSceneX()-25)/30);
				int y = (int)((e.getSceneY()-25)/30);
				if (y >= ROWS || y < 0 || x >= COLS || x < 0) return;
				if (e.getButton() == MouseButton.PRIMARY)
					currentPos = new int[] {y,x};
				else if (e.getButton() == MouseButton.SECONDARY)
					board.rightClick(y, x, this);
				updateBoard();
			}
		});
	}
	
	/**
	 * Allows other classes with access of an instance of this class to get
	 * 		user position
	 * @return int[] containing user's current position
	 */
	public int[] getPosition() {
		return currentPos;
	}
	
	/**
	 * Allows the board class to tell this class that the user has either won or lost.
	 * 		The boolean parameter should be true if user won and false if user lost. 
	 * @param won
	 */
	public void finished(boolean won) {
		solved = true;
		if (won) 
			uWin.setVisible(true);
		else
			uBad.setVisible(true);
	}

	/**
	 * Main driver class to start the program
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
