package picnix;

import picnix.data.FileParser;

public class World {
	
	// static stuff
	public static final int NUM_LOCATIONS = 6;
	public static final int NUM_WORLDS = NUM_LOCATIONS * 2;

	private static final double[] LOCATIONS_RAD = {0.0, Math.PI/3, 2*Math.PI/3, -Math.PI, -2*Math.PI/3, -Math.PI/3};
	private static final int[] LOCATIONS_YOFF = {0, 10, 0, 20, 0, 30, 5, 10, -10, 0, 11, 12};
	private static double maxRadDist;
	
	private static final String[] WORLD_FILES = new String[] {"test2.pwr", "test2.pwr", "test1.pwr", "test1.pwr", "test1.pwr", "test2.pwr",
															"test2.pwr", "test1.pwr", "test2.pwr", "test2.pwr", "test1.pwr", "test2.pwr"};
	
	private static World[] worlds = new World[NUM_WORLDS];
	
	static {
		double max = 0;
		for (int i = NUM_LOCATIONS - 1; i >= 0; i--) {
			double a = LOCATIONS_RAD[(i + 1) % NUM_LOCATIONS];
			double b1 = LOCATIONS_RAD[i];
			double b2 = b1 > 0 ? b1 - 2 * Math.PI : b1 + 2 * Math.PI;
			double dist = Math.min(Math.abs(a - b1), Math.abs(a - b2));
			max = Math.max(max, dist);
		}
		maxRadDist = max;
	}
	
	public static double getMaxRadianDistance() {
		return maxRadDist;
	}
	
	public static double getRadians(int location) {
		return LOCATIONS_RAD[location];
	}
	
	public static double getY(int location) {
		return LOCATIONS_YOFF[location];
	}
	
	public static int getEasyWorldId(int location) {
		return location * 2;
	}
	
	public static int getHardWorldId(int location) {
		return location * 2 + 1;
	}
	
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
	private int unlockScore;
	private boolean[] levels;
	
	public World(int id, int unlockScore, boolean[] levels) {
		this.id = id;
		this.unlockScore = unlockScore;
		this.levels = levels;
	}
	
	public int getId() {
		return id;
	}
	
	public int getUnlockScore() {
		return unlockScore;
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
	
	public Level loadLevel(int levelId) {
		return FileParser.readLevel(levelId, id);
	}
	
}
