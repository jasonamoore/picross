package puzzle;

public class Level {

	private Puzzle[] layers;
	private boolean layered;
	
	private int id;
	private int mistakeCap;
	private int timeLimit;
	
	protected Level(Puzzle[] layers, int id, int allowedMistakes, int timeLimit) {
		this.layers = layers;
		this.layered = layers.length > 1;
		this.id = id;
		this.mistakeCap = allowedMistakes;
		this.timeLimit = timeLimit;
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
	
	public int getMistakeCap() {
		return mistakeCap;
	}
	
	public int getTimeLimit() {
		return timeLimit;
	}
	
}