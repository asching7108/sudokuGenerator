package sudokuGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgreSqlConnection {

	static final String DB_URL = "jdbc:postgresql://localhost:5432/sudoku";
	static final String DB_USER = "esther_lin";
	static final String DB_PASSWORD = null;
	static final String DB_NAME = "sudoku";
	
	static class Queries {
		
		// get the maximal puzzle_id in the database
		static String getMaxPuzzleId = "SELECT id from " + DB_NAME + ".public.puzzles " +
			"ORDER BY id DESC LIMIT 1";
		
		// insert data into table: puzzles
		static String insertPuzzle = "INSERT INTO " + DB_NAME + ".public.puzzles " + 
			"(level, difficulty, num_empty_cells) " +
			"VALUES(?, ?, ?)";
		
		// insert data into table: puzzle_cells
		static String insertPuzzleCells = "INSERT INTO " + DB_NAME + ".public.puzzle_cells " + 
			"(cell_id, puzzle_id, is_default, value) " +
			"VALUES(?, ?, ?, ?)";

	}
	
	/**
	 * Returns the given number of randomly generated 9*9 sudoku boards.
	 * 
	 * @param pg PuzzleGenerator object
	 * @param num the number of sudoku boards to generate
	 * @return a list containing the given number of randomly generated sudoku boards
	 */
	static List<int[][]> prepareBoards(PuzzleGenerator pg, int num) {
		System.out.println("Generating boards.");
		List<int[][]> boards = pg.generateBoards(num);
		System.out.println(boards.size() + " boards generated.");
		return boards;
	}
	
	/**
	 * Returns a list of sudoku puzzles generated from the given board with the maximal 
	 * difficulty in the fixed amount of rounds.
	 * 
	 * @param pg PuzzleGenerator object
	 * @param boards a list of sudoku boards
	 * @return a list of sudoku puzzles randomly generated from the given list of boards
	 */
	static List<SudokuPuzzle> generatePuzzles(PuzzleGenerator pg, List<int[][]> boards) {
		int[] lvCount = new int[6];
		List<SudokuPuzzle> puzzles = new ArrayList<SudokuPuzzle>();
		
		System.out.println("Generating puzzles.");
		for (int i = 0; i < boards.size(); i++) {
			SudokuPuzzle sp = pg.generatePuzzle(boards.get(i));
			// System.out.println("Puzzle #" + (i + 1) + " generated.\tDifficulty " + sp.getDifficulty());
			lvCount[sp.getLevel() - 1]++;
			puzzles.add(sp);
		}

		System.out.println(puzzles.size() + " puzzles generated.");
		for (int i = 0; i < lvCount.length; i++) {
			System.out.println(
				"Level " + (i + 1) + " puzzles: " + lvCount[i] + "\t" + 
				String.format("%.0f", (double) lvCount[i] / puzzles.size() * 100) + "%"
			);
		}
		return puzzles;
	}
	
	/**
	 * Returns a list of sudoku puzzles generated from the given board with the given 
	 * difficulty. If a valid puzzle cannot be generated in the fixed amount of rounds.
	 * 
	 * @param pg PuzzleGenerator object
	 * @param boards a list of sudoku boards
	 * @return a list of sudoku puzzles randomly generated from the given list of boards
	 */
	static List<SudokuPuzzle> generatePuzzlesByLevel(PuzzleGenerator pg, List<int[][]> boards, int level) {
		List<SudokuPuzzle> puzzles = new ArrayList<SudokuPuzzle>();

		System.out.println("Generating puzzles of level " + level + ".");
		for (int i = 0; i < boards.size(); i++) {
			SudokuPuzzle sp = pg.generatePuzzleByLevel(boards.get(i), level);
			if (sp == null) {
				System.out.println("Puzzle #" + (i + 1) + " not generated.");
			}
			else {
				System.out.println("Puzzle #" + (i + 1) + " generated.\tDifficulty " + sp.getDifficulty());
				puzzles.add(sp);	
			}
		}

		System.out.println(puzzles.size() + " puzzles of level " + level + " generated.");
		return puzzles;
	}
	
	/**
	 * Returns the maximal puzzle_id in the database.
	 * 
	 * @param conn the connection
	 * @return the maximal puzzle_id
	 * @throws SQLException
	 */
	static int getPrevPuzzleId(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			ResultSet resultSet = stmt.executeQuery(Queries.getMaxPuzzleId);
			if (resultSet.next()) {
				return resultSet.getInt("id");
			}
			return 0;
		} catch (SQLException e) { throw e; }
	}

	/**
	 * Inserts puzzles into the database.
	 * 
	 * @param conn the connection
	 * @param puzzles a list of puzzles to insert
	 * @throws SQLException
	 */
	static void insertPuzzles(Connection conn, List<SudokuPuzzle> puzzles) throws SQLException {
		try (
			PreparedStatement stmt1 = conn.prepareStatement(Queries.insertPuzzle);
			PreparedStatement stmt2 = conn.prepareStatement(Queries.insertPuzzleCells);
		) {
			// gets the current max puzzle id
			int prevPuzzleId = getPrevPuzzleId(conn);
						
			// prepares statements in batch
			conn.setAutoCommit(false);
			for (int i = 0; i < puzzles.size(); i++) {
				SudokuPuzzle sp = puzzles.get(i);
				stmt1.setInt(1, sp.getLevel());				// level
				stmt1.setInt(2, sp.getDifficulty());		// difficulty
				stmt1.setInt(3, sp.getDifficulty() % 100);	// the number of empty cells
				stmt1.addBatch();
								
				for (int j = 0; j < 81; j++) {
					Cell cell = sp.getCell(j);
					stmt2.setInt(1, j);						// cell_id
					stmt2.setInt(2, prevPuzzleId + i + 1);	// puzzle_id
					stmt2.setBoolean(3, !cell.isEmpty());	// is_default
					stmt2.setInt(4, cell.getSltVal());		// value
					stmt2.addBatch();
				}
			}
			
			// inserts data
			System.out.println("Inserting data into puzzles.");
			stmt1.executeBatch();
			System.out.println("Data inserted into puzzles.");
			System.out.println("Inserting data into puzzle_cells.");
			stmt2.executeBatch();
			System.out.println("Data inserted into puzzle_cells.");
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) { throw e; }
	}
	
	public static void main(String[] args) {
		// prepares boards
		PuzzleGenerator pg = new PuzzleGenerator(20190628);
		List<int[][]> boards = prepareBoards(pg, 5000);
		// List<SudokuPuzzle> puzzles = generatePuzzles(pg, boards);
		
		// generates 500 puzzles of each level from the boards
		List<SudokuPuzzle> puzzles = new ArrayList<SudokuPuzzle>();
		for (int i = 1; i <= 6; i++) {
			puzzles.addAll(generatePuzzlesByLevel(pg, boards.subList(0, 500), i));
		}
		
		// inserts puzzles
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			System.out.println("Connected to PostgreSQL database.");
			insertPuzzles(conn, puzzles);
		} catch (SQLException e) { e.printStackTrace(); }
	}

}
