package picnix;

import picnix.puzzle.Puzzle;

public class Level {

	public static int NO_SCORE = 0;
	
	private Puzzle[] layers;
	private boolean layered;
	
	private int id;
	
	private int timeLimit;
	private int mistakeCap;
	
	public Level(Puzzle[] layers, int id, int timeLimit, int mistakeCap) {
		this.layers = layers;
		this.layered = layers.length > 1;
		this.id = id;
		this.timeLimit = timeLimit;
		this.mistakeCap = mistakeCap;
	}
	
	public Level(Puzzle puzzle, int id, int timeLimit, int mistakeCap) {
		this(new Puzzle[] {puzzle}, id, timeLimit, mistakeCap);
	}
	
	public Puzzle[] getPuzzles() {
		return layers;
	}
	
	public boolean isLayered() {
		return layered;
	}
	
	public int getLevelId() {
		return id;
	}
	
	public int getTimeLimit() {
		return timeLimit;
	}
	
	public int getMistakeCap() {
		return mistakeCap;
	}
	
}