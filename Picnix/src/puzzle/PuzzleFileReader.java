package puzzle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PuzzleFileReader {

	public static Level[] readWorld(String path) {
		File f = new File(path);
		if (!f.exists() || !f.isFile())
			return null;
		try {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			int count = dis.read();
			Level[] list = new Level[count];
			for (int n = 0; n < count; n++) {
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
				list[n] = new Level(solution);
			}
			dis.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Puzzle readOnePuzzle(String path) {
		File f = new File(path);
		if (!f.exists() || !f.isFile())
			return null;
		try {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
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
			dis.close();
			return new Puzzle(solution);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
