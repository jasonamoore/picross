package state;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Engine;
import picnix.Level;
import picnix.puzzle.Blanket;
import picnix.puzzle.Field;
import picnix.puzzle.Puzzle;
import picnix.puzzle.Stroke;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Icon;
import state.element.puzzle.LayerButton;
import state.element.puzzle.LayerProgress;
import state.element.puzzle.Sidebar;
import state.element.puzzle.ToolButton;
import state.particle.Particle;
import util.Animation;
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
	
	public PuzzleState(Level level) {
		layered = level.isLayered();
		timeSecLimit = level.getTimeLimit() * 60;
		mistakeCap = level.getMistakeCap();
		puzzleLayers = level.getPuzzles();
		activeLayerId = layered ? MAGENTA : NO_LAYER;
		activePuzzle = puzzleLayers[Math.max(0, activeLayerId)];
		setup();
	}
		
	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	STATE SETUP
	 * ~~~~~~~~~~~~~~~~~~~~
	 */
	
	/**
	 * Determines the puzzle cell size, creates a Field,
	 * sets the current tool to "plate", and initializes
	 * empty undo/redo arrays.
	 * Then calls {@link #generateUI()} Ito create and setup UI elements.
	 */
	private void setup() {
		int msize = Math.max(activePuzzle.getRows(), activePuzzle.getColumns());
		cellSize =  msize <= 5 ? CELL_SIZE_5x5 :
			msize <= 10 ? CELL_SIZE_10x10 :
			msize <= 15 ? CELL_SIZE_15x15 :
			/*msize>15?*/ CELL_SIZE_20x20;
		field = new Field(this);
		add(field);
		toolArrowAnim = new Animation(100, Animation.EASE_OUT, Animation.NO_LOOP);
		toolClicked(ToolButton.PLATE);
		undo = new ArrayList<Stroke>();
		redo = new ArrayList<Stroke>();
		clock = new Timer(true);
		generateUI();
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
		field.recenter();
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
		if (s.size() < 1) // empty stroke
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
		handleMistake(s.getMistakes());
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
		int mistakesDuringRevert = 0;
		for (int i = 0; i < toRevert.size(); i++) {
			int[] chngd = toRevert.getChange(i);
			int crow = chngd[Stroke.ROW];
			int ccol = chngd[Stroke.COL];
			// don't save mistakes in history; will be checked here when reverting
			toSave.addChange(crow, ccol, revPuzzle.getMark(crow, ccol), false); // <- hence, false
			boolean mistake = revPuzzle.markSpot(crow, ccol, chngd[Stroke.MARK]);
			if (mistake)
				mistakesDuringRevert++;
		}
		to.add(toSave);
		updatePlateEnabled();
		updateClearEnabled();
		updateUndoRedoEnabled();
		// change to layer that was changed
		if (activeLayerId != changedLayer)
			layerClicked(changedLayer);
		handleMistake(mistakesDuringRevert);
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	UI UPDATES
	 * ~~~~~~~~~~~~~~~~~~~~
	 */

	public void updatePlateEnabled() {
		boolean enabled = activePuzzle.getRemainingFillCount() > 0;
		tools[ToolButton.PLATE].setEnabled(enabled);
		// switch from this tool if disabled
		//if (!enabled && currentToolId == ToolButton.PLATE)
		//	toolClicked(ToolButton.FORKS);
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
	
	/* ~~~~~~~~~~~~~~~~~~~~~
	 * GAME HOOKS (LOSING/WINNING)
	 * ~~~~~~~~~~~~~~~~~~~~~
	 */
	
	private void handleMistake(int count) {
		if (count < 1)
			return;
		mistakeCount += count;
		if (mistakeCount >= mistakeCap)
			lose();
		Particle.generateParticle(ImageBank.plates35[0], 200, 200, -100, -300, 0.90, 0, 10, 10000);
	}
	
	private void win() {
		
	}
	
	private void lose() {
		freezeInput(true);
		clearMarks();
	}
	
	// ~~~~~~~~~~ TICK
	
	@Override
	public void tick() {
		super.tick();
		// check for time failure
		if (clock.elapsedSec() > timeSecLimit)
			lose();
	}
	
	// ~~~~~~~~~~ RENDER

	@Override
	public void render(Graphics g) {
		super.render(g);
		// tool arrow
		Composite oldComp = toolbar.setRenderComposite(g);
		toolbar.setRenderClips(g);
		if (tools[currentToolId].isEnabled())
			g.drawImage(ImageBank.toolarrows[!guessing ? 0 : 1], 3, toolArrowAnim.getIntValue(), null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
		// top bar
		g.drawImage(ImageBank.topbar[0], 0, 0, null);
		int b;
		for (b = 24; b < Engine.SCREEN_WIDTH - 24; b += 24)
			g.drawImage(ImageBank.topbar[b % 48 > 0 ? 1 : 2], b, 0, null);
		g.drawImage(ImageBank.topbar[3], b, 0, null);
		g.drawImage(ImageBank.time, 20, 5, null);
		int time = timeSecLimit - (int) clock.elapsedSec();
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
			g.drawImage(symbol, 80 + x, c == ':' ? 12 : 7, null);
			x += symbol.getWidth() + 1;
		}
		g.drawImage(ImageBank.score, 160, 5, null);
		g.drawImage(ImageBank.mistakes, 300, 5, null);
		for (int i = 0; i < mistakeCap; i++)
			g.drawImage(ImageBank.mistakeLives[mistakeCount < mistakeCap - i ? 0 : 1], 400 + i * 20, 5, null);
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
