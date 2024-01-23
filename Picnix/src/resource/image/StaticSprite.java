package resource.image;

public class StaticSprite extends Sprite {

	private Raster raster;
	
	@Override
	protected Raster getCurrentRaster() {
		return raster;
	}

}
