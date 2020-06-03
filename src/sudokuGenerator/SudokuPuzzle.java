package sudokuGenerator;

import java.util.*;

/**
 * Represents a sudoku puzzle.
 * 
 * Cell: Contains one of the digits 1-9. A puzzle has 9 * 9 = 81 cells.
 * Block: contains 3 * 3 = 9 cells. A puzzle has 9 blocks.
 * Set: Can be a row, a column, or a block of the puzzle. A puzzle has 27 sets.
 * 
 * Puzzle difficulty:
 *   
 *   sigma (Fi - 1)^2 * 100 + E 
 * 
 *   where i is each solving step, Fi is the candidate number of the ith step, and E 
 *   is the number of empty cells.
 * 
 * Puzzle solvability:
 * 
 *   A puzzle can be not solvable, uniquely solvable, or not uniquely solvable. Only
 *   a uniquely solvable puzzle is a valid sudoku puzzle.
 * 
 * @author Esther Lin
 */
public class SudokuPuzzle {
	
	public enum Solvable {
		NOT,		// Not solvable
		UNIQUE,		// Uniquely solvable
		NOT_UNIQUE	// Solvable, but not uniquely
	};

	private Cell[][] puzzle;		// the 2D array of Cells
	private Set[] sets;				// the row, column and block Sets
	private Solvable solvability;
	private int difficulty;
	private int numEmptyCell;		// the number of empty cells
	
