package util;

/**
 *	Class for creating animations consisting of a set of keyframes, timings, and transitions.
 *	Keyframes are numerical (doubles) and can be used with versatility to represent various kinds of animations.
 *
 *	For example: The HOLD transition can be used to iterate through a set of integers, which can be used
 *				 as indices of an array of images to create an animated image.
 *	For example: The LINEAR transition can be used to animate an entity moving horizontally across the screen.
 */
public class Animation {

	// loop constants
	public static final int LOOP_NONE = 0;
	public static final int LOOP_CONTINUE = 1;
	public static final int LOOP_BOUNCE = 2;
	
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
	
	private long offset;
	
	public Animation(double from, double to, int duration, double[] transition, int loopMode, boolean start) {
		timer = new Timer(false); // set up a timer for the anim
		this.from = from;
		this.to = to;
		this.duration = duration;
		this.transition = transition;
		this.loopMode = loopMode;
		if (start) resume(); // start anim
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
		playing = restart;
		// reset playback offset
		offset = 0;
		timer.reset(restart);
	}
	
	/**
	 * Reverses playback direction of the animation.
	 * WARNING: Loop behavior is undefined if an animation
	 * is reversed while playback has already started.
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
	 * WARNING: Loop behavior is undefined if an animation
	 * is reversed while playback has already started.
	 * @param fwd If true, direction is set to forward.
	 * @param play If true, resume the animation.
	 */
	public void setForward(boolean fwd, boolean play) {
		if (forward != fwd)
			reverse(play);
	}
	
	/**
	 * Calculates and returns the current animation value.
	 * @return The animation's current {@code double} value.
	 */
	public double getValue() {
		return calculateValue();
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
	 * This is achieved by first determining what the current keyframe of the animation is,
	 * based on the amount of time elapsed and the duration of each keyframe.
	 * After the current frame is found, the intermediate value is found between the
	 * current keyframe and the next keyframe. The intermediate value is determined
	 * by the duration of the keyframe, its transition, and the amount of time elapsed.
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
	
	private long getElapsed() {
		long ms = timer.elapsed() + offset;
		if (ms > duration) {
			if (loopMode == LOOP_BOUNCE) {
				int bounces = (int) (ms / duration);
				ms %= duration;
				if (bounces % 2 == 1) {
					// reverse
					forward = !forward;
					offset = ms;
					timer.reset(playing);
				}
					
			}
			else if (loopMode == LOOP_CONTINUE)
				ms %= duration;
			else {
				pause();
				ms = duration;
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
	private static double bezier(double x, double[] anchors) {
		// note the ternary expressions; saves calculation time
		// (Math.pow time) if result will be multiplied by 0
		return 	(anchors[0] == 0 ? 0 : anchors[0] * Math.pow(1 - x, 3)) +
				(anchors[1] == 0 ? 0 : anchors[1] * Math.pow(1 - x, 2) * x) +
				(anchors[2] == 0 ? 0 : anchors[2] * (1 - x) * Math.pow(x, 2)) +
				(anchors[3] == 0 ? 0 : anchors[3] * Math.pow(x, 3));
	}
	
}
