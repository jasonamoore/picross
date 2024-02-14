package puzzle;

public class Puzzle {

	public static final int UNCLEARED = -1;
	public static final int CLEARED = 0;
	public static final int FLAGGED = 1;
	public static final int MAYBE_CLEARED = 6;
	public static final int MAYBE_FLAGGED = 7;

	private int rows;
	private int columns;
	
	// the grid holding the puzzle solution (image)
	private boolean[][] solution;
	// grid holding the player's marks (unmarked, marked, flagged)
	private int[][] marks;
	
	// clue lists for rows and columns
	private int[][] rowClues;
	private int[][] colClues;
	
	private int correctCells;
	private int totalFilled;
	
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
		// initialize marks to -1
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < columns; c++)
				marks[r][c] = UNCLEARED;
		// meaning: it's not known if the puzzle is currently solved
		solvedStateDirty = true;
		// calculate clues
		calculateClues();
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public boolean validSpot(int row, int col) {
		return row >= 0 && row < rows && col >= 0 && col < columns;
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
			//if (num == 0)
			//	rowClues[r] = new int[] {0};
			//else
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
			//if (num == 0)
			//	colClues[c] = new int[] {0};
			//else
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
		// done with clues
		// count filled squares
		for (int i = 0; i < rowClues.length; i++)
			for (int j = 0; j < rowClues[i].length; j++)
				totalFilled += rowClues[i][j];
	}

	public int[] getClueRow(int row) {
		return rowClues[row];
	}
	
	public int[] getClueColumn(int col) {
		return colClues[col];
	}
	
	public int getLongestRowClueList() {
		int max = rowClues[0].length;
		for (int i = 1; i < rowClues.length; i++)
			max = Math.max(max, rowClues[i].length);
		return max;
	}
	
	public int getLongestColumnClueList() {
		int max = colClues[0].length;
		for (int i = 1; i < colClues.length; i++)
			max = Math.max(max, colClues[i].length);
		return max;
	}
	
	public int getTotalCellsInSolution() {
		return totalFilled;
	}

	public double getCorrectCells() {
		return correctCells;
	}
	
	public void markSpot(int row, int col, int flag) {
		int oldFlag = getMark(row, col);
		marks[row][col] = flag;
		if (oldFlag != CLEARED && flag == CLEARED && solution[row][col])
			correctCells++;
		else if (oldFlag == CLEARED && flag != CLEARED && solution[row][col])
			correctCells--;
		tryCrossingClueRow(row);
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
							|| chain != Math.abs(rowClues[r][num - 1]))) {
						solved = false;
						return;
					}
					chain = 0;
				}
			}
			if (chain != 0 && (num > rowClues[r].length
					|| chain != Math.abs(rowClues[r][num - 1]))
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
							|| chain != Math.abs(colClues[c][num - 1]))) {
						solved = false;
						return;
					}
					chain = 0;
				}
			}
			if (chain != 0 && (num > colClues[c].length
					|| chain != Math.abs(colClues[c][num - 1]))
					|| num < colClues[c].length) {
				solved = false;
				return;
			}
		}
		solved = true;
	}
	
	public void tryCrossingClueRow(int row) {
		int[] hints = rowClues[row];
		// go ahead and reset all hints to non-cleared
		for (int i = 0; i < hints.length; i++)
			hints[i] = Math.abs(hints[i]);
		// count number of blobs
		int numBlobs = 0;
		int lastMark = FLAGGED; // so a blob starting at the left is counted
		for (int c = 0; c < rows; c++) {
			int mark = marks[row][c];
			// went from non-blob to blob
			if (lastMark != mark && lastMark == FLAGGED)
				numBlobs++;
			lastMark = mark;
		}
		/* array of blob sizes - negative blob indicates "free space",
		 * i.e., a blob that has at least one open cell and therefore
		 * is not a complete/punctuated blob. we only try to cross
		 * hints for punctuated blobs, and use the free space to
		 * test how many p-blobs fit with a matching hint number
		 */
		int[] blobs = new int[numBlobs];
		int blobSize = 0;
		int blobNum = 0;
		lastMark = marks[row][0];
		for (int c = 0; c < rows; c++) {
			int mark = marks[row][c];
			if (lastMark == mark)
				blobSize++;
			else {
				blobs[blobNum++] = blobSize;
				blobSize = 1;
			}
			lastMark = mark;
		}
		// match hints to blobs
		for (int i = 0; i < hints.length; i++) {
			int hintMatches = 0;
			for (int j = 0; j < blobs.length; j++) {
				if (hints[i] > 0 && hints[i] == blobs[j]) {
					// blob matches to this hint. see if it fits in this spot
					
				}
			}
			if (hintMatches == 1) // if exactly one match
				hints[i] = -hints[i];
		}
	}
	
	private boolean matches(int[] hints, int[] blobs, int hintmin, int hintmax, int blobmin, int blobmax) {
		// match hints to blobs
		for (int i = hintmin; i < hintmax; i++) {
			for (int j = blobmin; j < blobmax; j++) {
				
			}
		}
		return false;
	}
	
	public void tryCrossingClueColumn(int col) {
	//	tryCrossing(colClues, col);
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
				grid[i][j] = Math.random() < 0.6;
		return grid;
	}
	
}
