package state;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Transition;
import picnix.World;
import resource.bank.ImageBank;
import state.element.BackButton;
import state.element.LevelButton;
import state.load.LoadPuzzleState;

public class LevelSelectState extends ScrollableState {

	public static final int TOP_MARGIN = 80;
	public static final int LEVEL_BUTTON_WIDTH = 48;
	public static final int LEVEL_BUTTON_HEIGHT = 48;
	public static final int LEVEL_BUTTON_MARGIN = 16;
	
	private World world;
	
	// tile background
	private BufferedImage[] background;
	
	public LevelSelectState(World world) {
		super(Engine.SCREEN_WIDTH, 1000);
				//calculateInnerHeight(world.getLevelCount()));
		this.world = world;
		background = ImageBank.tiledBackgrounds[world.getId()];
		setupLevels(world.getLevels());
		add(new BackButton());
	}

	@Override
	public void focus(int status) {
		// TODO Auto-generated method stub
	}

	public World getWorld() {
		return world;
	}
	
	private void setupLevels(boolean[] levels) {
		int xp, yp;
		xp = LEVEL_BUTTON_MARGIN;
		yp = TOP_MARGIN;
		for (int i = 0; i < levels.length; i++) {
			LevelButton lb = new LevelButton(this, i, xp, yp, LEVEL_BUTTON_WIDTH, LEVEL_BUTTON_HEIGHT);
			BufferedImage[] sheet = levels[i] ? ImageBank.layeredlevelbutton : ImageBank.normallevelbutton;
			lb.setBackgrounds(sheet[0], sheet[1], null);
			scrollContainer.add(lb);
			xp += LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_MARGIN;
			if (Engine.SCREEN_WIDTH - xp - LEVEL_BUTTON_WIDTH < LEVEL_BUTTON_MARGIN) {
				xp = LEVEL_BUTTON_MARGIN;
				yp += LEVEL_BUTTON_HEIGHT + LEVEL_BUTTON_MARGIN;
			}
		}
	}
	
	public static int calculateInnerHeight(int numLevels) {
		int h = TOP_MARGIN;
		int buttsPerRow = Engine.SCREEN_WIDTH / (LEVEL_BUTTON_WIDTH + LEVEL_BUTTON_MARGIN);
		h += LEVEL_BUTTON_HEIGHT * Math.ceil(numLevels / buttsPerRow) + LEVEL_BUTTON_MARGIN;
		return h;
	}

	public void levelClicked(int id) {
		LoadPuzzleState lss = new LoadPuzzleState(id, world.getId());
		Engine.getEngine().getStateManager().transitionToState(lss, Transition.FADE, 500, 0);
	}
	
	@Override
	public void render(Graphics g) {
		int camY = scrollContainer.getScrollY();
		// background tiling / chunking ;)
		g.translate(0, -camY);
		for (int x = 0; x * 100 < Engine.SCREEN_WIDTH; x++) {
			for (int y = camY / 45; y * 45 < Engine.SCREEN_HEIGHT + camY; y++) {
				if (x + y * 10 >= background.length)
					continue;
				BufferedImage tile = background[x + y * 10];
				g.drawImage(tile, x * 100, y * 45, null);
			}
		}
		g.translate(0, camY);
		super.render(g);
	}
	
}
