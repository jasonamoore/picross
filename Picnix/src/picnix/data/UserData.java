package picnix.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import picnix.World;

public class UserData {

	private static int INCOMPLETE = 0;
	
	private static int puzzleScores[][];
	
	public static int getPuzzleScore(int worldId, int levelId) {
		return puzzleScores[worldId][levelId];
	}

	public static void setPuzzleScore(int worldId, int levelId, int score) {
		puzzleScores[worldId][levelId] = score;
	}
	
	public static int getWorldScore(int worldId) {
		int count = 0;
		for (int i = 0; i < puzzleScores[worldId].length; i++)
			count += puzzleScores[worldId][i];
		return count;
	}
	
	public static boolean isPuzzleCleared(int worldId, int levelId) {
		return puzzleScores[worldId][levelId] != INCOMPLETE;
	}
	
	public static int getPuzzlesCompleted(int worldId) {
		int count = 0;
		for (int i = 0; i < puzzleScores[worldId].length; i++)
			if (isPuzzleCleared(worldId, i))
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
		try {
			File saveFile = new File(getPath());
			// if no save data was created yet
			if (saveFile.createNewFile())
				save(); // populate with default data (zeroes)
			InputStream is = new FileInputStream(saveFile);
			DataInputStream dis = new DataInputStream(is);
			for (int i = 0; i < World.NUM_WORLDS; i++) {
				puzzleScores[i] = new int[World.getWorld(i).getLevelCount()];
				World w = World.getWorld(i);
				for (int j = 0; j < w.getLevelCount(); j++)
					setPuzzleScore(i, j, dis.readInt());
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void save() {
		try {
			OutputStream is = new FileOutputStream(getPath());
			DataOutputStream dis = new DataOutputStream(is);
			for (int i = 0; i < World.NUM_WORLDS; i++) {
				puzzleScores[i] = new int[World.getWorld(i).getLevelCount()];
				World w = World.getWorld(i);
				for (int j = 0; j < w.getLevelCount(); j++)
					dis.writeInt(getPuzzleScore(i, j));
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getPath() {
		return "user.data";
	}
	
}
