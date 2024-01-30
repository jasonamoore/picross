package resource.image;

import java.awt.image.BufferedImage;

import util.Animation;

public class AnimatedSprite extends Sprite {

	private BufferedImage[] sheet;
	private Animation anim;
	
	@Override
	protected BufferedImage getCurrentRaster() {
		return sheet[anim.getIntValue()];
	}
	
}
