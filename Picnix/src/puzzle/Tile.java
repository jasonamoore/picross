package puzzle;

import java.awt.Graphics;

import resource.image.Sprite;

public class Tile {

	private Sprite sprite;
	
	private boolean renderValid;
	
	public void invalidateRender() {
		renderValid = false;
	}
	
	public void render(Graphics g) {
		// check if animation frame has updated:
		
		// skip rendering
		if (renderValid)
			return;
	}
	
}
