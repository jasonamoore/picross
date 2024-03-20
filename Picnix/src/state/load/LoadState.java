package state.load;

import java.awt.Graphics;

import engine.Engine;
import engine.Transition;
import resource.bank.Palette;
import state.State;
import util.Timer;

public abstract class LoadState extends State {

	public static final int DEFAULT_MIN_WAIT = 500;
	public static final int DEFAULT_TRANS_TYPE = Transition.FADE;
	public static final int DEFAULT_TRANS_DUR = 250;

	private Timer timer;
	private int minWait = DEFAULT_MIN_WAIT;
	
	private State nextState;
	private int transType = DEFAULT_TRANS_TYPE;
	private int transDur = DEFAULT_TRANS_DUR;
	
	protected boolean done;
	
	public LoadState() {
		timer = new Timer(false);
	}
	
	@Override
	public void focus(int status) {
		timer.reset(true);
		nextState = null;
		done = false;
		if (status == NEWLY_OPENED)
			load();
		else
			unload();
	}

	protected void done() {
		done = true;
	}
	
	public void setNextState(State nextState) {
		this.nextState = nextState;
	}
	
	public void setTransitionType(int type) {
		transType = type;
	}
	
	public void setTransitionDuration(int duration) {
		transDur = duration;
	}
	
	public void setMinWaitTime(int time) {
		minWait = time;
	}
	
	
	public abstract void load();
	
	public abstract void unload();
	
	@Override
	public void tick() {
		if (done && timer.elapsed() >= minWait)
			Engine.getEngine().getStateManager().transitionToState(nextState, transType, 0, transDur);
	}

	@Override
	public void render(Graphics g) {
		// render load animation
		g.setColor(Palette.BLACK);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		// render tips or whatever
	}
	
}
