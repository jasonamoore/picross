package picnix;

import picnix.io.FileParser;

public class World {
	
	// static stuff
	
	public static final int NUM_WORLDS = 6;
	
	private static final String[] WORLD_FILES = new String[] {"test.pwr", "test2.pwr", "test.pwr", "test2.pwr", "test.pwr", "test2.pwr"};
	
	private static World[] worlds = new World[WORLD_FILES.length];
	
	public static World loadWorld(int worldId) {
		worlds[worldId] = FileParser.readWorld(worldId);
		return getWorld(worldId);
	}
	
	public static void unloadWorld(int worldId) {
		worlds[worldId] = null;
	}
	
	public static World getWorld(int worldId) {
		return worlds[worldId];
	}
	
	public static String getWorldPath(int worldId) {
		return WORLD_FILES[worldId];
	}
	
	// non-static stuff

	private int id;
	private boolean[] levels;
	
	public World(int id, boolean[] levels) {
		this.id = id;
		this.levels = levels;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	 * Fetches this World's loaded level data, which
	 * is a boolean array whose length is equal to the
	 * number of levels in the world.
	 * For each index, false represents a normal level
	 * and true represents a layered level.
	 * @return The levels array.
	 */
	public boolean[] getLevels() {
		return levels;
	}
	

	/**
	 * Returns the number of levels in this world.
	 * @return The length of the levels array.
	 */
	public int getLevelCount() {
		return levels.length;
	}
	
	/**
	 * Fetches the list of high scores for each level
	 * in this World. For each index, a positive integer
	 * represents the high score for the level of whose
	 * id is that index. A special value of 0 indicates
	 * that no high score exists because the level has
	 * not been beaten.
	 * @return The scores array.
	 
	public int[] getScores() {
		return scores;
	}*/
	
	public Level loadLevel(int levelId) {
		return FileParser.readLevel(levelId, id);
	}
	
}
