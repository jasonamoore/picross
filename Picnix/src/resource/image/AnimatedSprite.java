package resource.image;

import util.Animation;

public class AnimatedSprite extends Sprite {

	private RasterSheet sheet;
	private Animation anim;
	
	@Override
	protected Raster getCurrentRaster() {
		return sheet.getRasterByIndex(anim.getIntValue());
	}
	
}
