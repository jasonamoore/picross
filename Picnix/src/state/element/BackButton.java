package state.element;

import engine.Input;
import resource.bank.ImageBank;
import resource.bank.Palette;

public class BackButton extends TiledButton {

	private static final int BACK_X = 10;
	private static final int BACK_Y = 10;
	private static final int BACK_W = 30;
	private static final int BACK_H = 30;
	
	public BackButton() {
		super(BACK_X, BACK_Y, BACK_W, BACK_H);
		setAllTileMaps(ImageBank.goldbutton, ImageBank.goldbuttonclick, ImageBank.buttondisabled);
		setMiddleFill(Palette.YELLOW);
		setLabel(ImageBank.backbuttonlabel);
		setZ(50);
	}
	
	@Override
	public void onRelease(int mbutton) {
		super.onRelease(mbutton);
		if (mbutton == Input.LEFT_CLICK && beingHovered())
			state.navigateBack();
	}
	
}
