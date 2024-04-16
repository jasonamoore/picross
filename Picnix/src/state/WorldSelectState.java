package state;

import java.awt.Graphics;

import engine.Engine;
import engine.Input;
import engine.Transition;
import picnix.Island;
import picnix.World;
import picnix.data.UserData;
import resource.bank.FontBank;
import resource.bank.ImageBank;
import state.element.BackButton;
import state.element.TiledButton;
import state.element.location.LocationBox;
import state.load.LoadLevelSelectState;
import util.Animation;

public class WorldSelectState extends State {

	public static final int SKY_HEIGHT = 120;
	public static final int OFF_X = -125;
	public static final int OFF_Y = 50;
	public static final double SCALE = 1;
	
	private static final int ISLAND_SCROLL_THRESHOLD = 2;
	
	private LocationBox locationBox;
	private TiledButton left, right;

	private int curLoc;
	private int highLoc;

	private Animation fadeIn;
	private Animation smoothBox;
	private Animation smoothRot;
	
	public WorldSelectState(double initRot) {
		fadeIn = new Animation(0, 1, 100, Animation.EASE_OUT, Animation.NO_LOOP, false);
		smoothBox = new Animation(0, 1, 125, Animation.CUBIC, Animation.NO_LOOP, false);
		smoothRot = new Animation(initRot, 0, 500, Animation.EASE_OUT, Animation.NO_LOOP, true);
		locationBox = new LocationBox(this);
		left = new TiledButton(15, 350, 48, 48) {
			@Override
			public void onButtonUp() {
				switchLocation(-1);
			}
			@Override
			public float getOpacity() {
				return (float) fadeIn.getValue();
			}
		};
		right = new TiledButton(240, 350, 48, 48) {
			@Override
			public void onButtonUp() {
				switchLocation(1);
			}
			@Override
			public float getOpacity() {
				return (float) fadeIn.getValue();
			}
		};
		add(locationBox);
		add(left);
		add(right);
		left.setAllTileMaps(ImageBank.goldbutton, ImageBank.goldbuttonclick, ImageBank.buttondisabled);
		//left.setMiddleFill(Palette.YELLOW);
		left.setLabel(ImageBank.arrowlabels[0]);
		right.setAllTileMaps(ImageBank.goldbutton, ImageBank.goldbuttonclick, ImageBank.buttondisabled);
		//right.setMiddleFill(Palette.YELLOW);
		right.setLabel(ImageBank.arrowlabels[1]);
		right.setTooltip("press this button to go to the right", FontBank.test, 50, 0, true);
		BackButton back = new BackButton() {
			@Override
			public float getOpacity() {
				return (float) fadeIn.getValue();
			}
		};
		add(back);
	}
	
	@Override
	public void focus(int status) {
		if (status == NEWLY_OPENED)
			smoothBox.reset(true);
		fadeIn.setForward(true);
		fadeIn.reset(true);
		// update info for this location
		locationBox.update(curLoc);
		highLoc = findHighestUnlockedLocation();
		// disable if no locs are unlocked
		left.setEnabled(highLoc > 0);
		right.setEnabled(highLoc > 0);
	}

	@Override
	public void navigateBack() {
		fadeIn.setForward(false);
		fadeIn.reset(true);
		smoothBox.setForward(false);
		smoothBox.resume();
		TitleState ts = (TitleState) Engine.getEngine().getStateManager().getPreviousState();
		ts.setZoomAnim(false, 0, smoothRot.getValue());
		Engine.getEngine().getStateManager().transitionExitState(Transition.NONE, 125, TitleState.ZOOM_TIME);
	}

	public void open(boolean easy) {
		int worldId = easy ? World.getEasyWorldId(curLoc) : World.getHardWorldId(curLoc);
		LoadLevelSelectState lws = new LoadLevelSelectState(worldId);
		Engine.getEngine().getStateManager().transitionToState(lws, Transition.FADE, 500, 0);
	}
	
	public int getBoxY() {
		return (int) ((1 - smoothBox.getValue()) * -25);
	}
	
	public float getBoxOpacity() {
		return (float) smoothBox.getValue();
	}
	
	private int findHighestUnlockedLocation() {
		int i;
		for (i = 0; i < World.NUM_LOCATIONS; i++) {
			int easyId = World.getEasyWorldId(i);
			int compLevs = UserData.getPuzzlesCompleted(easyId);
			if (compLevs < World.getWorld(easyId).getLevelCount())
				break;
		}
		return i;
	}
	
	private void switchLocation(int amount) {
		int oldLoc = curLoc;
		curLoc = (curLoc - amount + World.NUM_LOCATIONS) % highLoc;
		smoothRotate(oldLoc);
		smoothBox.setForward(false);
		smoothBox.resume();
		locationBox.setEnabled(false);
	}
	
	private void smoothRotate(int oldLoc) {
		double from = smoothRot.getValue();
		double oldRot = World.getRadians(oldLoc);
		double newRot = World.getRadians(curLoc);
		if (Math.abs(newRot - oldRot) > World.getMaxRadianDistance()) {
			if (oldRot > 0)
				from = from - 2 * Math.PI;
			else
				from = 2 * Math.PI + from;
		}
		smoothRot.setFrom(from);
		smoothRot.setTo(newRot);
		smoothRot.reset(true);
	}
	
	@Override
	public void tick() {
		super.tick();
		Input input = Input.getInstance();
		// check for rotating the island (ignore scroll if no locs unlocked)
		double scroll = highLoc > 0 ? input.getUnconsumedScrollAmount() : 0;
		if (scroll < -ISLAND_SCROLL_THRESHOLD) {
			switchLocation(-1);
			input.consumeMouseWheelScroll();
		}
		else if (scroll > ISLAND_SCROLL_THRESHOLD) {
			switchLocation(1);
			input.consumeMouseWheelScroll();
		}
		// updating box fade/slide animation stuff
		// if the box animation is stopped
		if (!smoothBox.isPlaying()) {
			// re-enable the box controls if faded in
			if (smoothBox.isForward())
				locationBox.setEnabled(true);
			// if faded out: make box fade back in
			else {
				smoothBox.reverse(true);
				//UserData.randomizeScores();
				locationBox.update(curLoc);
			}
		}
	}

	@Override
	public void render(Graphics g) {
		Island.renderIsland(g, SKY_HEIGHT, OFF_X, OFF_Y, SCALE, smoothRot.getValue());
		super.render(g);
	}
	
}
