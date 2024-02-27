package engine;

import state.State;
import util.Timer;

public class Transition {

	public static final int NONE = 0;
	public static final int FADE = 1;

	private State toState;
	private State oldState;
	private int type;
	private int durA, durB;
	
	private boolean exiting;
	private boolean checked;
	private Timer timer;
	
	/**
	 * 
	 * @param state
	 * @param type
	 * @param durA
	 * @param durB
	 */
	Transition(State toState, State oldState, int type, int durA, int durB) {
		this.toState = toState;
		this.oldState = oldState;
		exiting = toState == null;
		this.type = type;
		this.durA = durA;
		this.durB = durB;
		timer = new Timer(true);
	}

	public State getOldState() {
		return oldState;
	}
	
	public boolean isExitTransition() {
		return exiting;
	}
	
	public State getToState() {
		return toState;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean inPartA() {
		return !checked;
	}
	
	public double getAProgress() {
		if (durA == 0)
			return 1;
		return Math.max(0, Math.min(1, timer.elapsed() / (double) durA));
	}
	
	public double getBProgress() {
		if (durB == 0)
			return inPartA() ? 0 : 1;
		return Math.max(0, Math.min(1, (timer.elapsed() - durA) / (double) durB));
	}
	
	public boolean needsStateSwitch() {
		if (timer.elapsed() > durA && !checked)
			return (checked = true);
		else
			return false;
	}
	
	public boolean isFinished() {
		return checked && timer.elapsed() > durA + durB;
	}
	
}
