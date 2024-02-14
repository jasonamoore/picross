package picnic;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.PuzzleState;
import state.element.Element;
import state.element.ToolButton;
import util.Animation;
import util.Timer;

public class Blanket extends Element {

	// parent puzzle state
	private PuzzleState puzState;
	// the parent field
	private Field field;
	
	// if the player is dragging along the grid
	private boolean drawing;
	// the draw mode (mark to write)
	private int drawMode;
	// the stroke data for the current draw
	private Stroke drawStroke;
	
	// animation for hints fading in/out
	private Animation hintFade;
	
	public Blanket(Field field) {
		this.field = field;
		puzState = field.getPuzzleState();
		hintFade = new Animation(0, 1, 350, Animation.CUBIC, Animation.LOOP_NONE, true);
		fadeTimer = new Timer(false);
	}
	
	private boolean inHintBounds() {
		double dist = Math.sqrt(Math.pow(field.getCamCenterX() - field.getScrollX(), 2) + 
								Math.pow(field.getCamCenterY() - field.getScrollY(), 2));
		return dist < (puzState.getPuzzleDisplayWidth() / 2);
	}
	
	@Override
	public void onClick(int mbutton) {
		if (mbutton == Input.MIDDLE_CLICK ||
				field.getPuzzleState().getCurrentTool() == ToolButton.PAN) {
			if (notBeingClicked())
				upstreamClick(mbutton);
			return;
		}
		super.onClick(mbutton);
		// mark on the board where the click occurred
		int rmx = getRelativeMouseX();
		int rmy = getRelativeMouseY();
		lastDrawX = rmx;
		lastDrawY = rmy;
		int oldMark = puzState.getActivePuzzle().getMark(getCellAtPoint(rmy), getCellAtPoint(rmx));
		// the drawMode for the stroke to start
		int mode = getDrawMode(mbutton, oldMark);
		startDraw(mode);
	}
	
