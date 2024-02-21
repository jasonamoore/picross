package state.element;

import engine.Engine;
import engine.Input;
import resource.bank.ImageBank;
import state.LoadWorldState;
import state.State;
import state.WorldSelectState;

public class WorldBox extends Container {

	private WorldSelectState worState;
	
	public WorldBox(WorldSelectState worState, int x, int y, int viewWidth, int viewHeight) {
		super(x, y, viewWidth, viewHeight);
		this.worState = worState;
		//setBackground(ImageBank.worldbox);
		TiledButton goEasy = new TiledButton(10, 10, 75, 20) {
			@Override
			public void onRelease(int mbutton) {
				super.onRelease(mbutton);
				if (mbutton == Input.LEFT_CLICK && beingHovered())
					openEasy();
			}
		};
		goEasy.setAllTileMaps(ImageBank.greenbutton, ImageBank.greenbuttonclick, ImageBank.buttondisabled);
		add(goEasy);
	};
	
	private void openEasy() {
		LoadWorldState lws = new LoadWorldState(worState.getEasyWorldId());
		Engine.getEngine().getStateManager().openState(lws, State.NEWLY_OPENED);
	}

}
