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
import state.element.Icon;
import state.element.LayerButton;
import state.element.LayerProgress;
import state.element.Sidebar;
import state.element.ToolButton;

public class PuzzleState extends State {

	// grid cell sizes
	public static final int CELL_SIZE_5x5 = 35;
	public static final int CELL_SIZE_10x10 = 20;
	public static final int CELL_SIZE_15x15 = 15;
	public static final int CELL_SIZE_20x20 = 10;
	
	private int cellSize;

	public static final int NO_LAYER = -1;
	public static final int MAGENTA = 0;
	public static final int YELLOW = 1;
	public static final int CYAN = 2;
	public static final int TOTAL = 3;
	
	// the puzzle
	private Puzzle[] puzzleLayers;
	private boolean layered;
	
	// the puzzle
	private Puzzle activePuzzle;
	private int activeLayerId;
	
	// an object to start field data, like the blanket and critters
	private Field field;
	
	// tool and layer windows
	private Sidebar toolbar;
	private Sidebar layerbar;

	private static final int NUM_TOOLS = ToolButton.REDO + 1;
	private ToolButton[] tools;

	private static final int NUM_LAYERS = 3;
	private LayerButton[] layers;
	private LayerProgress[] layerProgs;
	private Icon totalLabel;
	private LayerProgress totalProg;
	
	private boolean guessing;
	private int currentToolId;

	private ArrayList<Stroke> undo;
	private ArrayList<Stroke> redo;
	
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

	public int getActiveLayerId() {
		return activeLayerId;
	}
	
	public Puzzle getActivePuzzle() {
		return activePuzzle;
	}
	
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
		int hintMax = activePuzzle.getLongestRowHint();
		for (int i = 0; i < puzzleLayers.length; i++)
			hintMax = Math.max(hintMax, puzzleLayers[i].getLongestRowHint());
		return Blanket.getHintScrollWidth(cellSize) * hintMax;
	}
	
	public int getPuzzleTopPadding() {
		int hintMax = activePuzzle.getLongestColumnHint();
		for (int i = 0; i < puzzleLayers.length; i++)
			hintMax = Math.max(hintMax, puzzleLayers[i].getLongestColumnHint());
		return Blanket.getHintScrollWidth(cellSize) * hintMax;
	}
	
	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}
	
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
			Color[] colors = new Color[] {
				new Color(0xff00ff),
				new Color(0xffff00),
				new Color(0x00ffff)
			};
			for (int i = MAGENTA; i <= CYAN; i++) {
				layers[i] = new LayerButton(this, i, 10, 27 + (i * 86), 62, 65);
				layerbar.add(layers[i]);
				layerProgs[i] = new LayerProgress(puzzleLayers[i], colors[i], 10, 94 + (i * 86), 62, 15);
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
	
	public int getCurrentTool() {
		return currentToolId;
	}
	
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

	private void centerCam() {
		field.recenter();
		setCenterEnabled(false);
	}

	private void clearMarks() {
		Stroke clear = new Stroke(activeLayerId);
		for (int r = 0; r < activePuzzle.getRows(); r++) {
			for (int c = 0; c < activePuzzle.getColumns(); c++) {
				int oldMark = activePuzzle.getMark(r, c);
				if (oldMark != Puzzle.UNCLEARED)
					clear.addChange(r, c, activePuzzle.getMark(r, c));
				activePuzzle.markSpot(r, c, Puzzle.UNCLEARED);
			}
		}
		// disable the button
		tools[ToolButton.CLEAR].setDisabled(true);
		pushStroke(clear, Puzzle.UNCLEARED);
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
		updateClearEnabled();
		updateUndoRedoEnabled();
		// change to layer that was changed
		if (activeLayerId != changedLayer)
			layerClicked(changedLayer);
	}
	
	public void pushStroke(Stroke s, int drawMode) {
		if (s.size() < 1) // empty stroke
			return;
		undo.add(s);
		// clear redo history
		redo = new ArrayList<Stroke>();
		updateClearEnabled();
		updateUndoRedoEnabled();
	}
		
	public void updateClearEnabled() {
		for (int r = 0; r < activePuzzle.getRows(); r++)
			for (int c = 0; c < activePuzzle.getColumns(); c++)
				if (activePuzzle.getMark(r, c) != Puzzle.UNCLEARED) {
					tools[ToolButton.CLEAR].setDisabled(false);
					return;
				}
		tools[ToolButton.CLEAR].setDisabled(true);
	}
	
	public void updateUndoRedoEnabled() {
		// update whether buttons are enabled
		tools[ToolButton.UNDO].setDisabled(undo.size() < 1);
		tools[ToolButton.REDO].setDisabled(redo.size() < 1);
	}
	
	public void setCenterEnabled(boolean enabled) {
		tools[ToolButton.CENTER].setDisabled(!enabled);
	}

	public boolean isGuessing() {
		return guessing;
	}
	
	private void toggleGuess() {
		guessing = !guessing;
		tools[ToolButton.PLATE].setVisible(!guessing);
		tools[ToolButton.FORKS].setVisible(!guessing);
		tools[ToolButton.MAYBE_PLATE].setVisible(guessing);
		tools[ToolButton.MAYBE_FORKS].setVisible(guessing);
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
	}

	private boolean selectable(int id) {
		return  id == ToolButton.PLATE ||
				id == ToolButton.FORKS ||
				id == ToolButton.MAYBE_PLATE ||
				id == ToolButton.MAYBE_FORKS ||
				id == ToolButton.PAN;
	}

	public void fadeSidebars(boolean fade) {
		toolbar.setFade(fade);
		layerbar.setFade(fade);
	}

	public void layerClicked(int layerId) {
		layers[activeLayerId].setActive(false);
		activeLayerId = layerId;
		activePuzzle = getPuzzleByLayerId(layerId);
		layers[layerId].setActive(true);
		updateClearEnabled();
	}

	public Puzzle getPuzzleByLayerId(int layerId) {
		if (layerId == NO_LAYER)
			return activePuzzle;
		return puzzleLayers[layerId];
	}

	@Override
	public void render(Graphics g) {
		super.render(g);
		g.drawImage(ImageBank.topbar[0], 0, 0, null);
		int i;
		for (i = 24; i < Engine.SCREEN_WIDTH - 24; i += 24)
			g.drawImage(ImageBank.topbar[i % 48 > 0 ? 1 : 2], i, 0, null);
		g.drawImage(ImageBank.topbar[3], i, 0, null);
	}
	
}
