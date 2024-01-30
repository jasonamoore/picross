package engine.thread;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

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
	public void begin() {
		running = true;
		Engine engine = Engine.getEngine();
		Canvas canvas = engine.getCanvas();
		
		// set up buffer strategy
    	canvas.createBufferStrategy(3);
		BufferStrategy bs = canvas.getBufferStrategy();
        VolatileImage image = canvas.createVolatileImage(Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		
		// render loop stuff
		long startTime = System.nanoTime();
		while (running) {
			if (paused)
				continue;
			long now = System.nanoTime();
			if (now - startTime >= frameTime) {
				startTime = now;
				
				// get Graphics
	            Graphics g = image.createGraphics();
	            // render game
				State state = engine.getActiveState();
				g.setColor(Color.PINK);
				g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				state.render(g);
	            g.dispose();
	            // present frame
	            g = bs.getDrawGraphics();
	            g.drawImage(image, 0, 0, engine.getDisplayWidth(), engine.getDisplayHeight(), null);
	            g.dispose();
	            bs.show();
	            try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
