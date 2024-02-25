package util;

/**
 *	Simplified & lightweight animation class that uses two keyframes, a duration, and a transition function
 *	to provide a stream of interpolated values used for smooth movement, fading, sprite animation, etc.
 */
public class Animation {

	// loop constants
	public static final int NO_LOOP = 0;
	public static final int CONTINUE = 1;
	public static final int BOUNCE = 2;
	public static final int UNLIMITED = Integer.MAX_VALUE;
	
	// animation transition strategies indices (corresponds to PRESETS array)
	private static final int HOLD_INDEX = 0;
	private static final int LINEAR_INDEX = 1;
	private static final int CUBIC_INDEX = 2;
	private static final int INV_CUBIC_INDEX = 3;
	private static final int EASE_IN_INDEX = 4;
	private static final int EASE_OUT_INDEX = 5;
	
	// bezier coefficients for presets (indices match to strategy constants)
	private static final double[][] PRESETS = {
			{0, 0, 0, 0},
			{0, 1, 2, 1},
			{0, 0, 3, 1},
			{0, 2.25, 0.75, 1},
			{0, 0, 0.75, 1},
			{0, 2.25, 3, 1},
	};
	
	// *FOR public use: the arrays for common animation transition strategies
	public static final double[] HOLD = PRESETS[HOLD_INDEX];
	public static final double[] LINEAR = PRESETS[LINEAR_INDEX];
	public static final double[] CUBIC = PRESETS[CUBIC_INDEX];
	public static final double[] INV_CUBIC = PRESETS[INV_CUBIC_INDEX];
	public static final double[] EASE_IN = PRESETS[EASE_IN_INDEX];
	public static final double[] EASE_OUT = PRESETS[EASE_OUT_INDEX];
	
	// Timer used for timing the animation
	private Timer timer;

	// keyframe and interpolation info
	private double from;
	private double to;
	private int duration;
	private double[] transition;

	// is the animation currently running
	private boolean playing = false;
	// the direction (forward/backward) of playback
	private boolean forward = true;
	// whether the animation should loop
	private int loopMode;
	// how many times the animation should loop
	private int loopLimit;
	// local count of how many times the anim has looped
	private int loopsSoFar;
	
	// offset to add to timer; needed when animation is reversed during playback
	private long offset;
	
	/**
	 * Creates an animation with the specified parameters.
	 * @param from The start keyframe/value (inclusive).
	 * @param to The end keyframe/value (inclusive).
	 * @param duration The duration of the animation, in milliseconds.
	 * @param transition The bezier function describing how to interoplate between keyframes.
	 * @param loopMode The loop mode; use loop constants.
	 * @param start Whether the animation should begin playing immediately upon construction.
	 */
	public Animation(double from, double to, int duration, double[] transition, int loopMode, boolean start) {
		timer = new Timer(false); // set up a timer for the anim
		this.from = from;
		this.to = to;
		this.duration = duration;
		this.transition = transition;
		this.loopMode = loopMode;
		loopLimit = loopMode == NO_LOOP ? 0 : UNLIMITED;
		if (start) resume(); // start anim
	}
	
	/**
	 * Creates an incomplete animation with the specified parameters.
	 * This is uses for setting up timing and transition for an Animation
	 * that will be reused (reassigned keyframes) over its lifetime.
	 * The "from" and "to" values will be initialized to zero, but the
	 * animation will not begin playing, since the use of this
	 * constructor is likely to assign keyframes later on and then play.
	 * @param duration The duration of the animation, in milliseconds.
	 * @param transition The bezier function describing how to interoplate between keyframes.
	 * @param loopMode The loop mode; use loop constants.
	 */
	public Animation(int duration, double[] transition, int loopMode) {
		this(0, 0, duration, transition, loopMode, false);
	}

	/**
	 * Returns whether the Animation is currently active.
	 * "Active" is a user-defined flag, which can be useful
	 * when another Object is observing this Animation to
	 * update some value, such as its position. Once that
	 * Object no longer needs to observe the Animation,
	 * it may mark it as inactive to keep note.
	 * This is a convenience compared to storing a boolean
	 * alongside each Animation field in that Object's class.
	 * @return True if the Animation is "active" (needs observation).
	 *
	public boolean active() {
		return active;
	}/
	
	/**
	 * Sets whether the Animation is currently active.
	 * It is not necessary to explicitly set an Animation
	 * as inactive to stop observing it. The first call to
	 * getValue after the Animation has stopped playing will
	 * automatically deactivates the Animation. This can be
	 * overridden if needed using this function to reactivate.
	 * @param active True to activate, false to deactivate the Animation.
	 * @see #active()
	 *
	public void setActive(boolean active) {
		this.active = active;
	}/
	
	/**
	 * Updates the "from" value.
	 * @param from The new keyframe value.
	 */
	public void setFrom(double from) {
		this.from = from;
	}
	
	/**
	 * Updates the "to" value.
	 * @param to The new keyframe value.
	 */
	public void setTo(double to) {
		this.to = to;
	}
	
	/**
	 * Updates the aninmation's duration.
	 * @param duration The new duration.
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	/**
	 * Sets how many times the animation should loop.
	 * @param limit The loop limit.
	 */
	public void setLoopLimit(int loopLimit) {
		this.loopLimit = loopLimit;
	}
	
