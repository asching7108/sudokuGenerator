package sudokuGenerator;

import java.util.Arrays;

/**
 * Represents a cell in a SudokuPuzzle.
 * 
 * @author Esther Lin
 */
public class Cell implements Comparable<Cell> {

	private int row;			// the row index in the puzzle
	private int col;			// the column index in the puzzle
	private int defVal;			// the default value of the cell, immutable
	private int val;			// the value of the cell
	private int numCandidates;	// the number of valid candidates
	private boolean[] flag;		// the array of valid candidates
	
	/**
	 * Generates a cell with the given indices and value.
	 * 
	 * @param r the row index
	 * @param c the column index
	 * @param defVal the default value of the cell
	 */
	public Cell(int row, int col, int defVal) {
		this.row = row;
		this.col = col;
		this.defVal = defVal;
		val = defVal;
		numCandidates = 0;
		flag = new boolean[10];
	}
	
	/**
	 * Clones a cell with the given cell.
	 * 
	 * @param o the cell to be cloned
	 */
	public Cell(Cell o) {
		this.row = o.row;
		this.col = o.col;
		this.defVal = o.defVal;
		val = o.val;
		numCandidates = 0;
		flag = new boolean[10];
	}
	
	/**
	 * Sets the value with the given value.
	 * 
	 * @param val the value
	 */
	public void setVal(int val) { this.val = val; }
	
	/**
	 * Updates the flag and numCandidates with the given puzzle.
	 * 
	 * @param puzzle the 2D array of cells representing the puzzle
	 */
	public void updateFlag(Cell[][] puzzle) {
		Arrays.fill(flag, true);
		for (int k = 0; k < 9; k++) {
			// validate row
			if (puzzle[row][k].getVal() > 0) flag[puzzle[row][k].getVal()] = false;
			
			// validate column
			if (puzzle[k][col].getVal() > 0) flag[puzzle[k][col].getVal()] = false;
			
			// validate block
			int r = row / 3 * 3 + k / 3;
			int c = col / 3 * 3 + k % 3;
			if (puzzle[r][c].getVal() > 0) flag[puzzle[r][c].getVal()] = false;
		}
		
		// updates numCandidates
		numCandidates = 0;
		for (int k = 1; k <= 9; k++) {
			if (flag[k]) numCandidates++;
		}
	}
	
	/**
	 * Returns true if the cell has default value.
	 * 
	 * @return true if defVal is 0, otherwise returns false
	 */
	public boolean hasDefVal() { return defVal != 0; }
	
	/**
	 * Returns if the cell is empty.
	 * 
	 * @return true if val is 0, otherwise returns false
	 */
	public boolean isEmpty() { return val == 0; }

	/**
	 * Compares Cells with their number of candidates.
	 */
	@Override
	public int compareTo(Cell o) {
		return this.numCandidates - o.numCandidates;
	}
	
	public int getRow() { return row; }
	
	public int getCol() { return col; }
	
	public int getVal() { return val; }
	
	public int getNumCandidates() { return numCandidates; }
	
	public boolean[] getFlag() { return flag; }
	
}
