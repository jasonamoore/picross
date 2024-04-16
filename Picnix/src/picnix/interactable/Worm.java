package picnix.interactable;

import java.awt.Graphics;

import engine.Input;

public class Worm extends Organism {

	public static final int PULL_DIST = 20;
	
	private boolean pulling;
	
	private void startPull() {
		pulling = true;
	}
	
	private void stopPull() {
		pulling = false;
	}
	
	@Override
	public void onClick(int mbutton) {
		if (mbutton == Input.LEFT_CLICK)
			startPull();
	}
	
	@Override
	public void onRelease(int mbutton) {
		if (mbutton == Input.LEFT_CLICK)
			stopPull();
	}
	
	@Override
	public void onReward() {
		stopPull();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (pulling && mouseDistance() > PULL_DIST)
			onReward();
	}
	
	private double mouseDistance() {
		Input input = Input.getInstance();
		return Math.sqrt(Math.pow(input.getMouseX() - getDisplayX(), 2) +
				Math.pow(input.getMouseY() - getDisplayY(), 2));
	}

	@Override
	public void render(Graphics g) {
		
	}
	
}
