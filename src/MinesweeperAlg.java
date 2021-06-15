
import java.util.ArrayList;

public final class MinesweeperAlg {
	
	//Game board size
	private int ROWS;
	private int COLS;
	
	//Instances of the game and the board in the game
	private Minesweeper game;
	private MinesweeperBoard board;
	
	//Fields that have information about whether or not a square is safe 
	private boolean[][] mineCertainty;
	private boolean[][] cannotBeMine;
	private int[][] outcomesWithMine;
	private int[][] totalOutcomes;
	
	//Contains positions that contain revealed numbers that are to be checked. 
	private ArrayList<Integer> positionsToCheck = new ArrayList<Integer>();
	
	/**
	 * Constructor
	 * 
	 * @param sf, main game
	 * @param bboard, game's board
	 * @param r, number of rows
	 * @param c, number of columns
	 */
	public MinesweeperAlg(Minesweeper sf, MinesweeperBoard bboard, int r, int c) {
		game = sf;
		ROWS = r;
		COLS = c;
		mineCertainty = new boolean[ROWS][COLS];
		cannotBeMine = new boolean[ROWS][COLS];
		outcomesWithMine = new int[ROWS][COLS];
		totalOutcomes = new int[ROWS][COLS];
		board = bboard;
	}
	
	/**
	 * Called when user makes a move command. It will run the algorithm again from scratch. 
	 * Note, the checkAllNumbers() and lookThroughNumbers() are to be called multiple times,
	 * 		and the number 50 is arbitrary right now and can be changed. 
	 * 		The number should be at least 10, and I recommend it to be at least 15 so that
	 * 		more cases may be able to be solved. 
	 */
	public void update() {
		board.updateStatus(game);
		mineCertainty = new boolean[ROWS][COLS];
		outcomesWithMine = new int[ROWS][COLS];
		totalOutcomes = new int[ROWS][COLS];
		cannotBeMine = new boolean[ROWS][COLS];
		for (int i=0;i<50;i++) {
			checkAllNumbers();
			lookThroughNumbers();
		}
	}
	
	/**
	 * Look through the board to find all positions where a number is revealed, and 
	 * 		add those numbers in positionsToCheck ArrayList in such a way that
	 * 		it will be accessible later.
	 */
	private void checkAllNumbers() {
		positionsToCheck.clear();
		for (int i=0;i<ROWS;i++) 
			for (int j=0;j<COLS;j++) 
				if (board.isNumAndRevealed(i, j) && board.getNum(i, j) > 0) 
					positionsToCheck.add(i*10000+j);
	}
	
	/**
	 * For each position found by checkAllNumbers(), trivially solve some cases as explained
	 * 		in the comments within this method. 
	 */
	private void lookThroughNumbers() {
		for (int nums : positionsToCheck) {
			int x = nums/10000;
			int y = nums%10000;
			int num = board.getNum(x, y);
			
			/**
			 * If the number on the square is equal to the number of mines this algorithm
			 * 		has determined is present, set all remaining squares near this number
			 * 		to be a safe spot. 
			 */
			if (getMinesNearSelf(x, y) == num)
				setCellsToNotMines(x, y);
			
			int unrevealed = unrevealedNearSelf(x, y);
			
			/**
			 * If the number of the square is equal to the number of non revealed squares
			 * 		near it, all the squares near the square with the number must be a mine. 
			 */
			if (num == unrevealed) 
				setCellsToMines(x, y);
			
			int effectiveUnrevealed = effectiveUnrevealedNearSelf(x, y);
			
			/**
			 * If the number on the square is equal to the number of non revealed AND non mine
			 * 		squares (determined by this algorithm) near it, all the remaining squares 
			 * 		near it are mines. 
			 */
			if (num == effectiveUnrevealed)
				setCellsToMines(x, y);
		}
	}
	
