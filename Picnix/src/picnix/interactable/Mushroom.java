package picnix.interactable;

import java.awt.Graphics;

import engine.Input;
import resource.bank.ImageBank;
import state.PuzzleState;

public class Mushroom extends Organism {

	private boolean pulling;
	
	private void startPull() {
		pulling = true;
	}
	
	private void stopPull() {
		pulling = false;
	}
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		if (mbutton == Input.LEFT_CLICK)
			startPull();
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK)
			stopPull();
	}
	
	@Override
	public void onReward() {
		stopPull();
		((PuzzleState) state).increaseCritterScore(2000);
		onRelease(Input.LEFT_CLICK);
		// remove itself
		updateState(null);
		// make particles
	}
	
	@Override
	public void tick() {
		super.tick();
		int distance = getPullDistance();
		if (pulling && distance > 20)
			onReward();
	}
	
	private int getPullDistance() {
		if (!pulling)
			return 0;
		Input input = Input.getInstance();
		int my = input.getMouseY();
		int dy = getDisplayY();
		return Math.max(0, dy - my);
	}
	
	@Override
	public void render(Graphics g) {
		int distance = getPullDistance();
		int xp = getDisplayX();
		int yp = getDisplayY();
		g.drawImage(ImageBank.mushroom, xp, yp - distance, getWidth(), distance + getHeight(), null);
	}
	
}
