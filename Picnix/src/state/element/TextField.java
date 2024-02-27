package state.element;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import resource.Font;

public class TextField extends Element {

	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_RIGHT = 2;
	
	private String text;
	private Font font;
	private int alignment;
	
	public TextField(Font font, int x, int y, int w, int h) {
		this("", font, x, y, w, h);
	}
	
	public TextField(String text, Font font, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.text = text;
		this.font = font;
	}

	public TextField(Font font, int x, int y, int w) {
		this("", font, x, y, w, font.getLineHeight());
	}
	
	public TextField(String text, Font font, int x, int y, int w) {
		this(text, font, x, y, w, font.getLineHeight());
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
	
	@Override
	public void render(Graphics g) {
		if (text.isBlank())
			return;
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		int dw = getWidth();
		// text rendering
		int cy = 0;
		int i = 0;
		while (i < text.length()) {
			// ~ first, calculate line width
			int j = i;
			// start width at the first char
			int cw = font.getGlyph(text.charAt(j++)).getWidth();
			// while fitting in line (and there is letters to fit)
			while (j < text.length() && cw < dw)
				// add next char to width and increment j
				cw += font.getGlyph(text.charAt(j++)).getWidth() + font.getCharPadding();
			// the starting x offset - based on the width of the line
			int cx = alignment == ALIGN_LEFT ? 0 : alignment == ALIGN_CENTER ? (dw - cw) / 2 : dw - cw;
			for (int k = i; k < j; k++) {
				// get char glyph and draw it
				BufferedImage glyph = font.getGlyph(text.charAt(k));
				g.drawImage(glyph, xp + cx, yp + cy, null);
				cx += glyph.getWidth() + font.getCharPadding();
			}
			// update i to next char to draw
			i = j;
			cy += font.getLineHeight();
		}
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
