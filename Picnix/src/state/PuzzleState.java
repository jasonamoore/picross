package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import engine.Input;
import puzzle.Puzzle;

public class PuzzleState extends State {

	public static final int OFFSET_X = 75;
	public static final int OFFSET_Y = 75;
	public static final int GRID_SIZE = 25;
	
	public static final int UNSELECTED = -1;
	public int selRow;
	public int selCol;
	
	private Puzzle puzzle;
	
	public PuzzleState(Puzzle puzzle) {
		this.puzzle = puzzle;
	}

	@Override
	public void tick() {
		Input input = Input.getInstance();
		
		selRow = Math.floorDiv(input.getMouseY() - OFFSET_Y, GRID_SIZE);
		selCol = Math.floorDiv(input.getMouseX() - OFFSET_X, GRID_SIZE);
		if (selRow < 0 || selRow >= puzzle.rows
				|| selCol < 0 || selCol >= puzzle.columns) {
			selRow = UNSELECTED;
			selCol = UNSELECTED;
		}
		boolean hasSelection = selRow != UNSELECTED && selCol != UNSELECTED;
		if (input.hasReleasedMouseButton(Input.LEFT_CLICK)) {
			if (hasSelection)
				puzzle.toggleCleared(selRow, selCol);
			//puzzle.markSpot(selRow, selCol, Puzzle.CLEARED);
			input.consumeMouseButtonRelease(Input.LEFT_CLICK);
		}
		else if (input.hasReleasedMouseButton(Input.RIGHT_CLICK) && hasSelection) {
			if (hasSelection)
				puzzle.toggleFlagged(selRow, selCol);
			input.consumeMouseButtonRelease(Input.RIGHT_CLICK);
		}
		
		if (input.hasReleasedKey(KeyEvent.VK_R)) {
			puzzle = new Puzzle(Puzzle.genPuzzle(10, 10));
			input.consumeKeyRelease(KeyEvent.VK_R);
		}
	}
	
	@Override
	public void render(Graphics g) {
		g.translate(OFFSET_X, OFFSET_Y);
		for (int i = 0; i < puzzle.rows; i++) {
			for (int j = 0; j < puzzle.columns; j++) {
				if (puzzle.getMark(i, j) == Puzzle.CLEARED)
					if (puzzle.isSolved())
						g.setColor(Color.BLUE);
					else
						g.setColor(Color.ORANGE);
				else if (i == selRow && j == selCol)
					g.setColor(Color.GRAY);
				else
					g.setColor(Color.WHITE);
				g.fillRect(j * GRID_SIZE, i * GRID_SIZE, GRID_SIZE, GRID_SIZE);
				g.setColor(Color.BLACK);
				g.drawRect(j * GRID_SIZE, i * GRID_SIZE, GRID_SIZE, GRID_SIZE);
				if (puzzle.getMark(i, j) == Puzzle.FLAGGED) {
					g.drawLine(j * GRID_SIZE + 2, i * GRID_SIZE + 2,
								j * GRID_SIZE + GRID_SIZE - 2, i * GRID_SIZE + GRID_SIZE - 2);
					g.drawLine(j * GRID_SIZE + GRID_SIZE - 2, i * GRID_SIZE + 2,
							j * GRID_SIZE + 2, i * GRID_SIZE + GRID_SIZE - 2);
				}
			}
		}
		// draw clues
		g.setColor(Color.BLACK);
		for (int i = 0; i < puzzle.rows; i++) {
			int[] rowClues = puzzle.getClueRow(i);
			for (int j = 0; j < rowClues.length; j++) {
				g.drawString(Integer.toString(rowClues[rowClues.length - j - 1]), (j + 1) * -10, i * GRID_SIZE + 15);
			}
		}
		for (int i = 0; i < puzzle.columns; i++) {
			int[] colClues = puzzle.getClueColumn(i);
			for (int j = 0; j < colClues.length; j++) {
				g.drawString(Integer.toString(colClues[colClues.length - j - 1]), i * GRID_SIZE + 10, (j + 1) * -10);
			}
		}
		g.translate(-OFFSET_X, -OFFSET_Y);
		//puzzle.render(g);
	}
	
}
