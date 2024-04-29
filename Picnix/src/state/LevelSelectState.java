package state;

import java.awt.Graphics;

import engine.Engine;
import engine.Transition;
import picnix.Parallax;
import picnix.World;
import picnix.data.UserData;
import resource.bank.ImageBank;
import state.element.BackButton;
import state.element.LevelButton;
import state.element.TiledButton;
import state.load.LoadPuzzleState;

public class LevelSelectState extends ScrollableState {
	
	private static final int BUTTON_MARGIN = Engine.SCREEN_WIDTH / 2;
	private static final int SLIDE_THRESHOLD = 45;
	
	private World world;
	// tile background
	//private BufferedImage[] background;
	// parallax background
	private Parallax background;
	// buttons for navigating the list
	private TiledButton prev, next;
	// array of the level buttons
	private LevelButton[] levelButtons;
	
	public LevelSelectState(World world) {
		super(calculateWidth(world.getLevelCount()), Engine.SCREEN_HEIGHT);
		this.world = world;
		//background = ImageBank.tiledBackgrounds[world.getId()];
		background = new Parallax(true, false);
		background.addLayer(ImageBank.paratest1, 0, 6000, false);
		int bottom = Engine.SCREEN_HEIGHT - ImageBank.paratest2.getHeight();
		background.addLayer(ImageBank.paratest2, bottom, 2000, false);
		background.addLayer(ImageBank.paratest3, 0, 10000, true);
		background.resumeScroll();
		generateUI();
	}

	@Override
	public void focus(int status) {
		//if (status == NEWLY_OPENED)
		updateUnlockedLevels();
		slideTo(getFirstUnclearedLevel());
	}

	private void updateUnlockedLevels() {
		// AFTER we reach an unbeaten level, the remaining
		// levels will be set to disabled
		boolean unlocked = true;
		for (int i = 0; i < world.getLevelCount(); i++) {
			levelButtons[i].setEnabled(unlocked);
			unlocked &= UserData.isPuzzleCleared(world.getId(), i);
		}
	}
	
	private int getFirstUnclearedLevel() {
		int i;
		for (i = 0; i < world.getLevelCount(); i++)
			if (!UserData.isPuzzleCleared(world.getId(), i))
				return i;
		return i - 1;
	}
	
	private void generateUI() {
		levelButtons = new LevelButton[world.getLevelCount()];
		// level buttons
		for (int i = 0; i < levelButtons.length; i++) {
			levelButtons[i] = new LevelButton(this, i);
			scrollContainer.add(levelButtons[i]);
		}
		// navigation buttons
		prev = new TiledButton(15, 330, 48, 48) {
			@Override
			public void onButtonUp() {
				slidePrevious();
			}
		};
		next = new TiledButton(417, 330, 48, 48) {
			@Override
			public void onButtonUp() {
				slideNext();
			}
		};
		prev.setAllTileMaps(ImageBank.goldbutton, ImageBank.goldbuttonclick, ImageBank.buttondisabled);
		prev.setLabel(ImageBank.arrowlabels[0]);
		next.setAllTileMaps(ImageBank.goldbutton, ImageBank.goldbuttonclick, ImageBank.buttondisabled);
		next.setLabel(ImageBank.arrowlabels[1]);
		prev.setZ(2);
		next.setZ(2);
		add(prev);
		add(next);
		// disable arrow buttons on scroll bar
		scrollContainer.getHorizontalScroller().disableArrowButtons();
		scrollContainer.getHorizontalScroller().setNudgeSpeed(250);
		// add back button
		add(new BackButton());
	}

	public World getWorld() {
		return world;
	}
	
	public int getFocalLevelId() {
		int viewCenter = scrollContainer.getHorizontalScroller().getViewportOffset() + Engine.SCREEN_WIDTH / 4;
		return (int) Math.ceil(viewCenter / BUTTON_MARGIN);
	}

	public int getFocalDistance(int levelId) {
		int viewCenter = scrollContainer.getHorizontalScroller().getViewportOffset() + Engine.SCREEN_WIDTH / 2;
		int distance = getButtonX(levelId) - viewCenter;
		return distance;
	}
	
	private void slidePrevious() {
		int cur = getFocalLevelId();
		int dist = getFocalDistance(cur);
		int dest = dist < 0 && Math.abs(dist) > SLIDE_THRESHOLD ? cur : cur - 1;
		slideTo(dest);
	}

	private void slideNext() {
		int cur = getFocalLevelId();
		int dist = getFocalDistance(cur);
		int dest = dist > 0 && Math.abs(dist) > SLIDE_THRESHOLD ? cur : cur + 1;
		slideTo(dest);
	}
	
	private void slideTo(int levelId) {
		levelId = Math.max(0, Math.min(world.getLevelCount() - 1, levelId));
		int buttonViewCenter = getButtonX(levelId) - Engine.SCREEN_WIDTH / 2;
		scrollContainer.getHorizontalScroller().setViewportOffset(buttonViewCenter);
	}

	public void levelClicked(int id) {
		// if this is center-screen, launch level
		if (id == getFocalLevelId()) {
			LoadPuzzleState lss = new LoadPuzzleState(id, world.getId());
			Engine.getEngine().getStateManager().transitionToState(lss, Transition.FADE, 500, 0);
		}
		// otherwise, center this level button
		else
			slideTo(id);
	}
	
	@Override
	public void tick() {
		super.tick();
		// if at the end, disable prev or next button
		int viewOffset = scrollContainer.getHorizontalScroller().getViewportOffset();
		prev.setEnabled(viewOffset > 0);
		next.setEnabled(viewOffset < scrollContainer.getInnerWidth() - Engine.SCREEN_WIDTH);
	}
	
	@Override
	public void render(Graphics g) {
		/*
		// background tiling / chunking ;)
		for (int x = 0; x * 100 < Engine.SCREEN_WIDTH; x++) {
			for (int y = 0; y * 45 < Engine.SCREEN_HEIGHT; y++) {
				if (x + y * 10 >= background.length)
					continue;
				BufferedImage tile = background[x + y * 10];
				g.drawImage(tile, x * 100, y * 45, null);
			}
		}*/
		background.setScroll(scrollContainer.getScrollX());
		background.render(g);
		super.render(g);
	}

	public static int getButtonX(int levelId) {
		// + 1 for the blank room at the start
		return (levelId + 1) * BUTTON_MARGIN;
	}

	private static int calculateWidth(int size) {
		// + 1 for blank room on either side
		return (size + 1) * BUTTON_MARGIN;
	}
	
}
