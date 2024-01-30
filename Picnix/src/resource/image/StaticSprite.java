package resource.image;

import java.awt.image.BufferedImage;

public class StaticSprite extends Sprite {

	private BufferedImage raster;
	
	@Override
	protected BufferedImage getCurrentRaster() {
		return raster;
	}

}
