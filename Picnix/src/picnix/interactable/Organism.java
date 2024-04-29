package picnix.interactable;

import state.element.Element;

public abstract class Organism extends Element {
	
	public abstract void onReward();

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
}
