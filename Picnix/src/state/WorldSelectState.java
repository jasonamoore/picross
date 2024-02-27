package state;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import engine.Engine;
import engine.Input;
import engine.Transition;
import picnix.Island;
import picnix.World;
import picnix.data.UserData;
import state.element.location.LocationBox;
import state.load.LoadWorldState;
import util.Animation;

public class WorldSelectState extends State {

	public static final int SKY_HEIGHT = 150;
	public static final int OFF_X = -125;
	public static final int OFF_Y = 75;
	public static final double SCALE = 1;
	
	private LocationBox locationBox;
	
	private int curLoc;

	private Animation smoothBox;
	private Animation smoothRot;
	
	public WorldSelectState(double initRot) {
		smoothBox = new Animation(0, 1, 125, Animation.CUBIC, Animation.NO_LOOP, false);
		smoothRot = new Animation(initRot, 0, 500, Animation.EASE_OUT, Animation.NO_LOOP, true);
		locationBox = new LocationBox(this);
		add(locationBox);
	}
	
	@Override
	public void focus(int status) {
		smoothBox.reset(true);
		locationBox.update(curLoc);
	}

	public void open(boolean easy) {
		int worldId = easy ? World.getEasyWorldId(curLoc) : World.getHardWorldId(curLoc);
		LoadWorldState lws = new LoadWorldState(worldId);
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
		boolean leftKey = input.isPressingKey(KeyEvent.VK_LEFT);
		boolean rightKey = input.isPressingKey(KeyEvent.VK_RIGHT);
		if (leftKey && !rightKey) {
			switchLocation(-1);
			input.consumeKeyPress(KeyEvent.VK_LEFT);
		}
		else if (rightKey && !leftKey) {
			switchLocation(1);
			input.consumeKeyPress(KeyEvent.VK_RIGHT);
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
				UserData.randomizeScores();
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
