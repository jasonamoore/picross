package engine.thread;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.VolatileImage;

import javax.swing.JFrame;

import engine.Engine;
import state.State;

public class Renderer extends ThreadManager {

	// frame time = nanoseconds per frame
	private int frameTime;

	/**
	 * Returns the target framerate, in frames per second.
	 * 
	 * @return The FPS (e.g. 30, 60). Returns Infinity for unlimited framerate.
	 */
	public int getFrameRate() {
		return 1_000_000_000 / frameTime;
	}

	/**
	 * Sets the frame time given a target framerate.
	 * 
	 * @param fps The target framerate to set. If fps is 0, frameTime will be
	 *            Infinity, denoting unlimited framerate.
	 */
	public void setFrameRate(int fps) {
		frameTime = 1_000_000_000 / fps;
	}

	@Override
	protected void begin() {
		Engine engine = Engine.getEngine();
		JFrame frame = engine.getFrame();
		Insets insets = frame.getInsets();
		
		// create blank volatile canvas
		VolatileImage canvas = frame.createVolatileImage(Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		
		// render loop stuff
		long startTime = System.nanoTime();
		while (running) {
			if (paused)
				continue;
			long now = System.nanoTime();
			if (now - startTime >= frameTime) {
				startTime = now;
				// accelerate?
				//canvas.setAccelerationPriority(1);
				// create graphics context
				Graphics g = canvas.createGraphics();
				
				// draw frame
				State state = engine.getActiveState();
				g.setColor(Color.PINK);
				g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				state.render(g);
				
				// dispose to free memory
				g.dispose();
				
				// draw full screen with backdrop
				VolatileImage fullscreen = frame.createVolatileImage(frame.getWidth(), frame.getHeight());
				g = fullscreen.getGraphics();
				// draw display border
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				// draw rendered screen
				g.drawImage(canvas, engine.getDisplayOffsetX(), engine.getDisplayOffsetY(),
						engine.getDisplayWidth(), engine.getDisplayHeight(), null);
				
				// dispose to free memory
				g.dispose();
				
				// present frame
				g = frame.getGraphics();
				g.drawImage(fullscreen, insets.left, insets.top, null);
			}
		}
	}

}
