package resource.bank;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageBank {

	//public static BufferedImage[] tiles;
	public static BufferedImage red;
	public static BufferedImage pink;
	public static BufferedImage white;
	public static BufferedImage grass;
	public static BufferedImage test;
	public static BufferedImage test2;
	public static BufferedImage title;

	public static BufferedImage[] topbar;

	public static BufferedImage[] minimize;
	public static BufferedImage toolbar;
	public static BufferedImage[] toolarrows;
	public static BufferedImage[] toolpopups;
	public static BufferedImage[] smallrednums;
	public static BufferedImage[] toolicons;
	public static BufferedImage[] tooldos;
	public static BufferedImage[] toolbacks;
	// --
	public static BufferedImage layerbar;
	public static BufferedImage layerframe;
	public static BufferedImage[] layernames;
	public static BufferedImage layerprogress;
	
	// blanket
	public static BufferedImage[] plates35;
	public static BufferedImage[] forks35;
	public static BufferedImage[] cells35;
	public static BufferedImage[] plates20;
	public static BufferedImage[] forks20;
	public static BufferedImage[] cells20;
	public static BufferedImage[] plates15;
	public static BufferedImage[] forks15;
	public static BufferedImage[] cells15;
	public static BufferedImage[] plates10;
	public static BufferedImage[] forks10;
	public static BufferedImage[] cells10;
	public static BufferedImage napkin35;
	public static BufferedImage napkin20;
	public static BufferedImage napkin15;
	public static BufferedImage napkin10;
	public static BufferedImage cupmed;
	public static BufferedImage[] numsbig;
	public static BufferedImage[] numsmed;
	public static BufferedImage[] numstiny;
	
	public static BufferedImage grassback;
	
	public static void loadGlobalResources() throws IOException {
		// tests
		red   =	 		loadSheet("redsquare.png");
		pink = 			loadSheet("pinksquare.png");
		white = 		loadSheet("whitesquare.png");
		grass = 		loadSheet("grass.png");
		title =			loadSheet("splash.png");
		grassback = 	loadSheet("grasstest.png");
		// ui sprite sheet
		BufferedImage uiSheet = loadSheet("ui.png");
		topbar = loadMany(uiSheet, 0, 0, 24, 32, 3, 1);
		minimize = loadMany(uiSheet, 126, 18, 14, 11, 2, 1);
		toolbar = loadOne(uiSheet, 0, 32, 80, 336);
		//toolarrows = loadMany(uiSheet, 96, 0, 10, 15, 1, 2);
		toolarrows = loadMany(uiSheet, 160, 125, 12, 17, 1, 2);
		toolpopups = loadMany(uiSheet, 106, 0, 18, 9, 2, 2);
		smallrednums = loadMany(uiSheet, 106, 18, 4, 5, 5, 2);
		toolicons = loadMany(uiSheet, 222, 0, 45, 40, 1, 8);
		tooldos = new BufferedImage[2];
		tooldos[0] = loadOne(uiSheet, 222, 320, 23, 40);
		tooldos[1] = loadOne(uiSheet, 244, 320, 23, 40);
		toolbacks = loadMany(uiSheet, 172, 125, 44, 30, 1, 4);
		layerbar = loadOne(uiSheet, 80, 32, 80, 336);
		layerframe = loadOne(uiSheet, 160, 0, 62, 50);
		layernames = loadMany(uiSheet, 160, 50, 62, 15, 1, 4);
		layerprogress = loadOne(uiSheet, 160, 110, 62, 15);
		// blanket sprite sheet
		BufferedImage blanketSheet = loadSheet("blanket.png");
		plates35 = loadMany(blanketSheet, 0, 0, 33, 33, 1, 5);
		forks35 = loadMany(blanketSheet, 33, 0, 32, 30, 1, 4);
		cells35 = loadMany(blanketSheet, 0, 165, 35, 35, 1, 3);
		plates20 = loadMany(blanketSheet, 66, 0, 18, 18, 1, 5);
		forks20 = loadMany(blanketSheet, 84, 0, 19, 17, 1, 4);
		cells20 = loadMany(blanketSheet, 66, 90, 20, 20, 1, 3);
		plates15 = loadMany(blanketSheet, 106, 0, 15, 15, 1, 5);
		forks15 = loadMany(blanketSheet, 121, 0, 13, 14, 1, 4);
		cells15 = loadMany(blanketSheet, 106, 75, 15, 15, 1, 3);
		plates10 = loadMany(blanketSheet, 136, 0, 10, 10, 1, 5);
		forks10 = loadMany(blanketSheet, 146, 0, 10, 10, 1, 2);
		cells10 = loadMany(blanketSheet, 136, 50, 10, 10, 1, 3);
		napkin35 = loadOne(blanketSheet, 35, 165, 35, 35);
		napkin20 = loadOne(blanketSheet, 86, 90, 20, 20);
		napkin15 = loadOne(blanketSheet, 121, 75, 15, 15);
		napkin10 = loadOne(blanketSheet, 146, 50, 10, 10);
		cupmed = loadOne(blanketSheet, 35, 235, 18, 18);
		numsbig = loadMany(blanketSheet, 70, 230, 19, 19, 5, 1);
		numsmed = loadMany(blanketSheet, 92, 150, 11, 12, 5, 2);
		numstiny = loadMany(blanketSheet, 106, 120, 6, 7, 5, 4);
	}
	
	private static BufferedImage loadSheet(String src) throws IOException {
		BufferedImage image = ImageIO.read(ImageBank.class.getClassLoader().getResourceAsStream(src));
		return image;
	}
	
	private static BufferedImage loadOne(BufferedImage fromSheet, int x, int y, int w, int h) {
		return fromSheet.getSubimage(x, y, w, h);
	}
	
	private static BufferedImage[] loadMany(BufferedImage fromSheet, int startx, int starty, int subw, int subh, int cols, int rows) {
		BufferedImage[] list = new BufferedImage[cols * rows];
		//if (startx + numwide * subw > fromSheet.getWidth()
		//		|| starty + numtall * subh > fromSheet.getHeight()) return null;
		int i = 0;
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < cols; x++)
				list[i++] = fromSheet.getSubimage(startx + x * subw, starty + y * subh, subw, subh);
		
		return list;
	}
	
}
