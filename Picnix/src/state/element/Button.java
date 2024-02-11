package state.element;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import engine.Input;

public class Button extends Element {

	private Color hoverColor = Color.RED;
	private BufferedImage clickBackground;
	
	public Button() {
		super();
	}
	
	public Button(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	public void setBackgrounds(BufferedImage reg, BufferedImage click) {
		setBackground(reg);
		setClickBackground(click);
	}
	
	public void setClickBackground(BufferedImage cbg) {
		clickBackground = cbg;
	}
	
	public void setHoverOutlineColor(Color hc) {
		hoverColor = hc;
	}
	
	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		int xp = getDisplayX();
		int yp = getDisplayY();
		// if not clicking, or has no click background
		if ((!beingClicked(Input.LEFT_CLICK) || clickBackground == null) && background != null) {
			g.drawImage(background, xp, yp, null);
			if (beingHovered()) { // draw focus rectangle
				g.setColor(hoverColor);
				g.drawRect(xp - 1, yp - 1, width + 1, height + 1);
			}
		}
		else if (beingClicked(Input.LEFT_CLICK) && clickBackground != null)
			g.drawImage(clickBackground, xp, yp, null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}
	
}
