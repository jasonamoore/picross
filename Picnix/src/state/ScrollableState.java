package state;

import engine.Engine;
import state.element.Container;
import state.element.Scroller;

public abstract class ScrollableState extends State {
	
	private static final int SCROLLER_Z = 1000;
	
	protected Container scrollContainer;
	
	protected ScrollableState(int innerWidth, int innerHeight) {
		scrollContainer = new Container(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT,
										innerWidth, innerHeight);
		add(scrollContainer);
		Scroller sHor = scrollContainer.getHorizontalScroller();
		Scroller sVer = scrollContainer.getVerticalScroller();
		if (sHor != null)
			scrollContainer.getHorizontalScroller().setZ(SCROLLER_Z);
		if (sVer != null)
			scrollContainer.getVerticalScroller().setZ(SCROLLER_Z);
	}
	
}
