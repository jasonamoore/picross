package picnix.puzzle;

public class Puzzle {

	public static final int EMPTY = -1;
	public static final int FILLED = 0;
	public static final int FLAGGED = 1;
	public static final int MAYBE_FILLED = 6;
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

	// counters for various info about cells and user marks
	private int correctFilledCells;
	private int filledCells;
	private int solutionFilledCells;
	
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
				marks[r][c] = EMPTY;
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
				solutionFilledCells += rowClues[i][j];
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
	
	public int getFilledCellsInSolution() {
		return solutionFilledCells;
	}

	public double getIncorrectCells() {
		return filledCells - correctFilledCells;
	}
	
	public double getCorrectCells() {
		return correctFilledCells;
	}

	public int getRemainingFillCount() {
		return solutionFilledCells - filledCells;
	}

	public int[] getHorizontalBlob(int row, int col, int drawMode) {
		if (!validSpot(row, col) || getMark(row, col) != drawMode)
			return null;
		int blobStart = col;
		for (int c = col; c >= 0; c--, blobStart--) {
			if (getMark(row, c) != drawMode)
				break;
		}
		blobStart++;
		int blobSize = col - blobStart;
		for (int c = col; c < columns; c++, blobSize++) {
			if (getMark(row, c) != drawMode)
				break;
		}
		return new int[] {blobStart, blobSize};
	}
	
	public int[] getVerticalBlob(int row, int col, int drawMode) {
		if (!validSpot(row, col) || getMark(row, col) != drawMode)
			return null;
		int blobStart = row;
		for (int r = row; r >= 0; r--, blobStart--) {
			if (getMark(r, col) != drawMode)
				break;
		}
		blobStart++;
		int blobSize = row - blobStart;
		for (int r = row; r < rows; r++, blobSize++) {
			if (getMark(r, col) != drawMode)
				break;
		}
		return new int[] {blobStart, blobSize};
	}
	
