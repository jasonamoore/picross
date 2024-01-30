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
	public static final double[] EASE_OUT= PRESETS[EASE_OUT_INDEX];
	
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
	// the number of allowed loops
	private int loopCount = 0;
	
	private int startFrame;
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
		timer = new Timer(false); // set up a timer for the anim
		frameCount = keys.length; // num of frames in the anim
		keyframes = keys;
		timings = times;
		transitions = trans;
		startFrame = start;
		loopCount = loops; // num of loops
		// to calculate duration and offset time:
		int sum = 0;
		for (int i = 0; i < times.length; i++) {
			if (i == start)
				offset = sum;
			sum += times[i];
		}
		duration = sum; // sum of each keyframe duration
		if (go) resume(); // start anim
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
		/*
		 *  step 0: UPDATE LOOP INFO
		 */
		// elapsed time divided by anim duration = # full loops
		int loopsSoFar = (int) Math.floor(timer.elapsed() / duration);
		if (loopsSoFar != INF_LOOP && loopsSoFar > loopCount) { // too many loops!
			// stop the anim at last reached frame (frame before startFrame)
			int frameBefore = (startFrame - 1 + frameCount) % frameCount;
			value = keyframes[frameBefore];
			pause(); // pause anim and return
			return;
		}
		// check if too many loops have occurred
		/*
		 *  step 1: FIND THE ANIMATION'S CURRENT KEYFRAME
		 */
		// start from first frame
		int frame = 0;
		// grab the time elapsed (modulus to get elapsed time within anim loop)
		long elapsed = (timer.elapsed() + offset) % duration;
		// increment frame as needed, adding key durations until we reached elapsed
		int sum = timings[frame];
		do {
			sum += timings[frame++];
		} while (elapsed > sum);
		// rollback to last valid frame
		sum -= timings[frame--];
		
		/*
		 *  step 2: FIND INTERMEDIATE VALUE
		 */
		// first grab relevant info
		double keyfrom = keyframes[frame]; // the current key
		double keyto = keyframes[(frame + 1) % frameCount]; // the next key (to interpolate to)
		double keydur = timings[frame]; // the duration of the current key
		double[] keytrans = transitions[frame]; // the transition from the curr to next key
		// the x-value of the curve, i.e. the percent from [0-1) of completion of this frame
		double progress = (sum - elapsed) / keydur; // i.e., the amount of this key that has passed
		System.out.println(progress);
		// catch cases for easy to compute transitions
		if (keytrans == HOLD) { // easy, just return the held key
			value = keyfrom;
		}
		else if (keytrans == LINEAR) { // easy, just multiply by progress ratio
			value = keyfrom + (keyto - keyfrom) * progress;
		}
		else { // otherwise, transition is cubic
			// y = start + range * bezier_progress
			value = keyfrom + (keyto - keyfrom) * bezier(progress, keytrans);
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
