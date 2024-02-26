package resource;

import java.awt.image.BufferedImage;

import resource.bank.FontBank;

public class Font {

	private BufferedImage[] glyphs;
	private int charPadding;
	private int lineHeight;
	
	public Font(BufferedImage[] glyphs, int charPadding, int lineHeight) {
		this.glyphs = glyphs;
		this.charPadding = charPadding;
		this.lineHeight = lineHeight;
	}
	
	public int getCharPadding() {
		return charPadding;
	}
	
	public int getLineHeight() {
		return lineHeight;
	}
	
	public BufferedImage getGlyph(int ascii) {
		ascii -= FontBank.START_OFFSET;
		if (ascii >= 0 && ascii < glyphs.length)
			return glyphs[ascii];
		else
			return null;
	}
	
}
