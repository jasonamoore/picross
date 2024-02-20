package state;

import java.awt.image.BufferedImage;

import engine.Engine;
import puzzle.Level;
import resource.bank.ImageBank;
import state.element.LevelButton;

public class LevelSelectState extends ScrollableState {

	public static final int LEVEL_BUTTON_WIDTH = 48;
	public static final int LEVEL_BUTTON_HEIGHT = 48;
	public static final int LEVEL_BUTTON_MARGIN = 16;
	
	private Level[] levels;
	
	public LevelSelectState(Level[] list) {
		super(Engine.SCREEN_WIDTH, calculateInnerHeight(list.length));
		levels = list;
		setupLevels(levels);
	}

	private void setupLevels(Level[] list) {
		int xp, yp;
		xp = yp = LEVEL_BUTTON_MARGIN;
		for (int i = 0; i < list.length; i++) {
			LevelButton lb = new LevelButton(this, i, xp, yp, LEVEL_BUTTON_WIDTH, LEVEL_BUTTON_HEIGHT);
			BufferedImage[] sheet = list[i].isLayered() ? ImageBank.layeredlevelbutton : ImageBank.normallevelbutton;
			lb.setBackgrounds(sheet[0], sheet[1], null);
			add(lb);
			xp += LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_MARGIN;
			if (Engine.SCREEN_WIDTH - xp - LEVEL_BUTTON_WIDTH < LEVEL_BUTTON_MARGIN) {
				xp = LEVEL_BUTTON_MARGIN;
				yp += LEVEL_BUTTON_HEIGHT + LEVEL_BUTTON_MARGIN;
			}
		}
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}
	
	public static int calculateInnerHeight(int numLevels) {
		int h = LEVEL_BUTTON_MARGIN;
		int buttsPerRow = Engine.SCREEN_WIDTH / (LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_MARGIN);
		h += LEVEL_BUTTON_HEIGHT * Math.ceil(numLevels / buttsPerRow) + LEVEL_BUTTON_MARGIN;
		return h;
	}

	public void levelClicked(int id) {
		Engine.getEngine().getStateManager().openState(new PuzzleState(levels[id]), State.NEWLY_OPENED);
	}
	
}
