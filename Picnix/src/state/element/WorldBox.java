package state.element;

import engine.Engine;
import engine.Input;
import engine.Transition;
import resource.bank.ImageBank;
import state.State;
import state.WorldSelectState;
import state.load.LoadWorldState;

public class WorldBox extends Container {

	private WorldSelectState worState;
	
	public WorldBox(WorldSelectState worState, int x, int y, int viewWidth, int viewHeight) {
		super(x, y, viewWidth, viewHeight);
		this.worState = worState;
		//setBackground(ImageBank.worldbox);
		TiledButton goEasy = new TiledButton(30, 50, 75, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered())
					open(false);
			}
		};
		goEasy.setAllTileMaps(ImageBank.greenbutton, ImageBank.greenbuttonclick, ImageBank.buttondisabled);
		TiledButton goHard = new TiledButton(30, 100, 75, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered())
					open(true);
			}
		};
		goHard.setAllTileMaps(ImageBank.pinkbutton, ImageBank.pinkbuttonclick, ImageBank.buttondisabled);
		add(goEasy);
		add(goHard);
	};
	
	@Override
	public float getOpacity() {
		return worState.getBoxOpacity();
	}
	
	private void open(boolean hard) {
		int worldId = hard ? worState.getHardWorldId() : worState.getEasyWorldId();
		LoadWorldState lws = new LoadWorldState(worldId);
		Engine.getEngine().getStateManager().transitionToState(lws, Transition.FADE, 750, 0, State.NEWLY_OPENED);
	}

}
