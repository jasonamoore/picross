package state.load;

import java.awt.Graphics;

import engine.Engine;
import engine.Transition;
import resource.bank.Palette;
import state.State;
import util.Timer;

public abstract class LoadState extends State {
	
	public static final int MIN_WAIT_MS = 500;
	
	private State nextState;
	private Timer timer;
	protected boolean done;
	
	public LoadState() {
		timer = new Timer(false);
	}
	
	@Override
	public void focus(int status) {
		timer.reset(true);
		if (status == State.NEWLY_OPENED)
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
	
	public abstract void load();
	
	public abstract void unload();
	
	@Override
	public void tick() {
		if (done && timer.elapsed() >= MIN_WAIT_MS)
			Engine.getEngine().getStateManager()
				.transitionToState(nextState, Transition.FADE, 0, 250, State.NEWLY_OPENED);
	}

	@Override
	public void render(Graphics g) {
		// render load animation
		g.setColor(Palette.BLACK);
		g.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
		// render tips or whatever
	}
	
}
