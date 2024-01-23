package resource.image;

import java.awt.Graphics;

public abstract class Sprite {

	public void render(Graphics g) {
		getCurrentRaster().render(g);
	}
	
	protected abstract Raster getCurrentRaster();
	
}
