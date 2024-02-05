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
	
	public static BufferedImage toolbar;
	public static BufferedImage[] toolarrows;
	public static BufferedImage[] toolpopups;
	public static BufferedImage[] smallrednums;
	public static BufferedImage[] toolicons;

	public static BufferedImage layerbar;
	public static BufferedImage layerframe;
	public static BufferedImage[] layernames;
	public static BufferedImage layerprogress;
	
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
		toolbar = loadOne(uiSheet, 0, 32, 80, 336);
		toolarrows = loadMany(uiSheet, 96, 0, 10, 15, 1, 2);
		toolpopups = loadMany(uiSheet, 106, 0, 18, 9, 2, 2);
		smallrednums = loadMany(uiSheet, 106, 18, 4, 5, 5, 2);
		toolicons = loadMany(uiSheet, 222, 0, 45, 40, 1, 2);
		layerbar = loadOne(uiSheet, 80, 32, 80, 336);
		layerframe = loadOne(uiSheet, 160, 0, 62, 50);
		layernames = loadMany(uiSheet, 160, 50, 62, 15, 1, 4);
		layerprogress = loadOne(uiSheet, 160, 110, 62, 15);
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
