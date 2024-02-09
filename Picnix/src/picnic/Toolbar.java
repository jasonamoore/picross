package picnic;

import java.awt.Graphics;

import state.element.Container;
import util.Animation;

public class Toolbar extends Container {

	Animation fade;
	Animation collapse;
	
	public Toolbar(int x, int y, int viewWidth, int viewHeight) {
		super(x, y, viewWidth, viewHeight);
	}

	@Override
	public void render(Graphics g) {
		
	}
	
}
