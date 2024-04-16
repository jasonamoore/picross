package resource.bank;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import resource.Font;

public class FontBank {

	public static final char START_OFFSET = ' ';
	public static final int NUM_GLYPHS = 94;
	public static final int DELIMITER_COLOR = 0xFFFF00FF;

	public static Font test;
	public static Font defout;
	
	public static void loadGlobalResources() throws IOException {
		test = loadFont("testFont.png", " abcdefghijklmnopqrstuvwxyz0123456789/");
		defout = loadFont("defout.png", " abcdefghijklmnopqrstuvwxyz0123456789/");
	}
	
	private static Font loadFont(String src, String map) throws IOException {
		BufferedImage image = ImageIO.read(ImageBank.class.getClassLoader().getResourceAsStream(src));
		BufferedImage[] glyphs = new BufferedImage[NUM_GLYPHS];
		int startX = 0, endX = 0;
		int index = 0;
		while (endX < image.getWidth()) {
			if (image.getRGB(endX, 0) == DELIMITER_COLOR) { // reached end of a character
				// get subimage from start to end x points
				BufferedImage glyph = image.getSubimage(startX, 0, endX - startX, image.getHeight());
				// from the string map, get what character this is and put glyph at that spot in array
				glyphs[map.charAt(index) - START_OFFSET] = glyph;
				// move forward
				startX = endX + 1;
				index++;
			}
			endX++;
		}
		return new Font(glyphs, 1, image.getHeight());
	}
	
}
