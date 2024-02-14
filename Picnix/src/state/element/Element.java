package state.element;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Input;
import state.State;

/**
 * An abstract class that defines a UI object existing within a State.
 */
public abstract class Element implements Comparable<Element> {

	// state to which this Element belongs
	protected State state;
	// parent Element (null if no parent)
	protected Element parent;
	
	// list of child elements
	private ArrayList<Element> children;
	// element's defined bounds
	protected int x, y, width, height;
	// the element's depth order (greater z means farther in front)
	protected int z;
	
	// whether the element is visible
	protected boolean visible;
	// whether the element is enabled
	private boolean enabled;
	
	// whether the element is currently being mouse hovered
	private boolean hovering;
	// array of three main mouse buttons: true at index if being clicked by that mouse button
	private boolean[] clicking = new boolean[Input.RIGHT_CLICK + 1];
	
	// the background image to render for this element
	protected BufferedImage background;
	
	/**
	 * Creates an Element with undefined bounds (all bounds set to zero).
	 */
	protected Element() {
		// bounds set to 0
		this(0, 0, 0, 0);
	}
	
	/**
	 * Creates an Element with the given bounds.
	 * @param x The x position of the Element (relative to its parent).
	 * @param y The y position of the Element (relative to its parent).
	 * @param w The width of the Element.
	 * @param h The height of the Element.
	 */
	protected Element(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		// visible/enabled by default
		visible = true;
		enabled = true;
		// initialize children list
		children = new ArrayList<Element>();
	}
	
	/**
	 * Used by State to order its elements when
	 * updating and rendering.
	 * Order is determined by z depth.
	 */
	@Override
	public int compareTo(Element e) {
		return z - e.z;
	}
	
	/**
	 * Adds an element to this Element's
	 * list of children.
	 * @param e The new child element.
	 */
	public void add(Element e) {
		// update state to match this one's
		e.updateState(state);
		// set its parent to this
		e.updateParent(this);
		// add to child list
		children.add(e);
	}
	
	/**
	 * Removes an element from this Element's
	 * list of children.
	 * @param e The child to remove.
	 */
	public void remove(Element e) {
		// make the child orphaned
		e.updateState(null);
		e.updateParent(null);
		// remove from child list
		children.remove(e);
	}
	
	/**
	 * Updates what State this Element belongs
	 * to. If needed, also updates any of this
	 * Element's children.
	 * @param newState The new State.
	 */
	public void updateState(State newState) {
		// only if state changed
		if (state != newState) {
			// remove from old state
			if (state != null)
				state.remove(this);
			state = newState;
			// add to new state
			if (newState != null)
				state.add(this);
			// update children
			for (int i = 0; i < children.size(); i++)
				children.get(i).updateState(state);
		}
	}
	
	/**
	 * Updates the parent of this Element.
	 * Sets this Element's z-depth to be greater
	 * than the new parent's z-depth (by one).
	 * @param newParent The new parent.
	 */
	protected void updateParent(Element newParent) {
		if (parent != newParent) {
			parent = newParent;
			// make sure this displays in front
			if (parent != null)
				setZ(parent.z + 1);
		}
	}
	
	/**
	 * Updates this Element's z-depth.
	 * @param z The new z-depth.
	 */
	public void setZ(int z) {
		this.z = z;
		if (state != null)
			state.sortChildren();
	}
	
	/**
	 * Sets whether this Element is visible.
	 * An invisible Element does not render,
	 * but still accepts input events.
	 * @param visible True if the Element should become visible.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * Sets whether this Element is enabled.
	 * A disabled Element does not accept
	 * input events, but still renders.
	 * @param enabled True if the Element should become enabled.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Sets the {@code enabled} status of all
	 * of this Element's children.
	 * @see #setEnabled(boolean)
	 * @param enabled True if the children should become enabled.
	 */
	public void setChildrenEnabled(boolean enabled) {
		for (int i = 0; i < children.size(); i++)
			children.get(i).setEnabled(enabled);
	}

	/**
	 * @return Returns whether the Element is visible.
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * @return Returns whether the Element is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Sets the background image of the Element.
	 * @param back The new background image.
	 */
	public void setBackground(BufferedImage back) {
		background = back;
	}
	
	/**
	 * Calculates this Element's absolute x position,
	 * by adding its declared x to its parent's absolute x position.
	 * @return The x position, relative to the display.
	 */
	public int getDisplayX() {
		return x + (parent != null ? parent.getDisplayX() - parent.getScrollX() : 0);
	}
	
	/**
	 * Calculates this Element's absolute y position,
	 * by adding its declared y to its parent's absolute y position.
	 * @return The y position, relative to the display.
	 */
	public int getDisplayY() {
		return y + (parent != null ? parent.getDisplayY() - parent.getScrollY() : 0);
	}
	
