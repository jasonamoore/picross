package picnix.puzzle;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Input;
import resource.bank.AudioBank;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.PuzzleState;
import state.element.Element;
import state.element.puzzle.ToolButton;
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
	// used for mouse ray tracing
	private int lastDrawX, lastDrawY;
	// used for inferring which blob to size up when drawing
	private static final int ROW = 0, COL = 1;
	private int[] lastCell;
	private int[] currCell;
	// animation blob size helper smooth movement
	private Animation[] blobSizeAnim;

	// animation for hints fading in/out
	private Animation hintFade;
	
	// used to fade sidebars when drawing
	private final int FADE_TIME = 550;
	private Timer fadeTimer;
	
	// streak constants
	public static final double[] MULTIPLIERS = {1, 1.2, 1.5, 2, 3, 5};
	public static final int STREAK_GAP = 4500;
	
	// for keeping track of what grids have ever been marked (for score)
	private boolean[][] scoreChart;
	// timer for score streak
	private Timer streakTimer;
	// size of the streak
	private int streak;
	
	public Blanket(Field field) {
		this.field = field;
		puzState = field.getPuzzleState();
		lastCell = new int[] {-1, -1};
		currCell = new int[] {-1, -1};
		blobSizeAnim = new Animation[2];
		blobSizeAnim[0] = new Animation(100, Animation.EASE_OUT, Animation.NO_LOOP);
		blobSizeAnim[1] = new Animation(100, Animation.EASE_OUT, Animation.NO_LOOP);
		hintFade = new Animation(0, 1, 350, Animation.CUBIC, Animation.NO_LOOP, true);
		fadeTimer = new Timer(false);
		scoreChart = new boolean[puzState.getRows()][puzState.getColumns()];
		streakTimer = new Timer(false);
		foodMap = new FoodContainer[puzState.getActivePuzzle().getRows()][puzState.getActivePuzzle().getColumns()];
		foodSparseMap = new FoodContainer[puzState.getActivePuzzle().getRows()][puzState.getActivePuzzle().getColumns()];
	}
	
	private boolean inHintBounds() {
		double dist = Math.sqrt(Math.pow(field.getCamCenterX(true) - field.getScrollX(), 2) + 
								Math.pow(field.getCamCenterY(true) - field.getScrollY(), 2));
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
		// the location of the click
		int rmx = getRelativeMouseX();
		int rmy = getRelativeMouseY();
		// logic for drawing on picture
		if (puzState.getState() == PuzzleState.PICTURE_SOLVING) {
			// mark on the board where the click occurred
			lastDrawX = rmx;
			lastDrawY = rmy;
			int oldMark = puzState.getActivePuzzle().getMark(getCellAtPoint(rmy), getCellAtPoint(rmx));
			// the drawMode for the stroke to start
			int mode = getDrawMode(mbutton, oldMark);
			startDraw(mode);
		}
		// (new) logic for arranging food
		else if (puzState.getState() == PuzzleState.FOOD_SOLVING) {
			int prow = getCellAtPoint(rmy);
			int pcol = getCellAtPoint(rmx);
			// is there a food here? (get it)
			FoodContainer fc = getFoodAt(prow, pcol);
			// if there's food, pick up and drag
			if (fc != null) {
				// pickup and save orig location
				pickupFood(fc, prow, pcol);
				dragOrigRow = prow;
				dragOrigCol = pcol;
				startDrag(fc);
			}
		}
	}
	
	/* ~~~~~~~~~~~~
	 *  FOOD STUFF
	 * ~~~~~~~~~~~~
	 */

	private FoodContainer[][] foodMap;
	private FoodContainer[][] foodSparseMap;
	private FoodContainer dragging;
	private int dragOrigRow = -1, dragOrigCol = -1;
	
	private class FoodContainer {
		int foodId;
		FoodContainer(int foodId) {
			this.foodId = foodId;
		}
	}
	
	private FoodContainer getFoodAt(int row, int col) {
		// bounds checking
		if (!isValidFoodSpot(row, col))
			return null;
		// return whatever food is in the map at that spot
		else
			return foodMap[row][col];
	}
	
	private void pickupFood(FoodContainer fc, int row, int col) {
		if (!isValidFoodSpot(row, col))
			return;
		if (foodMap[row][col] == fc) {
			foodMap[row][col] = null;
			// remove from sparse map (may not even be there)
			foodSparseMap[row][col] = null;
			// try picking up in the surrounding squares
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++)
					pickupFood(fc, row + i, col + j);
		}
	}
	
	private void placeFood(FoodContainer fc, int row, int col) {
		int[] food = PuzzleState.getFoodById(dragging.foodId);
		int fwidth = food[PuzzleState.F_WIDTH];
		int fheight = food[PuzzleState.F_HEIGHT];
		// place fc in all squares it occupies
		for (int i = 0; i < fheight; i++)
			for (int j = 0; j < fwidth; j++)
				foodMap[row + i][col + j] = fc;
		// place at its origin
		foodSparseMap[row][col] = fc;
		// CHECK IF PLACING FOOD HERE SOLVED THE PUZZLE
		Puzzle puzzle = puzState.getActivePuzzle();
		boolean valid = true;
		for (int r = 0; valid && r < puzzle.getRows(); r++)
			for (int c = 0; valid && c < puzzle.getColumns(); c++)
				valid &= !puzzle.isFilledInSolution(r, c) || foodMap[r][c] != null;
		if (valid) // solved
			puzState.solvedFood();
	}
	
	public void startDrag(int foodId) {
		startDrag(new FoodContainer(foodId));
	}
	
	private void startDrag(FoodContainer fc) {
		dragging = fc;
	}
	
	private void stopDrag() {
		// TODO animation?
		dragging = null;
		dragOrigRow = dragOrigCol = -1;
	}
	
	public void tryDrop() {
		// not dragging anything?
		if (dragging == null)
			return;
		// find out where to drop
		int rmx = getRelativeMouseX();
		int rmy = getRelativeMouseY();
		int[] food = PuzzleState.getFoodById(dragging.foodId);
		int fHalfW = (food[0] * puzState.getPuzzleCellSize()) / 2;
		int fHalfH = (food[1] * puzState.getPuzzleCellSize()) / 2;
		int row = getClosestCellAtPoint(rmy - fHalfH);
		int col = getClosestCellAtPoint(rmx - fHalfW);
		// where it will go
		int where[] = findValidInsertSpot(dragging.foodId, row, col);
		// check if we found anywhere to place
		if (where[0] == -1 || where[1] == -1) {
			// failed to place - try to return to its original place
			if (isValidFoodSpot(row, col) &&
					dragOrigRow != -1 && dragOrigCol != -1) {
				// figure out where the food was placed originally
				int[] origWhere = findValidInsertSpot(dragging.foodId, dragOrigRow, dragOrigCol);
				placeFood(dragging, origWhere[0], origWhere[1]);
			}
			// if no original spot recorded (or dragged off) return to food bar
			else
				puzState.returnFoodToWidget(dragging.foodId);
		}
		else {
			// successfully place the food, stop dragging
			placeFood(dragging, where[0], where[1]);
		}
		// in either case, stop dragging
		stopDrag();
	}
	
	private boolean canDrop(int[] food, int row, int col) {
		boolean tainted = false;
		int fwidth = food[PuzzleState.F_WIDTH];
		int fheight = food[PuzzleState.F_HEIGHT];
		// place fc in all squares it occupies
		for (int i = 0; !tainted && i < fheight; i++)
			for (int j = 0; !tainted && j < fwidth; j++)
				// bounds checking + if food is already there
				tainted = !isValidFoodSpot(row + i, col + j) ||
						  foodMap[row + i][col + j] != null;
		return !tainted;
	}
	
	private int[] findValidInsertSpot(int foodId, int row, int col) {
		// get the food data
		int[] food = PuzzleState.getFoodById(dragging.foodId);
		int fwidth = food[PuzzleState.F_WIDTH];
		int fheight = food[PuzzleState.F_HEIGHT];
		// where it will go
		int whereR = -1, whereC = -1;
		if (isValidFoodSpot(row, col)) { // (don't need to try this if not in bounds)
			// try all the ways you could place this food here
			for (int i = 0; i < fheight && whereR == -1; i++) {
				for (int j = 0; j < fwidth && whereC == -1; j++) {
					int startR = row - i;
					int startC = col - j;
					if (canDrop(food, startR, startC)) {
						whereR = startR;
						whereC = startC;
					}
				}
			}
		}
		return new int[] {whereR, whereC};
	}
	
	private boolean isValidFoodSpot(int row, int col) {
		return row >= 0 && row < foodMap.length &&
				col >= 0 && col < foodMap[row].length;
	}
	
	public boolean isDraggingFood() {
		return dragging != null;
	}
	
	//--------------------//
	
	
	private int getDrawMode(int mbutton, int oldMark) {
		int tool = puzState.getCurrentTool();
		int pMark = mbutton == Input.LEFT_CLICK ? tool : tool + (-2 * (tool % 2) + 1);
		if (oldMark == pMark)
			return Puzzle.EMPTY;
		else
			return pMark;
	}

	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		// logic for drawing on picture
		if (puzState.getState() == PuzzleState.PICTURE_SOLVING)
			stopDraw();
		// logic for arranging food
		else if (puzState.getState() == PuzzleState.FOOD_SOLVING)
			tryDrop();
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

	@Override
	public void onLeave() {
		super.onLeave();
		// make sure mouse wont leave and return somewhere else,
		// causing a janky blob size hint animation
		blobSizeAnim[0].pause();
		blobSizeAnim[1].pause();
	}
	
	private void startDraw(int mode) {
		if (drawing)
			return;
		drawStroke = Stroke.newStroke(puzState.getActiveLayerId());
		drawing = true;
		drawMode = mode;
		fadeTimer.reset(true);
	}
	
	private void stopDraw() {
		if (!drawing)
			return;
		// add stroke
		puzState.fadeSidebars(false);
		puzState.pushStroke(drawStroke, drawMode);
		drawing = false;
		drawMode = -1;
		drawStroke = null;
		currCell[ROW] = -1;
		currCell[COL] = -1;
		lastCell[ROW] = -1;
		lastCell[COL] = -1;
		blobSizeAnim[0].pause();
		blobSizeAnim[1].pause();
	}
	
	private int getCellAtPoint(int xory) {
		return Math.floorDiv(xory, puzState.getPuzzleCellSize());
	}
	
	private int getClosestCellAtPoint(int xory) {
		return (int) Math.round(xory / (double) puzState.getPuzzleCellSize());
	}
	
	@Override
	public void tick() {
		super.tick();
		hintFade.setForward(inHintBounds() || beingHovered());
		hintFade.resume();
		Puzzle puzzle = puzState.getActivePuzzle();
		int drawX = getRelativeMouseX();
		int drawY = getRelativeMouseY();
		int mrow = getCellAtPoint(drawY);
		int mcol = getCellAtPoint(drawX);
		if (currCell[ROW] != mrow || currCell[COL] != mcol) {
			lastCell[ROW] = currCell[ROW];
			currCell[ROW] = mrow;
			lastCell[COL] = currCell[COL];
			currCell[COL] = mcol;
		}
		// only need to do picture drawing stuff if solving picture puzzle
		if (puzState.getState() == PuzzleState.PICTURE_SOLVING) {
			if (drawMode == Puzzle.FILLED && puzzle.getRemainingFillCount() < 1) // no plates left
				stopDraw();
			else if (drawing) {
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
						// MAKE RAY-TRACED MARK
						tryDraw(puzzle, rdrow, rdcol);
					}
				}
				// MAKE FINAL MARK AT THE ACTUAL MOUSE POSITION
				tryDraw(puzzle, mrow, mcol);
				// UPDATE LAST DRAW X/Y
				lastDrawX = drawX;
				lastDrawY = drawY;
			}
		}
	}
	
	private void tryDraw(Puzzle puzzle, int row, int col) {
		// prevent drawing if drawing was stopped, also sanity check for row/col
		if (!drawing || !puzzle.validSpot(row, col))
			return;
		int oldMark = puzzle.getMark(row, col);
		if (oldMark != drawMode && !(drawMode == Puzzle.FILLED && oldMark == Puzzle.FLAGGED)) {
			boolean mistake = puzzle.markSpot(row, col, drawMode);   
			drawStroke.addChange(row, col, oldMark, mistake);
			if (mistake)
				stopDraw();
			// if not mistake, is plate draw, and not scored yet
			else if (drawMode == Puzzle.FILLED && !scoreChart[row][col]) {
				if (streakTimer.elapsed() < STREAK_GAP)
					streak++;
				else
					streak = 1;
				scoreChart[row][col] = true;
				// play plate sound
				AudioBank.plateDinks[Math.min(4, streak - 1)].play();
				puzState.increasePlateScore(getPlateScoreBonus());
				streakTimer.reset(true);
			}
		}
	}

	public int getStreak() {
		return streak;
	}
	
	public int getStreakTimeRemaining() {
		return STREAK_GAP - (int) streakTimer.elapsed();
	}

	private int getPlateScoreBonus() {
		/*if (streak == 1)
			System.out.println("Ok");
		if (streak == 2)
			System.out.println("Nice");
		if (streak == 3)
			System.out.println("Awesome");
		if (streak == 4)
			System.out.println("Swag");
		if (streak >= 5)
			System.out.println("Fantastic!");*/
		int size = Math.max(puzState.getRows(), puzState.getColumns());
		double sizeMultiplier = 25.0 / (size * size);
		return (int) (PuzzleState.PLATE_SCORE * sizeMultiplier *
				MULTIPLIERS[Math.min(streak, MULTIPLIERS.length - 1)]);
	}

	@Override
	public void render(Graphics g) {
		int xp = getDisplayX();
		int yp = getDisplayY();
		g.translate(xp, yp);
		Puzzle puzzle = puzState.getActivePuzzle();
		int cellSize = puzState.getPuzzleCellSize();
		int layid = puzState.getActiveLayerId();

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
		int rmx = getRelativeMouseX();
		int rmy = getRelativeMouseY();
		int highRow = getCellAtPoint(rmy);
		int highCol = getCellAtPoint(rmx);
		boolean hov = puzzle.validSpot(highRow, highCol) && !puzState.isPictureOver();
		int mode = hov ? getDrawMode(Input.LEFT_CLICK, puzzle.getMark(highRow, highCol)) : 0;

		Graphics2D gg = (Graphics2D) g;
		Composite oldComp = gg.getComposite();
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
				boolean hovered = hov && r == highRow && c == highCol;
				if ((!drawing || mark != drawMode) && hovered && mark == Puzzle.EMPTY && // draw half opacity hover hint
					(mode != Puzzle.FILLED || puzState.hasPlates())) { // don't if not enough plates
					gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					mark = mode;
				}
				if (mark == Puzzle.FILLED)
					g.drawImage(plates[puzState.getActiveLayerId()+1], c * cellSize+pxl, r * cellSize+pyl, null);
				else if (mark == Puzzle.FLAGGED)
					g.drawImage(forks[0], c * cellSize+fxl, r * cellSize+fyl, null);
				else if (mark == Puzzle.MAYBE_FILLED)
					g.drawImage(plates[4], c * cellSize+pxl, r * cellSize+pyl, null);
				else if (mark == Puzzle.MAYBE_FLAGGED)
					g.drawImage(forks[2], c * cellSize+fxl, r * cellSize+fyl, null);
				if (hovered) // reset opacity
					gg.setComposite(oldComp);
			}
		}
		// if there is a current stroke w/ at least 2 changes
		if (hov && drawStroke != null && drawStroke.size() > 1) {
			boolean horizontal = lastCell[ROW] == currCell[ROW];
			boolean vertical = lastCell[COL] == currCell[COL];
			if (horizontal || vertical) {
				int[] blob = horizontal ? puzzle.getHorizontalBlob(currCell[ROW], currCell[COL], drawMode)
						: puzzle.getVerticalBlob(currCell[ROW], currCell[COL], drawMode);
				if (blob != null) {
					int start = blob[0], size = blob[1];
					int axisPara = horizontal ? ROW : COL;
					int axisPerp = horizontal ? COL : ROW;
					boolean hintCover = currCell[axisPara] == 0;
					int numPara = horizontal ? nums[0].getWidth() : nums[0].getHeight();
					int numPerp = horizontal ? nums[0].getHeight() : nums[0].getWidth();
					int spa = start * cellSize + 1;
					int npa = currCell[axisPerp] * cellSize + (cellSize - numPara) / 2;
					int spe = !hintCover ? currCell[axisPara] * cellSize - 1 : (currCell[axisPara] + 1) * cellSize + 1;
					int npe = !hintCover ? spe - numPerp : spe;
					// animation stuff
					int newnx = horizontal ? npa : npe;
					int newny = horizontal ? npe : npa;
					if (!blobSizeAnim[0].isPlaying() || !blobSizeAnim[1].isPlaying()) {
						setBlobSizeAnim(newnx, newny);
					}
					else {
						int oldnx = blobSizeAnim[0].getIntValue();
						int oldny = blobSizeAnim[1].getIntValue();
						npa = horizontal ? oldnx : oldny;
						npe = horizontal ? oldny : oldnx;
						setBlobSizeAnim(newnx, newny);
					}
					int nepa = npa + numPara;
					int epe = !hintCover ? spe + (npe - spe) / 2 : spe + numPerp / 2;
					int epa = (start + size) * cellSize - 1;
					g.setColor(Palette.BLACK);
					if (horizontal) {
						g.drawLine(spa, spe, spa, epe);
						g.drawLine(spa, epe, npa - 3, epe);
						g.drawLine(nepa + 2, epe, epa, epe);
						g.drawLine(epa, epe, epa, spe);
						g.drawImage(nums[size - 1], npa, npe, null);
					} // swap arguments for vertical
					else {
						g.drawLine(spe, spa, epe, spa);
						g.drawLine(epe, spa, epe, npa - 3);
						g.drawLine(epe, nepa + 2, epe, epa);
						g.drawLine(epe, epa, spe, epa);
						g.drawImage(nums[size - 1], npe, npa, null);
					}
				}
			}
		}
		// if in the food solving stage, don't draw hints
		if (puzState.getState() < PuzzleState.FOOD_SOLVING) {
			// set opacity to hint fade anim
			gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) hintFade.getValue()));
			// if board is done, roll up hints - else 0, no roll up
			double rollUp = Animation.bezier(1 - puzState.getSwitchHideProgress(), Animation.EASE_IN);
			for (int r = 0; r < puzzle.getRows(); r++) {
				int[] hints = puzzle.getClueRow(r);
				int hh = r == highRow && hov ? 2 : 0;
				// if the puzzle was solved, the hints roll up, so we need to clip the hint width/heights
				int scrollW = hgridW * (hints.length);
				int minX = (int) -(scrollW * rollUp);
				g.setClip(minX, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				for (int i = 0; i < hints.length; i++) {
					int hintnum = hints[hints.length - 1 - i];
					g.drawImage(scrHoriz[1 + hh], -hgridW * (i+1), r * cellSize, null);
					g.drawImage(nums[Math.abs(hintnum)-1], -hgridW * (i+1) + rowXOff, r * cellSize + rowYOff, null);
					if (hintnum < 0) {
						g.setColor(Palette.RED);
						g.drawLine(-hgridW * (i+1) + rowXOff, r * cellSize + rowYOff,
								-hgridW * i - rowXOff, (r+1) * cellSize - rowYOff);
					}
				}
				// reset clip (if rolling)
				g.setClip(null);
				g.drawImage(scrHoriz[0 + hh], minX - hgridW, r * cellSize, null);
			}
			for (int c = 0; c < puzzle.getColumns(); c++) {
				int[] hints = puzzle.getClueColumn(c);
				int hh = c == highCol && hov ? 2 : 0;
				// if the puzzle was solved, the hints roll up, so we need to clip the hint width/heights
				int scrollH = hgridW * (hints.length);
				int minY = (int) -(scrollH * rollUp);
				g.setClip(0, minY, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				for (int i = 0; i < hints.length; i++) {
					int hintnum = hints[hints.length - 1 - i];
					g.drawImage(scrVert[1 + hh], c * cellSize, -hgridW * (i+1), null);
					g.drawImage(nums[Math.abs(hintnum)-1], c * cellSize + colXOff, -hgridW * (i+1) + colYOff, null);
					if (hintnum < 0) {
						g.setColor(Palette.RED);
						g.drawLine(c * cellSize + colXOff, -hgridW * (i+1) + colYOff,
								(c+1) * cellSize - colXOff, -hgridW * i - colYOff);
					}
				}
				// reset clip (if rolling)
				g.setClip(null);
				g.drawImage(scrVert[0 + hh], c * cellSize, minY - hgridW, null);
			}
		}
		// if state has passed puzzle completion, draw food
		else {
			BufferedImage[] foods = getFoodSheet(cellSize);
			for (int r = 0; r < foodMap.length; r++) {
				for (int c = 0; c < foodMap.length; c++) {
					FoodContainer fc = foodSparseMap[r][c];
					if (fc != null)
						g.drawImage(foods[fc.foodId], c * cellSize, r * cellSize, null);
				}
			}
			// render drop hitbox
			if (isDraggingFood()) {
				int[] food = PuzzleState.getFoodById(dragging.foodId);
				int fHalfW = (food[0] * cellSize) / 2;
				int fHalfH = (food[1] * cellSize) / 2;
				int foodRow = getClosestCellAtPoint(rmy - fHalfH);
				int foodCol = getClosestCellAtPoint(rmx - fHalfW);
				// all of the food needs to be in bounds to render
				if (isValidFoodSpot(foodRow, foodCol) &&
					isValidFoodSpot(foodRow + food[1] - 1, foodCol + food[0] - 1)) {
					// half opacity for the color
					gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					int where[] = findValidInsertSpot(dragging.foodId, foodRow, foodCol);
					// if there is a valid spot, draw it green
					if (where[0] != -1 && where[1] != -1) {
						g.setColor(Palette.PEAR);
						g.fillRect(where[1] * cellSize, where[0] * cellSize, food[0] * cellSize, food[1] * cellSize);
					}
					// if no valid spot, draw red
					else {
						g.setColor(Palette.RED);
						g.fillRect(foodCol * cellSize, foodRow * cellSize, food[0] * cellSize, food[1] * cellSize);
					}
					gg.setComposite(oldComp);
				}
				g.drawImage(foods[dragging.foodId], rmx - fHalfW, rmy - fHalfH, null);
			}
		}
		gg.setComposite(oldComp);
		g.translate(-xp, -yp);
	}

	private void setBlobSizeAnim(int x, int y) {
		Animation b0 = blobSizeAnim[0];
		Animation b1 = blobSizeAnim[1];
		double from0 = b0.isPlaying() ? b0.getValue() : x;
		double from1 = b1.isPlaying() ? b1.getValue() : y;
		b0.setFrom(from0);
		b1.setFrom(from1);
		b0.setTo(x);
		b1.setTo(y);
		b0.reset(true);
		b1.reset(true);
	}

	private static double randomConsistent(int id, int row, int col) {
		long hash = (Math.abs(id) * 79 + row * 31 + col * 37) % 17;
		return hash / 17.0;
	}
	
	public static BufferedImage[] getCellSheet(int cellSize) {
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
	
	public static BufferedImage[] getPlateSheet(int cellSize) {
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
	
	public static BufferedImage[] getForkSheet(int cellSize) {
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
	
	public static BufferedImage[] getHintScrollHorizontalSheet(int cellSize) {
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
	
	public static BufferedImage[] getHintScrollVerticalSheet(int cellSize) {
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
	
	public static BufferedImage[] getHintNumbersSheet(int cellSize) {
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
	
	public static BufferedImage[] getFoodSheet(int cellSize) {
		switch (cellSize) {
		case PuzzleState.CELL_SIZE_5x5:
			return ImageBank.foods35;
		case PuzzleState.CELL_SIZE_10x10:
			return ImageBank.foods20;
		case PuzzleState.CELL_SIZE_15x15:
			return ImageBank.foods15;
		case PuzzleState.CELL_SIZE_20x20:
			return ImageBank.foods10;
		}
		return null;
	}
	
}
