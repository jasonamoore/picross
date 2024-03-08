package picnix;

import picnix.puzzle.Puzzle;

public class Level {
	
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
	
}