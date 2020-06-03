package sudokuGenerator;

import java.util.*;

import sudokuGenerator.SudokuPuzzle.Solvable;

/**
 * Randomly generates 9 * 9 sudoku boards and puzzles with difficulty.
 * 
 * A valid sudoku board:
 * 
 *  * Each of the digits 1-9 must occur exactly once in each row.
 *  * Each of the digits 1-9 must occur exactly once in each column.
 *  * Each of the the digits 1-9 must occur exactly once in each of the 9 3*3 
 *    blocks of the board.
 *    
 * A valid sudoku puzzle:
 * 
 *  * Has a random number of empty cells.
 *  * Has one and only one solution.
 *  
 * @author Esther Lin
 */
public class PuzzleGenerator {
	
	private Random rand;
	
	public PuzzleGenerator(long seed) {
		rand = new Random(seed);
	}
	
	/**
	 * Returns the given number of randomly generated 9*9 sudoku boards.
	 *  
	 * @param num the number of sudoku boards to generate
	 * @return a list containing the given number of randomly generated sudoku boards
	 */
	public List<int[][]> GenerateBoards(int num) {
		List<int[][]> boards = new ArrayList<int[][]>();
		
		for (int n = 0; n < num; n++) {
			int[][] board = new int[9][9];
			boolean[] flag = new boolean[10];
			
			// randomly fills the top-most 3 blocks by
			for (int k = 0; k < 9; k += 3) {				// block
				for (int i = 0; i < 3; i++) {				// row
					while (true) {
						validate(board, i, k, flag);
						for (int j = k; j < k + 3; j++) {	// cell
							int r = randomCandidate(flag);
							board[i][j] = r;
							flag[r] = false;
						}
						// for the second row of the middle block, after randomly
						// filling numbers, check if the next row has 3 candidates.
						// If not, revert and try again.
						if (k != 3 || i != 1) break;
						validate(board, i + 1, k, flag);
						if (numOfcandidates(flag) == 3) break;
						for (int j = k; j < k + 3; j++) {
							board[i][j] = 0;
						}
					}
				}
			}
			
			// randomly fills the left-most 2 blocks except the one at the top
			for (int k = 3; k < 9; k += 3) {				// block
				for (int j = 0; j < 3; j++) {				// column
					while (true) {
						validate(board, k, j, flag);
						for (int i = k; i < k + 3; i++) {	// cell
							int r = randomCandidate(flag);
							board[i][j] = r;
							flag[r] = false;
						}
						// for the second column of the middle block, after randomly
						// filling numbers, checks if the next column has 3 candidates.
						// If not, revert and try again.
						if (k != 3 || j != 1) break;
						validate(board, k, j + 1, flag);
						if (numOfcandidates(flag) == 3) break;
						for (int i = k; i < k + 3; i++) {
							board[i][j] = 0;
						}
					}
				}
			}
			
			solve(board, 30);	// solves the other 4 blocks
			boards.add(board);
		}
		
		return boards;
	}
	
	/**
	 * Returns a random valid candidate.
	 * 
	 * @param flag the array of whether each of the digits 1-9 is valid
	 * @return a random valid candidate
	 */
	private int randomCandidate(boolean[] flag) {
		while (true) {
			int r = rand.nextInt(9) + 1;
			if (flag[r]) return r;
		}
	}
	
	/**
	 * Returns the number of valid candidates in the given flag.
	 * 
	 * @param flag the array of whether each of the digits 1-9 is valid
	 * @return the number of valid candidates in flag
	 */
	private int numOfcandidates(boolean[] flag) {
		int res = 0;
		for (int k = 1; k <= 9; k++) {
			if (flag[k]) res++;
		}
		return res;
	}
	
