package picnix.data;

import picnix.World;

public class UserData {

	private static int INCOMPLETE = 0;
	
	private static int puzzleScores[][];
	
	public static int getPuzzleScore(int worldId, int levelId) {
		return puzzleScores[worldId][levelId];
	}
	
	public static int getWorldScore(int worldId) {
		int count = 0;
		for (int i = 0; i < puzzleScores[worldId].length; i++)
			count += puzzleScores[worldId][i];
		return count;
	}
	
	public static boolean isPuzzleCompleted(int worldId, int levelId) {
		return puzzleScores[worldId][levelId] != INCOMPLETE;
	}
	
	public static int getPuzzlesCompleted(int worldId) {
		int count = 0;
		for (int i = 0; i < puzzleScores[worldId].length; i++)
			if (isPuzzleCompleted(worldId, i))
				count++;
		return count;
	}
	
	public static boolean isWorldCompleted(int worldId) {
		return getPuzzlesCompleted(worldId) == puzzleScores[worldId].length;
	}
	
	/**
	 * @deprecated
	 */
	public static void randomizeScores() {
		for (int i = 0; i < World.NUM_WORLDS; i++)
			for (int j = 0; j < puzzleScores[i].length; j++)
				puzzleScores[i][j] = Math.random() > 0.5 ? 0 : (int) (Math.random() * 2000);
	}
	
	public static void load() {
		puzzleScores = new int[World.NUM_WORLDS][];
		for (int i = 0; i < World.NUM_WORLDS; i++) {
			puzzleScores[i] = new int[World.getWorld(i).getLevelCount()];
		}
	}
	
	public static void save() {
		
	}
	
}
