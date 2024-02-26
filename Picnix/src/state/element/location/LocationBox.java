package state.element.location;

import engine.Engine;
import engine.Transition;
import resource.bank.ImageBank;
import state.State;
import state.WorldSelectState;
import state.element.Container;
import state.load.LoadWorldState;

public class LocationBox extends Container {

	private static final int X_POS = Engine.SCREEN_WIDTH - 180;
	private static final int WIDTH = 168;
	private static final int HEIGHT = 394;
	
	private WorldSelectState worState;

	private LevelSetBox easy;
	private LevelSetBox hard;
	private LockedSetBox lock;
	
	public LocationBox(WorldSelectState worState) {
		super(X_POS, 0, WIDTH, HEIGHT);
		this.worState = worState;
		setBackground(ImageBank.locationboxframe);
		easy = new LevelSetBox(worState, true);
		easy.setBounds(11, 84, 146, 146);
		hard = new LevelSetBox(worState, false);
		hard.setBounds(11, 239, 146, 146);
		hard.setExisting(false);
		hard.setChildrenExisting(false);
		lock = new LockedSetBox();
		lock.setBounds(11, 239, 146, 146);
		add(easy);
		add(hard);
		add(lock);
	};
	
	@Override
	public float getOpacity() {
		return worState.getBoxOpacity();
	}

}
