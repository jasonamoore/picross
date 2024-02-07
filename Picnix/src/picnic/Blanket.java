package picnic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Input;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import state.element.Element;
import state.element.ToolButton;

public class Blanket extends Element {

	// the parent field
	private Field field;
	// the puzzle
	private Puzzle puzzle;
	
	// grid cell sizes
	private static final int CELL_SIZE_5x5 = 35;
	private static final int CELL_SIZE_10x10 = 20;
	private static final int CELL_SIZE_15x15 = 15;
	private static final int CELL_SIZE_20x20 = 8;
	
	// size of this puzzle's grid cells
	private int cellSize;
	
	// if the player is dragging along the grid
	private boolean drawing;
	private int drawMode;
	// the row/col the mouse is hovering over
	private int hoveredRow, hoveredCol;
	
	// constant for draw modes
	private static final int DRAW_PRIMARY = -1;
	private static final int DRAW_SECONDARY = -2;
	private static final int DRAW_CLEARED = Puzzle.CLEARED;
	private static final int DRAW_UNCLEARED = Puzzle.UNCLEARED;
	private static final int DRAW_FLAGGED = Puzzle.FLAGGED;
	
	public Blanket(Field field, Puzzle puzzle) {
		this.field = field;
		this.puzzle = puzzle;
		int msize = Math.max(puzzle.getRows(), puzzle.getColumns());
		cellSize =  msize <= 5 ? CELL_SIZE_5x5 :
					msize <= 10 ? CELL_SIZE_10x10 :
					msize <= 15 ? CELL_SIZE_15x15 :
					/*msize>15?*/ CELL_SIZE_20x20;
	}
	
	public int getPixelWidth() {
		return cellSize * puzzle.getColumns();
	}

	public int getPixelHeight() {
		return cellSize * puzzle.getRows();
	}
	
	@Override
	public void onClick(int mbutton) {
		if (mbutton == Input.MIDDLE_CLICK ||
				field.getParentState().getCurrentTool() == ToolButton.PAN) {
			if (notBeingClicked())
				upstreamClick(mbutton);
			return;
		}
		super.onClick(mbutton);
		drawing = true;
		drawMode = mbutton == Input.LEFT_CLICK ? DRAW_PRIMARY : DRAW_SECONDARY;
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		drawing = false;
	}

	@Override
	public void tick() {
		super.tick();
		if (drawing && beingHovered()) {
			int lx = getRelativeMouseX();
			int ly = getRelativeMouseY();
			hoveredCol = lx / cellSize;
			hoveredRow = ly / cellSize;
			if (drawMode == DRAW_PRIMARY || drawMode == DRAW_SECONDARY) {
				int oldMark = puzzle.getMark(hoveredRow, hoveredCol);
				int primDraw = field.getParentState().getCurrentTool() == ToolButton.PLATE ? DRAW_CLEARED : DRAW_FLAGGED;
				int secDraw = field.getParentState().getCurrentTool() == ToolButton.PLATE ? DRAW_FLAGGED : DRAW_CLEARED;
				int posDraw = drawMode == DRAW_PRIMARY ? primDraw : secDraw;
				drawMode = oldMark == Puzzle.UNCLEARED ? posDraw : oldMark == posDraw ? DRAW_UNCLEARED : posDraw;
			}
			// double check valid, and make the mark
			if (puzzle.validSpot(hoveredRow, hoveredCol))
				puzzle.markSpot(hoveredRow, hoveredCol, drawMode);
		}
	}

	@Override
	public void render(Graphics g) {
		g.translate(getDisplayX(), getDisplayY());
		for (int r = 0; r < puzzle.getRows(); r++) {
			for (int c = 0; c < puzzle.getColumns(); c++) {
				BufferedImage pic = r % 2 == 0 ? (c % 2 == 0 ? ImageBank.cells20[0] : ImageBank.cells20[1])
						: (c % 2 == 0 ? ImageBank.cells20[1] : ImageBank.cells20[2]);
				g.drawImage(pic, c * cellSize, r * cellSize, cellSize, cellSize, null);
				if (puzzle.getMark(r, c) == Puzzle.CLEARED) {
					g.drawImage(ImageBank.plates20[0], c * cellSize, r * cellSize, null);
				}
				else if (puzzle.getMark(r, c) == Puzzle.FLAGGED) {
					g.drawImage(ImageBank.forks20[0], c * cellSize, r * cellSize, null);
				}
			}
		}
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		for (int r = 0; r < puzzle.getRows(); r++) {
			int[] hints = puzzle.getClueRow(r);
			for (int i = 0; i < hints.length; i++) {
				g.setColor(Color.BLACK);
				g.drawString(Integer.toString(hints[hints.length-1-i]), -cellSize * (i+1) + 1, r * cellSize + 1 + 10);
				g.setColor(Color.WHITE);
				g.drawString(Integer.toString(hints[hints.length-1-i]), -cellSize * (i+1), r * cellSize + 10);
			}
		}
		for (int c = 0; c < puzzle.getColumns(); c++) {
			int[] hints = puzzle.getClueColumn(c);
			for (int i = 0; i < hints.length; i++) {
				g.setColor(Color.BLACK);
				g.drawString(Integer.toString(hints[hints.length-1-i]), c * cellSize + 1, -cellSize * (i+1) + 1);
				g.setColor(Color.WHITE);
				g.drawString(Integer.toString(hints[hints.length-1-i]), c * cellSize, -cellSize * (i+1));
			}
		}
		g.translate(-getDisplayX(), -getDisplayY());
	}
	
}
