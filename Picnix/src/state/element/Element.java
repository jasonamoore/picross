package state.element;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import engine.Input;
import state.State;

public abstract class Element implements Comparable<Element> {

	//
	public static final int Z_UNSET = 0;
	
	protected State state;
	
	protected Element parent;
	private ArrayList<Element> children;
	
	// the element's depth order (greater means farther in front)
	protected int z;
	
	protected int x, y, width, height;
	
	protected boolean hovering;
	
	protected Color backgroundColor;
	
	public Element() {
		children = new ArrayList<Element>();
	}
	
	@Override
	public int compareTo(Element e) {
		return z - e.z;
	}
	
	public void add(Element e) {
		e.updateState(state);
		e.updateParent(this);
		children.add(e);
	}
	
	public void remove(Element e) {
		e.updateState(null);
		e.updateParent(null);
		children.remove(e);
	}
	
	public void updateState(State newState) {
		if (state != newState) {
			if (state != null)
				state.remove(this);
			state = newState;
			if (newState != null)
				state.add(this);
			for (int i = 0; i < children.size(); i++)
				children.get(i).updateState(state);
		}
	}
	
	protected void updateParent(Element newParent) {
		if (parent != newParent) {
			parent = newParent;
			// make sure this displays in front
			if (parent != null && z == Z_UNSET)
				setZ(parent.z + 1);
		}
	}
	
	public void setZ(int z) {
		this.z = z;
		if (state != null)
			state.sortChildren();
	}
	
	public int getRelativeMouseX() {
		Input input = Input.getInstance();
		return input.getMouseX() - x;
	}
	
	public int getRelativeMouseY() {
		Input input = Input.getInstance();
		return input.getMouseY() - y;
	}
	
	public int getScrollX() {
		return 0;
	}

	public int getScrollY() {
		return 0;
	}
	
	public int getDisplayX() {
		return x + (parent != null ? parent.getDisplayX() - parent.getScrollX() : 0);
	}
	
	public int getDisplayY() {
		return y + (parent != null ? parent.getDisplayY() - parent.getScrollY() : 0);
	}
	
	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		width = w;
		height = h;
	}
	
	public boolean inBounds(int posX, int posY) {
		int dpx = getDisplayX();
		int dpy = getDisplayY();
		return (posX >= dpx && posX < dpx + width
				&& posY >= dpy && posY < dpy + height);
	}
	
	public void onHover() {
		hovering = true;
	}

	public void onLeave() {
		hovering = false;
	}
	
	public void tick() {
		Input input = Input.getInstance();
		boolean nowHovering = inBounds(input.getMouseX(), input.getMouseY());
		if (!hovering && nowHovering)
			onHover();
		if (hovering && !nowHovering)
			onLeave();
	}
	
	public abstract void render(Graphics g);
	
}
