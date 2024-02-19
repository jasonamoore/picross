package state.element;

import java.awt.Color;
import java.awt.Graphics;

import engine.Input;
import util.Animation;

/**      Thumb
 * Arrow   v   Rail
 *  v   _-------^----_
 * [<][ [  ==  ]       ][>]
 * 
 * (flip everything for vertical scroll bars)
 */
public class Scroller extends Element {

	// some convenience constants
	public static final int DEFAULT_THICKNESS = 10;
	public static final int ARROW_NUDGE_AMOUNT = 20;
	public static final double SCROLL_WHEEL_NUDGE_AMOUNT = 30;
	
	public static final boolean HORIZONTAL = false;
	public static final boolean VERTICAL = true;

	// thickness of the bar (its size in the non-scrolling axis)
	// AND the size of the end buttons on the scroll bar: [<][    ][>]
	private int thickness;
	// length of the whole scroll rail (excluding arrows [>])
	private int railSize;
	// portion of the rail that is reachable by the left side of the thumb (see below)
	private double realRailSize;
	// length of the scroll bar (the thumb) itself
	private double thumbSize;
	/* the (x) position of the scroll bar (the thumb) start
	   relative to the size of the bar slot:
	      x=0   x=realRailSize   x=railSize
	      v        v               v
	   [<][        [     =====    ]][>]
	*/
	private double thumbOffset;
	
	/* THE INTENTION OF THE SCROLLBAR IS TO BE A MEANS
	   of navigating a page whose content spans a larger
	   space than the viewport for that content (screen size).
	   These factors determine the size of the scroll bar thumb
	   and the ratios of where the bar is to the viewport offset.
	*/
	// the size of the viewport that will show the content
	private int viewportSize;
	// the size of the inner content that the bar scrolls over
	private int contentSize;
	
	// whether the scroll bar is horizontal or vertical
	private boolean orientation;
	
	// STUFF FOR KEEPING TRACKING OF MOUSE DRAGGING
	// whether the thumb is being dragged
	private boolean dragging;
	// mouse position, relative to thumb, where the drag started
	private double dragStartOffset;

	// for nudging smoothly
	private Animation nudgeAnim;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param thickness
	 * @param dispSize
	 * @param viewSize
	 * @param contSize
	 * @param orient
	 */
	public Scroller(int x, int y, int thickness, int dispSize, int viewSize, int contSize, boolean orient) {	
		orientation = orient;
		this.x = x;
		this.y = y;
		this.thickness = thickness;
		if (isHorizontal()) {
			width = dispSize;
			height = thickness;
		}
		else {
			height = dispSize;
			width = thickness;
		}
		viewportSize = viewSize;
		contentSize = contSize;
		double ratio = (double) viewportSize / (double) contentSize;
		railSize = dispSize - thickness * 2;
		thumbSize = ratio * railSize;
		realRailSize = railSize - thumbSize;
		thumbOffset = 0;
		nudgeAnim = new Animation(100, Animation.EASE_OUT, Animation.LOOP_NONE);
	}

	public int getViewportOffset() {
		double ratio = (contentSize - viewportSize) / realRailSize;
		return (int) (thumbOffset * ratio);
	}
	
	@Override
	public void onClick(int mbutton) {
		super.onClick(mbutton);
		if (mbutton != Input.LEFT_CLICK)
			return;
		// check if inside thumb bounds
		int railPos = getMouseRailPosition();
		if (railPos >= thumbOffset && railPos < thumbOffset + thumbSize) {
			dragging = true;
			dragStartOffset = railPos - thumbOffset;
		}
		else { // no thumb press
			// check if arrow buttons clicked
			if (railPos < 0) // "previous" arrow clicked
				nudgeSmooth(-ARROW_NUDGE_AMOUNT);
			else if (railPos >= railSize) // "previous" arrow clicked
				nudgeSmooth(ARROW_NUDGE_AMOUNT);
			else { // must be a rail click
				// calculate one third the distance from thumb to click
				double nudgeAmt = (railPos - thumbOffset - thumbSize / 2) / 3;
				nudgeSmooth((int) nudgeAmt);
			}
		}
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK)
			dragging = false;
	}
	
	public boolean isHorizontal() {
		return orientation == HORIZONTAL;
	}
	
	private int getMouseRailPosition() {
		return (isHorizontal() ? getRelativeMouseX()
							   : getRelativeMouseY()) - thickness;
	}
	
	public void nudgeSmooth(int amount) {
		// find where to nudge to from the current offset (and keep in bounds!)
		double end = Math.max(0, Math.min(realRailSize, thumbOffset + amount));
		nudgeAnim.setFrom(thumbOffset);
		nudgeAnim.setTo(end);
		nudgeAnim.reset(true);
	}

	@Override
	public void tick() {
		super.tick();
		// update thumb to nudge anim, if observing
		if (nudgeAnim.active())
			thumbOffset = nudgeAnim.getValue();
		// update scroll position if its being dragged
		if (dragging) {
			//    relMouse = 0
			//    v
			// [<][         ..
			double newDragPos = getMouseRailPosition() - dragStartOffset;
			// put thumb at mouse pos, or min of 0 / max of realRailSize
			thumbOffset = Math.max(0, Math.min(realRailSize, newDragPos));
			nudgeAnim.setActive(false); // clear any current nudge if needed
		}
	}
	
	@Override
	public void render(Graphics g) {
		int xp = getDisplayX();
		int yp = getDisplayY();
		// if horizontal
		if (isHorizontal()) {
			g.setColor(Color.BLACK);
			g.drawRect(xp, yp, thickness - 1, thickness - 1);
			g.drawRect(width - thickness, yp, thickness - 1, thickness - 1);
			g.setColor(Color.GRAY);
			g.fillRect(xp + thickness + (int) thumbOffset, yp, (int) Math.ceil(thumbSize), thickness);
		}
		// if vertical
		else if (!isHorizontal()) {
			g.setColor(Color.BLACK);
			g.drawRect(xp, yp, thickness - 1, thickness - 1);
			g.drawRect(xp, height - thickness, thickness - 1, thickness - 1);
			g.setColor(Color.GRAY);
			g.fillRect(xp, yp + thickness + (int) thumbOffset, thickness, (int) Math.ceil(thumbSize));
		}
	}
	
}
