
import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

/**
 * Class containing positions of mines
 * 
 * @author Haoxian (Roger) Hu
 */
public final class MinesweeperBoard {
	
	//Information on the board itself
	private int ROWS;
	private int COLS;
	private int MINES;
	
	private boolean revealedBeginning = false;
	
	//Whether a certain square has been revealed
	private boolean[][] revealed;
		
	//Whether a certain square has a mine
	private boolean[][] mines;
		
	//The numbers on the squares if it is to be revealed
	private int[][] labels;
		
	//The actual labels containing numbers or X or E
	private Label[][] lbls;
	
	//Positions that are marked mines by the user. 
	private boolean[][] markedMines;
	
	/**
	 * Allows the main class to get access to all the labels this class contains. 
	 * @return Label[][]
	 */
	public Label[][] getLabels() {
		return lbls;
	}
	
	/**
	 * Allows the algorithm class to check whether a square is revealed. If so, return
	 * 		true (because a mine will never be revealed anyway)
	 * @param x, row index
	 * @param y, column index
	 * @return boolean, true if square revealed, false otherwise
	 */
	public boolean isNumAndRevealed(int x, int y) {
		return labels[x][y] >= 0 && revealed[x][y];
	}
	
	/**
	 * Allows the algorithm class to check whether a square is hidden from user. 
	 * 		Returns true if so, false otherwise
	 * @param x, row index
	 * @param y, column index
	 * @return boolean as stated above
	 */
	public boolean isUnrevealed(int x, int y) {
		return !revealed[x][y];
	}
	
	/**
	 * Allows the algorithm class to get the value of a square that it has already
	 * 		checked that it is revealed. 
	 * @param x, row index
	 * @param y, column index
	 * @return value of a square
	 */
	public int getNum(int x, int y) {
		if (revealed[x][y]) 
			return labels[x][y];
		else
			throw new RuntimeException("Illegal attempt to get number!");
	}
	
	/**
	 * Constructor
	 */
	public MinesweeperBoard(int r, int c, int s) {
		ROWS = r;
		COLS = c;
		MINES = s;
		revealed = new boolean[r][c];
		mines = new boolean[r][c];
		labels = new int[r][c];
		lbls = new Label[r][c];
		markedMines = new boolean[r][c];
		init();
	}
	
	/**
	 * Initialize fields and mine locations and reveal bottom row
	 */
	public void init() {
		markedMines = new boolean[ROWS][COLS];
		resetAll();
		addMines();
		initLabels();
	}

	/**
	 * Remove all mines, hide everything
	 */
	public void resetAll() {
		for (int i=0;i<mines.length;i++) {
			for (int j=0;j<mines[0].length;j++) {
				mines[i][j] = false;
				revealed[i][j] = false;
				labels[i][j] = 0;
			}
		}
	}
	
	/**
	 * Add the mines to the board. Currently, not totally random because it is to save
	 * 		time to run for hard mode. Although not totally random, it's good enough
	 * 		for this game. 
	 */
	public void addMines() {
		ArrayList<Integer> nums = new ArrayList<Integer>();
		while (nums.size() < MINES) {
			int n = (int)(Math.random() * (ROWS)) * 10000 + (int)(Math.random() * (COLS));
			if (nums.contains(n))
				/*nums.clear();*/continue;
			else
				nums.add(n);
			if (nums.size() == MINES) {
				int[] cs = new int[ROWS];
				int[] csa = new int[COLS];
				for (int na : nums) {
					cs[na/10000]++;
					csa[na%10000]++;
				}
				for (int nab : cs) {
					if (nab > 2*ROWS/3-1) {
						nums.clear();
						continue;
					}
				}
				for (int nab : csa) {
					if (nab > 2*COLS/3-1) {
						nums.clear();
						continue;
					}
				}
			}
		}
		for (int n : nums) 
			mines[n/10000][n%10000] = true;
	}
	
	/**
	 * Set up labels and use the setId() and setAccessibleHelp() methods to keep access to 
	 * 		original text and original style. 
	 */
	private void initLabels() {
		for (int i=0;i<labels.length;i++) {
			for (int j=0;j<labels[0].length;j++) {
				labels[i][j] = minesNearSelf(i, j);
				Label lb;
				if (mines[i][j]) {
					lb = new Label("X");
					lb.setStyle("-fx-text-fill: red;-fx-font-weight: bold;");
				} else if (labels[i][j] == 0) {
					lb = new Label(" ");
					lb.setStyle("-fx-text-fill: blue;-fx-font-weight:bold;");
				} else {
					lb = new Label(String.valueOf(labels[i][j]));
				}
				lb.setAlignment(Pos.CENTER);
				lb.setId(lb.getText());
				lb.setAccessibleHelp(lb.getStyle()+"-fx-border-width: 2;-fx-border-color: darkgray;");
				lb.setMaxSize(30, 30);
				lb.setMinSize(30, 30);
				lb.setLayoutX(25 + 30*j);
				lb.setLayoutY(25 + 30*i);
				setVisible(lb, false);
				lbls[i][j] = lb;
			}
		}
	}
	
