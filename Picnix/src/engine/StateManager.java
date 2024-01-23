package engine;

import java.util.Stack;

import state.State;

public class StateManager {

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
	
}
