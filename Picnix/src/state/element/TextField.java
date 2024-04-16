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
	
	private int charLimit;
	
	public TextField(Font font, int x, int y, int w, int h) {
		this("", font, x, y, w, h);
	}
	
	public TextField(String text, Font font, int x, int y, int w, int h) {
		super(x, y, w, h);
		this.font = font;
		setText(text);
	}

	public TextField(Font font, int x, int y, int w) {
		this("", font, x, y, w, font.getLineHeight());
	}
	
	public TextField(String text, Font font, int x, int y, int w) {
		this(text, font, x, y, w, getTextHeight(text, font, w));
	}

	public void setText(String text) {
		this.text = text;
		setCharacterLimit(text.length());
	}
	
	public void setCharacterLimit(int limit) {
		charLimit = limit;
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
		int txtLen = Math.min(charLimit, text.length());
		while (i < txtLen) {
			// ~ first, calculate line width
			int j = i;
			// track how much whitespace this line has
			boolean whitespaceSoFar = true;
			boolean noWhiteSpace = true;
			// the width of the line
			int cw = 0;
			// do while fitting in line (and there is letters to fit)
			do {
				// add next char to width and increment j
				char jth = text.charAt(j);
				whitespaceSoFar &= Character.isWhitespace(jth);
				noWhiteSpace &= (whitespaceSoFar || !Character.isWhitespace(jth));
				if (whitespaceSoFar)
					i++; // skipping drawing whitespace chars
				else
					cw += font.getGlyph(jth).getWidth() + font.getCharPadding();
				j++;
			}
			while (j < txtLen && cw < dw);
			j--; // the actual last fitting character
			
			// if we ended off mid-word, rollback to last whitespace char
			boolean broken = !noWhiteSpace && cw > dw;
			do {
				// if there was any white space in the line but we did not break on it (or EOL)
				broken &= !Character.isWhitespace(text.charAt(j));
				// remove next char from width and decrement j
				if (broken) {
					cw -= font.getGlyph(text.charAt(j)).getWidth() + font.getCharPadding();
					j--;	
				}
			}
			while (broken);
			
			// the starting x offset - based on the width of the line
			int cx = alignment == ALIGN_LEFT ? 0 : alignment == ALIGN_CENTER ? (dw - cw) / 2 : dw - cw;
			for (int k = i; k <= j; k++) {
				// get char glyph and draw it
				BufferedImage glyph = font.getGlyph(text.charAt(k));
				g.drawImage(glyph, xp + cx, yp + cy, null);
				cx += glyph.getWidth() + font.getCharPadding();
			}
			// update i to next char to draw
			i = j + 1;
			cy += font.getLineHeight();
		}
		//
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
	public static int getTextHeight(String text, Font font, int dw) {
		int cy = 0;
		int i = 0;
		while (i < text.length()) {
			// ~ first, calculate line width
			int j = i;
			// track how much whitespace this line has
			boolean whitespaceSoFar = true;
			boolean noWhiteSpace = true;
			// the width of the line
			int cw = 0;
			// do while fitting in line (and there is letters to fit)
			do {
				// add next char to width and increment j
				char jth = text.charAt(j);
				whitespaceSoFar &= Character.isWhitespace(jth);
				noWhiteSpace &= (whitespaceSoFar || !Character.isWhitespace(jth));
				if (whitespaceSoFar)
					i++; // skipping drawing whitespace chars
				else
					cw += font.getGlyph(jth).getWidth() + font.getCharPadding();
				j++;
			}
			while (j < text.length() && cw < dw);
			j--; // the actual last fitting character
			
			// if we ended off mid-word, rollback to last whitespace char
			boolean broken = !noWhiteSpace && cw > dw;
			do {
				// if there was any white space in the line but we did not break on it (or EOL)
				broken &= !Character.isWhitespace(text.charAt(j));
				// remove next char from width and decrement j
				if (broken) {
					cw -= font.getGlyph(text.charAt(j)).getWidth() + font.getCharPadding();
					j--;	
				}
			}
			while (broken);
			
			// update i to next char to draw
			i = j + 1;
			cy += font.getLineHeight();
		}
		return cy;
	}
	
}
