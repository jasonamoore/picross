package state;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

import engine.Input;
import state.element.Element;

public abstract class State {

	// status code constants for focus calls
	public static final int NEWLY_OPENED = 0;
	public static final int RETURNING = 1;
	public static final int ERROR_RETURN = 2;
	
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
		focusElement = e;
	}
	
	public void deactivate(Element e) {
		if (activeElement == e)
			activeElement = null;
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
		Input input = Input.getInstance();
		// call events (only if focused element has not had the event called yet)
		if (focusElement != null) {
			System.out.println(focusElement);
			// mouse is over it: call hover
			if (!focusElement.beingHovered()) {
				focusElement.onHover();
				// leave previous hovered elem
				if (lastFocus != null)
					lastFocus.onLeave();
			}
			// mouse is over and is clicking: call click
			if (input.isPressingMouseButton(Input.LEFT_CLICK)) {
				int lastX = input.getLastMousePressXPosition(Input.LEFT_CLICK);
				int lastY = input.getLastMousePressYPosition(Input.LEFT_CLICK);
				if (focusElement.inBounds(lastX, lastY)
						&& activeElement == null
						&& !focusElement.beingClicked()) {
					activeElement = focusElement;
					focusElement.onClick();
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
