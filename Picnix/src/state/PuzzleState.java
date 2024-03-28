package state;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Engine;
import engine.Input;
import engine.Transition;
import picnix.Level;
import picnix.World;
import picnix.data.UserData;
import picnix.puzzle.Blanket;
import picnix.puzzle.Field;
import picnix.puzzle.FoodBar;
import picnix.puzzle.FoodWidget;
import picnix.puzzle.Puzzle;
import picnix.puzzle.Stroke;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Icon;
import state.element.TextField;
import state.element.puzzle.LayerButton;
import state.element.puzzle.LayerProgress;
import state.element.puzzle.Sidebar;
import state.element.puzzle.ToolButton;
import state.load.LoadWinState;
import state.particle.Particle;
import util.Animation;
import util.MultiAnimation;
import util.Timer;

/**
 * A State the handles all the puzzling.
 */
public class PuzzleState extends State {

	// CONSTANTS for the puzzle cell sizes
	public static final int CELL_SIZE_5x5 = 35;
	public static final int CELL_SIZE_10x10 = 20;
	public static final int CELL_SIZE_15x15 = 15;
	public static final int CELL_SIZE_20x20 = 10;
	// CONSTANT for the layer ids
	public static final int NO_LAYER = -1;
	public static final int MAGENTA = 0;
	public static final int YELLOW = 1;
	public static final int CYAN = 2;
	public static final int TOTAL = 3;
	// constants for win/lose screen
	private static final int SWITCH_DURATION = 5000;
	private static final int SWITCH_HIDE_DURATION = 500;
	private static final double SCORE_LINES_DELAY = 1000;
	// lose messages
	private static final String LOSE_MESSAGE_TIME = "hurry up next time";
	private static final String LOSE_MESSAGE_MISTAKE = "keep your plates in order";
	// score messages and text fields
	private static final int NUM_SCORE_LINES = 5;
	private static final String[] SCORE_MESSAGES = {"time bonus", "accuracy bonus", "plate bonus", "critter bonus", "total score"};
	private TextField[] scoreLines;
	
	// render positioning constants;
	private static final int TOPBAR_HEIGHT = 32;
	private static final int STREAK_X = 380;
	private static final int STREAK_Y = 300;
	
	// the puzzle(s)
	private Puzzle[] puzzleLayers;
	// whether this is a layered game
	private boolean layered;
	
	// the active puzzle
	private Puzzle activePuzzle;
	// layer id of the active puzzle
	private int activeLayerId;
	
	// clock for hwo long the puzzle has been running
	private Timer clock; 
	private int timeSecLimit;
	// score constants
	public static final int PLATE_SCORE = 1000;
	public static final int MISTAKE_BONUS = 10_000;
	private static final int MAX_TIME_BONUS = 100_000;
	private static final double MS_PER_POINT = 0.9;
	// score fields
	private int plateScore;
	private int critterScore;
	private Animation scoreAnim;
	// keeps track of mistakes
	private int mistakeCap;
	private int mistakeCount;
	
	// stores field data, like the picnic blanket and critters
	private Field field;
	
	// tool and layer sidebars
	private Sidebar toolbar;
	private Sidebar layerbar;

	// array of ToolButtons for each of the tools
	private static final int NUM_TOOLS = ToolButton.REDO + 1;
	private ToolButton[] tools;
	
	// if guessing mode is on
	private boolean guessing;
	// id of the selected tool
	private int currentToolId;
	// for smooth tool arrow movement
	private Animation toolArrowAnim;

	// dynamic sequences of Strokes, used to undo/redo draws
	private static final int MAX_UNDO_HISTORY = Stroke.STROKE_BANK_SIZE;
	private ArrayList<Stroke> undo;
	private ArrayList<Stroke> redo;

	// array of LayerButtons and progress bars for each layer
	private static final int NUM_LAYERS = 3;
	private LayerButton[] layers;
	private LayerProgress[] layerProgs;
	// special elements for the "total" section
	private Icon totalLabel;
	private LayerProgress totalProg;
	
	// the cell size for puzzles in this State
	private int cellSize;
	
