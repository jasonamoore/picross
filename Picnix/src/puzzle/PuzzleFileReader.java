package puzzle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PuzzleFileReader {

	private static final int NUM_LAYERS = 3;
	
	public static Level[] readLayeredWorld(String path) {
		File f = new File(path);
		if (!f.exists() || !f.isFile())
			return null;
		try {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			int count = dis.read();
			Level[] list = new Level[count];
			for (int n = 0; n < count; n++) {
				int time = dis.read();
				int mistakes = dis.read();
				int rows = dis.read();
				int cols = dis.read();
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
				list[n] = new Level(layers, n, time, mistakes);
			}
			dis.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Level[] readNormalWorld(String path) {
		File f = new File(path);
		if (!f.exists() || !f.isFile())
			return null;
		try {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			int count = dis.read();
			Level[] list = new Level[count];
			for (int n = 0; n < count; n++) {
				int time = dis.read();
				int mistakes = dis.read();
				int rows = dis.read();
				int cols = dis.read();
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
				list[n] = new Level(new Puzzle(solution), n, time, mistakes);
			}
			dis.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