	public boolean markSpot(int row, int col, int flag) {
		boolean mistake = false;
		int oldMark = getMark(row, col);
		marks[row][col] = flag;
		if (oldMark != FILLED && flag == FILLED) {
			filledCells++;
			if (solution[row][col])
				correctFilledCells++;
			else
				mistake = true;
		}
		else if (oldMark == FILLED && flag != FILLED) {
			filledCells--;
			if (solution[row][col])
				correctFilledCells--;
		}
		tryCrossingRowClues(row);
		tryCrossingColumnClues(col);
		solvedStateDirty = true;
		return mistake;
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
				if (marks[r][c] == FILLED) {
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
				if (marks[r][c] == FILLED) {
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

	/**
	 * Auto-crosses clues for the given row.
	 * @param row The row whose clues to cross.
	 * @see #tryCrossing(boolean, int)
	 */
	public void tryCrossingRowClues(int row) {
		tryCrossing(true, row);
	}
	
	/**
	 * Auto-crosses clues for the given column.
	 * @param row The column whose clues to cross.
	 * @see #tryCrossing(boolean, int)
	 */
	public void tryCrossingColumnClues(int col) {
		tryCrossing(false, col);
	}
	
	/**
	 * Auto-crosses clues for a given row or column.
	 * @param rowMode If true, crosses a row. Otherwise a column.
	 * @param pos The number of the row or column to try crossing.
	 */
	public void tryCrossing(boolean rowMode, int pos) {
		int[] clues = rowMode ? rowClues[pos] : colClues[pos];
		// go ahead and reset all clues to non-cleared
		for (int i = 0; i < clues.length; i++)
			clues[i] = Math.abs(clues[i]);
		// count number of blobs
		int numBlobs = 0;
		int lastMark = FLAGGED; // so a blob starting at the left is counted
		int length = rowMode ? columns : rows;
		for (int g = 0; g < length; g++) {
			int mark = rowMode ? marks[pos][g] : marks[g][pos];
			// went from non-blob to blob
			if (lastMark != mark && lastMark == FLAGGED)
				numBlobs++;
			lastMark = mark;
		}
		/* array of blob sizes - negative blob indicates "free space",
		* i.e., a blob that has at least one open cell and therefore
		* is not a complete/punctuated blob. we only try to cross
		* clues for punctuated blobs, and use the free space to
		* test how many p-blobs fit with a matching clue number
		*/
		int[] blobs = new int[numBlobs];
		int blobNum = -1;
		int blobSize = 0;
		boolean punctuated = true;
		lastMark = FLAGGED;
		for (int g = 0; g < length; g++) {
			int mark = rowMode ? marks[pos][g] : marks[g][pos];
			// went from non-blob to blob
			if (lastMark != mark && lastMark == FLAGGED) {
				// reset everything
				blobNum++;
				blobSize = 0;
				punctuated = true;
			}
			// if a blob cell
			if (mark != FLAGGED) {
				if (mark != FILLED) // if any in this blob is uncleared...
					punctuated = false;
				// another cell of blob reached
				blobSize++;
				// if an unpunctuated blob, mark as negative
				blobs[blobNum] = punctuated ? blobSize : -blobSize;
			}
			lastMark = mark;
		}
		// keeps track of which clue a blob has been matched to
		// if an already matched blob tries to match another clue,
		// then unmatch the first clue and prevent further matches
		final int NEVER_MATCHED = -1;
		final int MATCH_BANNED = -2;
		int[] blobClueMatch = new int[numBlobs];
		for (int i = 0; i < numBlobs; i++)
			blobClueMatch[i] = NEVER_MATCHED;
		// match clues to blobs
		for (int i = 0; i < clues.length; i++) {
			int matchedBlob = NEVER_MATCHED;
			for (int j = 0; j < blobs.length; j++) {
				if (clues[i] == blobs[j]) {	// blob matches to this clue. see if it fits in this spot
					boolean fitsLeft = matches(clues, blobs, 0, i, 0, j);
					boolean fitsRight = matches(clues, blobs, i + 1, clues.length, j + 1, blobs.length);
					// the blob is a match for this clue
					if (fitsLeft && fitsRight) {
						if (blobClueMatch[j] != NEVER_MATCHED) { // this blob has matched more than one clue
							if (blobClueMatch[j] != MATCH_BANNED) { // need to undo & ban match
								int oldIndex = blobClueMatch[j]; // the previous matched clue
								clues[oldIndex] = Math.abs(clues[oldIndex]); // uncross the clue (ambiguous!)
								blobClueMatch[j] = MATCH_BANNED; // ban this blob from matches
							}
							matchedBlob = MATCH_BANNED; // this clue can't be crossed - ambiguous
						}
						else { // blob's first match
							blobClueMatch[j] = i;
							matchedBlob = matchedBlob == NEVER_MATCHED ? j : MATCH_BANNED;
						}
					}
				}
			}
			if (matchedBlob > NEVER_MATCHED)// if exactly one match found:
				clues[i] = -clues[i]; // cross out the clue
		}
	}
	
	/**
	 * This is a work of art.
	 * @param clues
	 * @param blobs
	 * @param clueLow
	 * @param clueHigh
	 * @param blobLow
	 * @param blobHigh
	 * @return A work of art.
	 */
	private boolean matches(int[] clues, int[] blobs, int clueLow, int clueHigh, int blobLow, int blobHigh) {
		/**
		 * Based on the clue list, try all blobs
		 * that match a given clue to see if any match.
		 * The result is true if any match, otherwise false.
		 * For each attempted match, recurse to test.
		 * At a base case, if there are only unpunctuated
		 * blobs, then we only need to see if the clues
		 * can fit within this free space blob.
		 * Also, if there are no clues, the match is
		 * trivially successful.
		 */
		if (clueHigh - clueLow < 1) { // no clues to fit!
			for (int i = blobLow; i < blobHigh; i++)
				if (blobs[i] > 0) // if there are any punctuated blobs
					return false; // then this must not be a match
			return true; // otherwise, no clues means nothing to match
		}
		else if (blobHigh - blobLow < 1) // clues, but no blobs
			return false;
		int matchingBlobs = 0;
		// match clues to blobs
		for (int i = clueLow; i < clueHigh; i++) {
			for (int j = blobLow; j < blobHigh; j++) {
				// blob matches to this clue, see if it fits in this spot
				if (Math.abs(clues[i]) == blobs[j]) {
					matchingBlobs++;
					boolean matchLeft = matches(clues, blobs, clueLow, i, blobLow, j);
					boolean matchRight = matches(clues, blobs, i + 1, clueHigh, j + 1, blobHigh);
					if (matchLeft && matchRight)
						return true;
				}
			}
		}
		// blobs matched clue, but didn't fit
		if (matchingBlobs > 0)
			return false;
		else { // no more matching blobs - just fit clues within space
			int curClue = clueLow; // the current clue we're fitting clues into a blob
			for (int i = blobLow; i < blobHigh; i++) {
				if (blobs[i] > 0) // not a free space blob
					continue;
				int blobBuf = Math.abs(blobs[i]); // amount of space
				while (blobBuf >= clues[curClue]) { // at least enough room for a clue
					// subtract room taken for this clue, plus one for punctuation
					blobBuf -= clues[curClue++] + 1; // (also increment clue index)
					if (curClue >= clueHigh) // fit all the clues needed
						return true;
				}
			}
			// couldn't fit into blobs
			return false;
		}
	}

	/**
	 * Creates a random puzzle with given width and height.
	 * @param width Width of the puzzle to generate.
	 * @param height Height of the puzzle to generate.
	 * @return
	 */
	public static Puzzle genPuzzle(int width, int height) {
		boolean[][] grid = new boolean[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				grid[i][j] = Math.random() < 0.6;
		return new Puzzle(grid);
	}
	
}