	/**
	 * Determines the amount of non revealed squares near the position (x, y), excluding squares
	 * 		near it that are deemed "cannot be mine."
	 * 
	 * @param x, row index
	 * @param y, column index
	 * @return number determined
	 */
	private int effectiveUnrevealedNearSelf(int x, int y) {
		int counter = 0;
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (board.isUnrevealed(i, j) && !cannotBeMine[i][j]) counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Change all the non revealed squares near the position (x, y) that are also not 
	 * 		guaranteed to be mines to be all safe squares.
	 * 
	 * @param x, row index
	 * @param y, column index
	 */
	private void setCellsToNotMines(int x, int y) {
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (board.isUnrevealed(i, j) && !mineCertainty[i][j])
					cannotBeMine[i][j] = true;
			}
		}
	}
	
	/**
	 * Determines amount of mines near a position. This value is determined by the algorithm
	 * 		itself after calling "setCellsToMines()" on some squares that are trivially solved.
	 * 
	 * @param x, row index
	 * @param y, column index
	 * @return number determined
	 */
	private int getMinesNearSelf(int x, int y) {
		int counter = 0;
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (mineCertainty[i][j]) counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Change all the non revealed squares near the position (x, y) that are not safe zones
	 * 		to be all mine squares.
	 * 
	 * @param x, row index
	 * @param y, column index
	 */
	private void setCellsToMines(int x, int y) {
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (board.isUnrevealed(i, j) && !cannotBeMine[i][j])
					mineCertainty[i][j] = true;
			}
		}
	}
	
	/**
	 * Determines number of squares near this position (x, y) that are not revealed.
	 * 
	 * @param x, row index
	 * @param y, column index
	 * @return number determined
	 */
	private int unrevealedNearSelf(int x, int y) {
		int counter = 0;
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (board.isUnrevealed(i, j)) counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Determines position of all non revealed squares near this square. Used by the calculate
	 * 		probability method to give probability to each square. 
	 * 
	 * @param x, row index
	 * @param y, column index
	 * @return ArrayList of positions stored as an integer. 
	 */
	private ArrayList<Integer> unrevealedNearSelfArrList(int x, int y) {
		ArrayList<Integer> nums = new ArrayList<Integer>();
		for (int i = x-1; i <= x+1; i++) {
			if (i < 0 || i > ROWS-1) continue;
			for (int j = y-1; j <= y+1; j++) {
				if (j < 0 || j > COLS-1 || i == x && j == y) continue;
				if (board.isUnrevealed(i, j)) nums.add(i*10000+j);
			}
		}
		return nums;
	}
	
	/**
	 * Determine the probability of each square being a mine. 
	 * Note: Squares trivially solved either have probability of 0 or 1, for obvious reasons.
	 * 		Safe = 0 percent of having a mine, Mine = 100 percent of having a mine. 
	 * Also, squares that are empty or have numbers on it are obviously safe, so those are 
	 * 		given an arbitrary value -1 to not be confused with squares trivially determined to
	 * 		be safe.
	 * Squares that are NOT trivially solved are to be given an exact probability of how likely
	 * 		they may contain a mine. It is a very simple process to calculate this. 
	 * 
	 * This method itself will contain many explanations on what each part of the code does and
	 * 		how the probability is correct. 
	 * 
	 * @return double[][], individual probability of containing a mine for each square. 
	 */
	public double[][] calculateProbability() {
		
		/**
		 * This part of the code determines information for non trivial squares. 
		 * 
		 * For each square, first determine if it is a revealed number. If not, check the next 
		 * 		square. 
		 * If it is a revealed number, retrieve an ArrayList of all the non revealed squares'
		 * 		position near it. 
		 * For each of non revealed squares, if they are NOT trivially solved, do the following:
		 * 		for that square, add the value of the revealed number near it to the outcomesWithMine,
		 * 		then add the number of non revealed squares near that revealed number to totalOutcomes. 
		 * We cannot directly give it a probability because this non revealed square may be next
		 * 		to another number, and the individual probability of that other number saying
		 * 		this non revealed square is a mine might be very different. 
		 * 
		 * Why does this formula work?
		 * Let's define a scenario, a revealed number has a value of N, and there are an M number of 
		 * 		non revealed squares near it. 
		 * 		The probability of any non revealed square near N is the same (if we ignore other numbers).
		 * 		Because of this, and we know the total number of mines there should be, we can consider
		 * 			total number of mines as the expected value of mines, which is N. There are
		 * 			M squares near the center square we're focusing on, and since the expected value is N, 
		 * 			the probabilities of each square MUST add up to N. The only way this can happen
		 * 			with each square having the same probability is if the probability of each square
		 * 			is N/M, which if you add to itself M times, you get the expected value of N.
		 * 
		 * Again, we cannot directly attribute the value N/M now, because there are other squares that will
		 * 		modify the probability of this square. For example, this square may be next to a 1 and a 2, 
		 * 		and no other squares. Let's say there are 2 non revealed squares near the 1, and 3 non
		 * 		revealed squares near the 2. Let's say there is exactly 1 square (S) that is in contact with 
		 * 		both of these 2 numbers. If the number 2 was not there, the probability of S containing
		 * 		a mine is 0.5. If we pretend the 2 is there, and the 1 is NOT there, the probability
		 * 		of the square S having a mine is 0.6667. 
		 * 		Since we are not playing quantum mine finder, there cannot be two different probabilities
		 * 			for the same square so we must think of this differently. 
		 * 		For the case of the number 1, there are 2 possibilities for S, in 1 of them, it has a mine,
		 * 			for the other, there isn't a mine. 
		 * 		For the case of the number 2, there are 3 possibilities for S, in 2 of them, it has a mine,
		 * 			for the other 1 case, there isn't a mine. 
		 * 		This means that there are 5 possibilities for S, and there are 3 of them where there is a 
		 * 			mine. 
		 * 		Important note: this final probability for S is 0.6, and NOT the average of 0.5 and 0.6667. 
		 * 	
		 */
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (board.isNumAndRevealed(i, j)) {
					ArrayList<Integer> nums = unrevealedNearSelfArrList(i, j);
					for (int n : nums) {
						if (!mineCertainty[n/10000][n%10000] && !cannotBeMine[n/10000][n%10000]) {
							outcomesWithMine[n/10000][n%10000] += board.getNum(i, j);
							totalOutcomes[n/10000][n%10000] += nums.size();
						}
					}
				}
			}
		}
		
		double[][] values = new double[ROWS][COLS];
		
		/**
		 * Determine smallest and largest probability of containing mine that is not the trivial case. 
		 */
		double minVal = 5;
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (mineCertainty[i][j] || totalOutcomes[i][j] == 0) continue;
				else
					minVal = Math.min(minVal, ((double)outcomesWithMine[i][j])/totalOutcomes[i][j]);
			}
		}
		double maxVal = 0;
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (cannotBeMine[i][j] || totalOutcomes[i][j] == 0) continue;
				else
					maxVal = Math.max(maxVal, ((double)outcomesWithMine[i][j])/totalOutcomes[i][j]);
			}
		}
		
		/**
		 * This value constant is used to make the most dangerous square that isn't trivially a mine
		 * 		red. 
		 */
		double constant = (maxVal-minVal+0.05)*(1/(1-minVal+0.08))+0.01;
		
		/**
		 * With the information for non trivial cases, we are now ready to determine probability. 
		 * 
		 * If the square has been trivially determined to be a mine, the probability is set to be 1. 
		 * If the square has been trivially determined to be safe, the probability is set to be 0. 
		 * If the square has no data whatsoever, it means that the square contains a number, or is
		 * 		not in contact with any squares that are revealed, and we have no information for it. 
		 * 		In this scenario, we are giving it the arbitrary value of -1 to distinguish it from
		 * 		squares that are trivially determined to be safe. 
		 * Otherwise, the square is NOT trivially solved, and it is also non revealed, and is in contact
		 * 		with a revealed number. This probability is simple, divide total outcomes where there is 
		 * 		a mine by total outcomes. 
		 * However, this value is to be scaled differently so that
		 * 		the lowest probability of having a mine (besides trivial cases) will be green regardless
		 * 		of the probability itself. Then, the highest probability is used to use the value 
		 * 		{@code constant} that will cause the most dangerous square to be red. This is using the 
		 * 		formula (P - (min - dx))*(1/(min - dx - dx))/{@code constant} where dx is an arbitrary 
		 * 		small number compared to min. 
		 */
		for (int i=0;i<ROWS;i++) {
			for (int j=0;j<COLS;j++) {
				if (mineCertainty[i][j])
					values[i][j] = 1;
				else if (cannotBeMine[i][j])
					values[i][j] = 0;
				else if (totalOutcomes[i][j] == 0)
					values[i][j] = -1;
				else
					values[i][j] = (((double)outcomesWithMine[i][j])/totalOutcomes[i][j]-minVal+0.05)*(1/(1-minVal+0.08))/constant;

			}
		}
		return values;
	}
}