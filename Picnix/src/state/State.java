package state;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

import engine.Input;
import state.element.Button;
import state.element.Container;
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
	
	// keeps track of which (1) element has an onClick event to call
	private Button onClickElement;
	
	public abstract void focus(int status);
	
	public void add(Element e) {
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

	public void requestClick(Button button) {
		onClickElement = button;
	}
	
	public void tick() {
		Input input = Input.getInstance();
		// reset this
		onClickElement = null;
		// tick children in ascending z-order
		for (int i = 0; i < elements.size(); i++)
			elements.get(i).tick();
		// call onClick event (only the clicking element is newly clicked)
		// for synchronization reasons, also ensure the mouse is still pressed
		if (onClickElement != null && !onClickElement.beingClicked()
				&& input.isPressingMouseButton(Input.LEFT_CLICK)) {
			System.out.println(onClickElement);
			onClickElement.onClick();
		}
	}
	
	public void render(Graphics g) {
		// render children in ascending z-order
		for (int i = 0; i < elements.size(); i++)
			elements.get(i).render(g);
	}
	
}
