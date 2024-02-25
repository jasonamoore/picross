package state;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

import engine.Input;
import state.element.Element;

public abstract class State {

	// status code constants for focus calls
	public static final int NEWLY_OPENED = 0;
	public static final int RETURNING = -1;
	public static final int ERROR_RETURN = -2;
	
	protected ArrayList<Element> elements;
	
	public State() {
		elements = new ArrayList<Element>();
	}
	
	// keeps track of which (1) element has focus for onHover/onClick events
	private Element focusElement;
	// keeps track of which (1) element has pending negative events,
	// i.e., an element that was clicked and not yet released.
	// if non-null, this signals that another click event cannot occur
	private Element activeElement;
	// whether handling input events for elements
	private boolean frozen;
	
	public abstract void focus(int status);
	
	public void add(Element e) {
		if (!elements.contains(e))
			elements.add(e);
		e.updateState(this);
		sortChildren();
	}
	
	public void remove(Element e) {
		elements.remove(e);
		e.updateState(null);
		sortChildren();
	}

	public void sortChildren() {
		// sort by z-index
		Collections.sort(elements);
	}

	public void requestFocus(Element e) {
		if (!frozen)
			focusElement = e;
	}
	
	public void activate(Element e) {
		if (activeElement == null)
			activeElement = e;
	}
	
	public void deactivate(Element e) {
		if (activeElement == e)
			activeElement = null;
	}
	
	public void freezeInput(boolean freeze) {
		frozen = freeze;
	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	public void tick() {
		// (first save) then reset this
		Element lastFocus = focusElement;
		focusElement = null;
		// tick children in ascending z-order
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			if (e.isVisible())
				e.tick();
		}
		if (frozen) {
			// leave hover element and don't need to check anything else
			if (lastFocus != null)
				lastFocus.onLeave();
			return;
		}
		Input input = Input.getInstance();
		// call events (only if focused element has not had the event called yet)
		if (focusElement != null) {
			// mouse is over it: call hover
			if (!focusElement.beingHovered()) {
				focusElement.onHover();
				// leave previous hovered elem
				if (lastFocus != null)
					lastFocus.onLeave();
			}
			// mouse is over and is clicking: call click
			// for each mouse button (left to middle to right click)
			for (int mb = Input.LEFT_CLICK; mb <= Input.RIGHT_CLICK; mb++) {
				if (input.isPressingMouseButton(mb)) {
					int lastX = input.getLastMousePressXPosition(mb);
					int lastY = input.getLastMousePressYPosition(mb);
					if (focusElement.inBounds(lastX, lastY)
							&& (activeElement == null || activeElement == focusElement)
							&& !focusElement.beingClicked(mb)) {
						activate(focusElement);
						focusElement.onClick(mb);
					}
				}
			}
		}
	}
	
	public void render(Graphics g) {
		// render children in ascending z-order
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			if (e.isVisible())
				e.render(g);
		}
	}
	
}
