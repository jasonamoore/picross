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
	
	private static BufferedImage load(String src) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(ImageBank.class.getClassLoader().getResourceAsStream(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	private static BufferedImage[] loadSheet(String src, int stx, int sty, int sw, int sh, int lx, int ly) {
		BufferedImage sheet = load(src);
		BufferedImage[] list = new BufferedImage[lx * ly];
		
		if (stx + lx * sw > sheet.getWidth() || sty + ly * sh > sheet.getHeight()) return null;
		
		int i = 0;
		for (int y = 0; y < ly; y++) {
			for (int x = 0; x < lx; x++) {
				list[i] = sheet.getSubimage(stx + x * sw, sty + y * sh, sw, sh);
				i++;
			}
		}
		return list;
	}
	
	public static void loadGlobalResources() {
		red   =	 		load("redsquare.png");
		pink = 			load("pinksquare.png");
		white = 		load("whitesquare.png");
		grass = 		load("grass.png");
	}
	
}