	/**
	 * Called by the main class after a user input or after user starts or stops the algorithm. 
	 * It will change background color of the square the user is currently on to lime-green color
	 * 
	 * If the algorithm is deployed, the data from the algorithm is applied and the background
	 * 		color of squares that it has data on will change. If a square is for sure a mine,
	 * 		the color will be black; if it is definitely not a mine, the color will be blue. 
	 * 		If a square is guaranteed to be safe or dangerous, the color will be a spectrum of 
	 * 			green to red, with green being safer than red. This color is based on a probability
	 * 			determined by the algorithm of a square being able to contain a mine
	 * 
	 * @param sfn the main class
	 * @param sfa, optional parameter. This is sent if algorithm is deployed. 
	 */
	public void updateStatus(Minesweeper sfn, MinesweeperAlg...sfa) {
		int[] currentPos = sfn.getPosition();
		if (!(currentPos[0] == ROWS && currentPos[1] == COLS)) {
			revealed[currentPos[0]][currentPos[1]] = true;
			setVisible(lbls[currentPos[0]][currentPos[1]], true);
		}
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (revealed[i][j]) 
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + "-fx-background-color: lightgray;");
				else if (markedMines[i][j])
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + "-fx-background-color: fuchsia;");
				else
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + " -fx-background-color: ghostwhite;");
			}
		}
		if (!revealedBeginning && currentPos[0] == ROWS && currentPos[1] == COLS) {
			boolean b = true;
			int x = (int)(ROWS * Math.random());
			int y = (int)(COLS * Math.random());
			for (int i=x;i<x+ROWS && b;i++) {
				for (int j=y;j<y+COLS && b;j++) {
					if (!revealed[(i%ROWS)][(j%COLS)] && !mines[(i%ROWS)][(j%COLS)] && labels[(i%ROWS)][(j%COLS)]==0) {
						revealedBeginning = true;
						currentPos = new int[] {i%ROWS, j%COLS};
						revealed[i%ROWS][j%COLS] = true;
						setVisible(lbls[i%ROWS][j%COLS], true);
						lbls[i%ROWS][j%COLS].setStyle(lbls[i%ROWS][j%COLS].getAccessibleHelp() + "-fx-background-color: lightgray;");
						revealAllNear(i%ROWS, j%COLS);
						b = false;
					}
				}
			}
		}
		try {
			if (labels[currentPos[0]][currentPos[1]] == 0)
				revealAllNear(currentPos[0], currentPos[1]);
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			//Ignore because I don't know how to fix this and it works fine.
		}
		
		if (sfa.length > 0) {
			sfa[0].update();
			double[][] values = sfa[0].calculateProbability();
			for (int i=0;i<ROWS;i++) {
				for (int j=0;j<COLS;j++) {
					if (values[i][j] < 0) 
						continue;
					if (values[i][j] == 0)
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: rgb(6,82,255);");
					else if (values[i][j] == 1 && !markedMines[i][j])
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: rgb(0,0,0);");
					else if (values[i][j] == 1)
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: fuchsia;");
					else if (values[i][j] < 0.5) 
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: rgb("+ (int)(510*values[i][j]) + ", 255, 0);");
					else
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: rgb(255, "+(int)(255-510*(1-values[i][j]))+", 0);");
				}
			}
		}
		
		try {
			if (mines[currentPos[0]][currentPos[1]]) //Lost
				revealAllSquares(false, sfn);
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			//Ignore cuz it's too much work to prevent this from happening
			//also nothing bad happens when this error comes up
		}

		if (Arrays.deepEquals(this.markedMines, this.mines)) {
			revealAllSquares(true, sfn);
			return;
		}

		for (int i=0;i<ROWS;i++)
			for (int j=0;j<COLS;j++)
				if (labels[i][j]>=0 && !revealed[i][j])
					return;
		revealAllSquares(true, sfn);
		
	}
	
	/**
	 * Called when a user wins or steps on a mine to display all squares.
	 * It will also turn the squares the user has revealed before this method is called to a
	 * 		light blue background. 
	 * 
	 * @param b
	 * @param sfn
	 */
	private void revealAllSquares(boolean b, Minesweeper sfn) {
		sfn.finished(b);
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (i==sfn.getPosition()[0] && j==sfn.getPosition()[1]) continue;
				if (revealed[i][j])
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp()+"-fx-background-color: lightgray;");
				else
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp());
			}
		}
		for (Label[] ls : lbls)
			for (Label l : ls)
				setVisible(l, true);
	}
	
	/**
	 * Helper method that reveals all squares near a position (x, y)
	 * If doing so reveals another blank (E), the method is called recursively to reveal all blanks near that
	 */
	private void revealAllNear(int x, int y) {
		for (int i=x-1;i<=x+1;i++) {
			if (i < 0 || i > ROWS-1)
				continue;
			for (int j=y-1;j<=y+1;j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y || revealed[i][j])
					continue;
				setVisible(lbls[i][j], true);
				revealed[i][j] = true;
				lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + "-fx-background-color: lightgray;");
			}
		}
		for (int i=x-1;i<=x+1;i++) {
			if (i < 0 || i > ROWS-1)
				continue;
			for (int j=y-1;j<=y+1;j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y)
					continue;
				if (labels[i][j] == 0 && !allNearRevealed(i, j)) {
					revealAllNear(i,j);
				}
			}
		}
	}
	
	
	/**
	 * Helper method to determine if a position (x, y) has all its squares near it revealed. 
	 * @param x, row index
	 * @param y, column index
	 * @return boolean
	 */
	private boolean allNearRevealed(int x, int y) {
		for (int i=x-1;i<=x+1;i++) {
			if (i < 0 || i > ROWS-1)
				continue;
			for (int j=y-1;j<=y+1;j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y)
					continue;
				if (!revealed[i][j])
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Determine how many mines are near a position (x, y)
	 * @param x, row index
	 * @param y, column index
	 * @return int, amount of mines near it
	 */
	public int minesNearSelf(int x, int y) {
		if (mines[x][y])
			return -1;
		int counter = 0;
		for (int i=x-1;i<=x+1;i++) {
			if (i < 0 || i > ROWS-1)
				continue;
			for (int j=y-1;j<=y+1;j++) {
				if (j < 0 || j > COLS-1 || x == i && y == j)
					continue;
				counter += (mines[i][j] ? 1 : 0);
			}
		}
		return counter;
	}

	/**
	 * Called when user decides to use the same board and start over. 
	 */
	public void restart() {
		markedMines = new boolean[ROWS][COLS];
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				revealed[i][j] = false;
				setVisible(lbls[i][j], false);
			}
		}
		useCheat();
	}
	
	/**
	 * Called when user decides to use a new board and start again
	 */
	public void newGame() {
		init();
	}
	
	/**
	 * Helper method to turn a label to be "visible"
	 * @param l
	 * @param b
	 */
	private void setVisible(Label l, boolean b) {
		if (b)
			l.setText(l.getId());
		else
			l.setText("");
	}
	
	/**
	 * Called when user right clicks on the screen. 
	 * 
	 * @param x, row index
	 * @param y, column index
	 * @param sfn, game class
	 */
	public void rightClick(int x, int y, Minesweeper sfn) {
		if (!revealed[x][y])
			markedMines[x][y] = !markedMines[x][y];
		else {
			int counter = 0;
			for (int i=x-1;i<=x+1;i++) {
				if (i < 0 || i > ROWS-1)
					continue;
				for (int j=y-1;j<=y+1;j++) {
					if (j < 0 || j > COLS-1 || x == i && y == j)
						continue;
					if (markedMines[i][j])
						counter++;
				}
			}
			if (counter != labels[x][y])
				return;
			boolean failed = false;
			for (int i=x-1;i<=x+1;i++) {
				if (i < 0 || i > ROWS-1)
					continue;
				for (int j=y-1;j<=y+1;j++) {
					if (j < 0 || j > COLS-1 || x == i && y == j)
						continue;
					if (!revealed[i][j] && !markedMines[i][j]) {
						if (mines[i][j])
							failed = true;
						revealed[i][j] = true;
						setVisible(lbls[i][j], true);
						lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + "-fx-background-color: lightgray;");
						if (labels[i][j] == 0)
							revealAllNear(i, j);
					}
				}
			}
			if (failed)
				revealAllSquares(false, sfn);
		}
	}
	
	/**
	 * Reveal an empty square if there exists one that isn't revealed. 
	 * If there isn't, reveal 3 squares. 
	 */
	public void useCheat() {
		int x = (int)(ROWS * Math.random());
		int y = (int)(COLS * Math.random());
		for (int i=x;i<x+ROWS;i++) {
			for (int j=y;j<y+COLS;j++) {
				if (!revealed[(i%ROWS)][(j%COLS)] && !mines[(i%ROWS)][(j%COLS)] && labels[(i%ROWS)][(j%COLS)]==0) {
					revealed[i%ROWS][j%COLS] = true;
					setVisible(lbls[i%ROWS][j%COLS], true);
					lbls[i%ROWS][j%COLS].setStyle(lbls[i%ROWS][j%COLS].getAccessibleHelp() + "-fx-background-color: lightgray;");
					revealAllNear(i%ROWS, j%COLS);
					return;
				}
			}
		}
		useCheatHelper();
		useCheatHelper();
		useCheatHelper();
	}
	
	/**
	 * Used to reveal 3 squares. 
	 */
	private void useCheatHelper() {
		double x = Math.random();
		double y = Math.random();
		for (int i=(x > 0.5 ? 0 : ROWS-1);(x > 0.5 ? i<ROWS : i>=0);i += (x > 0.5 ? 1 : -1)) {
			for (int j=(y > 0.5 ? 0 : COLS-1);(y > 0.5 ? j<COLS : j>=0);j += (y > 0.5 ? 1 : -1)) {
				if (!revealed[i][j] && !mines[i][j] && labels[i][j]>0) {
					revealed[i][j] = true;
					setVisible(lbls[i][j], true);
					lbls[i][j].setStyle(lbls[i][j].getAccessibleHelp() + "-fx-background-color: lightgray;");
					return;
				}
			}
		}
	}
}
