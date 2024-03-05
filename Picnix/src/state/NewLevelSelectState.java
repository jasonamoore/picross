package state;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Engine;
import engine.Transition;
import picnix.World;
import picnix.data.UserData;
import resource.bank.ImageBank;
import state.element.Button;
import state.element.NewLevelButton;
import state.load.LoadPuzzleState;
import util.Animation;

public class NewLevelSelectState extends State {

	public static final int TURNSTILE_COUNT = 3;
	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	
	private World world;
	// tile background
	private BufferedImage[] background;
	// the three buttons visible on the turnstile
	private NewLevelButton[] turnstile;
	// buttons for navigating the turnstile
	private Button prev, next;
	// currently selected (centered level)
	private int curLevelId;
	
	private Animation turnAnim;
	
	public NewLevelSelectState(World world) {
		this.world = world;
		background = ImageBank.tiledBackgrounds[world.getId()];
		curLevelId = getFirstUnclearedLevel();
		turnAnim = new Animation(0, 1, 500, Animation.EASE_OUT, Animation.NO_LOOP, false);
		generateUI();
	}

	private int getFirstUnclearedLevel() {
		int i;
		for (i = 0; i < world.getLevelCount(); i++)
			if (!UserData.isPuzzleCleared(world.getId(), i))
				return i;
		return i - 1;
	}

	@Override
	public void focus(int status) {
		slide(getFirstUnclearedLevel() - curLevelId);
	}

	public World getWorld() {
		return world;
	}
	
	private void generateUI() {
		// turnstile buttons
		turnstile = new NewLevelButton[TURNSTILE_COUNT];
		for (int i = 0; i < TURNSTILE_COUNT; i++) {
			turnstile[i] = new NewLevelButton(this, i);
			add(turnstile[i]);
		}
		// navigation buttons
		Button back = new Button(5, 5, 20, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered())
					navigateBack();
			}
		};
		prev = new Button(5, 300, 20, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered())
					slidePrevious();
			}
		};
		next = new Button(300, 300, 20, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (beingHovered())
					slideNext();
			}
		};
		back.setBackground(ImageBank.normallevelbutton[0]);
		prev.setBackground(ImageBank.normallevelbutton[0]);
		next.setBackground(ImageBank.normallevelbutton[0]);
		add(back);
		add(prev);
		add(next);
	}

	public int getButtonX(int tid) {
		///int atid = turnAnim.getValue() < 0.5 ? tid : Math.abs(tid - 2);
		int atid = tid;
		if (turnAnim.getValue() > 0.5 && tid == 0)
			atid = 3;
		double maybe = -NewLevelButton.WIDTH / 2 + Engine.SCREEN_WIDTH / 2 * (atid - turnAnim.getValue());
		return (int) maybe;
	}
	
	public int getButtonY(int tid) {
		return 100;
	}
	
	public double getButtonScale(int tid) {
		return 1;
	}

	public int getLevelId(int tid) {
		return curLevelId - tid - 1;
	}
	
	private void slidePrevious() {
		slide(-1);
	}
	
	private void slideNext() {
		slide(1);
	}
	
	private void slide(int amount) {
		// find new level id, update curLevelId
		int newId = Math.max(0, Math.min(world.getLevelCount() - 1, curLevelId + amount));
		// the actual amount, after capping id within bounds
		amount = newId - curLevelId;
		curLevelId = newId;
		// hide left or right buttons if at the edge
		boolean prevExists = curLevelId > 0;
		turnstile[LEFT].setExisting(prevExists);
		prev.setEnabled(prevExists);
		boolean nextExists = curLevelId < world.getLevelCount() - 1;
		turnstile[RIGHT].setExisting(nextExists);
		next.setEnabled(nextExists);
		// restart anim (only if turnstile actually moved)
		if (amount != 0) {
			turnAnim.setForward(amount > 0); // if negative, reverse direction
			turnAnim.reset(true);
		}
		System.out.println(curLevelId);
	}
	
	private void navigateBack() {
		Engine.getEngine().getStateManager().transitionExitState(Transition.FADE, 500, 0);
	}

	public void levelClicked(int id) {
		LoadPuzzleState lss = new LoadPuzzleState(id, world.getId());
		Engine.getEngine().getStateManager().transitionToState(lss, Transition.FADE, 500, 0);
	}
	
	@Override
	public void tick() {
		super.tick();
		// enable turnstile buttons only if animation is not playing
		for (int i = 0; i < TURNSTILE_COUNT; i++)
			turnstile[i].setEnabled(!turnAnim.isPlaying());
	}
	
	@Override
	public void render(Graphics g) {
		// background tiling / chunking ;)
		for (int x = 0; x * 100 < Engine.SCREEN_WIDTH; x++) {
			for (int y = 0; y * 45 < Engine.SCREEN_HEIGHT; y++) {
				if (x + y * 10 >= background.length)
					continue;
				BufferedImage tile = background[x + y * 10];
				g.drawImage(tile, x * 100, y * 45, null);
			}
		}
		super.render(g);
	}
	
}
