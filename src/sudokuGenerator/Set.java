package sudokuGenerator;

import java.util.*;

/**
 * Represents a set in a sudoku puzzle. A set is one of the following:
 * 
 *   1. A row in the puzzle
 *   2. A column in the puzzle
 *   3. A 3 * 3 block in the puzzle
 *   
 * @author Esther Lin
 */
public class Set {

	private int start_row;
	private int start_col;
	private int end_row;
	private int end_col;
	private boolean[] missingVal;		// the missing values in the set
	private List<Cell> emptyCells;	// the empty positions in the set
	
	/**
	 * Generates a set with the given indices.
	 * 
	 * @param sr the start row index
	 * @param sc the start column index
	 * @param er the end row index
	 * @param ec the end column index
	 */
	public Set(int sr, int sc, int er, int ec) {
		start_row = sr;
		start_col = sc;
		end_row = er;
		end_col = ec;
		missingVal = new boolean[10];
		emptyCells = new ArrayList<Cell>();
	}
	
	/**
	 * Clones a set with the given set.
	 * 
	 * @param o the set to be cloned
	 */
	public Set(Set o) {
		this(o.start_row, o.start_col, o.end_row, o.end_col);
	}
	
	/**
	 * Updates the missing values and the empty positions in the set.
	 * 
	 * @param puzzle the 2D array of positions representing the puzzle
	 */
	public void updateSet(Cell[][] puzzle) {
		Arrays.fill(missingVal, true);
		emptyCells.clear();
		for (int i = start_row; i <= end_row; i++) {
			for (int j = start_col; j <= end_col; j++) {
				if (puzzle[i][j].isEmpty()) {
					emptyCells.add(puzzle[i][j]);
				}
				else {
					missingVal[puzzle[i][j].getVal()] = false;
				}
			}
		}
	}
	
	public boolean[] getMissingVal() { return missingVal; }
	
	public List<Cell> getEmptyCells() { return emptyCells; }
	
}
