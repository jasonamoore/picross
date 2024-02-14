package engine;

import java.util.Stack;

import state.State;

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
		stateStack.push(start);
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
	 */
	public void openState(State state) {
		stateStack.push(state);
	}

	/**
	 * Pops off the active (top) state, closing it.
	 */
	public void exitTopState() {
		stateStack.pop();
	}
	
}
