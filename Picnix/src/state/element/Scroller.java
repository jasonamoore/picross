package state.element;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import engine.Input;
import resource.bank.ImageBank;
import resource.bank.Palette;
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
	public static final int DEFAULT_NUDGE_DURATION = 100;
	
	public static final boolean HORIZONTAL = false;
	public static final boolean VERTICAL = true;

	// thickness of the bar (its size in the non-scrolling axis)
	private int thickness;
	// AND by default, the size of the end buttons on the scroll bar: [<][    ][>]
	private int arrowThickness;
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
	   [<][        [     =====    ]][>] */
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
	private int nudgeAnimDuration = DEFAULT_NUDGE_DURATION;
	
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
		setBounds(x, y, isHorizontal() ? dispSize : thickness, isHorizontal() ? thickness : dispSize);
		this.thickness = thickness;
		arrowThickness = thickness;
		viewportSize = viewSize;
		contentSize = contSize;
		thumbOffset = 0;
		nudgeAnim = new Animation(nudgeAnimDuration, Animation.EASE_OUT, Animation.NO_LOOP);
		calculateSizes();
	}
	
	private void calculateSizes() {
		int dispSize = isHorizontal() ? getWidth() : getHeight();
		double ratio = (double) viewportSize / (double) contentSize;
		railSize = dispSize - arrowThickness * 2;
		thumbSize = ratio * railSize;
		realRailSize = railSize - thumbSize;
	}

	public void disableArrowButtons() {
		arrowThickness = 0;
		calculateSizes();
	}
	
	public int getViewportOffset() {
		// update thumb to nudge anim, if observing
		if (nudgeAnim.isPlaying())
			thumbOffset = nudgeAnim.getValue();
		double ratio = (contentSize - viewportSize) / realRailSize;
		return (int) (thumbOffset * ratio);
	}
	
	public void setViewportOffset(int viewOffset) {
		/* find where the thumb should be to
		 * align the contents at the given offset */
		double ratio = (contentSize - viewportSize) / realRailSize;
		double newThumb = viewOffset / ratio;
		// nudge by the needed amount so the thumb lands at newThumb
		nudgeSmooth(newThumb - thumbOffset);
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
		return (isHorizontal() ? getRelativeMouseX() : getRelativeMouseY()) - arrowThickness;
	}
	
	public void setNudgeSpeed(int duration) {
		nudgeAnim.setDuration(duration);
	}
	
	public void nudgeSmooth(double amount) {
		// first maybe update thumb offset
		if (nudgeAnim.isPlaying())
			thumbOffset = nudgeAnim.getValue();
		// find where to nudge to from the current offset (and keep in bounds!)
		double end = Math.max(0, Math.min(realRailSize, thumbOffset + amount));
		nudgeAnim.setFrom(thumbOffset);
		nudgeAnim.setTo(end);
		nudgeAnim.reset(true);
	}
	
	@Override
	public void tick() {
		super.tick();
		// update scroll position if its being dragged
		if (dragging)
			updateDrag();
	}
	
	private void updateDrag() {
		//    relMouse = 0
		//    v
		// [<][         ..
		double newDragPos = getMouseRailPosition() - dragStartOffset;
		// put thumb at mouse pos, or min of 0 / max of realRailSize
		thumbOffset = Math.max(0, Math.min(realRailSize, newDragPos));
		nudgeAnim.pause(); // clear any current nudge if needed
	}

	@Override
	public void render(Graphics g) {
		// doing this in render makes it very smooth :)
		if (dragging)
			updateDrag();
		int xp = getDisplayX();
		int yp = getDisplayY();
		// if horizontal
		if (isHorizontal()) {
			g.setColor(Palette.PALE_BLUE);
			g.fillRect(xp, yp, getWidth(), getHeight());
			if (arrowThickness > 0) {
				g.drawImage(ImageBank.scrollarrowshoriz[0], xp, yp, null);
				g.drawImage(ImageBank.scrollarrowshoriz[1], xp + getWidth() - arrowThickness, yp, null);
			}
			BufferedImage[] tiles = ImageBank.scrollthumbhoriz;
			int thumbStart = (int) (xp + arrowThickness + thumbOffset);
			int thumbEnd = (int) Math.ceil(thumbStart + thumbSize);
			for (int i = 0; i + thickness < thumbSize; i += thickness)
				g.drawImage(tiles[i == 0 ? 0 : 1], thumbStart + i, yp, null);
			g.drawImage(tiles[2], thumbEnd - thickness, yp, null);
			int cx = (int) (arrowThickness + thumbOffset +
					(thumbSize - ImageBank.scrollthumbgriphoriz.getWidth()) / 2);
			int cy = (thickness - ImageBank.scrollthumbgriphoriz.getHeight()) / 2;
			g.drawImage(ImageBank.scrollthumbgriphoriz, xp + cx, yp + cy, null);
		}
		// if vertical
		else if (!isHorizontal()) {
			g.setColor(Palette.PALE_BLUE);
			g.fillRect(xp, yp, getWidth(), getHeight());
			if (arrowThickness > 0) {
				g.drawImage(ImageBank.scrollarrowsvert[0], xp, yp, null);
				g.drawImage(ImageBank.scrollarrowsvert[1], xp, yp + getHeight() - arrowThickness, null);
			}
			BufferedImage[] tiles = ImageBank.scrollthumbvert;
			int thumbStart = (int) (yp + arrowThickness + thumbOffset);
			int thumbEnd = (int) Math.ceil(thumbStart + thumbSize);
			for (int i = 0; i + thickness < thumbSize; i += thickness)
				g.drawImage(tiles[i == 0 ? 0 : 1], xp, thumbStart + i, null);
			g.drawImage(tiles[2], xp, thumbEnd - thickness, null);
			int cx = (thickness - ImageBank.scrollthumbgripvert.getWidth()) / 2;
			int cy = (int) (arrowThickness + thumbOffset +
					(thumbSize - ImageBank.scrollthumbgripvert.getHeight()) / 2);
			g.drawImage(ImageBank.scrollthumbgripvert, xp + cx, yp + cy, null);
		}
	}
	
}
