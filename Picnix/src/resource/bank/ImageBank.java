package resource.bank;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageBank {

	public static BufferedImage title;
	public static BufferedImage grassback;
	public static BufferedImage island;

	public static BufferedImage[] bluebutton;
	public static BufferedImage[] pinkbutton;
	public static BufferedImage[] greenbutton;
	public static BufferedImage[] goldbutton;
	public static BufferedImage[] bluebuttonclick;
	public static BufferedImage[] pinkbuttonclick;
	public static BufferedImage[] greenbuttonclick;
	public static BufferedImage[] goldbuttonclick;
	public static BufferedImage[] buttondisabled;
	// --
	public static BufferedImage[] normallevelbutton;
	public static BufferedImage[] layeredlevelbutton;
	public static BufferedImage hiscorebar;
	public static BufferedImage newlevelalert;
	public static BufferedImage hiddenpreview;
	// --
	public static BufferedImage[] topbar;
	public static BufferedImage time;
	public static BufferedImage score;
	public static BufferedImage mistakes;
	public static BufferedImage[] digits;
	public static BufferedImage comma;
	public static BufferedImage colon;
	public static BufferedImage[] mistakeLives;
	// --
	public static BufferedImage[] minimaxers;
	public static BufferedImage toolbar;
	public static BufferedImage toolbarbottom;
	public static BufferedImage[] toolarrows;
	public static BufferedImage[] toolpopups;
	public static BufferedImage[] smallrednums;
	public static BufferedImage[] toolicons;
	public static BufferedImage[] tooldos;
	public static BufferedImage[] toolbacks;
	// --
	public static BufferedImage layerbar;
	public static BufferedImage layerbarbottom;
	public static BufferedImage[] layerframes;
	public static BufferedImage layeractive;
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
	public static BufferedImage[] hintHoriz35;
	public static BufferedImage[] hintVert35;
	public static BufferedImage[] hintHoriz20;
	public static BufferedImage[] hintVert20;
	public static BufferedImage[] hintHoriz15;
	public static BufferedImage[] hintVert15;
	public static BufferedImage[] hintHoriz10;
	public static BufferedImage[] hintVert10;
	public static BufferedImage cupmed;
	public static BufferedImage[] numsbig;
	public static BufferedImage[] numsmed;
	public static BufferedImage[] numstiny;
	
	//
	
	
	public static void loadGlobalResources() throws IOException {
		// tests
		title =			loadSheet("title.png");
		grassback = 	loadSheet("grasstest.png");
		island = 	loadSheet("island.png");
		// ui sprite sheet
		BufferedImage uiSheet = loadSheet("ui.png");
		bluebutton = loadMany(uiSheet, 267, 151, 3, 3, 3, 3);
		pinkbutton = loadMany(uiSheet, 276, 151, 3, 3, 3, 3);
		greenbutton = loadMany(uiSheet, 285, 151, 3, 3, 3, 3);
		goldbutton = loadMany(uiSheet, 294, 151, 3, 3, 3, 3);
		bluebuttonclick = loadMany(uiSheet, 267, 160, 3, 3, 3, 3);
		pinkbuttonclick = loadMany(uiSheet, 276, 160, 3, 3, 3, 3);
		greenbuttonclick = loadMany(uiSheet, 285, 160, 3, 3, 3, 3);
		goldbuttonclick = loadMany(uiSheet, 294, 160, 3, 3, 3, 3);
		buttondisabled = loadMany(uiSheet, 303, 160, 3, 3, 3, 3);
		normallevelbutton = loadMany(uiSheet, 267, 169, 48, 48, 2, 1);
		layeredlevelbutton = loadMany(uiSheet, 267, 217, 48, 48, 2, 1);
		hiscorebar = loadOne(uiSheet, 303, 153, 41, 7);
		newlevelalert = loadOne(uiSheet, 363, 169, 39, 39);
		hiddenpreview = loadOne(uiSheet, 363, 208, 39, 31);
		//
		topbar = loadMany(uiSheet, 0, 0, 24, 32, 4, 1);
		time = loadOne(uiSheet, 267, 94, 55, 19);
		score = loadOne(uiSheet, 267, 113, 71, 19);
		mistakes = loadOne(uiSheet, 267, 132, 97, 19);
		digits = loadMany(uiSheet, 267, 60, 11, 17, 5, 2);
		comma = loadOne(uiSheet, 322, 60, 3, 5);
		colon = loadOne(uiSheet, 325, 60, 4, 10);
		mistakeLives = loadMany(uiSheet, 322, 93, 19, 20, 2, 1);
		minimaxers = loadMany(uiSheet, 96, 18, 14, 11, 4, 1);
		toolbar = loadOne(uiSheet, 0, 32, 80, 333);
		toolbarbottom = loadOne(uiSheet, 0, 365, 80, 3);
		toolarrows = loadMany(uiSheet, 160, 175, 12, 17, 1, 2);
		toolpopups = loadMany(uiSheet, 96, 0, 18, 9, 3, 2);
		smallrednums = loadMany(uiSheet, 267, 0, 4, 5, 5, 2);
		toolicons = loadMany(uiSheet, 222, 0, 45, 40, 1, 8);
		tooldos = new BufferedImage[2];
		tooldos[0] = loadOne(uiSheet, 222, 320, 23, 40);
		tooldos[1] = loadOne(uiSheet, 244, 320, 23, 40);
		toolbacks = loadMany(uiSheet, 172, 175, 44, 30, 1, 6);
		layerbar = loadOne(uiSheet, 80, 32, 80, 333);
		layerbarbottom = loadOne(uiSheet, 80, 365, 80, 3);
		layerframes = loadMany(uiSheet, 160, 0, 62, 50, 1, 2);
		layeractive = loadOne(uiSheet, 267, 10, 62, 50);
		layernames = loadMany(uiSheet, 160, 100, 62, 15, 1, 4);
		layerprogress = loadOne(uiSheet, 160, 160, 62, 15);
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
		cells15 = loadMany(blanketSheet, 146, 40, 15, 15, 1, 3);
		plates10 = loadMany(blanketSheet, 136, 0, 10, 10, 1, 5);
		forks10 = loadMany(blanketSheet, 146, 0, 10, 10, 1, 4);
		cells10 = loadMany(blanketSheet, 156, 0, 10, 10, 1, 3);
		hintHoriz35 = loadMany(blanketSheet, 35, 165, 20, 35, 4, 1);
		hintVert35 = loadMany(blanketSheet, 35, 200, 35, 20, 2, 2);
		hintHoriz20 = loadMany(blanketSheet, 86, 90, 15, 20, 4, 1);
		hintVert20 = loadMany(blanketSheet, 86, 110, 20, 15, 2, 2);
		hintHoriz15 = loadMany(blanketSheet, 161, 40, 10, 15, 4, 1);
		hintVert15 = loadMany(blanketSheet, 161, 55, 15, 10, 2, 2);
		hintHoriz10 = loadMany(blanketSheet, 166, 0, 7, 10, 4, 1);
		hintVert10 = loadMany(blanketSheet, 166, 10, 10, 7, 2, 2);
		numsbig = loadMany(blanketSheet, 35, 240, 11, 17, 5, 1);
		numsmed = loadMany(blanketSheet, 86, 140, 8, 10, 5, 2);
		numstiny = loadMany(blanketSheet, 161, 75, 6, 7, 5, 4);
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
