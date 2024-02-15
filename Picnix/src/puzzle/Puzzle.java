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
		int blobNum = -1;
		int blobSize = 0;
		boolean punctuated = true;
		lastMark = FLAGGED;
		for (int c = 0; c < rows; c++) {
			int mark = marks[row][c];
			// went from non-blob to blob
			if (lastMark != mark && lastMark == FLAGGED) {
				// reset everything
				blobNum++;
				blobSize = 0;
				punctuated = true;
			}
			// if a blob cell
			if (mark != FLAGGED) {
				if (mark == UNCLEARED) // if any in this blob is uncleared...
					punctuated = false;
				// another cell of blob reached
				blobSize++;
				// if an unpunctuated blob, mark as negative
				blobs[blobNum] = punctuated ? blobSize : -blobSize;
			}
			lastMark = mark;
		}
		// keeps track of which hint a blob has been matched to
		// if an already matched blob tries to match another hint,
		// then unmatch the first hint and prevent further matches
		final int NEVER_MATCHED = -1;
		final int MATCH_BANNED = -2;
		int[] blobHintMatch = new int[numBlobs];
		for (int i = 0; i < numBlobs; i++)
			blobHintMatch[i] = NEVER_MATCHED;
		// match hints to blobs
		for (int i = 0; i < hints.length; i++) {
			int matchedBlob = NEVER_MATCHED;
			for (int j = 0; j < blobs.length; j++) {
				if (hints[i] == blobs[j]) {	// blob matches to this hint. see if it fits in this spot
					boolean fitsLeft = matches(hints, blobs, 0, i, 0, j);
					boolean fitsRight = matches(hints, blobs, i + 1, hints.length, j + 1, blobs.length);
					if (fitsLeft && fitsRight) {
						if (blobHintMatch[j] != NEVER_MATCHED) { // this blob has matched more than one hint
							if (blobHintMatch[j] != MATCH_BANNED) { // need to undo & ban match
								int oldIndex = blobHintMatch[j]; // the previous matched hint
								hints[oldIndex] = Math.abs(hints[oldIndex]); // uncross the hint (ambiguous!)
								blobHintMatch[j] = MATCH_BANNED; // ban this blob from matches
							}
							matchedBlob = MATCH_BANNED; // this hint can't be crossed - ambiguous
							//break; // force quit hint matching
						}
						else { // blob's first match
							blobHintMatch[j] = i;
							matchedBlob = matchedBlob == NEVER_MATCHED ? j : MATCH_BANNED;
						}
					}
				}
			}
			if (matchedBlob > NEVER_MATCHED)// if exactly one match found:
				hints[i] = -hints[i]; // cross out the hint
		}
	}
	
	private boolean matches(int[] hints, int[] blobs, int hintLow, int hintHigh, int blobLow, int blobHigh) {
		/**
		 * Based on the hint list, try all blobs
		 * that match a given hint to see if any match.
		 * The result is true if any match, otherwise false.
		 * For each attempted match, recurse to test.
		 * At a base case, if there are only unpunctuated
		 * blobs, then we only need to see if the hints
		 * can fit within this free space blob.
		 * Also, if there are no hints, the match is
		 * trivially successful.
		 */
		if (hintHigh - hintLow < 1) { // no hints to fit!
			for (int i = blobLow; i < blobHigh; i++)
				if (blobs[i] > 0) // if there are any punctuated blobs
					return false; // then this must not be a match
			return true; // otherwise, no hints means nothing to match
		}
		else if (blobHigh - blobLow < 1) // hints, but no blobs
			return false;
		int matchingBlobs = 0;
		// match hints to blobs
		for (int i = hintLow; i < hintHigh; i++) {
			for (int j = blobLow; j < blobHigh; j++) {
				// blob matches to this hint, see if it fits in this spot
				if (Math.abs(hints[i]) == blobs[j]) {
					matchingBlobs++;
					boolean matchLeft = matches(hints, blobs, hintLow, i, blobLow, j);
					boolean matchRight = matches(hints, blobs, i + 1, hintHigh, j + 1, blobHigh);
					if (matchLeft && matchRight)
						return true;
				}
			}
		}
		// blobs matched hint, but didn't fit
		if (matchingBlobs > 0)
			return false;
		else { // no more matching blobs - just fit hints within space
			int curHint = hintLow; // the current hint we're fitting hints into a blob
			for (int i = blobLow; i < blobHigh; i++) {
				if (blobs[i] > 0) // not a free space blob
					continue;
				int blobBuf = Math.abs(blobs[i]); // amount of space
				while (blobBuf >= hints[curHint]) { // at least enough room for a hint
					// subtract room taken for this hint, plus one for punctuation
					blobBuf -= hints[curHint++] + 1; // (also increment hint index)
					if (curHint >= hintHigh) // fit all the hints needed
						return true;
				}
			}
			// couldn't fit into blobs
			return false;
		}
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
