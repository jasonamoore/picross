package state.element;

import java.awt.Color;
import java.awt.Graphics;

import engine.Input;

public abstract class Button extends Element {
	
	protected boolean clicking;
	
	public Button() {
		this(Color.GRAY);
	}
	
	public Button(Color back) {
		super();
		backgroundColor = back;
	}
	
	public boolean beingClicked() {
		return clicking;
	}
	
	/**
	 * Called when the left mouse is pressed and the
	 * mouse cursor is in this button's bounds.
	 */
	public void onClick() {
		clicking = true;
	}
	
	/**
	 * Called when the left mouse is released after
	 * this button had been pressed (clicking = true),
	 * and the cursor may be anywhere in the window.
	 */
	public void onRelease() {
		clicking = false;
	}
	
	public void tick() {
		super.tick();
		Input input = Input.getInstance();
		// if left clicking...
		if (input.isPressingMouseButton(Input.LEFT_CLICK)) {
			// and if the click was performed in bounds...
			int mpx = input.getLastMousePressXPosition(Input.LEFT_CLICK);
			int mpy = input.getLastMousePressYPosition(Input.LEFT_CLICK);
			if (clicking || inBounds(mpx, mpy)) {
				// queue up this element for a possible onClick event
				state.requestClick(this);
			}
		} // if this item was being clicked, and the mouse has released
		// note: mouse can be released anywhere (after mouse has moved off)
		else if (clicking && input.hasReleasedMouseButton(Input.LEFT_CLICK)) {
			onRelease();
			input.consumeMouseButtonRelease(Input.LEFT_CLICK);
		}
	}
	
	public void render(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(getDisplayX(), getDisplayY(), width, height);
	}
	
}
