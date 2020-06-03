package sudokuGenerator;

import java.util.*;

/**
 * Represents a missing value in a set.
 * 
 * @author Esther Lin
 */
public class MissingVal implements Comparable<MissingVal> {

	private int val;
	private Set set;	// todo
	private List<Cell> possibleCells;
	
	public MissingVal(Set set, int val) {
		this.val = val;
		this.set = set;
		possibleCells = new ArrayList<Cell>();
		for (Cell c : set.getEmptyCells()) {
			if (c.getFlag()[val]) {
				possibleCells.add(c);
			}
		}
	}
	
	/**
	 * Compares MissingVals with their number of possible Cells.
	 */
	@Override
	public int compareTo(MissingVal o) {
		return this.possibleCells.size() - o.possibleCells.size();
	}
	
	/**
	 * Returns the number of possible Cells.
	 * 
	 * @return the number of possible Cells
	 */
	public int getNumCandidates() { return possibleCells.size(); }
	
	public int getVal() { return val; }
	
	public List<Cell> getPossibleCells() { return possibleCells; }
	
}
