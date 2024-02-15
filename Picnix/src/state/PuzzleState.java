package state;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import engine.Engine;
import picnic.Blanket;
import picnic.Field;
import picnic.Stroke;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import resource.bank.Palette;
import state.element.Icon;
import state.element.LayerButton;
import state.element.LayerProgress;
import state.element.Sidebar;
import state.element.ToolButton;

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

	// dynamic sequences of Strokes, used to undo/redo draws
	private static final int MAX_UNDO_HISTORY = 50;
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
	
	public PuzzleState(Puzzle puzzle) {
		layered = false;
		puzzleLayers = new Puzzle[] {puzzle};
		activeLayerId = NO_LAYER;
		activePuzzle = puzzle;
		setup();
	}
	
	public PuzzleState(Puzzle magenta, Puzzle yellow, Puzzle cyan) {
		layered = true;
		puzzleLayers = new Puzzle[] {magenta, yellow, cyan};
		activeLayerId = MAGENTA;
		activePuzzle = puzzleLayers[activeLayerId];
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
		currentToolId = ToolButton.PLATE;
		undo = new ArrayList<Stroke>();
		redo = new ArrayList<Stroke>();
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
			for (int i = MAGENTA; i <= CYAN; i++) {
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
		if (selectable(id))
			currentToolId = id;
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
		Stroke clear = new Stroke(activeLayerId);
		for (int r = 0; r < activePuzzle.getRows(); r++) {
			for (int c = 0; c < activePuzzle.getColumns(); c++) {
				int oldMark = activePuzzle.getMark(r, c);
				if (!guessing && oldMark != Puzzle.UNCLEARED ||
					oldMark == Puzzle.MAYBE_CLEARED || oldMark == Puzzle.MAYBE_FLAGGED) {
					clear.addChange(r, c, activePuzzle.getMark(r, c));
					activePuzzle.markSpot(r, c, Puzzle.UNCLEARED);
				}
			}
		}
		// disable the button
		tools[ToolButton.CLEAR].setEnabled(false);
		pushStroke(clear, Puzzle.UNCLEARED);
	}
	
	public void pushStroke(Stroke s, int drawMode) {
		if (s.size() < 1) // empty stroke
			return;
		undo.add(s);
		// maintain undo history limit
		if (undo.size() > MAX_UNDO_HISTORY)
			undo.remove(0);
		// clear redo history
		redo = new ArrayList<Stroke>();
		updatePlateEnabled();
		updateClearEnabled();
		updateUndoRedoEnabled();
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
		Stroke toSave = new Stroke(changedLayer);
		Puzzle revPuzzle = getPuzzleByLayerId(changedLayer);
		for (int i = 0; i < toRevert.size(); i++) {
			int[] chngd = toRevert.getChange(i);
			int crow = chngd[Stroke.ROW];
			int ccol = chngd[Stroke.COL];
			toSave.addChange(crow, ccol, revPuzzle.getMark(crow, ccol));
			revPuzzle.markSpot(crow, ccol, chngd[Stroke.MARK]);
		}
		to.add(toSave);
		updatePlateEnabled();
		updateClearEnabled();
		updateUndoRedoEnabled();
		// change to layer that was changed
		if (activeLayerId != changedLayer)
			layerClicked(changedLayer);
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~
	 * 	UI UPDATES
	 * ~~~~~~~~~~~~~~~~~~~~
	 */
	
	public void updatePlateEnabled() {
		boolean enabled = activePuzzle.getRemainingClearCount() > 0;
		tools[ToolButton.PLATE].setEnabled(enabled);
		// switch from this tool if disabled
		//if (!enabled && currentToolId == ToolButton.PLATE)
		//	toolClicked(ToolButton.FORKS);
	}
	
	public void updateClearEnabled() {
		for (int r = 0; r < activePuzzle.getRows(); r++) {
			for (int c = 0; c < activePuzzle.getColumns(); c++) {
				int mark = activePuzzle.getMark(r, c);
				if (mark != Puzzle.UNCLEARED && !guessing ||
					mark == Puzzle.MAYBE_CLEARED || mark == Puzzle.MAYBE_FLAGGED) {
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

	// ~~~~~~~~~~ RENDER
	
	@Override
	public void render(Graphics g) {
		super.render(g);
		g.drawImage(ImageBank.topbar[0], 0, 0, null);
		int i;
		for (i = 24; i < Engine.SCREEN_WIDTH - 24; i += 24)
			g.drawImage(ImageBank.topbar[i % 48 > 0 ? 1 : 2], i, 0, null);
		g.drawImage(ImageBank.topbar[3], i, 0, null);
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
