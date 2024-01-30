package engine.thread;

public abstract class ThreadManager {

	protected volatile boolean running;
	protected volatile boolean paused;
	protected Thread thread;
	
	/**
	 * Initializes a ThreadManager with a new Thread.
	 * The Thread's run method will call {@link #run()} on this ThreadManager.
	 */
	public ThreadManager() {
		thread = new Thread() {
			public void run() {
				running = true;
				paused = false;
				begin();
			}
		};
	}
	
	/**
	 * Starts the ThreadManager by calling {@link Thread#start()} on
	 * its wrapped {@code Thread} object.
	 * Does nothing if the manager has already started.
	 */
	public void start() {
		if (!running)
			thread.start();
	}
	
	/**
	 * Stops the ThreadManager by modifying the running state
	 * to false, signaling to the thread to exit its loop.
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {
		if (running)
			running = false;
		thread.join();
	}
	
	/**
	 * Pauses the ThreadManager by modifying the paused state
	 * to true, signaling to the thread to pause execution.
	 */
	public void pause() {
		paused = true;
	}
	
	/**
	 * Resumes the ThreadManager by modifying the paused state
	 * to false, signaling to the thread to resume execution.
	 */
	public void resume() {
		paused = false;
	}
	
	/**
	 * Called when the wrapped {@code Thread} is started.
	 * Handles the loop logic for the ThreadManager.
	 */
	protected abstract void begin();
	
}
