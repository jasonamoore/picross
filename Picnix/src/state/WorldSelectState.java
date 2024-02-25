package state;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import engine.Engine;
import engine.Input;
import picnix.Island;
import picnix.World;
import resource.bank.ImageBank;
import state.element.WorldBox;
import util.Animation;

public class WorldSelectState extends State {

	public static final int SKY_HEIGHT = 150;
	public static final int OFF_X = -125;
	public static final int OFF_Y = 75;
	public static final double SCALE = 1;
	
	private WorldBox worldBox;
	
	private int curLoc;

	private Animation smoothBox;
	private Animation smoothRot;
	
	public WorldSelectState(double initRot) {
		smoothBox = new Animation(0, 1, 250, Animation.CUBIC, Animation.NO_LOOP, false);
		smoothRot = new Animation(initRot, 0, 500, Animation.EASE_OUT, Animation.NO_LOOP, true);
		worldBox = new WorldBox(this, Engine.SCREEN_WIDTH - 180, Engine.getScreenCenterY(372), 168, 372);
		worldBox.setBackground(ImageBank.worldscroll);
		add(worldBox);
	}
	
	@Override
	public void focus(int status) {
		smoothBox.reset(true);
	}
	
	public int getEasyWorldId() {
		return World.getEasyWorldId(curLoc);
	}

	public int getHardWorldId() {
		return World.getHardWorldId(curLoc);
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
		worldBox.setEnabled(false);
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
				worldBox.setEnabled(true);
			else { // if faded out: make box fade back in
				//testIcon.updateLocation(curLoc);
				smoothBox.reverse(true);
			}
		}
		worldBox.setY((int) ((1 - smoothBox.getValue()) * -25 + Engine.getScreenCenterY(372)));
	}

	@Override
	public void render(Graphics g) {
		Island.renderIsland(g, SKY_HEIGHT, OFF_X, OFF_Y, SCALE, smoothRot.getValue());
		super.render(g);
	}
	
}