	/**
	 * Sets the bounds of this Element.
	 * @param x The x position of the Element (relative to its parent).
	 * @param y The y position of the Element (relative to its parent).
	 * @param w The width of the Element.
	 * @param h The height of the Element.
	 */
	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		width = w;
		height = h;
	}
	
	/**
	 * Checks if a given point (display-relative) falls within
	 * the absolute (display) bounds of this Element.
	 * @param posX The x position of the point to test.
	 * @param posY The y position of the point to test.
	 * @return True if the point is within this Element's bounds.
	 */
	public boolean inBounds(int posX, int posY) {
		int dpx = getDisplayX();
		int dpy = getDisplayY();
		// check if in parent bounds
		boolean parentPass = parent == null || parent.inBounds(posX, posY);
		return parentPass && (posX >= dpx && posX < dpx + width
							&& posY >= dpy && posY < dpy + height);
	}
	
	/**
	 * A convenience method to get the current mouse
	 * x position relative to this Element's display position.
	 * @return {@code mouseX - this.displayX}
	 */
	public int getRelativeMouseX() {
		Input input = Input.getInstance();
		return input.getMouseX() - getDisplayX();
	}
	
	/**
	 * A convenience method to get the current mouse
	 * y position relative to this Element's display position.
	 * @return {@code mouseY - this.displayY}
	 */
	public int getRelativeMouseY() {
		Input input = Input.getInstance();
		return input.getMouseY() - getDisplayY();
	}
	
	/**
	 * Checks whether the Element is currently being hovered.
	 * An Element is hovered until onRelease is called,
	 * which occurs on the first tick when the mouse
	 * position is not within the Element.
	 * @return True, if being hovered.
	 */
	public boolean beingHovered() {
		return hovering;
	}
	
	/**
	 * Checks whether the Element is currently being clicked.
	 * An Element is being clicked, regardless of mouse
	 * position, until the clicked mouse button is
	 * released (again, at any mouse position).
	 * @param mbutton The mouse button to test if this Element is being clicked by.
	 * @return True, if being clicked by the given mouse button.
	 */
	public boolean beingClicked(int mbutton) {
		return clicking[mbutton];
	}
	
	/**
	 * A convenience method that tests whether
	 * the Element is being clicked by any mouse buttons.
	 * @return True, if the {@code clicking} array is entirely false.
	 */
	public boolean notBeingClicked() {
		return  !clicking[Input.LEFT_CLICK] &&
				!clicking[Input.MIDDLE_CLICK] &&
				!clicking[Input.RIGHT_CLICK];
	}
	
	/**
	 * Called when the any of the three main mouse
	 * buttons (left, middle, right) is pressed while
	 * the mouse cursor is in this button's bounds.
	 * This event is called by the Element's State
	 * at its discretion of which Element should receive
	 * the click, such that only one Element may be
	 * "clicked" per game tick.
	 */
	public void onClick(int mbutton) {
		clicking[mbutton] = true;
	}
	
	/**
	 * Used to propogate a mouse click event
	 * back up to the Element's parent.
	 * Updates the State's active element
	 * information to switch click control.
	 * @param mbutton The mouse button being upstreamed.
	 */
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
	 * Called when the mouse button which had
	 * previously caused an {@link #onClick(int)} call
	 * is released after. The cursor may be anywhere
	 * when this event is called.
	 */
	public void onRelease(int mbutton) {
		clicking[mbutton] = false;
		if (notBeingClicked())
			state.deactivate(this);
	}
	
	/**
	 * Called when the mouse enters this Element.
	 * Only one Element within a State is hovered
	 * at a given time.
	 */
	public void onHover() {
		hovering = true;
	}

	/**
	 * Called when the mouse exits this Element,
	 * after it had previously been hovering.
	 */
	public void onLeave() {
		hovering = false;
	}
	
	/**
	 * This method is overridden by Containers to
	 * calculate children's display positions.
	 * @return The horizontal scroll amount.
	 * For non-Container elements, this is always zero.
	 */
	public int getScrollX() {
		return 0;
	}

	/**
	 * This method is overridden by Containers to
	 * calculate children's display positions.
	 * @return The vertical scroll amount.
	 * For non-Container elements, this is always zero.
	 */
	public int getScrollY() {
		return 0;
	}
	
	/**
	 * Calculates this Element's opacity, which
	 * is its parent's opacity. 
	 * @return If the parent Element does not
	 * override this method, it always returns {@code 1.0f}.
	 * Otherwise, it returns the parent's opacity.
	 */
	public float getOpacity() {
		float opacity = 1.0f;
		if (parent != null)
			opacity = Math.min(opacity, parent.getOpacity());
		return opacity;
	}
	
	/**
	 * Checks if the Element should request focus (for click/hover
	 * events), or should dispatch negative events (release/leave).
	 */
	public void tick() {
		Input input = Input.getInstance();
		// request focus for this if mouse is over it
		boolean nowHovering = inBounds(input.getMouseX(), input.getMouseY());
		if (nowHovering && isEnabled())
			state.requestFocus(this);
		// call negative events if state has changed
		if (hovering && !nowHovering)
			onLeave();
		for (int mb = Input.LEFT_CLICK; mb <= Input.RIGHT_CLICK; mb++)
			if (clicking[mb] && !input.isPressingMouseButton(mb))
				onRelease(mb);
	}
	
	/**
	 * Sets the render clips for a Graphics context, based on
	 * the bounds of this Element and its parent, if it has one.
	 * Applying these clips before rendering prevents the Element
	 * from rendering outside of its defined bounds.
	 * @param g The Graphics context to apply changes to.
	 */
	protected void setRenderClips(Graphics g) {
		// clip to parent bounds
		if (parent != null)
			g.setClip(parent.getDisplayX(), parent.getDisplayY(), parent.width, parent.height);
		// add clip to only draw bg image within this elem's bounds
		g.clipRect(getDisplayX(), getDisplayY(), width, height);
	}
	
	/**
	 * Sets the render composite for a Graphics context, based on
	 * the Element's opacity (calculated by {@link #getOpacity()}).
	 * @param g The Graphics context to apply changes to.
	 * @return A copy of the old Composite, before this method changed it.
	 */
	protected Composite setRenderComposite(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		Composite oldComp = gg.getComposite();
		gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity()));
		return oldComp;
	}
	
	/**
	 * Sets render clips and composite and draws the background, if not null.
	 * Restores the Graphics' clip and composite to its previous state after drawing.
	 * @param g Graphics, passed by the State.
	 */
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		if (background != null)
			g.drawImage(background, getDisplayX(), getDisplayY(), null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
