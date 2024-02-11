package state;

import picnic.Field;
import picnic.Sidebar;
import puzzle.Puzzle;
import resource.bank.ImageBank;
import state.element.Container;
import state.element.ToolButton;

public class PuzzleState extends State {

	// the puzzle
	private Puzzle puzzle;
	
	// an object to start field data, like the blanket and critters
	private Field field;
	
	// animation fields for UI stuff
	// tool collapse left anim (note: can reuse an array ;))
	// layer collapse right anim
	
	// tool and layer windows
	private Sidebar toolbar;
	private Sidebar layerbar;
	
	private boolean guessing;
	private int currentToolId;
	
	public PuzzleState(Puzzle puzzle) {
		this.puzzle = puzzle;
		field = new Field(this, puzzle);
		add(field);
		currentToolId = ToolButton.PLATE;
		generateUI();
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}

	private static final int NUM_TOOLS = ToolButton.REDO + 1;
	private ToolButton[] tools;
	
	private void generateUI() {
		toolbar = new Sidebar(Sidebar.TOOLBAR_X);
		layerbar = new Sidebar(Sidebar.LAYERBAR_X);
		toolbar.setBackground(ImageBank.toolbar);
		layerbar.setBackground(ImageBank.layerbar);
		add(toolbar);
		add(layerbar);
		tools = new ToolButton[NUM_TOOLS];
		// make the "normal" tools
		for (int i = 0; i < ToolButton.UNDO; i++) {
			tools[i] = new ToolButton(this, i, 16, 24 + (44 * (i % 6)), 45, 40);
			toolbar.add(tools[i]);
		}
		// make undo and redo
		for (int i = 0; i <= 1; i++) {
			tools[ToolButton.UNDO + i] = new ToolButton(this, ToolButton.UNDO + i, 15 + (i * 24), 288, 23, 40);
			toolbar.add(tools[ToolButton.UNDO + i]);
		}
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
			}
		}
	}

	private void centerCam() {
		field.recenter();
	}

	private void clearMarks() {
		for (int r = 0; r < puzzle.getRows(); r++) {
			for (int c = 0; c < puzzle.getColumns(); c++) {
				puzzle.markSpot(r, c, Puzzle.UNCLEARED);
			}
		}
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

	public void fadeSiderbars(boolean fade) {
		toolbar.setFade(fade);
		layerbar.setFade(fade);
	}
	
}
