package puzzle;

import java.awt.Graphics;

public class Puzzle {

	public static final int UNCLEARED = 0;
	public static final int CLEARED = 1;
	public static final int FLAGGED = 2;

	public int rows;
	public int columns;
	
	// the grid holding the puzzle solution (image)
	private boolean[][] solution;
	// grid holding the player's marks (unmarked, marked, flagged)
	private int[][] marks;
	
	// clue lists for rows and columns
	private int[][] rowClues;
	private int[][] colClues;
	
	// maintains whether the user's marks are a solution for the puzzle
	private boolean solved;
	// indicates whether the "solved" boolean is up-to-date
	// i.e.: (dirty = true when the user makes a mark)
	private boolean solvedStateDirty;
	
	/**
	 * Creates a puzzle with the given 2D boolean array
	 * representing the cleared grids in the puzzle's solution.
	 * @param sol A 2D boolean array, the puzzle's solution.
	 */
	public Puzzle(boolean[][] sol) {
		solution = sol.clone();
		rows = solution.length;
		columns = solution[0].length;
		marks = new int[rows][columns];
		// meaning: it's not known if the puzzle is currently solved
		solvedStateDirty = true;
		// calculate clues
		calculateClues();
	}
	
	private void calculateClues() {
		// initialize clue arrays
		rowClues = new int[rows][];
		colClues = new int[columns][];
		// determine number of clues for each row
		for (int r = 0; r < rows; r++) {
			int num = 0;
			boolean chain = false;
			for (int c = 0; c < columns; c++) {
				if (solution[r][c]) {
					if (!chain)
						num++;
					chain = true;
				}
				else
					chain = false;
			}
			if (num == 0)
				rowClues[r] = new int[] {0};
			else
				rowClues[r] = new int[num];
		}
		// determine number of clues for each col
		for (int c = 0; c < columns; c++) {
			int num = 0;
			boolean chain = false;
			for (int r = 0; r < rows; r++) {
				if (solution[r][c]) {
					if (!chain)
						num++;
					chain = true;
				}
				else
					chain = false;
			}
			if (num == 0)
				colClues[c] = new int[] {0};
			else
				colClues[c] = new int[num];
		}
		// set clue numbers for each row
		for (int r = 0; r < rows; r++) {
			int num = 0;
			int chain = 0;
			for (int c = 0; c < columns; c++) {
				if (solution[r][c]) {
					if (chain == 0)
						num++;
					chain++;
				}
				else {
					if (chain != 0)
						rowClues[r][num - 1] = chain;
					chain = 0;
				}
			}
			if (chain != 0)
				rowClues[r][num - 1] = chain;
		}
		// set clue numbers for each col
		for (int c = 0; c < columns; c++) {
			int num = 0;
			int chain = 0;
			for (int r = 0; r < rows; r++) {
				if (solution[r][c]) {
					if (chain == 0)
						num++;
					chain++;
				}
				else {
					if (chain != 0)
						colClues[c][num - 1] = chain;
					chain = 0;
				}
			}
			if (chain != 0)
				colClues[c][num - 1] = chain;
		}
		// done
	}

	public int[] getClueRow(int row) {
		return rowClues[row];
	}
	
	public int[] getClueColumn(int col) {
		return colClues[col];
	}
	
	public void markSpot(int row, int col, int flag) {
		marks[row][col] = flag;
		solvedStateDirty = true;
	}
	
	public void toggleCleared(int row, int col) {
		if (marks[row][col] == CLEARED)
			markSpot(row, col, UNCLEARED);
		else
			markSpot(row, col, CLEARED);
	}
	
	public void toggleFlagged(int row, int col) {
		if (marks[row][col] == FLAGGED)
			markSpot(row, col, UNCLEARED);
		else
			markSpot(row, col, FLAGGED);
	}
	
	public int getMark(int row, int col) {
		return marks[row][col];
	}
	
	public boolean isSolved() {
		if (solvedStateDirty)
			updateSolved();
		return solved;
	}
	
	private void updateSolved() {
		// updated!
		solvedStateDirty = false;
		// check that row hints match user marks
		for (int r = 0; r < rows; r++) {
			int num = 0;
			int chain = 0;
			for (int c = 0; c < columns; c++) {
				if (marks[r][c] == CLEARED) {
					if (chain == 0)
						num++;
					chain++;
				}
				else {
					if (chain != 0 && (num > rowClues[r].length
							|| chain != rowClues[r][num - 1])) {
						solved = false;
						return;
					}
					chain = 0;
				}
			}
			if (chain != 0 && (num > rowClues[r].length
					|| chain != rowClues[r][num - 1])
					|| num < rowClues[r].length) {
				solved = false;
				return;
			}
		}
		// check that column hints match user marks
		for (int c = 0; c < columns; c++) {
			int num = 0;
			int chain = 0;
			for (int r = 0; r < rows; r++) {
				if (marks[r][c] == CLEARED) {
					if (chain == 0)
						num++;
					chain++;
				}
				else {
					if (chain != 0 && (num > colClues[c].length
							|| chain != colClues[c][num - 1])) {
						solved = false;
						return;
					}
					chain = 0;
				}
			}
			if (chain != 0 && (num > colClues[c].length
					|| chain != colClues[c][num - 1])
					|| num < colClues[c].length) {
				solved = false;
				return;
			}
		}
		solved = true;
	}
	
	// rendering
	public void render(Graphics g) {
	}

	/**
	 * Creates a random puzzle with given width and height.
	 * @param width Width of the puzzle to generate.
	 * @param height Height of the puzzle to generate.
	 * @return
	 */
	public static boolean[][] genPuzzle(int width, int height) {
		boolean[][] grid = new boolean[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				grid[i][j] = Math.random() < 0.5;
		return grid;
	}
	
}