	// lose/win text drop animation
	private MultiAnimation textDrop;
	// state constants
	public static final int LOSING = -1;
	public static final int PICTURE_SOLVING = 0;
	public static final int PICTURE_WINNING = 1;
	public static final int FOOD_SOLVING = 2;
	public static final int COMPLETE_WINNING = 3;
	// puzzle status info - tracks losing/winning etc
	private int state;
	private Timer switchTimer;
	
	private World world;
	private Level level;
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	SETUP
	 * ~~~~~~~~~~~~~~~~~~~~
	 */
	
	public PuzzleState(World world, Level level) {
		this.world = world;
		this.level = level;
		state = PICTURE_SOLVING;
		layered = level.isLayered();
		clock = new Timer(false);
		timeSecLimit = level.getTimeLimit();
		mistakeCap = level.getMistakeCap();
		puzzleLayers = level.getPuzzles();
		activeLayerId = layered ? MAGENTA : NO_LAYER;
		activePuzzle = puzzleLayers[Math.max(0, activeLayerId)];
		int msize = Math.max(activePuzzle.getRows(), activePuzzle.getColumns());
		cellSize =  msize <= 5 ? CELL_SIZE_5x5 :
			msize <= 10 ? CELL_SIZE_10x10 :
			msize <= 15 ? CELL_SIZE_15x15 :
			/*msize>15?*/ CELL_SIZE_15x15;
		field = new Field(this, world.getId());
		add(field);
		// scoreAnim duration will be set dynamically, proportional to score increase
		scoreAnim = new Animation(0, 0, 0, Animation.LINEAR, Animation.NO_LOOP, false);
		toolArrowAnim = new Animation(100, Animation.EASE_OUT, Animation.NO_LOOP);
		toolClicked(ToolButton.PLATE);
		undo = new ArrayList<Stroke>();
		redo = new ArrayList<Stroke>();
		textDrop = new MultiAnimation(new double[] {-150, 142, 85, 142, 135, 142},
				new int[] {500, 200, 200, 75, 75, 0},
				new double[][] {MultiAnimation.EASE_IN, MultiAnimation.EASE_OUT, MultiAnimation.EASE_IN,
						MultiAnimation.EASE_OUT, MultiAnimation.EASE_IN, MultiAnimation.HOLD},
				0, 0, false);
		switchTimer = new Timer(false);
		generateUI();
		pictureWin();
	}
		
	@Override
	public void focus(int status) {
		if (status == NEWLY_OPENED)
			clock.resume();
	}
	
	/**
	 * Creates the toolbar and tools, layerbar and layers
	 * (if the game is layered), and updates enable status
	 * of the undo/redo, clear, and center buttons.
	 */
	private void generateUI() {
		toolbar = new Sidebar(Sidebar.TOOLBAR_X);
		toolbar.setBackground(ImageBank.toolbar);
		add(toolbar);
		tools = new ToolButton[NUM_TOOLS];
		// make the "normal" tools
		for (int i = 0; i < ToolButton.UNDO; i++) {
			tools[i] = new ToolButton(this, i, 16, 24 + (44 * (i % 6)), 45, 40);
			toolbar.add(tools[i]);
		}
		// disable guess tools
		tools[ToolButton.MAYBE_PLATE].setExisting(false);
		tools[ToolButton.MAYBE_FORKS].setExisting(false);
		// make undo and redo
		for (int i = 0; i <= 1; i++) {
			ToolButton urdo = new ToolButton(this, ToolButton.UNDO + i, 15 + (i * 24), 288, 23, 40);
			tools[ToolButton.UNDO + i] = urdo;
			toolbar.add(urdo);
		}
		layerbar = new Sidebar(Sidebar.LAYERBAR_X);
		add(layerbar);
		if (layered) {
			layerbar.setBackground(ImageBank.layerbar);
			layers = new LayerButton[NUM_LAYERS];
			layerProgs = new LayerProgress[NUM_LAYERS];
			for (int i = MAGENTA; i < TOTAL; i++) {
				layers[i] = new LayerButton(this, i, 10, 27 + (i * 86), 62, 65);
				layerbar.add(layers[i]);
				layerProgs[i] = new LayerProgress(puzzleLayers[i], getColorByLayerId(i), 10, 94 + (i * 86), 62, 15);
				layerbar.add(layerProgs[i]);
			}
			totalLabel = new Icon(ImageBank.layernames[TOTAL], 10, 295, 62, 15);
			totalProg = new LayerProgress(null, new Color(0x000000), 10, 312, 62, 15) {
				@Override
				public double getProgress() {
					double mp = layerProgs[MAGENTA].getProgress();
					double yp = layerProgs[YELLOW].getProgress();
					double cp = layerProgs[CYAN].getProgress();
					return (mp + yp + cp) / 3.0;
				}
			};
			layerbar.add(totalLabel);
			layerbar.add(totalProg);
			layerClicked(MAGENTA);
		}
		else
			layerbar.setVisible(false);
		updateUndoRedoEnabled();
		updateClearEnabled();
		setCenterEnabled(false);
	}

	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	SIZE STUFFS
	 * ~~~~~~~~~~~~~~~~~~~~
	 */
	
