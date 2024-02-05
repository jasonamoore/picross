package state.element;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Button extends Element {

	private Color hoverColor = Color.WHITE;
	private BufferedImage clickBackground;
	
	public Button() {
		super();
	}
	
	public Button(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	public void setClickBackground(BufferedImage cbg) {
		clickBackground = cbg;
	}
	
	public void setHoverOutlineColor(Color hc) {
		hoverColor = hc;
	}
	
	@Override
	public void render(Graphics g) {
		int xp = getDisplayX();
		int yp = getDisplayY();
		if (!clicking && background != null) {
			g.drawImage(background, xp, yp, width, height, null);
			if (hovering) { // draw focus rectangle
				g.setColor(hoverColor);
				g.drawRect(xp, yp, width - 1, height - 1);
			}
		}
		else if (clicking && clickBackground != null)
			g.drawImage(clickBackground, xp, yp, width, height, null);
	}
	
}
