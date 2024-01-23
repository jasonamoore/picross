package engine.thread;

import engine.Engine;
import engine.Input;

public class Executor extends ThreadManager {

	private static final int TICK_TIME = 1_000_000_000 / 30;
	
	@Override
	protected void begin() {
		// game loop stuff
		long startTime = System.nanoTime();
		while (running) {
			if (paused)
				continue;
			long now = System.nanoTime();
			if (now - startTime >= TICK_TIME) {
				startTime = now;
			}
			// tick this state
			Engine.getEngine().getActiveState().tick();
			
			// clear input button releases
			//Input.getInstance().markReleasesProcessed();
		}
	}
	
}