	/**
	 * Backtracks to determine if the given board is solvable, and fills the board
	 * with the solution.
	 * 
	 * @param board the 9*9 sudoku board
	 * @param p the cell number to start with
	 * @return true if the sudoku is solvable, otherwise returns false
	 */
	private boolean solve(int[][] board, int p) {
		if (p == 81) return true;	// solution found
		int i = p / 9;
		int j = p % 9;
		if (board[i][j] > 0) return solve(board, p + 1);	// skips filled cell
		
		boolean[] flag = new boolean[10];
		validate(board, i, j, flag);
		for (int k = 1; k <= 9; k++) {	// tries with each valid candidate
			if (flag[k]) {
				board[i][j] = k;
				if (solve(board, p + 1)) return true;	// solves next cell
			}
		}
		board[i][j] = 0;	// board not solvable, reverts filled value
		return false;
	}
	
	/**
	 * Updates flag for the given cell by checking its row, column and block.
	 * 
	 * @param board the 9*9 sudoku board
	 * @param i the index of row
	 * @param j the index of column
	 * @param flag the array of whether each of the digits 1-9 is valid
	 */
	private void validate(int[][] board, int i, int j, boolean[] flag) {
		Arrays.fill(flag, true);
		for (int k = 0; k < 9; k++) {
			if (board[i][k] > 0) flag[board[i][k]] = false;	// validate row
			if (board[k][j] > 0) flag[board[k][j]] = false;	// validate column
			int r = i / 3 * 3 + k / 3;
			int c = j / 3 * 3 + k % 3;
			if (board[r][c] > 0) flag[board[r][c]] = false;	// validate block
		}
	}
	
	/**
	 * Returns a randomly generated sudoku puzzle from the given board.
	 * 
	 * @param board the 9*9 sudoku board
	 * @return a randomly generated sudoku puzzle
	 */
	public SudokuPuzzle generatePuzzle(int[][] board) {
		SudokuPuzzle puzzle = new SudokuPuzzle(board);
		
		// keeps tracks of the puzzle with the greatest difficulty
		SudokuPuzzle best = puzzle;
		int maxDifficulty = 0;
		
		for (int i = 0; i < 200; i++) {
			puzzle = best;	// restarts with the current best puzzle
			for (int j = 0; j < 20; j++) {
				randomOperate(puzzle, board);
				if (puzzle.uniquelySolvable()) {
					int diff = puzzle.getDifficulty();
					// updates the best puzzle and difficulty accordingly
					if (maxDifficulty < diff) {
						maxDifficulty = diff;
						best = new SudokuPuzzle(puzzle);
					}
				}
			}
		}
		
		return puzzle;
	}
	
	/**
	 * Randomly adds or removes a pair of cells to / from the puzzle.
	 * 
	 * @param puzzle the puzzle to operate on
	 * @param board the original board
	 */
	private void randomOperate(SudokuPuzzle puzzle, int[][] board) {
		// generates a random cell
		int n = rand.nextInt(81), r = n / 9, c = n % 9;
		
		if (rand.nextBoolean()) {
			// removes a pair
			puzzle.setVal(r, c, 0);
			puzzle.setVal(8 - r, 8 - c, 0);
		}
		else {
			// adds a pair with the original values
			puzzle.setVal(r, c, board[r][c]);
			puzzle.setVal(8 - r, 8 - c, board[8 - r][8 - c]);
		}
		
		puzzle.updatePuzzle();
	}
	
	public void printBoards(List<int[][]> boards) {
		for (int k = 0; k < boards.size(); k++) {
			System.out.println("Board #" + (k + 1));
			printBoard(boards.get(k));
			System.out.println();
		}
	}
	
	public void printBoard(int[][] board) {
		for (int i = 0; i < 9; i++) {
			System.out.print("[");
			for (int j = 0; j < 9; j++) {
				System.out.print(board[i][j] + ",");
			}
			System.out.println(board[i][8] + "]");
		}
	}
	
	public void printPuzzles(List<SudokuPuzzle> puzzles) {
		for (int k = 0; k < puzzles.size(); k++) {
			System.out.println("Puzzle #" + (k + 1));
			puzzles.get(k).printPuzzle();
			System.out.println();
		}
	}
	
}
