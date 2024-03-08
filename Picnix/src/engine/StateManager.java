package engine;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Stack;

import resource.bank.Palette;
import state.State;
import state.TitleState;
import state.load.LoadState;
import state.particle.Particle;

public class StateManager {

	// stack of State objects - the State list
	private Stack<State> stateStack;

	// transition for states on the stack
	private Transition transition;
	private boolean transitioning;
	
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
		openState(start);
	}

	/**
	 * Returns the topmost (active) state from the stack.
	 * @return The element returned by calling peek on the state stack.
	 */
	public State getTopState() {
		return stateStack.peek();
	}

	/**
	 * Returns the second topmost state from the stack.
	 * @return The element at the second-to-last index of the stack.
	 */
	public State getPreviousState() {
		return stateStack.get(stateStack.size() - 2);
	}
	
	/**
	 * Opens this state, pushing it to the top of the stack.
	 * @param state
	 */
	public void openState(State state) {
		stateStack.push(state);
		state.focus(State.NEWLY_OPENED);
	}
	
	public void transitionToState(State toState, int type, int a, int b) {
		State oldState = getTopState();
		transition = new Transition(toState, oldState, type, a, b);
		transitioning = true;
		oldState.freezeInput(true);
	}
	
	public void transitionExitState(int type, int a, int b) {
		transitionToState(null, type, a, b);
	}
	
	/**
	 * Pops off the active (top) state, closing it.
	 */
	public void exitTopState(boolean error) {
		stateStack.pop();
		if (!stateStack.empty())
			stateStack.peek().focus(!error ? State.RETURNING : State.ERROR_RETURN);
	}
	
	/**
	 * Pops state off the stack until the next
	 * load state is reached.
	 */
	public void popUntilNextLoadState() {
		// pop current state
		exitTopState(false);
		// keep popping until its a load state
		while (!(stateStack.peek() instanceof LoadState))
			exitTopState(false);
	}
	
	public void tick() {
		if (transitioning) {
			// if we passed part a, open the new state
			if (transition.needsStateSwitch()) {
				if (!transition.isExitTransition()) {
					transition.getOldState().freezeInput(false); // unfreeze previous state
					transition.getToState().freezeInput(true); // freeze new state's input
					openState(transition.getToState());
				}
				else
					exitTopState(false);
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
		if (transitioning && !transition.isFinished())
			transition.render(g);
	}
	
}
