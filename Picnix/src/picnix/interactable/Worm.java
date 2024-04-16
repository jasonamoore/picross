package picnix.interactable;

import java.awt.Graphics;

import engine.Input;

public class Worm extends Organism {

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
		//if (pulling && distanceIsBig)
		//	onReward();
	}
	
	@Override
	public void render(Graphics g) {
		
	}
	
}
