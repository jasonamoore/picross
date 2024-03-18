package state;

import java.awt.Graphics;

import engine.Engine;
import engine.Input;
import engine.Transition;
import picnix.Island;
import picnix.World;
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
	
	private int curLoc;

	private Animation smoothBox;
	private Animation smoothRot;
	
	public WorldSelectState(double initRot) {
		smoothBox = new Animation(0, 1, 125, Animation.CUBIC, Animation.NO_LOOP, false);
		smoothRot = new Animation(initRot, 0, 500, Animation.EASE_OUT, Animation.NO_LOOP, true);
		locationBox = new LocationBox(this);
		TiledButton left = new TiledButton(15, 350, 48, 48) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered())
					switchLocation(-1);
			}
		};
		TiledButton right = new TiledButton(240, 350, 48, 48) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered())
					switchLocation(1);
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
		add(new BackButton());
	}
	
	@Override
	public void focus(int status) {
		if (status == NEWLY_OPENED)
			smoothBox.reset(true);
		// update info for this location
		locationBox.update(curLoc);
	}
	
	@Override
	public void navigateBack() {
		TitleState ts = (TitleState) Engine.getEngine().getStateManager().getPreviousState();
		ts.setZoomAnim(false, 0, smoothRot.getValue());
		Engine.getEngine().getStateManager().transitionExitState(Transition.NONE, 0, TitleState.ZOOM_TIME);
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
	
	private void switchLocation(int amount) {
		int oldLoc = curLoc;
		curLoc = (curLoc - amount + World.NUM_LOCATIONS) % World.NUM_LOCATIONS;
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
		// check for rotating the island
		double scroll = input.getUnconsumedScrollAmount();
		if (scroll > ISLAND_SCROLL_THRESHOLD) {
			switchLocation(-1);
			input.consumeMouseWheelScroll();
		}
		else if (scroll < -ISLAND_SCROLL_THRESHOLD) {
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
