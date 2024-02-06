package state.element;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Input;
import state.State;

public abstract class Element implements Comparable<Element> {

	private ArrayList<Element> children;
	protected State state;
	protected Element parent;
	// the element's depth order (greater means farther in front)
	protected int z;
	protected int x, y, width, height;
	protected boolean visible;
	private boolean hovering;
	// onClick events will be called for the three basic click types
	private boolean[] clicking = new boolean[Input.RIGHT_CLICK + 1];
	protected BufferedImage background;
	
	public Element() {
		// bounds set to 0
		this(0, 0, 0, 0);
	}
	
	public Element(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		visible = true;
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
			if (parent != null/* && z <= parent.z */)
				setZ(parent.z + 1);
		}
	}
	
	public void setZ(int z) {
		this.z = z;
		if (state != null)
			state.sortChildren();
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}
	
	public void setBackground(BufferedImage back) {
		background = back;
	}
	
	public int getRelativeMouseX() {
		Input input = Input.getInstance();
		return input.getMouseX() - getDisplayX();
	}
	
	public int getRelativeMouseY() {
		Input input = Input.getInstance();
		return input.getMouseY() - getDisplayY();
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
	
	public boolean beingHovered() {
		return hovering;
	}
	
	public boolean beingClicked(int mbutton) {
		return clicking[mbutton];
	}
	
	public boolean notBeingClicked() {
		return  !clicking[Input.LEFT_CLICK] &&
				!clicking[Input.MIDDLE_CLICK] &&
				!clicking[Input.RIGHT_CLICK];
	}
	
	public void upstreamClick(int mbutton) {
		if (state == null || parent == null) return;
		// simulate the click returning to the parent
		parent.onClick(mbutton);
		// make sure the state knows this elem was clicked
		state.requestFocus(parent);
		state.deactivate(this);
		state.activate(parent);
	}
	
	/**
	 * Called when the left mouse is pressed and the
	 * mouse cursor is in this button's bounds.
	 */
	public void onClick(int mbutton) {
		clicking[mbutton] = true;
	}
	
	/**
	 * Called when the left mouse is released after
	 * this button had been pressed (clicking = true),
	 * and the cursor may be anywhere in the window.
	 */
	public void onRelease(int mbutton) {
		clicking[mbutton] = false;
		if (notBeingClicked())
			state.deactivate(this);
	}
	
	public void onHover() {
		hovering = true;
	}

	public void onLeave() {
		hovering = false;
	}
	
	public void tick() {
		Input input = Input.getInstance();
		// request focus for this if mouse is over it
		boolean nowHovering = inBounds(input.getMouseX(), input.getMouseY());
		if (nowHovering)
			state.requestFocus(this);
		// call negative events if state has changed
		if (hovering && !nowHovering)
			onLeave();
		for (int mb = Input.LEFT_CLICK; mb <= Input.RIGHT_CLICK; mb++)
			if (clicking[mb] && !input.isPressingMouseButton(mb))
				onRelease(mb);
	}
	
	public void render(Graphics g) {
		if (background != null);
			g.drawImage(background, getDisplayX(), getDisplayY(),
					width, height, null);
	}
	
}
