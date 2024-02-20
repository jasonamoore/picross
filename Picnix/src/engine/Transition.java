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
	private int status;
	
	private boolean exiting;
	private boolean checked;
	private Timer timer;
	
	/**
	 * 
	 * @param state
	 * @param type
	 * @param durA
	 * @param durB
	 * @param status
	 */
	Transition(State toState, State oldState, int type, int durA, int durB, int status) {
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
	
	public int getStatus() {
		return status;
	}
	
	public boolean inPartA() {
		return timer.elapsed() <= durA;
	}
	
	public double getAProgress() {
		return timer.elapsed() / (double) durA;
	}
	
	public double getBProgress() {
		return (timer.elapsed() - durA) / (double) durB;
	}
	
	public boolean needsStateSwitch() {
		if (!inPartA() && !checked)
			return (checked = true);
		else return false;
	}
	
	public boolean isFinished() {
		return timer.elapsed() > durA + durB;
	}
	
}
