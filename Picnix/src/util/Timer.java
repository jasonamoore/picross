package util;

/**
 * A simple class used to keep track of time.
 * Only makes calculations when requested.
 */
public class Timer {
	
	// the Timer's latest start time
	private long start;
	// the amount of time elapsed while unpaused
	private long elapsedSum = 0;
	// whether the timer is paused
	private boolean paused = true;
	
	/**
	 * Creates a new timer with no time elapsed.
	 * @param started Whether the Timer should start immediately.
	 */
	public Timer(boolean started) {
		if (started) resume();
	}
	
	/**
	 * Returns the amount of unpaused time elapsed since the creation of this timer.
	 * @return The elapsed time, in milliseconds.
	 */
	public long elapsed() {
		// if paused return elapsedSum, because elapsedSum will have already been updated to the correct amount when pause() was called.
		// if not paused, add on the current elapsed time that has not yet been counted and added to elapsedSum (sum is only changed when timer pauses)
		if (paused) return elapsedSum;
		else return elapsedSum + time() - start;
	}
	
	/**
	 * Returns the result of {@link #elapsed()} converted to seconds.
	 * @return The elapsed time, in seconds.
	 */
	public double elapsedSec() {
		return elapsed() * 0.001;
	}
	
	/**
	 * Pauses the timer, which will cause it to ignore elapsed time
	 * until it is unpaused again.
	 */
	public void pause() {
		if (paused) return;
		paused = true;
		elapsedSum += time() - start;
	}
	
	/**
	 * Resumes the timer, causing it to continue accumulating
	 * elapsed time from the time of resuming.
	 */
	public void resume() {
		if (!paused) return;
		paused = false;
		start = time();
	}
	
	/**
	 * Toggles the pause state of the timer. If paused, the timer
	 * will be resumed. If resumed, the timer will be paused.
	 */
	public void toggle() {
		if (paused) resume();
		else pause();
	}
	
	/**
	 * Resets the timer, causing it to discard any previously elapsed time.
	 * @param restart Whether to restart the timer immediately after this call.
	 */
	public void reset(boolean restart) {
		paused = !restart; // if restart is true, the timer will start (unpause) immediately
		elapsedSum = 0;
		start = time();
	}
	
	/**
	 * A convenience method for calling {@link System#currentTimeMillis()}.
	 * @return The current time in milliseconds.
	 */
	private static long time() {
		return System.currentTimeMillis();
	}
	
}
