package state.element;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Icon extends Element {

	private BufferedImage image;
	
	public Icon(BufferedImage image) {
		this.image = image;
	}
	
	@Override
	public void render(Graphics g) {
		int px = getDisplayX();
		int py = getDisplayX();
		g.drawImage(image, px, py, width, height, null);
	}	
	
}
