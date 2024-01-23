package engine;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Used to detect resizing of the JFrame and send a signal to the Engine.
 * @author Jason
 *
 */
public class ResizeListener extends ComponentAdapter {

	@Override
	public void componentResized(ComponentEvent e) {
		Engine.getEngine().frameSizeChanged(e);
	}

}
