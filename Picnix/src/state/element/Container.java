package state.element;

import java.awt.event.KeyEvent;

import engine.Engine;
import engine.Input;
import state.State;

public class Container extends Element {

	protected Scroller scrollHoriz;
	protected Scroller scrollVert;
	
	public Container(int x, int y, int viewWidth, int viewHeight) {
		// assume container doesn't need scrolls (inner size = view size)
		this(x, y, viewWidth, viewHeight, viewWidth, viewHeight);
	}
	
	public Container(int x, int y, int viewWidth, int viewHeight, int innerWidth, int innerHeight) {
		super(x, y, viewWidth, viewHeight);
		final int thick = Scroller.DEFAULT_THICKNESS;
		if (innerWidth > Engine.SCREEN_WIDTH)
			scrollHoriz = new Scroller(0, Engine.SCREEN_HEIGHT - thick, thick, Engine.SCREEN_WIDTH - thick, Engine.SCREEN_WIDTH, innerWidth, Scroller.HORIZONTAL);
		if (innerHeight > Engine.SCREEN_HEIGHT)
			scrollVert = new Scroller(Engine.SCREEN_WIDTH - thick, 0, thick, Engine.SCREEN_HEIGHT - thick, Engine.SCREEN_HEIGHT, innerHeight, Scroller.VERTICAL);
	}

	@Override
	public void updateState(State newState) {
		super.updateState(newState);
		if (scrollHoriz != null)
			scrollHoriz.updateState(newState);
		if (scrollVert != null)
			scrollVert.updateState(newState);
	}
	
	@Override
	protected void updateParent(Element newParent) {
		super.updateParent(newParent);
		if (scrollHoriz != null)
			scrollHoriz.updateParent(newParent);
		if (scrollVert != null)
			scrollVert.updateParent(newParent);
	}
	
	public Scroller getHorizontalScroller() {
		return scrollHoriz;
	}
	
	public Scroller getVerticalScroller() {
		return scrollVert;
	}
	
	protected void disableScrollers() {
		scrollHoriz = null;
		scrollVert = null;
		if (state != null) {
			state.remove(scrollHoriz);
			state.remove(scrollVert);
		}
	}
	
	@Override
	public int getScrollX() {
		return scrollHoriz != null ? scrollHoriz.getViewportOffset() : 0;
	}

	@Override
	public int getScrollY() {
		return scrollVert != null ? scrollVert.getViewportOffset() : 0;
	}

	@Override
	public void tick() {
		super.tick();
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
			//System.out.println(scroll);
			targeted.nudgeSmooth((int) (scroll * Scroller.SCROLL_WHEEL_NUDGE_AMOUNT));
			input.consumeMouseWheelScroll();
		}
	}
	
}