	/**
	 * Generates a sudoku puzzle with the given puzzle board.
	 * 
	 * @param puzzleBoard the 2D array that holds values
	 */
	public SudokuPuzzle(int[][] puzzleBoard) {
		this();
		// generates Cells
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzle[i][j] = new Cell(i, j, puzzleBoard[i][j]);
				if (puzzleBoard[i][j] > 0) numEmptyCell--;
			}
		}
		// updates flags for Cells
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzle[i][j].updateFlag(puzzle);
			}
		}
		// initializes Sets
		for (int k = 0; k < 9; k++) {
			sets[k] = new Set(k, 0, k, 8);						// row Sets
			sets[k + 9] = new Set(0, k, 8, k);					// column Sets
			int sr = k / 3 * 3;
			int sc = k % 3 * 3;
			sets[k + 9 * 2] = new Set(sr, sc, sr + 2, sc + 2);	// block Sets
		}
		
		updatePuzzle();
		//System.out.println(difficulty);
	}
	
	/**
	 * Clones a sudoku puzzle with the given sudoku puzzle.
	 * 
	 * @param o the sudoku puzzle to be cloned
	 */
	public SudokuPuzzle(SudokuPuzzle o) {
		this();
		// copies Cells
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				puzzle[i][j] = new Cell(o.puzzle[i][j]);
			}
		}
		// copies Sets
		for (int k = 0; k < 27; k++) {
			sets[k] = new Set(o.sets[k]);
		}
		solvability = o.solvability;
		difficulty = o.difficulty;
		numEmptyCell = o.numEmptyCell;
	}
	
	/**
	 * Initializes member variables.
	 */
	public SudokuPuzzle() {
		puzzle = new Cell[9][9];
		sets = new Set[27];
		solvability = Solvable.NOT;
		difficulty = 0;
		numEmptyCell = 81;
	}
	
	/**
	 * Updates the solvability and difficulty of the sudoku puzzle.
	 */
	public void updatePuzzle() {
		solvability = Solvable.NOT;	// default to not solvable
		difficulty = numEmptyCell;	// default to the number of empty cells
		
		// initializes the priority queue of empty cells
		Queue<Cell> emptyCell = new PriorityQueue<Cell>();
		updateEmptyCell(emptyCell);
		
		// initializes the priority queue of missing values in sets
		Queue<MissingVal> missingVal = new PriorityQueue<MissingVal>();
		updateMissingVal(missingVal);
		
		solve(emptyCell, missingVal);
	}
	
	/**
	 * Updates the queue of empty cells prioritized with the number of candidate values.
	 * 
	 * @param emptyCell the priority queue of empty cells
	 */
	private void updateEmptyCell(Queue<Cell> emptyCell) {
		emptyCell.clear();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (puzzle[i][j].isEmpty()) {
					puzzle[i][j].updateFlag(puzzle);
					emptyCell.offer(puzzle[i][j]);
				}
			}
		}
	}
	
	/**
	 * Updates the queue of missing values in sets prioritized with the number of candidate 
	 * cells.
	 * 
	 * @param missingVal the priority queue of missing values in sets
	 */
	private void updateMissingVal(Queue<MissingVal> missingVal) {
		missingVal.clear();
		for (Set set : sets) {
			set.updateSet(puzzle);
			boolean[] missingValInSet = set.getMissingVal();
			for (int k = 1; k <= 9; k++) {
				if (missingValInSet[k]) {
					missingVal.offer(new MissingVal(set, k));
				}
			}
		}
	}
	
	/**
	 * Solves the sudoku puzzle:
	 * 
	 *   1. If no empty cell can be found, the puzzle is solved.
	 *   2. Finds the empty cell with the smallest number of candidate values.
	 *   3. Finds the missing value in a set with the smallest number of candidate cells.
	 *   4. If 2 <= 3, tries filling each candidate value in the identified cell and 
	 *      recursively solves. If all candidate values are exhausted, it is not solvable.
	 *   5. If 3 < 2, tries filling each candidate cells with the missing value and 
	 *      recursively solves. If all candidate cells are exhausted, it is not solvable.
	 * 
	 * Tries finding more than one solutions and updates the solvability. Updates the 
	 * difficulty when finding the first solution. If two solutions are found, stops solving 
	 * immediately and resets the difficulty to 0.
	 * 
	 * @param emptyCell the priority queue of empty cells
	 * @param missingVal the priority queue of missing values in sets
	 * @return true if solution(s) is found, otherwise returns false
	 */
	private boolean solve(Queue<Cell> emptyCell, Queue<MissingVal> missingVal) {
		if (emptyCell.isEmpty()) {		// solution found
			solvability = solvability == Solvable.UNIQUE
				? Solvable.NOT_UNIQUE	// second solution
				: Solvable.UNIQUE;		// first solution
			return true;
		}

		if (emptyCell.peek().getNumCandidates() <= missingVal.peek().getNumCandidates()) {
			// solves the empty cell with the smallest number of candidate values
			Cell p = emptyCell.poll();
			boolean[] flag = p.getFlag();
			
			// calculates the difficulty factor
			int diffFactor = (int) Math.pow(p.getNumCandidates() - 1, 2) * 100;
			if (solvability == Solvable.NOT) {
				difficulty += diffFactor;
			}
			
			for (int k = 1; k <= 9; k++) {	// solves each candidate value
				if (flag[k]) {
					p.setVal(k);
					updateEmptyCell(emptyCell);
					updateMissingVal(missingVal);
					if (solve(emptyCell, missingVal)) {	// solves next step
						p.setVal(0);					// reverts filled value
						if (solvability == Solvable.NOT_UNIQUE) {
							return true;
						}
					}
				}
			}
			// not solvable, reverts filled value and added difficulty factor
			p.setVal(0);
			if (solvability == Solvable.NOT) {
				difficulty -= diffFactor;
			}
		}
		else {
			// solves the missing value in set with smallest number of candidate cells
			MissingVal v = missingVal.poll();
			List<Cell> possibleCells = v.getPossibleCells();
			
			// calculates the difficulty factor
			int diffFactor = (int) Math.pow(possibleCells.size() - 1, 2) * 100;
			if (solvability == Solvable.NOT) {
				difficulty += diffFactor;
			}
			
			for (int k = 0; k < possibleCells.size(); k++) {	// solves each candidate cell
				Cell p = possibleCells.get(k);
				p.setVal(v.getVal());
				updateEmptyCell(emptyCell);
				updateMissingVal(missingVal);
				if (solve(emptyCell, missingVal)) {	// solves next
					p.setVal(0);					// reverts filled value
					if (solvability == Solvable.NOT_UNIQUE) {
						return true;
					}
				}
				p.setVal(0);	// not solvable, reverts filled value
			}
			// not solvable, reverts added difficulty factor
			if (solvability == Solvable.NOT) {
				difficulty -= diffFactor;
			}
		}
		return false;	// not solvable
	}
	
	/**
	 * Returns whether the sudoku puzzle is unique solvable.
	 * 
	 * @return true if the puzzle is uniquely solvable, otherwise returns false
	 */
	public boolean uniquelySolvable() { return solvability == Solvable.UNIQUE; }
	
	/**
	 * Sets the specified Cell to the given value.
	 * 
	 * @param r	the row index of the Cell
	 * @param c the column index of the Cell
	 * @param val the value
	 */
	public void setVal(int r, int c, int val) {
		// updates the number of empty cells accordingly
		if (puzzle[r][c].isEmpty() && val > 0) {
			numEmptyCell--;
		}
		else if (!puzzle[r][c].isEmpty() && val == 0) {
			numEmptyCell++;
		}
		
		puzzle[r][c].setVal(val);
	}
	
	public Cell getCell(int r, int c) { return puzzle[r][c]; }
	
	public int getVal(int r, int c) { return puzzle[r][c].getVal(); }
	
	public int getDifficulty() { return difficulty; }
	
	public int getNumEmptyCell() { return numEmptyCell; }
	
	public void printPuzzle() {
		for (int i = 0; i < 9; i++) {
			System.out.print("[");
			for (int j = 0; j < 8; j++) {
				System.out.print(puzzle[i][j].getVal() + ",");
			}
			System.out.println(puzzle[i][8].getVal() + "]");
		}
	}
	
}
