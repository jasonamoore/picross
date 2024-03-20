package picnix;

import picnix.puzzle.Puzzle;

public class Level {
	
	public static final int MIN_MISTAKES = 1;
	public static final int MAX_MISTAKES = 10;
	
	private Puzzle[] layers;
	private boolean layered;
	
	private int id;
	
	public Level(Puzzle[] layers, int id) {
		this.layers = layers;
		this.layered = layers.length > 1;
		this.id = id;
	}
	
	public Level(Puzzle puzzle, int id) {
		this(new Puzzle[] {puzzle}, id);
	}
	
	public Puzzle[] getPuzzles() {
		return layers;
	}
	
	public boolean isLayered() {
		return layered;
	}
	
	public int getId() {
		return id;
	}

	public int getMistakeCap() {
		return 3;
	}

	public int getTimeLimit() {
		return 60 * (layers[0].getRows() * 2 * layers.length);
	}
	
}