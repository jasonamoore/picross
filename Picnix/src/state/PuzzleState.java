package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Engine;
import engine.Input;
import picnic.Food;
import puzzle.Puzzle;
import resource.bank.ImageBank;

public class PuzzleState extends State {

	public static final int GRID_SIZE = 16;
	public static final int TILE_WIDTH = Engine.SCREEN_WIDTH / GRID_SIZE;
	public static final int TILE_HEIGHT = Engine.SCREEN_HEIGHT/ GRID_SIZE;
	
	private int startRow;
	private int startCol;
	
	public static final int UNSELECTED = -1;
	public int selRow;
	public int selCol;
	
	private Puzzle puzzle;

	public static int GRASS = 0;
	public static int PINK = 1;
	public static int RED = 2;
	public static int WHITE = 3;
	
	private int tiles[][];
	
	private ArrayList<Food> food;
	
	public PuzzleState(Puzzle puzzle) {
		tiles = new int[TILE_HEIGHT][TILE_WIDTH];
		food = new ArrayList<Food>();
		loadPuzzle(puzzle);
	}

	private int startX() {
		return startCol * GRID_SIZE;
	}
	
	private int startY() {
		return startRow * GRID_SIZE;
	}
	
	private int getXPosFromCol(int col) {
		return (startCol + selCol) * GRID_SIZE;
	}
	
	private int getYPosFromRow(int row) {
		return (startRow + selRow) * GRID_SIZE;
	}
	
	private void loadPuzzle(Puzzle puzzle) {
		this.puzzle = puzzle;
		startRow = (TILE_HEIGHT - puzzle.getRows()) / 2;
		startCol = (TILE_WIDTH - puzzle.getColumns()) / 2;
		for (int r = 0; r < puzzle.getRows(); r++) {
			int n = r % 2 + 1;
			for (int c = 0; c < puzzle.getColumns(); c++) {
				int i = (n % 2) * (r % 2 == 0 ? 1 : 2) + 1;
				tiles[r + startRow][c + startCol] = i;
				n++;
			}
		}
	}
	
	private boolean inPuzzleBounds(int row, int col) {
		return 	row >= startRow && row < startRow + puzzle.getRows() &&
				col >= startCol && col < startCol + puzzle.getColumns();
	}
	
	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void tick() {
		Input input = Input.getInstance();
		
		selRow = Math.floorDiv(input.getMouseY() - startY(), GRID_SIZE);
		selCol = Math.floorDiv(input.getMouseX() - startX(), GRID_SIZE);
		if (selRow < 0 || selRow >= puzzle.getRows())
			selRow = UNSELECTED;
		if (selCol < 0 || selCol >= puzzle.getColumns())
			selCol = UNSELECTED;
		boolean hasSelection = selRow != UNSELECTED && selCol != UNSELECTED;
		if (input.hasReleasedMouseButton(Input.LEFT_CLICK)) {
			if (hasSelection)
				puzzle.toggleCleared(selRow, selCol);
			//puzzle.markSpot(selRow, selCol, Puzzle.CLEARED);
			input.consumeMouseButtonRelease(Input.LEFT_CLICK);
			food.add(new Food(getXPosFromCol(selCol), getYPosFromRow(selRow)));
		}
		else if (input.hasReleasedMouseButton(Input.RIGHT_CLICK)) {
			if (hasSelection)
				puzzle.toggleFlagged(selRow, selCol);
			input.consumeMouseButtonRelease(Input.RIGHT_CLICK);
		}
		
		if (input.hasReleasedKey(KeyEvent.VK_R)) {
			loadPuzzle(new Puzzle(Puzzle.genPuzzle(10, 10)));
			input.consumeKeyRelease(KeyEvent.VK_R);
		}
	}
	
	@Override
	public void render(Graphics g) {
		for (int r = 0; r < TILE_HEIGHT; r++) {
			for (int c = 0; c < TILE_WIDTH; c++) {
				int tile = tiles[r][c];
				BufferedImage spr = tile == GRASS ? ImageBank.grass :
									tile == RED ? ImageBank.red	:
									tile == PINK ? ImageBank.pink :
												ImageBank.white;
				g.drawImage(spr, c * GRID_SIZE, r * GRID_SIZE, null);
				if (inPuzzleBounds(r, c)) {
					int prow = r - startRow;
					int pcol = c - startCol;
					if (puzzle.getMark(prow, pcol) == Puzzle.CLEARED) {
						if (puzzle.isSolved())
							g.setColor(Color.BLUE);
						else
							g.setColor(Color.ORANGE);
						g.fillRect(c * GRID_SIZE, r * GRID_SIZE, GRID_SIZE, GRID_SIZE);
					}
					else if (puzzle.getMark(prow, pcol) == Puzzle.FLAGGED) {
						g.setColor(Color.BLACK);
						g.drawLine(c * GRID_SIZE + 2, r * GRID_SIZE + 2,
									c * GRID_SIZE + GRID_SIZE - 2, r * GRID_SIZE + GRID_SIZE - 2);
						g.drawLine(c * GRID_SIZE + GRID_SIZE - 2, r * GRID_SIZE + 2,
								c * GRID_SIZE + 2, r * GRID_SIZE + GRID_SIZE - 2);
					}
					if (prow == selRow && pcol == selCol) {
						g.setColor(Color.WHITE);
						g.drawRect(c * GRID_SIZE, r * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
					}
				}
			}
		}
		for (int i = 0; i < food.size(); i++) {
			Food f = food.get(i);
			g.setColor(Color.YELLOW);
			g.fillOval(f.xPos, f.drop.getIntValue(), GRID_SIZE, GRID_SIZE);
		}
		// draw clues
		g.translate(startX(), startY());
		g.setColor(Color.BLACK);
		for (int i = 0; i < puzzle.getRows(); i++) {
			int[] rowClues = puzzle.getClueRow(i);
			for (int j = 0; j < rowClues.length; j++) {
				g.drawString(Integer.toString(rowClues[rowClues.length - j - 1]), (j + 1) * -10, i * GRID_SIZE + 15);
			}
		}
		for (int i = 0; i < puzzle.getColumns(); i++) {
			int[] colClues = puzzle.getClueColumn(i);
			for (int j = 0; j < colClues.length; j++) {
				g.drawString(Integer.toString(colClues[colClues.length - j - 1]), i * GRID_SIZE + 10, (j + 1) * -10);
			}
		}
		g.translate(-startX(), -startY());
	}
	
}