	/**
	 * Pauses the animation.
	 */
	public void pause() {
		playing = false;
		timer.pause();
	}
	
	/**
	 * Resumes the animation.
	 */
	public void resume() {
		playing = true;
		timer.resume();
	}

	/**
	 * Returns whether the animation is currently playing.
	 * @return True if the animation is in progress.
	 */
	public boolean isPlaying() {
		return playing;
	}
	
	/**
	 * Resets the animation to its starting frame.
	 * Does so by resetting the internal timer.
	 * @param restart If true, the animation resumes playing.
	 */
	public void reset(boolean restart) {
		timer.reset(restart);
		// reset loops
		loopsSoFar = 0;
		// reset playback offset
		offset = 0;
		if (restart)
			resume();
		else
			pause();
	}
	
	/**
	 * Reverses playback direction of the animation.
	 */
	public void reverse(boolean play) {
		forward = !forward;
		// find where playback should start from opposite direction
		offset = duration - getElapsed();
		// reset time elapsed (and resume if needed)
		playing = play;
		timer.reset(playing);
	}
	
	/**
	 * Sets playback direction of the animation.
	 * @param fwd If true, direction is set to forward.
	 * @param play If true, resume the animation.
	 */
	public void setForward(boolean fwd) {
		if (forward != fwd)
			reverse(false);
	}
	
	/**
	 * Checks whether the playback of this animation is
	 * forwards or backwards (has been reversed).
	 * @return True if the animation is playing forwards.
	 */
	public boolean isForward() {
		return forward;
	}
	
	/**
	 * Calculates and returns the current animation value.
	 * @return The animation's current {@code double} value.
	 */
	public double getValue() {
		double val = calculateValue();
		//if (!playing)
		//	active = false;
		return val;
	}

	/**
	 * Returns the current animation value as an integer.
	 * Equivalent to {@code (int)} {@link #getValue()}.
	 * @return The truncated animation value.
	 */
	public int getIntValue() {
		return (int) getValue();
	}
	
	/**
	 * Updates the animation by recalculating its current value.
	 * The intermediate (interpolated) value is found between the
	 * start and end keyframes using the duration of the animation,
	 * its transition function, and the amount of time elapsed.
	 */
	private double calculateValue() {
		// get amount elapsed; capped if not looping
		long elapsed = getElapsed();
		// swap from and to if playing backwards
		double realfrom = forward ? from : to;
		double realto = forward ? to : from;
		// the x-value of the curve, i.e. the percent from [0-1) of completion of this frame
		double progress = elapsed / (double) duration; // i.e., the amount of this key that has passed
		// catch cases for easy to compute transitions
		if (transition == HOLD) { // easy, just return the held key
			return realfrom;
		}
		else if (transition == LINEAR) { // easy, just multiply by progress ratio
			return realfrom + (realto - realfrom) * progress;
		}
		else { // otherwise, transition is cubic
			// y = start + range * bezier_progress
			return realfrom + (realto - realfrom) * bezier(progress, transition);
		}
	}
	
	/**
	 * Calculates the amount of time elapsed during playback.
	 * 
	 * @return If the animation does not loop, returns the
	 * elapsed time, capped by the duration.
	 * If the animation loops (continue mode), returns the
	 * elapsed time {@code mod} duration.
	 * If the animation loops (bounce mode), may reverse
	 * playback, update offset and reset timer if
	 * needed. After doing so, returns the elapsed time
	 * the same as loop continue mode.
	 */
	private long getElapsed() {
		long ms = timer.elapsed() + offset;
		if (ms > duration) {
			if (loopMode == NO_LOOP) {
				ms = duration;
				pause();
			}
			else {
				loopsSoFar += (int) (ms / duration);
				if (loopMode == BOUNCE) {
					int bounces = (int) (ms / duration);
					ms %= duration;
					if (bounces % 2 == 1) {
						// reverse
						forward = !forward;
						offset = ms;
						timer.reset(playing);
					}
				}
				else if (loopMode == CONTINUE)
					ms %= duration;
				if (loopLimit != Integer.MAX_VALUE && loopsSoFar > loopLimit) {
					// return duration to end frame
					ms = duration - offset;
					pause();
				}
			}
		}
		return ms;
	}

	/**
	 * A simple function to calculate the output of a bezier curve with given anchors.
	 * @param x The x input of the bezier function, where the x axis is time.
	 * @param anchors The bezier anchors used to construct the curve function.
	 * @return The y-value of the bezier curve at the given x.
	 */
	public static double bezier(double x, double[] anchors) {
		// note the ternary expressions; saves calculation time
		// (Math.pow time) if result will be multiplied by 0
		return 	(anchors[0] == 0 ? 0 : anchors[0] * Math.pow(1 - x, 3)) +
				(anchors[1] == 0 ? 0 : anchors[1] * Math.pow(1 - x, 2) * x) +
				(anchors[2] == 0 ? 0 : anchors[2] * (1 - x) * Math.pow(x, 2)) +
				(anchors[3] == 0 ? 0 : anchors[3] * Math.pow(x, 3));
	}
	
}