	private int getDrawMode(int mbutton, int oldMark) {
		int tool = puzState.getCurrentTool();
		int pMark = mbutton == Input.LEFT_CLICK ? tool : tool + (-2 * (tool % 2) + 1);
		if (oldMark == pMark)
			return Puzzle.UNCLEARED;
		else
			return pMark;
	}

	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		stopDraw();
	}
	
	@Override
	public void onHover() {
		super.onHover();
		// make sure mouse didnt leave and return somewhere else,
		// causing a stray ray trace across the blanket
		if (drawing) {
			lastDrawX = getRelativeMouseX();
			lastDrawY = getRelativeMouseY();
		}
	}
	
	private void startDraw(int mode) {
		if (drawing)
			return;
		drawStroke = new Stroke(puzState.getActiveLayerId());
		drawing = true;
		drawMode = mode;
		fadeTimer.reset(true);
	}
	
	private void stopDraw() {
		if (!drawing)
			return;
		// add stroke
		puzState.pushStroke(drawStroke, drawMode);
		drawing = false;
		drawMode = -1;
		drawStroke = null;
		puzState.fadeSidebars(false);
	}
	
	private int getCellAtPoint(int xory) {
		return Math.floorDiv(xory, puzState.getPuzzleCellSize());
	}

	private final int FADE_TIME = 550;
	private Timer fadeTimer;
	private int lastDrawX, lastDrawY;
	
	@Override
	public void tick() {
		super.tick();
		hintFade.setForward(inHintBounds() || beingHovered(), true);
		Puzzle puzzle = puzState.getActivePuzzle();
		int drawX = getRelativeMouseX();
		int drawY = getRelativeMouseY();
		int mrow = getCellAtPoint(drawY);
		int mcol = getCellAtPoint(drawX);
		if (drawing && beingHovered() && puzzle.validSpot(mrow, mcol)) {
			// DO FADE TIMER IF ENOUGH TIME PASSED
			if (fadeTimer.elapsed() >= FADE_TIME) {
				puzState.fadeSidebars(true);
				fadeTimer.reset(false);
			}
			// CHECK IF NEED MOUSE RAYTRACE
			double dist = Math.sqrt(Math.pow(drawY - lastDrawY, 2) + Math.pow(drawX - lastDrawX, 2));
			int cellSize = puzState.getPuzzleCellSize();
			if (dist >= cellSize) {
				// MOUSE RAYTRACING
				//System.out.println("lastDrawX: " + lastDrawX + ", lastDrawY: " + lastDrawY + ", drawX: " + drawX + ", drawY: " + drawY);
				double angle = Math.atan((drawY - lastDrawY) / (double) (drawX - lastDrawX));
				if (drawX < lastDrawX)
					angle = Math.PI + angle;
				for (int r = 0; r < dist; r += cellSize) {
					int rdrow = getCellAtPoint(lastDrawY + (int) Math.round(Math.sin(angle) * r));
					int rdcol = getCellAtPoint(lastDrawX + (int) Math.round(Math.cos(angle) * r));
					if (!puzzle.validSpot(rdrow, rdcol))
						continue;
					// MAKE MARKS
					int oldMark = puzzle.getMark(rdrow, rdcol);
					if (oldMark != drawMode) {
						drawStroke.addChange(rdrow, rdcol, puzzle.getMark(rdrow, rdcol));
						puzzle.markSpot(rdrow, rdcol, drawMode);
					}
				}
			}
			// MAKE FINAL MARK AT THE ACTUAL MOUSE POSITION
			int oldMark = puzzle.getMark(mrow, mcol);
			if (oldMark != drawMode) {
				drawStroke.addChange(mrow, mcol, puzzle.getMark(mrow, mcol));
				puzzle.markSpot(mrow, mcol, drawMode);
			}
			// UPDATE LAST DRAW X/Y
			lastDrawX = drawX;
			lastDrawY = drawY;
		}
	}

	@Override
	public void render(Graphics g) {
		g.translate(getDisplayX(), getDisplayY());
		Puzzle puzzle = puzState.getActivePuzzle();
		int cellSize = puzState.getPuzzleCellSize();
		int layid = puzState.getActiveLayerId();

		final int guessIndex = 4;
		BufferedImage[] cells = getCellSheet(cellSize);
		BufferedImage[] plates = getPlateSheet(cellSize);
		BufferedImage[] forks = getForkSheet(cellSize);
		BufferedImage[] scrHoriz = getHintScrollHorizontalSheet(cellSize);
		BufferedImage[] scrVert = getHintScrollVerticalSheet(cellSize);
		BufferedImage[] nums = getHintNumbersSheet(cellSize);
		int hgridW = getHintScrollWidth(cellSize);
		int rowXOff = (hgridW - nums[0].getWidth()) / 2;
		int rowYOff = (cellSize - nums[0].getHeight()) / 2;
		int colXOff = (cellSize - nums[0].getWidth()) / 2;
		int colYOff = (hgridW - nums[0].getHeight()) / 2;

		int plateXLeeway = cellSize - plates[0].getWidth();
		int plateYLeeway = cellSize - plates[0].getHeight();
		int forkXLeeway = cellSize - forks[0].getWidth();
		int forkYLeeway = cellSize - forks[0].getHeight();

		// the highlighted row and column hints
		boolean hov = beingHovered();
		int highRow = getCellAtPoint(getRelativeMouseY());
		int highCol = getCellAtPoint(getRelativeMouseX());
		
		for (int r = 0; r < puzzle.getRows(); r++) {
			for (int c = 0; c < puzzle.getColumns(); c++) {
				BufferedImage cell = cells[r % 2 + c % 2];
				g.drawImage(cell, c * cellSize, r * cellSize, cellSize, cellSize, null);
				int mark = puzzle.getMark(r, c);
				double seed = randomConsistent(layid, r, c);
				int pxl = (int) Math.round(plateXLeeway * seed);
				int pyl = (int) Math.round(plateYLeeway * seed);
				int fxl = (int) Math.round(forkXLeeway * seed);
				int fyl = (int) Math.round(forkYLeeway * seed);
				if (mark == Puzzle.CLEARED)
					g.drawImage(plates[puzState.getActiveLayerId()+1], c * cellSize+pxl, r * cellSize+pyl, null);
				else if (mark == Puzzle.FLAGGED)
					g.drawImage(forks[0], c * cellSize+fxl, r * cellSize+fyl, null);
				else if (mark == Puzzle.MAYBE_CLEARED)
					g.drawImage(plates[guessIndex], c * cellSize+pxl, r * cellSize+pyl, null);
				else if (mark == Puzzle.MAYBE_FLAGGED)
					g.drawImage(forks[2], c * cellSize+fxl, r * cellSize+fyl, null);
			}
		}
		// set opacity to hint fade anim
		Graphics2D gg = (Graphics2D) g;
		Composite oldComp = gg.getComposite();
		gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) hintFade.getValue()));
		for (int r = 0; r < puzzle.getRows(); r++) {
			int[] hints = puzzle.getClueRow(r);
			int hh = r == highRow && hov ? 2 : 0;
			for (int i = 0; i < hints.length; i++) {
				int hintnum = hints[hints.length - 1 - i];
				g.drawImage(scrHoriz[1 + hh], -hgridW * (i+1), r * cellSize, null);
				g.drawImage(nums[Math.abs(hintnum)-1], -hgridW * (i+1) + rowXOff, r * cellSize + rowYOff, null);
				if (hintnum < 0) {
					g.setColor(Palette.RED);
					g.drawLine(-hgridW * (i+1) + rowXOff, r * cellSize + rowYOff,
							-hgridW * (i+1) + rowXOff + cellSize, r * cellSize + rowYOff + cellSize);
				}
			}
			g.drawImage(scrHoriz[0 + hh], -hgridW * (hints.length+1), r * cellSize, null);
		}
		for (int c = 0; c < puzzle.getColumns(); c++) {
			int[] hints = puzzle.getClueColumn(c);
			int hh = c == highCol && hov ? 2 : 0;
			for (int i = 0; i < hints.length; i++) {
				int hintnum = hints[hints.length - 1 - i];
				g.drawImage(scrVert[1 + hh], c * cellSize, -hgridW * (i+1), null);
				g.drawImage(nums[hintnum-1], c * cellSize + colXOff, -hgridW * (i+1) + colYOff, null);
			}
			g.drawImage(scrVert[0 + hh], c * cellSize, -hgridW * (hints.length+1), null);
		}
		gg.setComposite(oldComp);
		g.translate(-getDisplayX(), -getDisplayY());
	}

	private static double randomConsistent(int id, int row, int col) {
		long hash = (Math.abs(id) * 79 + row * 31 + col * 37) % 17;
		return hash / 17.0;
	}
	
	private static BufferedImage[] getCellSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.cells35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.cells20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.cells15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.cells10;
		}
		return null;
	}
	
	private static BufferedImage[] getPlateSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.plates35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.plates20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.plates15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.plates10;
		}
		return null;
	}
	
	private static BufferedImage[] getForkSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.forks35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.forks20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.forks15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.forks10;
		}
		return null;
	}
	
	private static BufferedImage[] getHintScrollHorizontalSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.hintHoriz35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.hintHoriz20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.hintHoriz15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.hintHoriz10;
		}
		return null;
	}
	
	private static BufferedImage[] getHintScrollVerticalSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.hintVert35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.hintVert20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.hintVert15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.hintVert10;
		}
		return null;
	}
	
	public static int getHintScrollWidth(int cellSize) {
		return getHintScrollHorizontalSheet(cellSize)[0].getWidth();
	}
	
	private static BufferedImage[] getHintNumbersSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.numsbig;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.numsmed;
		case PuzzleState.CELL_SIZE_15x15:
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.numstiny;
		}
		return null;
	}
	
}
