package sudokuGenerator;

import java.util.*;

public class TestPuzzleGenerator {

	public static void main(String[] args) {
		PuzzleGenerator pg = new PuzzleGenerator(19870511);

		List<int[][]> boards = pg.generateBoards(10);
		
		// pg.printBoards(boards);
		
		List<SudokuPuzzle> puzzles = new ArrayList<SudokuPuzzle>();
		
		for (int i = 0; i < boards.size(); i++) {
			SudokuPuzzle puzzle = pg.generatePuzzleByLevel(boards.get(i), 6);
			puzzles.add(puzzle);
			System.out.println(
				"Puzzle #" + (i + 1) + " generated. Difficulty " + puzzle.getDifficulty()
			);
		}
		
		// pg.printPuzzles(puzzles);
	}

}
