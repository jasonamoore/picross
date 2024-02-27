package picnix.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import picnix.Level;
import picnix.World;
import picnix.puzzle.Puzzle;

public class FileParser {

	private static final int NUM_LAYERS = 3;
	
	public static World readWorld(int worldId) {
		InputStream is = FileParser.class.getClassLoader().getResourceAsStream(World.getWorldPath(worldId));
		try {
			DataInputStream dis = new DataInputStream(is);
			// first byte = num of levels in world
			int count = dis.read();
			// next four (int) - world unlock score
			int unlockScore = dis.readInt();
			boolean[] levels = new boolean[count];
			// read next n bits; tells whether levels are normal or layered
			int b = 0;
			int curByte = 0;
			for (int n = 0; n < count; n++) {
				if (b == 0)
					curByte = dis.read();
				// if the bit is a 0, not layered (false); 1 is true
				levels[n] = ((curByte >> b) & 1) == 0 ? false : true;
				b = (b + 1) % 8;
			}
			dis.close();
			return new World(worldId, unlockScore, levels);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Level readLevel(int levelId, int worldId) {
		InputStream is = FileParser.class.getClassLoader().getResourceAsStream(World.getWorldPath(worldId));
		try {
			DataInputStream dis = new DataInputStream(is);
			World world = World.getWorld(worldId);
			// don't care about header bytes, will skip past them
			// 1 byte for level count + 4 for unlock score, and n puzzle bits (padded)
			int metaBytes = 1 + 4 + (int) Math.ceil(world.getLevelCount() / 8.0);
			if (dis.skipBytes(metaBytes) != metaBytes) {
				dis.close();
				return null; // skip bytes failed (shouldn't happen)
			}
			boolean[] levels = world.getLevels();
			// start reading levels from here, until the nth level
			for (int n = 0; n < levelId; n++) {
				// skip the number of bytes this level contains
				int rows = dis.read();
				int cols = dis.read();
				// two bytes for time and mistake data, then row * col bits (padded) for one layer, times three if layered
				int numBytes = 2 + (int) Math.ceil(rows * cols / 8.0) * (levels[n] ? 3 : 1);
				if (dis.skipBytes(numBytes) != numBytes) {
					dis.close();
					return null; // skip bytes failed (shouldn't happen)
				}
			}
			// now have skipped enough bytes to be aligned at the desired level id
			// if this level is layered, use read layered function, else read normal, save result
			Level theOne = levels[levelId] ? readOneLayeredLevel(dis, levelId) : readOneNormalLevel(dis, levelId);
			dis.close();
			return theOne;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Level readOneNormalLevel(DataInputStream dis, int id) throws IOException {
		int rows = dis.read();
		int cols = dis.read();
		int time = dis.read();
		int mistakes = dis.read();
		boolean[][] solution = new boolean[rows][cols];
		int b = 0;
		int curByte = 0;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (b == 0)
					curByte = dis.read();
				solution[r][c] = ((curByte >> b) & 1) == 0 ? false : true;
				b = (b + 1) % 8;
			}
		}
		return new Level(new Puzzle(solution), id, time, mistakes);
	}
	
	private static Level readOneLayeredLevel(DataInputStream dis, int id) throws IOException {
		int rows = dis.read();
		int cols = dis.read();
		int time = dis.read();
		int mistakes = dis.read();
		Puzzle[] layers = new Puzzle[NUM_LAYERS];
		for (int p = 0; p < NUM_LAYERS; p++) {
			boolean[][] solution = new boolean[rows][cols];
			int b = 0;
			int curByte = 0;
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (b == 0)
						curByte = dis.read();
					solution[r][c] = ((curByte >> b) & 1) == 0 ? false : true;
					b = (b + 1) % 8;
				}
			}
			layers[p] = new Puzzle(solution);
		}
		return new Level(layers, id, time, mistakes);
	}

}
