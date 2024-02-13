package state.element;

import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Icon extends Element {
	
	public Icon(BufferedImage image) {
		setBackground(image);
	}
	
	public Icon(BufferedImage image, int x, int y, int w, int h) {
		super(x, y, w, h);
		setBackground(image);
	}

	@Override
	public void render(Graphics g) {
		setRenderClips(g);
		Composite oldComp = setRenderComposite(g);
		if (background != null)
			g.drawImage(background, getDisplayX(), getDisplayY(), width, height, null);
		g.setClip(null);
		((Graphics2D) g).setComposite(oldComp);
	}	
	
}
