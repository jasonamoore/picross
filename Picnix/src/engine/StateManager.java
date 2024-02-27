package engine;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Stack;

import resource.bank.Palette;
import state.State;
import state.particle.Particle;

public class StateManager {

	// stack of State objects - the State list
	private Stack<State> stateStack;
	
	/**
	 * Initializes a state manager with an empty state stack.
	 */
	private StateManager() {
		// create empty state stack
		stateStack = new Stack<State>();
	}
	
	/**
	 * Initializes a state manager with a start state.
	 * @param start The start state to push onto the stack.
	 */
	public StateManager(State start) {
		this();
		openState(start, State.NEWLY_OPENED);
	}

	/**
	 * Returns the topmost (active) state from the stack.
	 * @return The element returned by calling peek on the state stack.
	 */
	public State getTopState() {
		return stateStack.peek();
	}

	/**
	 * Opens this state, pushing it to the top of the stack.
	 * @param state
	 * @param status 
	 */
	public void openState(State state, int status) {
		stateStack.push(state);
		state.focus(status);
	}
	
	
	public void transitionToState(State toState, int type, int a, int b, int status) {
		State oldState = getTopState();
		transition = new Transition(toState, oldState, type, a, b, status);
		transitioning = true;
		oldState.freezeInput(true);
	}
	
	public void transitionExitState(int type, int a, int b, int status) {
		transitionToState(null, type, a, b, status);
	}

	/**
	 * Pops off the active (top) state, closing it.
	 */
	public void exitTopState(int status) {
		stateStack.pop();
		if (!stateStack.empty())
			stateStack.peek().focus(status);
	}

	private Transition transition;
	private boolean transitioning;
	
	public void tick() {
		if (transitioning) {
			// if we passed part a, open the new state
			if (transition.needsStateSwitch()) {
				transition.getToState().freezeInput(true); // freeze new state's input
				if (!transition.isExitTransition())
					openState(transition.getToState(), transition.getStatus());
				else
					exitTopState(transition.getStatus());
				transition.getOldState().freezeInput(false); // unfreeze previous state
			}
			// if everything is over
			if (transition.isFinished()) {
				transitioning = false; // mark that we're done
				getTopState().freezeInput(false); // unfreeze the new state
			}
		}
		getTopState().tick();
	}
	
	public void render(Graphics g) {
		// render active state
		getTopState().render(g);
		// render particles
		Particle.renderParticles(g);
		// render transition effect
		if (transitioning && !transition.isFinished()) {
			if (transition.getType() == Transition.FADE) {
				float opacity = (float) (transition.inPartA() ? transition.getAProgress() : 1.0 - transition.getBProgress());
				Graphics2D gg = (Graphics2D) g;
				Composite oldComp = gg.getComposite();
				gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				gg.setColor(Palette.BLACK);
				gg.fillRect(0, 0, Engine.SCREEN_WIDTH, Engine.SCREEN_HEIGHT);
				gg.setComposite(oldComp);
			}
		}
	}
	
}
