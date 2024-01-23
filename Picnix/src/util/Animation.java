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
	public static final int NO_LOOP = 0;
	public static final int INF_LOOP = Integer.MAX_VALUE;
	
	// animation transition strategies (corresponds to PRESETS array)
	public static final int HOLD = 0;
	public static final int LINEAR = 1;
	public static final int CUBIC = 2;
	public static final int INV_CUBIC = 3;
	public static final int EASE_IN = 4;
	public static final int EASE_OUT = 5;
	
	// bezier coefficients for presets (indices match to strategy constants)
	private static final double[][] PRESETS = {
			{0, 0, 0, 0},
			{0, 1, 2, 1},
			{0, 0, 3, 1},
			{0, 2.25, 0.75, 1},
			{0, 0, 0.75, 1},
			{0, 2.25, 3, 1},
	};
	
	// Timer used for timing the animation
	private Timer timer;

	/* KEYFRAMES: stored as three arrays (values, timings & transitions)
	 * 
	 * keyframes: the keyframe decimal values
	 * timings: the END time of each keyframe
	 * transitions:
	 * 		a double array of coefficients for a bezier curve
	 * 		representing the transition between two keyframes
	 */
	private double[] keyframes;
	private int[] timings;
	private double[][] transitions;
	
	// is the animation currently running
	private boolean playing = false;
	// the number of remaining loops (0 means no loop)
	private int loopCount = 0;
	
	// the index of the current keyframe
	private int frame;
	// the offset in ms, of the start keyframe
	private long offset;
	// the total length, in frames, of the animation
	private int frameCount;
	// the total length, in ms, of the animation
	private long duration;
	
	// the current animation value
	private double value;
	
	/**
	 * Creates an Animation with the specified keyframes and additional parameters.
	 * @param keys The list of animation keyframes.
	 * @param time The list of durations of each keyframe.
	 * @param trans The list of transitions between keyframes.
	 * @param bez The list of custom bezier anchors for transitions between keyframes.
	 * @param start The keyframe at which the animation begins.
	 * @param loops The amount of times the animation should loop.
	 * @param go Whether the animation should begin playing immediately.
	 */
	public Animation(double[] keys, int[] times, double[][] trans, int start, int loops, boolean go) {
		timer = new Timer(false);
		frame = start;
		offset = start == 0 ? 0 : times[start - 1];
		frameCount = keys.length;
		duration = times[frameCount - 1];
		keyframes = keys;
		timings = times;
		transitions = trans;
		loopCount = loops;
		if (go) resume();
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
	 * Calculates and returns the current animation value.
	 * @return The animation's current {@code double} value.
	 */
	public double getValue() {
		if (playing)
			update();
		return value;
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
	private void update() {
		// increment frame if needed, until find the two keyframes which bound the elapsed time
		// e.g. if elapsed = 11, with keyframe times {5, 10, 12, 15} find frame = k[1] (right bound = k[2])
		long elapsed = (timer.elapsed() + offset) % duration;
		while (elapsed >= timings[frame]) {
			frame++;
			// if past the last frame, and no loops left, value holds on the last keyframe
			if (frame >= frameCount && loopCount < 1) {
				value = keyframes[frameCount - 1];
				return;
			}
			// otherwise, loop to beginning (keyframe 0) and decrement # of loops
			else {
				frame %= frameCount;
				loopCount--;
			}
		}
		// to find intermediate - get relevant animation information
		double keyfrom = keyframes[frame];
		double keyto = keyframes[(frame + 1) % frameCount];
		int start = frame == 0 ? 0 : timings[frame - 1];
		int end = timings[frame];
		double[] trans = transitions[frame];
		// the x-value of the curve, i.e. the percent from [0-1) of completion of this frame
		double progress = (elapsed - start) / (end - start);
		// catch cases for easy to compute transitions
		if (trans == PRESETS[HOLD]) { // easy, just return the held key
			value = keyfrom;
		}
		else if (trans == PRESETS[LINEAR]) { // easy, just multiply by progress ratio
			value = keyfrom + (keyto - keyfrom) * progress;
		}
		else { // otherwise, transition is cubic
			// y = start + range * bezier_progress
			value = keyfrom + (keyto - keyfrom) * bezier(progress, trans);
		}
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
		return 	anchors[0] == 0 ? 0 : (anchors[0] * Math.pow(1 - x, 3)) +
				anchors[1] == 0 ? 0 : (anchors[1] * Math.pow(1 - x, 2) * x) +
				anchors[2] == 0 ? 0 : (anchors[2] * (1 - x) * Math.pow(x, 2)) +
				anchors[3] == 0 ? 0 : (anchors[3] * Math.pow(x, 3));
	}
	
}