	public int getPuzzleCellSize() {
		return cellSize;
	}
	
	public int getPuzzleDisplayWidth() {
		return cellSize * activePuzzle.getColumns();
	}

	public int getPuzzleDisplayHeight() {
		return cellSize * activePuzzle.getRows();
	}
	
	public int getPuzzleLeftPadding() {
		int hintMax = activePuzzle.getLongestRowClueList();
		for (int i = 0; i < puzzleLayers.length; i++)
			hintMax = Math.max(hintMax, puzzleLayers[i].getLongestRowClueList());
		return Blanket.getHintScrollWidth(cellSize) * hintMax;
	}
	
	public int getPuzzleTopPadding() {
		int hintMax = activePuzzle.getLongestColumnClueList();
		for (int i = 0; i < puzzleLayers.length; i++)
			hintMax = Math.max(hintMax, puzzleLayers[i].getLongestColumnClueList());
		return Blanket.getHintScrollWidth(cellSize) * hintMax;
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 	GETTERS FOR TOOL/LAYER STATE
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	public int getRows() {
		return activePuzzle.getRows();
	}
	
	public int getColumns() {
		return activePuzzle.getColumns();
	}
	
	public int getCurrentTool() {
		return currentToolId;
	}

	public int getActiveLayerId() {
		return activeLayerId;
	}
	
	public Puzzle getActivePuzzle() {
		return getPuzzleByLayerId(activeLayerId);
	}

	public Puzzle getPuzzleByLayerId(int layerId) {
		if (layerId == NO_LAYER)
			return activePuzzle;
		return puzzleLayers[layerId];
	}

	public boolean isGuessing() {
		return guessing;
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~
	 * 	TOOL/LAYER FUNCTIONS
	 * ~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	public void toolClicked(int id) {
		if (selectable(id)) {
			currentToolId = id;
			toolArrowAnim.setFrom(toolArrowAnim.getValue());
			toolArrowAnim.setTo(Sidebar.SIDEBAR_Y + 24 + (44 * (id % 6)) + 10);
			toolArrowAnim.reset(true);
		}
		else {
			switch(id) {
			case ToolButton.GUESS:
				toggleGuess();
				break;
			case ToolButton.CLEAR:
				clearMarks();
				break;
			case ToolButton.CENTER:
				centerCam();
				break;
			case ToolButton.UNDO:
				undo();
				break;
			case ToolButton.REDO:
				redo();
				break;
			}
		}
	}
	
	public void layerClicked(int layerId) {
		layers[activeLayerId].setActive(false);
		activeLayerId = layerId;
		activePuzzle = getPuzzleByLayerId(layerId);
		layers[layerId].setActive(true);
		updatePlateEnabled();
		updateClearEnabled();
	}
	
	private void toggleGuess() {
		guessing = !guessing;
		tools[ToolButton.PLATE].setExisting(!guessing);
		tools[ToolButton.FORKS].setExisting(!guessing);
		tools[ToolButton.MAYBE_PLATE].setExisting(guessing);
		tools[ToolButton.MAYBE_FORKS].setExisting(guessing);
		switch (currentToolId) {
		case ToolButton.PLATE:
			currentToolId = ToolButton.MAYBE_PLATE;
			break;
		case ToolButton.FORKS:
			currentToolId = ToolButton.MAYBE_FORKS;
			break;
		case ToolButton.MAYBE_PLATE:
			currentToolId = ToolButton.PLATE;
			break;
		case ToolButton.MAYBE_FORKS:
			currentToolId = ToolButton.FORKS;
			break;
		}
		if (!guessing) // make sure to deactivate plate if necessary
			updatePlateEnabled();
		// guess clear mode - only clears guesses
		// so clear button is disabled if no guess marks
		updateClearEnabled();
	}
	
	private void centerCam() {
		field.recenter(true);
		setCenterEnabled(false);
	}

	private void clearMarks() {
		Stroke clear = Stroke.newStroke(activeLayerId);
		for (int r = 0; r < activePuzzle.getRows(); r++) {
			for (int c = 0; c < activePuzzle.getColumns(); c++) {
				int oldMark = activePuzzle.getMark(r, c);
				if (!guessing && oldMark != Puzzle.EMPTY ||
					oldMark == Puzzle.MAYBE_FILLED || oldMark == Puzzle.MAYBE_FLAGGED) {
					clear.addChange(r, c, oldMark, false); // false bc clearing cannot cause mistakes
					activePuzzle.markSpot(r, c, Puzzle.EMPTY);
				}
			}
		}
		// disable the button
		tools[ToolButton.CLEAR].setEnabled(false);
		pushStroke(clear, Puzzle.EMPTY);
	}
	
	public void pushStroke(Stroke s, int drawMode) {
		if (s.isEmpty()) // empty stroke
			return;
		undo.add(s);
		// maintain undo history limit
		if (undo.size() > MAX_UNDO_HISTORY)
			undo.remove(0);
		// clear redo history
		redo.clear();
		updatePlateEnabled();
		updateClearEnabled();
		updateUndoRedoEnabled();
		// maybe lost?
		handleMistake(s);
		// maybe finished the board?
		boolean bwon = true;
		// check all puzzles are solved
		for (int i = 0; i < puzzleLayers.length; i++)
			bwon = bwon && puzzleLayers[i].isSolved();
		if (bwon)
			pictureWin();
	}
	
	public void undo() {
		doHistory(undo, redo);
	}
	
	public void redo() {
		doHistory(redo, undo);
	}
	
	public void doHistory(ArrayList<Stroke> from, ArrayList<Stroke> to) {
		Stroke toRevert = from.remove(from.size() - 1);
		int changedLayer = toRevert.getLayerId();
		Stroke toSave = Stroke.newStroke(changedLayer);
		Puzzle revPuzzle = getPuzzleByLayerId(changedLayer);
		//int mistakesDuringRevert = 0;
		for (int i = 0; i < toRevert.size(); i++) {
			int[] chngd = toRevert.getChange(i);
			int crow = chngd[Stroke.ROW];
			int ccol = chngd[Stroke.COL];
			// mistakes should never be in history since they are immediately fixed
			toSave.addChange(crow, ccol, revPuzzle.getMark(crow, ccol), false); // <- hence, false
			//boolean mistake = revPuzzle.markSpot(crow, ccol, chngd[Stroke.MARK]);
			//if (mistake)
			//	mistakesDuringRevert++;
		}
		to.add(toSave);
		updatePlateEnabled();
		updateClearEnabled();
		updateUndoRedoEnabled();
		// change to layer that was changed
		if (activeLayerId != changedLayer)
			layerClicked(changedLayer);
		//handleMistake(mistakesDuringRevert);
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	UI UPDATES
	 * ~~~~~~~~~~~~~~~~~~~~
	 */

	public void updatePlateEnabled() {
		tools[ToolButton.PLATE].setEnabled(hasPlates());
		// switch from this tool if disabled
		//if (!enabled && currentToolId == ToolButton.PLATE)
		//	toolClicked(ToolButton.FORKS);
	}

	public boolean hasPlates() {
		return activePuzzle.getRemainingFillCount() > 0;
	}
	
	public void updateClearEnabled() {
		for (int r = 0; r < activePuzzle.getRows(); r++) {
			for (int c = 0; c < activePuzzle.getColumns(); c++) {
				int mark = activePuzzle.getMark(r, c);
				if (mark != Puzzle.EMPTY && !guessing ||
					mark == Puzzle.MAYBE_FILLED || mark == Puzzle.MAYBE_FLAGGED) {
					tools[ToolButton.CLEAR].setEnabled(true);
					return;
				}
			}
		}
		tools[ToolButton.CLEAR].setEnabled(false);
	}

	public void setCenterEnabled(boolean enabled) {
		tools[ToolButton.CENTER].setEnabled(enabled);
	}
	
	public void updateUndoRedoEnabled() {
		// update whether buttons are enabled
		tools[ToolButton.UNDO].setEnabled(undo.size() > 0);
		tools[ToolButton.REDO].setEnabled(redo.size() > 0);
	}

	public void fadeSidebars(boolean fade) {
		toolbar.setFade(fade);
		layerbar.setFade(fade);
	}
	
	public void disableSidebars() {
		toolbar.setExisting(false);
		toolbar.setChildrenExisting(false);
		layerbar.setExisting(false);
		layerbar.setChildrenExisting(false);
	}
	
	private void updateScore() {
		int orig = scoreAnim.getIntValue();
		int nnew = getScore();
		scoreAnim.setFrom(orig);
		scoreAnim.setTo(nnew);
		scoreAnim.setDuration((int) (MS_PER_POINT * (nnew - orig)));
		scoreAnim.reset(true);
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~
	 * SCORE AND TIME STUFF
	 * ~~~~~~~~~~~~~~~~~~~~~
	 */
	
	public void increasePlateScore(int amount) {
		plateScore += amount;
		updateScore();
	}
	
	public void increaseCritterScore(int amount) {
		critterScore += amount;
		updateScore();
	}
	
	private int getScore() {
		// if beat the picture, add the bonus from the extra time
		int timeBonus = state == PICTURE_WINNING ? getTimeBonus() : 0;
		int missBonus = state == PICTURE_WINNING ? getMistakeBonus() : 0;
		return timeBonus + missBonus + plateScore + critterScore;
	}
	
	private int getMistakeBonus() {
		return MISTAKE_BONUS * (mistakeCap - mistakeCount);
	}
	
	private int getTimeBonus() {
		return (int) (MAX_TIME_BONUS * (getRemainingTime() / (double) timeSecLimit));
	}
	
	private int getRemainingTime() {
		return timeSecLimit - (int) clock.elapsedSec();
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~
	 * PARTICLES
	 * ~~~~~~~~~~~~~~~~~~~~~
	 */
	
	private void popParticle(int row, int col, int mark) {
		BufferedImage[] plates = Blanket.getPlateSheet(cellSize);
		BufferedImage[] forks = Blanket.getForkSheet(cellSize);
		int bx = field.getBlanket().getDisplayX();
		int by = field.getBlanket().getDisplayY();
		BufferedImage image = null;
		if (mark == Puzzle.FILLED)
			image = plates[activeLayerId + 1];
		else if (mark == Puzzle.FLAGGED)
			image = forks[0];
		else if (mark == Puzzle.MAYBE_FILLED)
			image = plates[4];
		else if (mark == Puzzle.MAYBE_FLAGGED)
			image = forks[2];
		if (image != null) {
			double randXVel = Math.random() * 200 - 100;
			double randYVel = Math.random() * 100 - 150;
			Particle.generateParticle(image, bx + col * cellSize, by + row * cellSize, randXVel, randYVel, 0.92, 0, 10, 7000);
		}
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~
	 * GAME HOOKS (LOSING/WINNING)
	 * ~~~~~~~~~~~~~~~~~~~~~
	 */
	
	public int getState() {
		return state;
	}
	
	private double getScoreLinesProgress() {
		if (state != PICTURE_WINNING)
			return 0;
		else
			return Math.min(1, (switchTimer.elapsed() - SCORE_LINES_DELAY) / (double) SWITCH_DURATION);
	}
	
	public double getSwitchHideProgress() {
		if (state != PICTURE_WINNING && state != COMPLETE_WINNING)
			return 0;
		else
			return Math.min(1, switchTimer.elapsed() / (double) SWITCH_HIDE_DURATION);
	}
	
	private void handleMistake(Stroke s) {
		if (s.hasMistake()) {
			int mr = s.getMistakeRow();
			int mc = s.getMistakeCol();
			// do mistake particle
			popParticle(mr, mc, activePuzzle.getMark(mr, mc));
			// clear the mistake spot
			activePuzzle.markSpot(mr, mc, Puzzle.EMPTY);
			mistakeCount++;
			if (mistakeCount >= mistakeCap)
				lose();
		}
	}
	
	private void pictureWin() {
		state = PICTURE_WINNING;
		scoreLines = new TextField[NUM_SCORE_LINES];
		clock.pause();
		disableSidebars();
		updateScore();
		finish();
	}
	
	private void lose() {
		state = LOSING;
		finish();
		// make particles for each cell
		for (int r = 0; r < activePuzzle.getRows(); r++)
			for (int c = 0; c < activePuzzle.getColumns(); c++)
				popParticle(r, c, activePuzzle.getMark(r, c));
		clearMarks();
		// add lose text
		boolean lostByMistakes = mistakeCount == mistakeCap;
		TextField loss = new TextField(lostByMistakes ? LOSE_MESSAGE_MISTAKE : LOSE_MESSAGE_TIME, FontBank.test, 0, 300, Engine.SCREEN_WIDTH);
		loss.setAlignment(TextField.ALIGN_CENTER);
		add(loss);
	}
	
	private void finish() {
		freezeInput(true);
		textDrop.resume();
		switchTimer.resume();
	}
	
	private void foodWin() {
		state = COMPLETE_WINNING;
		UserData.setPuzzleScore(world.getId(), level.getId(), getScore());
		finish();
	}

	private void goOn() {
		switch (state) {
		case PICTURE_WINNING:
			freezeInput(false);
			switchToFoodSolving();
			break;
		case COMPLETE_WINNING:
			LoadWinState lws = new LoadWinState(world, level);
			Engine.getEngine().getStateManager().transitionToState(lws, Transition.CURTAIN, 500, 750);
			break;
		case LOSING:
			Engine.getEngine().getStateManager().transitionExitState(Transition.FADE, 500, 0);
			break;
		}
	}

	public boolean isPictureOver() {
		return state > PICTURE_SOLVING;
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~
	 * FOOD SOLVING MINIGAME
	 * ~~~~~~~~~~~~~~~~~~~~~
	 */

	private static int[][] FOOD_TYPES = {
			{1, 1}, {1, 2}, {2, 1}, {2, 2}, {1, 3}, {3, 1}, {2, 3}, {3, 2}, {3, 3}
	};
	public static final int F_WIDTH = 0, F_HEIGHT = 1;
	public static final int NUM_FOOD_TYPES = FOOD_TYPES.length;
	
	private FoodBar foodbar;

	public static int[] getFoodById(int foodId) {
		return FOOD_TYPES[foodId];
	}
	
	public void switchToFoodSolving() {
		state = FOOD_SOLVING;
		// hide score lines
		for (int i = 0; i < scoreLines.length; i++)
			remove(scoreLines[i]);
		// center, not padded for hints
		field.recenter(false);
		// CREATE FOODS
		Puzzle p = getActivePuzzle();
		// use to keep track of how many of each food type was placed
		int[] foodsList = new int[NUM_FOOD_TYPES];
		boolean[][] foodMap = new boolean[p.getRows()][p.getColumns()];
		// mark any spots without plates true, rest are false (open)
		for (int r = 0; r < p.getRows(); r++)
			for (int c = 0; c < p.getColumns(); c++)
				foodMap[r][c] = !p.isFilledInSolution(r, c);
		// TODO to (maybe) make this better, choose grids randomly
		for (int r = 0; r < p.getRows(); r++) {
			for (int c = 0; c < p.getColumns(); c++) {
				// no food occupying this space - try placing
				if (!foodMap[r][c])
					// placeRandomFood returns the type id of food placed
					foodsList[placeRandomFood(foodMap, r, c)]++;
			}
		}
		// create "food bar"
		foodbar = new FoodBar(foodsList);
		add(foodbar);
	}
	
	private int placeRandomFood(boolean[][] foodMap, int r, int c) {
		// determine which foods could be placed here
		boolean[] valid = new boolean[NUM_FOOD_TYPES];
		int validCount = 0;
		for (int i = 0; i < NUM_FOOD_TYPES; i++) {
			valid[i] = canFoodGoHere(foodMap, FOOD_TYPES[i], r, c);
			if (valid[i])
				validCount++;
		}
		// generate a random number up to validCount
		int validNum = (int) (Math.random() * validCount);
		// find which food id that is
		int seen = 0;
		int foodType = -1;
		for (int i = 0; foodType < 0 && i < valid.length; i++) {
			if (valid[i])
				seen++;
			if (seen > validNum)
				foodType = i;
		}
		// place the chosen food item
		int[] cfood = FOOD_TYPES[foodType];
		for (int or = 0; or < cfood[F_WIDTH]; or++)
			for (int oc = 0; oc < cfood[F_HEIGHT]; oc++)
				foodMap[r + or][c + oc] = true;
		// return what id of food placed
		return foodType;
	}
	
	private boolean canFoodGoHere(boolean[][] foodMap, int[] food, int r, int c) {
		boolean can = true;
		// loop through all spaces this food would occupy
		// return true if all spaces are available (and exist)
		for (int or = 0; can && or < food[F_WIDTH]; or++) {
			for (int oc = 0; can && oc < food[F_HEIGHT]; oc++) {
					// check for out of bounds
				if (r + or >= foodMap.length ||
					c + oc >= foodMap[r].length ||
					// check for the space being full
					foodMap[r + or][c + oc])
					// if so, can't place here
					can = false;
			}
		}
		return can;
	}
	
	public void dragNewFood(FoodWidget fw) {
		field.getBlanket().startDrag(fw.getFoodId());
	}
	
	public void tryDropNewFood() {
		field.getBlanket().tryDrop();
	}
	
	public void returnFoodToWidget(int foodId) {
		// increment that food type
		foodbar.incrementWidget(foodId);
	}
	
	// ~~~~~~~~~~ TICK
	
	@Override
	public void tick() {
		super.tick();
		// check for time failure
		if (clock.elapsedSec() > timeSecLimit)
			lose();
		/* check to go on from this state (after lose/win)
		   switches after switch time has elapsed, or in the case
		   of PICTURE_WINNING state, when time is elapsed AND clicking */
		if (switchTimer.elapsed() > SWITCH_DURATION && (state != PICTURE_WINNING ||
				Input.getInstance().isPressingMouseButton(Input.LEFT_CLICK))) {
			switchTimer.reset(false);
			goOn();
		}
		// if mid-win state, staggered add score lines
		else if (state == PICTURE_WINNING) {
			double progress = getScoreLinesProgress();
			double step = (1.0 / NUM_SCORE_LINES);
			double progThresh = 0;
			for (int i = 0; i < NUM_SCORE_LINES; i++, progThresh += step) {
				// if progress has reached enough that line should exist (and it doesn't yet)
				if (scoreLines[i] == null && progress >= progThresh) {
					int iscore = 0;
					switch(i) {
					case 0: iscore = getTimeBonus(); break;
					case 1: iscore = getMistakeBonus(); break;
					case 2: iscore = plateScore; break;
					case 3: iscore = critterScore; break;
					case 4: iscore = getScore(); break;
					}
					String scoreStr = Integer.toString(iscore);
					// create new score line using the ith message - put in array and add to state
					final int strLength = 30;
					int numSpaces = strLength - SCORE_MESSAGES[i].length() - scoreStr.length();
					String message = SCORE_MESSAGES[i] + " ".repeat(numSpaces) + scoreStr;
					TextField tf = new TextField(message, FontBank.test, 0, 250 + i * 20, Engine.SCREEN_WIDTH);
					tf.setAlignment(TextField.ALIGN_CENTER);
					scoreLines[i] = tf;
					add(tf);
				}
			}
		}
	}
	
	// ~~~~~~~~~~ RENDER

	@Override
	public void render(Graphics g) {
		super.render(g);
		
		double switchHideProg = getSwitchHideProgress();
		// top bar rendering
		if (state < FOOD_SOLVING) {
			int offY = (int) (switchHideProg * -TOPBAR_HEIGHT);
			// translate if top bar is sliding away
			g.translate(0, offY);
			g.drawImage(ImageBank.topbar[0], 0, 0, null);
			int b;
			for (b = 24; b < Engine.SCREEN_WIDTH - 24; b += 24)
				g.drawImage(ImageBank.topbar[b % 48 > 0 ? 1 : 2], b, 0, null);
			g.drawImage(ImageBank.topbar[3], b, 0, null);
			g.drawImage(ImageBank.time, 15, 5, null);
			int time = getRemainingTime();
			int min = time / 60;
			int sec = time % 60;
			String timeString = String.format("%02d:%02d", min, sec);
			int x = 0;
			for (int i = 0; i < timeString.length(); i++) {
				char c = timeString.charAt(i);
				BufferedImage symbol;
				if (c == ':')
					symbol = ImageBank.colon;
				else
					symbol = ImageBank.digits[c - '0'];
				g.drawImage(symbol, 75 + x, c == ':' ? 12 : 7, null);
				x += symbol.getWidth() + 1;
			}
			g.drawImage(ImageBank.score, 140, 5, null);
			int rendScore = scoreAnim.getIntValue();
			StringBuilder scoreSB = new StringBuilder(Integer.toString(rendScore));
			int cc = 0;
			for (int i = scoreSB.length() - 1; i > 0; i--) {
				if ((cc + 1) % 3 == 0)
					scoreSB.insert(i, ',');
				cc++;
			}
			String scoreString = scoreSB.toString();
			x = 0;
			for (int i = 0; i < scoreString.length(); i++) {
				char c = scoreString.charAt(i);
				BufferedImage symbol;
				if (c == ',')
					symbol = ImageBank.comma;
				else
					symbol = ImageBank.digits[c - '0'];
				g.drawImage(symbol, 215 + x, c == ',' ? 20 : 7, null);
				x += symbol.getWidth() + 1;
			}
			g.drawImage(ImageBank.mistakes, 305, 5, null);
			for (int i = 0; i < mistakeCap; i++)
				g.drawImage(ImageBank.mistakeLives[mistakeCount < mistakeCap - i ? 0 : 1], 405 + i * 20, 5, null);
			// undo earlier translate, (if top bar is sliding away)
			g.translate(0, -offY);
		}

		// tool arrow
		Composite oldComp = toolbar.setRenderComposite(g);
		toolbar.setRenderClips(g);
		if (tools[currentToolId].isEnabled())
			g.drawImage(ImageBank.toolarrows[!guessing ? 0 : 1], 3, toolArrowAnim.getIntValue(), null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
		
		// streak viewer
		int streak = field.getBlanket().getStreak();
		if (streak > 0 && state == PICTURE_SOLVING) {
			final int max = ImageBank.streakWords.length - 1;
			int remTime = field.getBlanket().getStreakTimeRemaining();
			int fadeTime = Math.max(0, remTime + Blanket.STREAK_GAP);
			float opacity = Math.min(1f, fadeTime / (Blanket.STREAK_GAP / 2f));
			Graphics2D gg = (Graphics2D) g;
			gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g.drawImage(ImageBank.streakWords[Math.min(streak - 1, max)], STREAK_X, STREAK_Y, null);
			final int w = 75;
			int barW = (int) (w * Math.max(0, remTime) / (double) Blanket.STREAK_GAP);
			int yup = 20;
			g.setColor(Palette.BLACK);
			g.drawRect(STREAK_X, STREAK_Y - yup, w, 10);
			g.setColor(Palette.RED);
			g.fillRect(STREAK_X + w - barW, STREAK_Y + 1 - yup, barW, 9);
			gg.setComposite(oldComp);
		}
		// draw big text if during transition substates
		int textY = textDrop.getIntValue();
		if (state == LOSING) {
			g.drawImage(ImageBank.ohno, 37, textY, null);
		}
		else if (state == PICTURE_WINNING) {
			g.drawImage(ImageBank.youwin, 37, textY - 75, null);
		}
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	STATIC HELPERS
	 * ~~~~~~~~~~~~~~~~~~~~
	 */
	
	private static boolean selectable(int id) {
		return  id == ToolButton.PLATE ||
				id == ToolButton.FORKS ||
				id == ToolButton.MAYBE_PLATE ||
				id == ToolButton.MAYBE_FORKS ||
				id == ToolButton.PAN;
	}
	
	public static Color getColorByLayerId(int layerId) {
		switch (layerId) {
		case PuzzleState.NO_LAYER:
			return Palette.BEIGE;
		case PuzzleState.MAGENTA:
			return Palette.MAGENTA;
		case PuzzleState.YELLOW:
			return Palette.YELLOW;
		case PuzzleState.CYAN:
			return Palette.CYAN;
		}
		return Palette.BLACK;
	}
	
}
