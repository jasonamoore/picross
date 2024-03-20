package state.element;

import java.awt.event.KeyEvent;

import engine.Input;
import state.State;

/**
 * A class for Containers, a type of element specialized for holding
 * child elements. This is usually useful for display organization.
 * Containers also have built in scrolling functionality, and can
 * be used to enable their children to have a dynamic display position.
 */
public class Container extends Element {

	private int innerWidth, innerHeight;
	
	// the built-in Scrollers (one for each axis);
	// behave like "pseudo-elements" and siblings, not children
	protected Scroller scrollHoriz;
	protected Scroller scrollVert;
	
	/**
	 * Creates a Container without Scrollers. That is,
	 * the Container's inner size matches its display size.
	 * @param x The x position of the Container (relative to its parent).
	 * @param y The y position the Container (relation to its parent).
	 * @param viewWidth The width of the Container.
	 * @param viewHeight The height of the Container.
	 */
	public Container(int x, int y, int viewWidth, int viewHeight) {
		// assume container doesn't need scrolls (inner size = view size)
		this(x, y, viewWidth, viewHeight, viewWidth, viewHeight);
	}
	
	/**
	 * Creates a Container with the given inner size, setting it
	 * up with Scrollers for either (or both) scroll axes.
	 * @param x The x position of the Container (relative to its parent).
	 * @param y The y position the Container (relation to its parent).
	 * @param viewWidth The width of the Container.
	 * @param viewHeight The height of the Container.
	 * @param innerWidth The inner width of the Container (bounds to scroll within).
	 * @param innerHeight The inner height of the Container (bounds to scroll within).
	 */
	public Container(int x, int y, int viewWidth, int viewHeight, int innerWidth, int innerHeight) {
		super(x, y, viewWidth, viewHeight);
		this.innerWidth = innerWidth;
		this.innerHeight = innerHeight;
		final int thick = Scroller.DEFAULT_THICKNESS;
		boolean hasHoriz = innerWidth > viewWidth;
		boolean hasVert = innerHeight > viewHeight;
		boolean hasBoth = hasHoriz && hasVert;
		if (hasHoriz)
			scrollHoriz = new Scroller(x, y + viewHeight - thick, thick, viewWidth - (hasBoth ? thick : 0), viewWidth, innerWidth, Scroller.HORIZONTAL);
		if (hasVert)
			scrollVert = new Scroller(x + viewWidth - thick, y, thick, viewHeight - (hasBoth ? thick : 0), viewHeight, innerHeight, Scroller.VERTICAL);
	}

	/**
	 * Overrides to also update the Scroller states.
	 */
	@Override
	public void updateState(State newState) {
		super.updateState(newState);
		if (scrollHoriz != null)
			scrollHoriz.updateState(newState);
		if (scrollVert != null)
			scrollVert.updateState(newState);
	}
	
	/**
	 * Overrides to also update the Scroller parents.
	 */
	@Override
	protected void updateParent(Element newParent) {
		super.updateParent(newParent);
		if (scrollHoriz != null)
			scrollHoriz.updateParent(newParent);
		if (scrollVert != null)
			scrollVert.updateParent(newParent);
	}
	
	/**
	 * @return The horizontal Scroller, which may be null.
	 */
	public Scroller getHorizontalScroller() {
		return scrollHoriz;
	}
	
	/**
	 * @return The vertical Scroller, which may be null.
	 */
	public Scroller getVerticalScroller() {
		return scrollVert;
	}
	
	/**
	 * Disables both scrollers by making them
	 * invisible and disabled.
	 */
	protected void disableScrollers() {
		scrollHoriz.setExisting(false);
		scrollVert.setExisting(false);
	}
	
	public int getInnerWidth() {
		return innerWidth;
	}
	
	public int getInnerHeight() {
		return innerHeight;
	}
	
	/**
	 * Returns the x-axis scroll amount, as
	 * determined by the horizontal Scroller.
	 */
	@Override
	public int getScrollX() {
		return scrollHoriz != null ? scrollHoriz.getViewportOffset() : 0;
	}

	/**
	 * Returns the y-axis scroll amount, as
	 * determined by the vertical Scroller.
	 */
	@Override
	public int getScrollY() {
		return scrollVert != null ? scrollVert.getViewportOffset() : 0;
	}

	/**
	 * Handles input for controlling the Scrollers.
	 */
	@Override
	public void tick() {
		super.tick();
		if (isEnabled()) {
			Input input = Input.getInstance();
			double scroll = input.getUnconsumedScrollAmount();
			Scroller targeted = null;
			if (scrollHoriz != null && scrollVert == null)
				targeted = scrollHoriz;
			else if (scrollHoriz == null && scrollVert != null)
				targeted = scrollVert;
			else if (scrollHoriz != null && scrollVert != null) {
				if (input.isPressingKey(KeyEvent.VK_SHIFT))
					targeted = scrollHoriz;
				else
					targeted = scrollVert;
			}
			if (scroll != 0 && targeted != null) {
				targeted.nudgeSmooth((int) (scroll * Scroller.SCROLL_WHEEL_NUDGE_AMOUNT));
				input.consumeMouseWheelScroll();
			}
		}
	}
	
}
